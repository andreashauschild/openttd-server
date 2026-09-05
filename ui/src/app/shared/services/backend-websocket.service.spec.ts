import {OpenttdTerminalSnapshotEvent} from '@api/models/openttd-terminal-snapshot-event';
import {OpenttdTerminalUpdateEvent} from '@api/models/openttd-terminal-update-event';
import {SESSION_STORAGE_KEY} from '../model/constants';

import {
  BackendWebsocketService,
  SubscribeMessage,
  TerminalSocket,
  TerminalSocketHandlers
} from './backend-websocket.service';

class FakeSocket implements TerminalSocket {
  sent: string[] = [];
  closed = false;

  send(data: string): void {
    this.sent.push(data);
  }

  close(): void {
    this.closed = true;
  }
}

class TestBackendWebsocketService extends BackendWebsocketService {
  readonly fakeSocket = new FakeSocket();
  url: string | null = null;
  handlers: TerminalSocketHandlers | null = null;

  protected override createSocket(url: string, handlers: TerminalSocketHandlers): TerminalSocket {
    this.url = url;
    this.handlers = handlers;
    return this.fakeSocket;
  }
}

describe('BackendWebsocketService', () => {

  let service: TestBackendWebsocketService;

  function open(): void {
    service.handlers!.open();
  }

  function receive(message: object): void {
    service.handlers!.message(JSON.stringify(message));
  }

  function frames(): SubscribeMessage[] {
    return service.fakeSocket.sent.map(f => JSON.parse(f) as SubscribeMessage);
  }

  function snapshot(fields: Partial<OpenttdTerminalSnapshotEvent>): object {
    return {_type: 'OpenttdTerminalSnapshotEvent', processId: 'p-1', source: 'test', created: 1, ...fields};
  }

  function update(fields: Partial<OpenttdTerminalUpdateEvent>): object {
    return {_type: 'OpenttdTerminalUpdateEvent', processId: 'p-1', source: 'test', created: 1, ...fields};
  }

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem(SESSION_STORAGE_KEY, 'session-1');
    service = new TestBackendWebsocketService();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should send the subscription as the first frame after the socket opened', () => {
    service.connect();
    expect(service.fakeSocket.sent).toEqual([]);

    open();

    expect(service.url).toContain('/data-stream');
    expect(frames()).toEqual([{type: 'subscribe', sessionId: 'session-1', processId: null, fromOffset: null}]);
  });

  it('should switch the subscription of the open socket', () => {
    service.connect();
    open();

    service.subscribeToProcess('p-1', null);

    expect(frames()[1]).toEqual({type: 'subscribe', sessionId: 'session-1', processId: 'p-1', fromOffset: null});
  });

  it('should not send a subscription without a session id', () => {
    localStorage.clear();
    service.connect();

    open();

    expect(service.fakeSocket.sent).toEqual([]);
  });

  it('should route snapshots and updates to their own streams', () => {
    const snapshots: OpenttdTerminalSnapshotEvent[] = [];
    const updates: OpenttdTerminalUpdateEvent[] = [];
    service.snapshots$.subscribe(e => snapshots.push(e));
    service.events$.subscribe(e => updates.push(e));
    service.connect();
    open();
    service.subscribeToProcess('p-1', null);

    receive(snapshot({text: 'old\n', endOffset: 100}));
    receive(update({text: 'new\n', offset: 100}));
    receive({_type: 'SomethingElse'});

    expect(snapshots.map(e => e.text)).toEqual(['old\n']);
    expect(updates.map(e => e.text)).toEqual(['new\n']);
  });

  it('should ignore a frame that is not json', () => {
    const updates: OpenttdTerminalUpdateEvent[] = [];
    service.events$.subscribe(e => updates.push(e));
    service.connect();
    open();

    expect(() => service.handlers!.message('not json')).not.toThrow();
    expect(updates).toEqual([]);
  });

  it('should drop an update that the snapshot already contained', () => {
    const updates: OpenttdTerminalUpdateEvent[] = [];
    service.events$.subscribe(e => updates.push(e));
    service.connect();
    open();
    service.subscribeToProcess('p-1', null);

    receive(snapshot({text: 'old\n', endOffset: 100}));
    receive(update({text: 'old\n', offset: 96}));
    receive(update({text: 'new\n', offset: 100}));

    expect(updates.map(e => e.offset)).toEqual([100]);
  });

  it('should re-subscribe with the offset of the last applied batch after a reconnect', () => {
    const reconnects: number[] = [];
    service.reconnected$.subscribe(() => reconnects.push(1));
    service.connect();
    open();
    service.subscribeToProcess('p-1', null);
    receive(snapshot({text: 'old\n', endOffset: 100}));
    receive(update({text: 'new\n', offset: 100}));

    open();

    expect(frames()[frames().length - 1])
      .toEqual({type: 'subscribe', sessionId: 'session-1', processId: 'p-1', fromOffset: 104});
    expect(reconnects.length).toBe(1);
  });

  it('should not emit reconnected on the first open', () => {
    const reconnects: number[] = [];
    service.reconnected$.subscribe(() => reconnects.push(1));

    service.connect();
    open();

    expect(reconnects).toEqual([]);
  });

  it('should not track offsets in dashboard mode', () => {
    const updates: OpenttdTerminalUpdateEvent[] = [];
    service.events$.subscribe(e => updates.push(e));
    service.connect();
    open();

    receive(update({processId: 'p-1', text: 'a\n', offset: 500}));
    receive(update({processId: 'p-2', text: 'b\n', offset: 12}));

    expect(updates.map(e => e.processId)).toEqual(['p-1', 'p-2']);
  });

  it('should report the connection state', () => {
    const states: boolean[] = [];
    service.connected$.subscribe(state => states.push(state));

    service.connect();
    open();
    service.handlers!.close();

    expect(states).toEqual([false, true, false]);
  });

  it('should close the socket and reset the subscription on disconnect', () => {
    service.connect();
    open();
    service.subscribeToProcess('p-1', 100);

    service.disconnect();

    expect(service.fakeSocket.closed).toBeTrue();
    expect(service.fakeSocket.sent.length).toBe(2);

    // A reconnect after a logout starts from scratch instead of re-subscribing the old process.
    service.connect();
    open();
    expect(frames()[2]).toEqual({type: 'subscribe', sessionId: 'session-1', processId: null, fromOffset: null});
  });

  it('should connect only once', () => {
    service.connect();
    const handlers = service.handlers;

    service.connect();

    expect(service.handlers).toBe(handlers);
  });
});

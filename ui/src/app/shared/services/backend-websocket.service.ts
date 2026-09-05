import {Injectable} from '@angular/core';
import ReconnectingWebSocket from 'reconnecting-websocket';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {environment} from '../../../environments/environment';
import {OpenttdTerminalSnapshotEvent} from '../../api/models/openttd-terminal-snapshot-event';
import {OpenttdTerminalUpdateEvent} from '../../api/models/openttd-terminal-update-event';
import {readSessionId} from './session-storage';

/**
 * The subscription frame. It must be the first frame after every (re)connect, otherwise the backend closes the
 * session. Sending it again switches the subscription of the open socket.
 */
export interface SubscribeMessage {
  type: 'subscribe';
  sessionId: string;
  processId: string | null;
  fromOffset: number | null;
}

/** The part of the socket this service uses. Kept minimal so tests can supply a fake. */
export interface TerminalSocket {
  send(data: string): void;

  close(): void;
}

/** Callbacks the service wires into the socket created by {@link BackendWebsocketService.createSocket}. */
export interface TerminalSocketHandlers {
  open(): void;

  close(): void;

  message(data: string): void;
}

/**
 * Single socket to the backend's `/data-stream` endpoint. It owns the subscription state, so a reconnect can
 * re-subscribe with the offset of the last applied batch and no output is lost or applied twice.
 */
@Injectable({
  providedIn: 'root'
})
export class BackendWebsocketService {

  private socket: TerminalSocket | null = null;
  private processId: string | null = null;
  /** Absolute end offset of the last batch handed out for the subscribed process, or null if nothing is known. */
  private lastKnownEndOffset: number | null = null;
  private openedOnce = false;

  private readonly eventsSubject = new Subject<OpenttdTerminalUpdateEvent>();
  private readonly snapshotsSubject = new Subject<OpenttdTerminalSnapshotEvent>();
  private readonly connectedSubject = new BehaviorSubject<boolean>(false);
  private readonly reconnectedSubject = new Subject<void>();

  readonly events$: Observable<OpenttdTerminalUpdateEvent> = this.eventsSubject.asObservable();
  readonly snapshots$: Observable<OpenttdTerminalSnapshotEvent> = this.snapshotsSubject.asObservable();
  readonly connected$: Observable<boolean> = this.connectedSubject.asObservable();
  /** Emits on every open except the first one, i.e. whenever the stream was interrupted and is live again. */
  readonly reconnected$: Observable<void> = this.reconnectedSubject.asObservable();

  connect(): void {
    if (this.socket) {
      return;
    }
    this.socket = this.createSocket(environment.wsServerRoot + `/data-stream`, {
      open: () => this.onOpen(),
      close: () => this.connectedSubject.next(false),
      message: (data) => this.onMessage(data)
    });
  }

  disconnect(): void {
    const socket = this.socket;
    this.socket = null;
    this.openedOnce = false;
    this.processId = null;
    this.lastKnownEndOffset = null;
    this.connectedSubject.next(false);
    socket?.close();
  }

  /**
   * Switches the subscription. `processId === null` is the dashboard mode: no snapshot, updates of all processes.
   * `fromOffset === null` asks for the default snapshot (last 20.000 characters).
   */
  subscribeToProcess(processId: string | null, fromOffset: number | null): void {
    this.processId = processId;
    this.lastKnownEndOffset = fromOffset;
    if (this.socket && this.connectedSubject.value) {
      this.sendSubscription();
    }
  }

  /** Overridable in tests. */
  protected createSocket(url: string, handlers: TerminalSocketHandlers): TerminalSocket {
    const socket = new ReconnectingWebSocket(url, [], {minReconnectionDelay: 500});
    socket.onopen = () => handlers.open();
    socket.onclose = () => handlers.close();
    socket.onerror = () => handlers.close();
    socket.onmessage = (event: MessageEvent) => handlers.message(String(event.data));
    return socket;
  }

  private onOpen(): void {
    this.connectedSubject.next(true);
    this.sendSubscription();
    if (this.openedOnce) {
      this.reconnectedSubject.next();
    }
    this.openedOnce = true;
  }

  private sendSubscription(): void {
    const sessionId = readSessionId();
    if (!sessionId || !this.socket) {
      return;
    }
    const message: SubscribeMessage = {
      type: 'subscribe',
      sessionId,
      processId: this.processId,
      fromOffset: this.lastKnownEndOffset
    };
    this.socket.send(JSON.stringify(message));
  }

  private onMessage(data: string): void {
    let message: { _type?: string };
    try {
      message = JSON.parse(data) as { _type?: string };
    } catch {
      return;
    }
    if (message._type === 'OpenttdTerminalSnapshotEvent') {
      const snapshot = message as OpenttdTerminalSnapshotEvent;
      if (this.isSubscribedProcess(snapshot.processId) && snapshot.endOffset != null) {
        this.lastKnownEndOffset = snapshot.endOffset;
      }
      this.snapshotsSubject.next(snapshot);
    } else if (message._type === 'OpenttdTerminalUpdateEvent') {
      const update = message as OpenttdTerminalUpdateEvent;
      if (this.isSubscribedProcess(update.processId) && update.offset != null) {
        if (this.lastKnownEndOffset != null && update.offset < this.lastKnownEndOffset) {
          // Already applied before the reconnect: the catch-up snapshot contained it.
          return;
        }
        this.lastKnownEndOffset = update.offset + (update.text?.length ?? 0);
      }
      this.eventsSubject.next(update);
    }
  }

  /** Offsets are only tracked while a single process is subscribed; in dashboard mode they interleave. */
  private isSubscribedProcess(processId: string | undefined): boolean {
    return this.processId != null && processId === this.processId;
  }
}

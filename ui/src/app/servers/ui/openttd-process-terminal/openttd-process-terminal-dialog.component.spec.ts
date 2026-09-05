import {fakeAsync, TestBed, tick, discardPeriodicTasks} from '@angular/core/testing';
import {HttpErrorResponse} from '@angular/common/http';
import {Store} from '@ngrx/store';
import {BehaviorSubject, Observable, of, Subject, throwError} from 'rxjs';

import {OpenttdTerminalSnapshotEvent, OpenttdTerminalUpdateEvent} from '@api/models';
import {OpenttdServerResourceService} from '@api/services/openttd-server-resource.service';
import {ApplicationService} from '@shared/services/application.service';
import {BackendWebsocketService} from '@shared/services/backend-websocket.service';

import {OpenttdProcessTerminalDialogComponent} from './openttd-process-terminal-dialog.component';

describe('OpenttdProcessTerminalDialogComponent', () => {

  const processId = 'process-uuid-1';

  let component: OpenttdProcessTerminalDialogComponent;
  let written: string[];
  let controls: string[];
  let snapshots: Subject<OpenttdTerminalSnapshotEvent>;
  let events: Subject<OpenttdTerminalUpdateEvent>;
  let connected: BehaviorSubject<boolean>;
  let websocket: { snapshots$: Observable<OpenttdTerminalSnapshotEvent>; events$: Observable<OpenttdTerminalUpdateEvent>; connected$: Observable<boolean>; reconnected$: Observable<void>; subscribeToProcess: jasmine.Spy };
  let openttd: { sendTerminalCommand: jasmine.Spy; terminalOpenInUi: jasmine.Spy };
  let app: { handleError: jasmine.Spy };
  let store: { dispatch: jasmine.Spy };

  function snapshot(fields: Partial<OpenttdTerminalSnapshotEvent>): OpenttdTerminalSnapshotEvent {
    return {processId, ...fields};
  }

  function update(fields: Partial<OpenttdTerminalUpdateEvent>): OpenttdTerminalUpdateEvent {
    return {processId, ...fields};
  }

  function create(exitCode?: number | null, skippedCommands?: number): void {
    component = TestBed.runInInjectionContext(() => new OpenttdProcessTerminalDialogComponent());
    component.openttdProcess = {id: 'server-1', process: {processId, exitCode, skippedCommands}};
    component.serverId = 'server-1';
    written = [];
    controls = [];
    component.consoleInput.subscribe(text => written.push(text));
    component.terminalControl.subscribe(cmd => controls.push(cmd));
    component.ngOnInit();
  }

  beforeEach(() => {
    snapshots = new Subject<OpenttdTerminalSnapshotEvent>();
    events = new Subject<OpenttdTerminalUpdateEvent>();
    connected = new BehaviorSubject<boolean>(true);
    websocket = {
      snapshots$: snapshots.asObservable(),
      events$: events.asObservable(),
      connected$: connected.asObservable(),
      reconnected$: new Subject<void>().asObservable(),
      subscribeToProcess: jasmine.createSpy('subscribeToProcess')
    };
    openttd = {
      sendTerminalCommand: jasmine.createSpy('sendTerminalCommand').and.returnValue(of(undefined)),
      terminalOpenInUi: jasmine.createSpy('terminalOpenInUi').and.returnValue(of(undefined))
    };
    app = {handleError: jasmine.createSpy('handleError')};
    store = {dispatch: jasmine.createSpy('dispatch')};

    TestBed.configureTestingModule({
      providers: [
        {provide: BackendWebsocketService, useValue: websocket},
        {provide: OpenttdServerResourceService, useValue: openttd},
        {provide: ApplicationService, useValue: app},
        {provide: Store, useValue: store}
      ]
    });
  });

  it('should subscribe to its own process on init', () => {
    create();

    expect(websocket.subscribeToProcess).toHaveBeenCalledWith(processId, null);
  });

  it('should write the snapshot and then the live updates in order', () => {
    create();

    snapshots.next(snapshot({text: 'line 1\nline 2\n', endOffset: 14}));
    events.next(update({text: 'line 3\n', offset: 14}));
    events.next(update({text: 'line 4\n', offset: 21}));

    expect(written).toEqual(['line 1\nline 2\n', 'line 3\n', 'line 4\n']);
    expect(controls).toEqual(['scroll']);
  });

  it('should ignore events of other processes', () => {
    create();

    events.next({processId: 'other-process', text: 'not mine\n', offset: 0});

    expect(written).toEqual([]);
  });

  it('should drop an update the snapshot already contained', () => {
    create();

    snapshots.next(snapshot({text: 'line 1\n', endOffset: 100}));
    events.next(update({text: 'line 1\n', offset: 93}));
    events.next(update({text: 'line 2\n', offset: 100}));

    expect(written).toEqual(['line 1\n', 'line 2\n']);
  });

  it('should append a catch-up snapshot instead of clearing the screen', () => {
    create();

    snapshots.next(snapshot({text: 'line 1\n', endOffset: 7}));
    snapshots.next(snapshot({text: 'missed while offline\n', endOffset: 28}));

    expect(written).toEqual(['line 1\n', 'missed while offline\n']);
    expect(controls).toEqual(['scroll', 'scroll']);
  });

  it('should disable the input and write a banner when the process exited', () => {
    create();

    events.next(update({text: 'bye\n', offset: 0, exitCode: 137}));

    expect(component.exited).toBeTrue();
    expect(written[0]).toBe('bye\n');
    expect(written[1]).toContain('[openttd-server] process exited with code 137');
  });

  it('should start in the exited state when the process is already gone', () => {
    create(1);

    expect(component.exited).toBeTrue();
    expect(written[0]).toContain('[openttd-server] process exited with code 1');
  });

  it('should write the banner only once', () => {
    create();

    events.next(update({text: 'bye\n', offset: 0, exitCode: 0}));
    snapshots.next(snapshot({text: '', endOffset: 4, exitCode: 0}));

    expect(written.filter(t => t.includes('process exited')).length).toBe(1);
  });

  it('should report a failed command in the terminal and as an alert', () => {
    create();
    const error = new HttpErrorResponse({status: 500, statusText: 'Internal Server Error'});
    openttd.sendTerminalCommand.and.returnValue(throwError(() => error));

    component.sendCommand('pause');

    expect(openttd.sendTerminalCommand).toHaveBeenCalledWith({id: 'server-1', body: 'pause'});
    expect(app.handleError).toHaveBeenCalledWith(error);
    expect(written[0]).toContain('[openttd-server] command failed:');
  });

  it('should reload the server behind the dialog after the process exited', fakeAsync(() => {
    create();

    events.next(update({text: 'bye\n', offset: 0, exitCode: 0}));
    tick(700);

    expect(store.dispatch).toHaveBeenCalledWith(
      jasmine.objectContaining({type: '[App] loadServer', id: 'server-1'}));
    discardPeriodicTasks();
  }));

  it('should send commands one after another instead of in parallel', () => {
    create();
    const first = new Subject<void>();
    const second = new Subject<void>();
    openttd.sendTerminalCommand.and.returnValues(first.asObservable(), second.asObservable());

    component.sendCommand('pause');
    component.sendCommand('unpause');

    expect(openttd.sendTerminalCommand.calls.allArgs()).toEqual([[{id: 'server-1', body: 'pause'}]]);

    first.complete();

    expect(openttd.sendTerminalCommand.calls.allArgs())
      .toEqual([[{id: 'server-1', body: 'pause'}], [{id: 'server-1', body: 'unpause'}]]);
    second.complete();
  });

  it('should keep sending commands after one failed', () => {
    create();
    openttd.sendTerminalCommand.and.returnValues(throwError(() => new HttpErrorResponse({status: 500})), of(undefined));

    component.sendCommand('boom');
    component.sendCommand('pause');

    expect(app.handleError).toHaveBeenCalled();
    expect(openttd.sendTerminalCommand.calls.allArgs())
      .toEqual([[{id: 'server-1', body: 'boom'}], [{id: 'server-1', body: 'pause'}]]);
  });

  it('should expose the skipped automatic commands of the process', () => {
    create(null, 7);

    expect(component.skippedCommands).toBe(7);
  });

  it('should clear the terminal on request', () => {
    create();

    component.clearTerminal();

    expect(controls).toEqual(['clear']);
  });

  it('should fall back to dashboard mode when the dialog is closed', () => {
    create();

    component.ngOnDestroy();

    expect(websocket.subscribeToProcess).toHaveBeenCalledWith(null, null);
  });
});

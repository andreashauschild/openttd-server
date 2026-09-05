import {Component, DestroyRef, inject, OnDestroy, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpErrorResponse} from '@angular/common/http';
import {MatDialogRef} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {AsyncPipe} from '@angular/common';
import {Store} from '@ngrx/store';
import {EMPTY, Observable, Subject, timer} from 'rxjs';
import {catchError, concatMap, filter} from 'rxjs/operators';

import {OpenttdProcess, OpenttdTerminalSnapshotEvent, OpenttdTerminalUpdateEvent} from '@api/models';
import {OpenttdServerResourceService} from '@api/services/openttd-server-resource.service';
import {loadServer} from '@store/actions/app.actions';
import {ApplicationService} from '@shared/services/application.service';
import {BackendWebsocketService} from '@shared/services/backend-websocket.service';
import {BaseDialogComponent} from '@shared/ui/base-dialog/base-dialog.component';
import {TerminalComponent} from '@shared/ui/terminal/terminal.component';

@Component({
    selector: 'app-openttd-process-terminal',
    templateUrl: './openttd-process-terminal-dialog.component.html',
    styleUrls: ['./openttd-process-terminal-dialog.component.scss'],
    standalone: true,
    imports: [AsyncPipe, MatButton, BaseDialogComponent, TerminalComponent]
})
export class OpenttdProcessTerminalDialogComponent implements OnInit, OnDestroy {

  private readonly app = inject(ApplicationService);
  private readonly openttd = inject(OpenttdServerResourceService);
  private readonly websocket = inject(BackendWebsocketService);
  private readonly store = inject(Store);
  private readonly destroyRef = inject(DestroyRef);

  public dialogTitle = '';

  public dialogRef: MatDialogRef<OpenttdProcessTerminalDialogComponent, boolean> | null = null;

  public openttdProcess!: OpenttdProcess;

  /** Id of the server, used as the key of the persisted command history. */
  public serverId: string | null = null;

  readonly consoleInput = new Subject<string>();

  readonly terminalControl = new Subject<string>();

  readonly connected$: Observable<boolean> = this.websocket.connected$;

  /** True once the process behind this terminal is gone; the terminal is read-only from then on. */
  exited = false;

  skippedCommands = 0;

  /** Commands are sent strictly one after another, so OpenTTD receives a pasted block in the typed order. */
  private readonly outgoing = new Subject<string>();

  private processId: string | null = null;
  /** Absolute end offset of the last applied batch, used to drop events the snapshot already contained. */
  private lastKnownEndOffset: number | null = null;

  ngOnInit(): void {
    this.processId = this.openttdProcess?.process?.processId ?? null;
    this.skippedCommands = this.openttdProcess?.process?.skippedCommands ?? 0;

    this.websocket.snapshots$.pipe(
      filter(snapshot => snapshot.processId === this.processId),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(snapshot => this.applySnapshot(snapshot));

    this.websocket.events$.pipe(
      filter(event => event.processId === this.processId),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(event => this.applyUpdate(event));

    // The socket answers with one snapshot and then forwards the live batches of this process only. On a
    // reconnect the service re-subscribes with the offset of the last applied batch, so the catch-up snapshot
    // is appended instead of replacing what is on screen.
    this.websocket.subscribeToProcess(this.processId, null);

    const exitCode = this.openttdProcess?.process?.exitCode;
    if (exitCode != null) {
      this.enterExitedState(exitCode);
    }

    this.outgoing.pipe(
      concatMap(cmd => this.openttd.sendTerminalCommand({id: this.openttdProcess.id!, body: cmd}).pipe(
        catchError((e: HttpErrorResponse) => {
          this.app.handleError(e);
          this.consoleInput.next(`[openttd-server] command failed: ${e.message}\n`);
          return EMPTY;
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe();

    // Tell the backend that the client has an open terminal. This prevents the backend from executing automated
    // commands that otherwise would pollute the terminal. The first call must not wait 10 seconds.
    timer(0, 10000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() =>
      this.openttd.terminalOpenInUi({id: this.openttdProcess.id!}).subscribe({
        error: (e: HttpErrorResponse) => this.app.handleError(e)
      }));
  }

  ngOnDestroy(): void {
    // Back to dashboard mode, so the backend stops sending this process's batches.
    this.websocket.subscribeToProcess(null, null);
  }

  sendCommand(cmd: string): void {
    this.outgoing.next(cmd);
  }

  clearTerminal(): void {
    this.terminalControl.next('clear');
  }

  private applySnapshot(snapshot: OpenttdTerminalSnapshotEvent): void {
    if (snapshot.text) {
      this.consoleInput.next(snapshot.text);
    }
    if (snapshot.endOffset != null) {
      this.lastKnownEndOffset = snapshot.endOffset;
    }
    this.terminalControl.next('scroll');
    if (snapshot.exitCode != null) {
      this.enterExitedState(snapshot.exitCode);
    }
  }

  private applyUpdate(event: OpenttdTerminalUpdateEvent): void {
    const offset = event.offset;
    if (offset != null && this.lastKnownEndOffset != null && offset < this.lastKnownEndOffset) {
      return;
    }
    const text = event.text ?? '';
    if (text) {
      this.consoleInput.next(text);
    }
    if (offset != null) {
      this.lastKnownEndOffset = offset + text.length;
    }
    if (event.exitCode != null) {
      this.enterExitedState(event.exitCode);
    }
  }

  private enterExitedState(exitCode: number): void {
    if (this.exited) {
      return;
    }
    this.exited = true;
    this.consoleInput.next(`[openttd-server] process exited with code ${exitCode} — reopen the terminal after the server was started again\n`);
    // Give the backend a moment to drop the process, then refresh the card behind the dialog (a `quit` typed in
    // the terminal removes the process, so the reload turns the server card to "Offline").
    timer(700).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() =>
      this.store.dispatch(loadServer({src: OpenttdProcessTerminalDialogComponent.name, id: this.openttdProcess.id!})));
  }
}

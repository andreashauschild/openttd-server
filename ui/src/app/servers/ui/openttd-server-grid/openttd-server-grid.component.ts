import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, DestroyRef, inject, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Store} from "@ngrx/store";
import {MatDialog} from "@angular/material/dialog";
import {NgFor, NgIf} from '@angular/common';
import {MatIcon} from '@angular/material/icon';
import {MatTooltip} from '@angular/material/tooltip';
import {RouterLink} from '@angular/router';
import {debounceTime, filter} from 'rxjs/operators';

import {deleteServer, loadServerConfig, pauseUnpauseServer, saveServer, startServer, stopServer} from '@store/actions/app.actions';
import {selectServers} from '@store/selectors/app.selectors';
import {OpenttdServer} from '@api/models/openttd-server';
import {BackendWebsocketService} from '@shared/services/backend-websocket.service';
import {OpenttdProcessTerminalDialogComponent} from "../openttd-process-terminal/openttd-process-terminal-dialog.component";

@Component({
    selector: 'app-openttd-server-grid',
    templateUrl: './openttd-server-grid.component.html',
    styleUrls: ['./openttd-server-grid.component.scss'],
    animations: [
        trigger('detailExpand', [
            state('collapsed', style({ height: '0px', minHeight: '0' })),
            state('expanded', style({ height: '*' })),
            transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
        ]),
    ],
    standalone: true,
    imports: [NgFor, NgIf, MatIcon, MatTooltip, RouterLink]
})
export class OpenttdServerGridComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly websocket = inject(BackendWebsocketService);

  dataSource: OpenttdServer[] = []
  columnsToDisplay = ['name', 'port', 'config', 'startSaveGame', 'saveGame', 'autoSaveGame', 'actions'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay];
  expandedElement: OpenttdServer | null | undefined;

  constructor(private store: Store, public dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.store.dispatch(loadServerConfig({src: OpenttdServerGridComponent.name}))
    this.store.select(selectServers).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(server => {
      this.dataSource = server
    })

    // The socket is in dashboard mode while no terminal is open, so an exit of any process shows up here. Reload
    // the cards shortly after, when the backend has dropped the process.
    this.websocket.events$.pipe(
      filter(event => event.exitCode != null),
      debounceTime(700),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => this.store.dispatch(loadServerConfig({src: OpenttdServerGridComponent.name})))
  }

  T(v: any): OpenttdServer {
    return v as OpenttdServer;
  }

  /** A process entry exists and the process behind it is alive. */
  isRunning(server: OpenttdServer): boolean {
    return !!server.process && server.process.process?.exitCode == null;
  }

  /** A process entry exists but the process is gone; stopping it cleans the entry up. */
  isExited(server: OpenttdServer): boolean {
    return !!server.process && server.process.process?.exitCode != null;
  }


  startServer(server: OpenttdServer) {
    this.store.dispatch(startServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  stopServer(server: OpenttdServer) {
    this.store.dispatch(stopServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  trackBy(index: number, item: OpenttdServer) {
    return item.name;
  }

  loadTerminal(server: OpenttdServer | undefined) {
    if (server && server.process) {
      const dialogRef = this.dialog.open(OpenttdProcessTerminalDialogComponent, {
        minWidth: '60%',
        height: '80vh',
      });

      dialogRef.componentInstance.dialogRef = dialogRef;
      dialogRef.componentInstance.dialogTitle = `Server: ${server.name}`;
      dialogRef.componentInstance.openttdProcess = server.process;
      dialogRef.componentInstance.serverId = server.id ?? null;
    }

  }

  deleteServer(server: OpenttdServer) {
    this.store.dispatch(deleteServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  save(server: OpenttdServer) {
    this.store.dispatch(saveServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  pauseServer(server: OpenttdServer) {
    this.store.dispatch(pauseUnpauseServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }
}

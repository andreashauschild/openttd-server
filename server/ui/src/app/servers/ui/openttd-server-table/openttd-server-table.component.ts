import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {
  deleteServer,
  loadProcesses,
  loadServerConfig,
  saveServer,
  startServer
} from 'src/app/shared/store/actions/app.actions';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {selectServers} from '../../../shared/store/selectors/app.selectors';
import {CreateServerDialogComponent} from "../create-server-dialog/create-server-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {OpenttdProcessTerminalDialogComponent} from "../openttd-process-terminal/openttd-process-terminal-dialog.component";
import {OpenttdProcess} from "../../../api/models/openttd-process";

@Component({
  selector: 'app-openttd-server-table',
  templateUrl: './openttd-server-table.component.html',
  styleUrls: ['./openttd-server-table.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class OpenttdServerTableComponent implements OnInit {
  dataSource: OpenttdServer[] = []
  columnsToDisplay = ['name', 'port', 'config', 'startSaveGame', 'saveGame', 'autoSaveGame', 'actions'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay];
  expandedElement: OpenttdServer | null | undefined;
  showTerminal = false;

  constructor(private store: Store, public dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.store.dispatch(loadServerConfig({src: OpenttdServerTableComponent.name}))
    this.store.select(selectServers).subscribe(server => {

      this.dataSource = server
    })
  }

  T(v: any): OpenttdServer {
    return v as OpenttdServer;
  }


  startServer(server: OpenttdServer) {
    this.store.dispatch(startServer({src: OpenttdServerTableComponent.name, id: server.id!}))
  }

  stopServer(server: OpenttdServer) {

  }

  trackBy(index: number, item: OpenttdServer) {
    return item.name;
  }

  loadTerminal(process: OpenttdProcess | undefined) {
    console.log(">>>>>>>>>>>###", process)
    if (process) {
      const dialogRef = this.dialog.open(OpenttdProcessTerminalDialogComponent, {
        minWidth: '60%',
      });

      dialogRef.componentInstance.dialogRef = dialogRef;
      dialogRef.componentInstance.openttdProcess = process;
      dialogRef.afterClosed().subscribe(result => {
        console.log(`Dialog result: ${result}`);
      });
    }

  }

  deleteServer(server: OpenttdServer) {
    this.store.dispatch(deleteServer({src: OpenttdServerTableComponent.name, id: server.name!}))
  }

  save(server: OpenttdServer) {
    this.store.dispatch(saveServer({src: OpenttdServerTableComponent.name, id: server.name!}))
  }
}

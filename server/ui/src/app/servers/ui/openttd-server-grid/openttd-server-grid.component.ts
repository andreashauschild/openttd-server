import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit} from '@angular/core';
import {Store} from "@ngrx/store";
import {deleteServer, loadServerConfig, saveServer, startServer} from 'src/app/shared/store/actions/app.actions';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {selectServers} from '../../../shared/store/selectors/app.selectors';
import {MatDialog} from "@angular/material/dialog";
import {OpenttdProcessTerminalDialogComponent} from "../openttd-process-terminal/openttd-process-terminal-dialog.component";

@Component({
  selector: 'app-openttd-server-grid',
  templateUrl: './openttd-server-grid.component.html',
  styleUrls: ['./openttd-server-grid.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class OpenttdServerGridComponent implements OnInit {
  dataSource: OpenttdServer[] = []
  columnsToDisplay = ['name', 'port', 'config', 'startSaveGame', 'saveGame', 'autoSaveGame', 'actions'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay];
  expandedElement: OpenttdServer | null | undefined;
  showTerminal = false;

  constructor(private store: Store, public dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.store.dispatch(loadServerConfig({src: OpenttdServerGridComponent.name}))
    this.store.select(selectServers).subscribe(server => {

      this.dataSource = server
    })
  }

  T(v: any): OpenttdServer {
    return v as OpenttdServer;
  }


  startServer(server: OpenttdServer) {
    this.store.dispatch(startServer({src: OpenttdServerGridComponent.name, name: server.name!, saveGame: undefined}))
  }

  stopServer(server: OpenttdServer) {

  }

  trackBy(index: number, item: OpenttdServer) {
    return item.name;
  }

  loadTerminal(server: OpenttdServer | undefined) {
    console.log(">>>>>>>>>>>###", process)
    if (server && server.process) {
      const dialogRef = this.dialog.open(OpenttdProcessTerminalDialogComponent, {
        minWidth: '60%',
      });

      dialogRef.componentInstance.dialogRef = dialogRef;
      dialogRef.componentInstance.dialogTitle = `Server: ${server.name}`;
      dialogRef.componentInstance.openttdProcess = server.process;
      dialogRef.afterClosed().subscribe(result => {
        console.log(`Dialog result: ${result}`);
      });
    }

  }

  deleteServer(server: OpenttdServer) {
    this.store.dispatch(deleteServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  save(server: OpenttdServer) {
    this.store.dispatch(saveServer({src: OpenttdServerGridComponent.name, id: server.id!}))
  }

  pauseServer(server: OpenttdServer) {

  }
}

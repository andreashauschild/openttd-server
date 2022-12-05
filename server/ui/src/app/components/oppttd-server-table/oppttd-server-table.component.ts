import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, OnInit} from '@angular/core';
import {OpenttdServer} from "../../api/models/openttd-server";
import {Store} from "@ngrx/store";
import {deleteServer, loadServerConfig, saveServer, startServer} from "../../store/actions/app.actions";
import {selectServers} from "../../store/selectors/app.selectors";

@Component({
  selector: 'app-oppttd-server-table',
  templateUrl: './oppttd-server-table.component.html',
  styleUrls: ['./oppttd-server-table.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class OppttdServerTableComponent implements OnInit {
  dataSource: OpenttdServer[] = []
  columnsToDisplay = ['name', 'port', 'config', 'startSaveGame', 'saveGame', 'autoSaveGame', 'actions'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay];
  expandedElement: OpenttdServer | null | undefined;
  showTerminal = false;

  constructor(private store: Store) {
  }

  ngOnInit(): void {
    this.store.dispatch(loadServerConfig({src: OppttdServerTableComponent.name}))
    this.store.select(selectServers).subscribe(server => {

      this.dataSource = server
    })
  }

  T(v: any): OpenttdServer {
    return v as OpenttdServer;
  }


  startServer(server: OpenttdServer) {
    this.store.dispatch(startServer({src: OppttdServerTableComponent.name, name: server.name!, saveGame: undefined}))
  }

  stopServer(server: OpenttdServer) {

  }

  trackBy(index: number, item: OpenttdServer) {
    return item.name;
  }

  loadTerminal() {
    setTimeout(() => this.showTerminal = true, 3000)
  }

  deleteServer(server: OpenttdServer) {
    this.store.dispatch(deleteServer({src: OppttdServerTableComponent.name, name: server.name!}))
  }

  save(server: OpenttdServer) {
    this.store.dispatch(saveServer({src: OppttdServerTableComponent.name, name: server.name!}))
  }
}

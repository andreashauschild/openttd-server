import {Component, OnInit} from '@angular/core';
import {BackendWebsocketService} from './shared/services/backend-websocket.service';
import {Router} from '@angular/router';
import {AuthenticationService} from './shared/services/authentication.service';
import {SidebarLayoutModel} from './shared/ui/sidebar-layout/sidebar-layout.component';
import {ApplicationService} from './shared/services/application.service';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  window: any;

  sidebarLayoutModel: SidebarLayoutModel = {
    entries: [
      {
        icon: 'dns',
        path: "/servers",
        title: "Servers"
      },
      {
        icon: 'settings',
        path: "/settings",
        title: "Settings"
      },
      {
        icon: 'folder_copy',
        path: "/explorer",
        title: "File-Explorer"
      },
    ]
  }

  constructor(private app: ApplicationService, public auth: AuthenticationService, private router: Router, private backendWebsocketService: BackendWebsocketService) {
    this.window = window;
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();


  }


}

import {Component, OnInit} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {NgIf} from '@angular/common';
import {LoadingBarModule} from '@ngx-loading-bar/core';

import {BackendWebsocketService} from '@shared/services/backend-websocket.service';
import {AuthenticationService} from '@shared/services/authentication.service';
import {ApplicationService} from '@shared/services/application.service';
import {SidebarLayoutComponent, SidebarLayoutModel} from '@shared/ui/sidebar-layout/sidebar-layout.component';


@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: true,
    imports: [NgIf, RouterOutlet, SidebarLayoutComponent, LoadingBarModule]
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
      }
    ]
  }

  constructor(private app: ApplicationService, public auth: AuthenticationService, private router: Router, private backendWebsocketService: BackendWebsocketService) {
    this.window = window;
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();


  }


}

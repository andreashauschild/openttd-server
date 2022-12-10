import {Component, OnInit} from '@angular/core';
import SunsetTheme from 'highcharts/themes/dark-unica.js';
import * as Highcharts from 'highcharts';
import * as HighchartsStock from 'highcharts/highstock';
import {BackendWebsocketService} from './shared/services/backend-websocket.service';
import {Router} from '@angular/router';
import {AuthenticationService} from './shared/services/authentication.service';
import {SidebarLayoutModel} from './shared/ui/sidebar-layout/sidebar-layout.component';
import {Store} from '@ngrx/store';
import {selectAlerts} from './shared/store/selectors/app.selectors';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppNotificationsComponent} from './shared/ui/app-notifications/app-notifications.component';
import {createAlert} from './shared/store/actions/app.actions';
import {ApplicationService} from './shared/services/application.service';

SunsetTheme(Highcharts);
SunsetTheme(HighchartsStock);

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
    ]
  }

  constructor(private app:ApplicationService,private _snackBar: MatSnackBar, public auth: AuthenticationService, private router: Router, private backendWebsocketService: BackendWebsocketService) {
    this.window = window;
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();

    this._snackBar.openFromComponent(AppNotificationsComponent, {duration: 1000000, verticalPosition: 'top', panelClass: 'snackbar'})
  }


  test() {
    this.app.createErrorMessage("ERROR");
    this.app.createInfoMessage("ERROR",2000);
    this.app.createErrorMessage("ERROR");
    this.app.createErrorMessage("ERROR");
  }
}

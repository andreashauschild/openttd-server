import {Component, OnInit} from '@angular/core';
import SunsetTheme from 'highcharts/themes/dark-unica.js';
import * as Highcharts from 'highcharts';
import * as HighchartsStock from 'highcharts/highstock';
import {BackendWebsocketService} from './shared/services/backend-websocket.service';
import {ActivatedRoute, Router} from '@angular/router';
import * as url from 'url';
import {AuthResourceService} from './api/services/auth-resource.service';
import {AuthenticationService} from './shared/services/authentication.service';

SunsetTheme(Highcharts);
SunsetTheme(HighchartsStock);

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  window: any;

  constructor(public auth:AuthenticationService,private router: Router, private backendWebsocketService: BackendWebsocketService) {
    this.window = window;
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();
  }


}

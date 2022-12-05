import {Component, OnInit} from '@angular/core';
import SunsetTheme from 'highcharts/themes/dark-unica.js';
import * as Highcharts from 'highcharts';
import * as HighchartsStock from 'highcharts/highstock';
import {BackendWebsocketService} from './service/backend-websocket.service';

SunsetTheme(Highcharts);
SunsetTheme(HighchartsStock);

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(private backendWebsocketService: BackendWebsocketService) {
  }

  ngOnInit(): void {
    this.backendWebsocketService.connect();
  }


}

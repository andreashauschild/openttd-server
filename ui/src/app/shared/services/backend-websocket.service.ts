import {Injectable} from '@angular/core';
import ReconnectingWebSocket from 'reconnecting-websocket';
import {Store} from '@ngrx/store';
import {processUpdateEvent} from '../store/actions/app.actions';
import {environment} from '../../../environments/environment';
import {OpenttdTerminalUpdateEvent} from '../../api/models/openttd-terminal-update-event';

@Injectable({
  providedIn: 'root'
})
export class BackendWebsocketService {
  private webSocket: ReconnectingWebSocket | null = null;

  constructor(private store: Store<{}>) {
  }

  connect(): void {
    this.webSocket = new ReconnectingWebSocket(environment.wsServerRoot + `/data-stream`, [], {
      minReconnectionDelay: 500
    })

    this.webSocket.onopen = (event) => {
      console.log("onopen", event);
      // this.store.dispatch(setAdminAuctionWebsocketState({src:AuctionWebsocketService.name,connected:true}))
    };

    this.webSocket.onerror = (event) => {
      console.log("onerror", event);

    };

    this.webSocket.onclose = (event) => {
      console.log("onclose", event);
      // this.store.dispatch(setAdminAuctionWebsocketState({src:AuctionWebsocketService.name,connected:false}))
    };

    this.webSocket.onmessage = (event) => {
      console.log(event)
      if (JSON.parse(event.data)?.ping) {
        console.log("received Ping", event);
      } else {
        const msg = JSON.parse(event.data);
        if (msg._type === "OpenttdTerminalUpdateEvent") {
          this.store.dispatch(processUpdateEvent({src: BackendWebsocketService.name, event: msg as OpenttdTerminalUpdateEvent}))
        }
        // callback(JSON.parse(event.data) as AuctionPage)
        // console.log("onmessage", event);
      }

    }
  }
}

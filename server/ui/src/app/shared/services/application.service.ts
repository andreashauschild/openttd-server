import { Injectable } from '@angular/core';
import {Store} from '@ngrx/store';
import {createAlert, removeAlert} from '../store/actions/app.actions';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private messageId = 1;

  constructor(private store: Store<{}>) { }

  createErrorMessage(message: string): void {

    this.messageId++;

    this.store.dispatch(
      createAlert({
        src: ApplicationService.name,
        alert: {
          id: this.messageId + '',
          message: 'Error: ' + message,
          type: 'error',
        },
      })
    );

  }

  createInfoMessage(message: string,millis?: number): void {
    this.messageId++;
    const msgid = this.messageId;

    this.store.dispatch(
      createAlert({
        src: ApplicationService.name,
        alert: {
          id: msgid + '',
          message: 'Info: ' + message,
          type: 'info',
        },
      })
    );

    if(millis && millis>=0){
      setTimeout(() => {
        this.store.dispatch(
          removeAlert({
            src: ApplicationService.name,
            alertId: msgid + ``
          }))
      }, millis)
    }
  }

  createWarningMessage(message: string): void {
    this.messageId++;
    this.store.dispatch(
      createAlert({
        src: ApplicationService.name,
        alert: {
          id: this.messageId + '',
          message: 'Warning: ' + message,
          type: 'warning',
        },
      })
    );
  }
}

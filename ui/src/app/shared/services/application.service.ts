import {Injectable} from '@angular/core';
import {Store} from '@ngrx/store';
import {createAlert, removeAlert} from '../store/actions/app.actions';
import {MatSnackBar} from '@angular/material/snack-bar';
import {AppNotificationsComponent} from '../ui/app-notifications/app-notifications.component';
import {HttpErrorResponse} from '@angular/common/http';
import {ServiceError} from '../../api/models/service-error';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private messageId = 1;

  constructor(private store: Store<{}>, private _snackBar: MatSnackBar) {
    // Set up the snackbar but the size is set to 0px, so it is not visible
    this._snackBar.openFromComponent(AppNotificationsComponent, {verticalPosition: 'top', panelClass: 'snackbar'})
  }

  createErrorMessage(message: string, stacktrace?: string): void {

    this.messageId++;

    this.store.dispatch(
      createAlert({
        src: ApplicationService.name,
        alert: {
          id: this.messageId + '',
          message: 'Error: ' + message,
          type: 'error',
          stacktrace
        },
      })
    );

  }

  createInfoMessage(message: string, millis?: number): void {
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

    if (millis && millis >= 0) {
      setTimeout(() => {
        this.store.dispatch(
          removeAlert({
            src: ApplicationService.name,
            alertId: msgid + ``
          }))
      }, millis)
    }
  }

  createWarningMessage(message: string, stacktrace?: string): void {
    this.messageId++;
    this.store.dispatch(
      createAlert({
        src: ApplicationService.name,
        alert: {
          id: this.messageId + '',
          message: 'Warning: ' + message,
          type: 'warning',
          stacktrace
        },
      })
    );
  }

  handleError(e: HttpErrorResponse): void {
    if (e.error) {
      let error = (e.error as ServiceError);
      this.createErrorMessage(`${error.message}`,error.stackTrace)
    } else {
      this.createErrorMessage(`HTTP Error. See console log`)
    }
    console.error(e);
  }
}

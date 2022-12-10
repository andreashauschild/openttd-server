import {Component, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {selectAlerts} from '../../store/selectors/app.selectors';
import {AppAlert} from '../../store/reducers/app.reducer';
import {removeAlert} from '../../store/actions/app.actions';

@Component({
  selector: 'app-app-notifications',
  templateUrl: './app-notifications.component.html',
  styleUrls: ['./app-notifications.component.scss']
})
export class AppNotificationsComponent implements OnInit {

  alerts: AppAlert[] = [];

  constructor(private store: Store<{}>) {
  }

  ngOnInit(): void {
    this.store.select(selectAlerts).subscribe(alerts => {
      console.log(alerts)
      this.alerts = JSON.parse(JSON.stringify(alerts));
    })
  }

  remove(messageId:string){
    this.store.dispatch(
      removeAlert({
        src: AppNotificationsComponent.name,
        alertId: messageId
      }))
  }

}

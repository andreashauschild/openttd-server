import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppNotificationsComponent } from './app-notifications.component';
import {MatIconModule} from '@angular/material/icon';



@NgModule({
  declarations: [
    AppNotificationsComponent
  ],
    imports: [
        CommonModule,
        MatIconModule
    ]
})
export class AppNotificationsModule { }

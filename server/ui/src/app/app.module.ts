import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {StoreModule} from '@ngrx/store';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {environment} from '../environments/environment';
import {EffectsModule} from '@ngrx/effects';
import {AppEffects} from './shared/store/effects/app.effects';
import {metaReducers, reducers} from './shared/store/reducers';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpAuthInterceptor} from './shared/interceptors/http-auth-interceptor';
import {SidebarLayoutModule} from './shared/ui/sidebar-layout/sidebar-layout.module';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatIconModule} from '@angular/material/icon';
import {AppNotificationsModule} from './shared/ui/app-notifications/app-notifications.module';
import {DatePipe} from '@angular/common';
import {LoadingBarModule} from "@ngx-loading-bar/core";
import {LoadingBarHttpClientModule} from "@ngx-loading-bar/http-client";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    LoadingBarHttpClientModule,
    BrowserAnimationsModule,
    EffectsModule.forRoot([AppEffects]),
    StoreModule.forRoot(reducers, {metaReducers}),
    !environment.production ? StoreDevtoolsModule.instrument(
      // {
      //   actionsBlocklist: [
      //     //terminalUpdateEvent.name
      //   ]
      // }
    ) : [],
    SidebarLayoutModule,
    AppNotificationsModule,
    MatSnackBarModule,
  ],
  providers: [
    DatePipe,
    {provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

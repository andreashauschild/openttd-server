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
import {AppNotificationsModule} from './shared/ui/app-notifications/app-notifications.module';
import {DatePipe} from '@angular/common';
import {LoadingBarHttpClientModule} from "@ngx-loading-bar/http-client";
import {CustomLoadingBarInterceptor} from "./shared/interceptors/custom-loading-bar.interceptor";
import {LoadingBarModule} from "@ngx-loading-bar/core";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    LoadingBarModule,
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
    {provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptor, multi: true},
    {
      provide: HTTP_INTERCEPTORS,
      useClass: CustomLoadingBarInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

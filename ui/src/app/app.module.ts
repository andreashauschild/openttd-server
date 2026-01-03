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
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {HttpAuthInterceptor} from './shared/interceptors/http-auth-interceptor';
import {SidebarLayoutModule} from './shared/ui/sidebar-layout/sidebar-layout.module';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {AppNotificationsModule} from './shared/ui/app-notifications/app-notifications.module';
import {DatePipe} from '@angular/common';
import {CustomLoadingBarInterceptor} from "./shared/interceptors/custom-loading-bar.interceptor";
import {LoadingBarModule} from "@ngx-loading-bar/core";
import {DataService} from 'ngx-explorer';
import {FileExplorerDataService} from './file-explorer/file-explorer/file-explorer-data.service';

@NgModule({ declarations: [
        AppComponent
    ],
    bootstrap: [AppComponent], imports: [BrowserModule,
        AppRoutingModule,
        LoadingBarModule,
        BrowserAnimationsModule,
        EffectsModule.forRoot([AppEffects]),
        StoreModule.forRoot(reducers, { metaReducers }),
        !environment.production ? StoreDevtoolsModule.instrument(
        // {
        //   actionsBlocklist: [
        //     //terminalUpdateEvent.name
        //   ]
        // }
        { connectInZone: true }) : [],
        SidebarLayoutModule,
        AppNotificationsModule,
        MatSnackBarModule], providers: [
        DatePipe,
        { provide: HTTP_INTERCEPTORS, useClass: HttpAuthInterceptor, multi: true },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: CustomLoadingBarInterceptor,
            multi: true,
        },
        {
            provide: DataService,
            useExisting: FileExplorerDataService
        },
        provideHttpClient(withInterceptorsFromDi())
    ] })
export class AppModule {
}

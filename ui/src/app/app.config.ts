import {ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideStore} from '@ngrx/store';
import {provideEffects} from '@ngrx/effects';
import {provideStoreDevtools} from '@ngrx/store-devtools';
import {DatePipe} from '@angular/common';

import {routes} from './app.routes';
import {metaReducers, reducers} from './shared/store/reducers';
import {AppEffects} from './shared/store/effects/app.effects';
import {environment} from '../environments/environment';
import {httpAuthInterceptor} from './shared/interceptors/http-auth.interceptor';
import {loadingBarInterceptor} from './shared/interceptors/loading-bar.interceptor';
import {DataService} from 'ngx-explorer';
import {FileExplorerDataService} from './file-explorer/file-explorer/file-explorer-data.service';
import {LoadingBarModule} from '@ngx-loading-bar/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([httpAuthInterceptor, loadingBarInterceptor])),
    provideAnimations(),
    provideStore(reducers, {metaReducers}),
    provideEffects([AppEffects]),
    !environment.production ? provideStoreDevtools({connectInZone: true}) : [],
    importProvidersFrom(LoadingBarModule),
    DatePipe,
    {provide: DataService, useExisting: FileExplorerDataService},
  ]
};

import {ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideStore} from '@ngrx/store';
import {provideEffects} from '@ngrx/effects';
import {provideStoreDevtools} from '@ngrx/store-devtools';
import {DatePipe} from '@angular/common';
import {DataService} from 'ngx-explorer';
import {LoadingBarModule} from '@ngx-loading-bar/core';

import {routes} from '@app/app.routes';
import {metaReducers, reducers} from '@store/reducers';
import {AppEffects} from '@store/effects/app.effects';
import {httpAuthInterceptor} from '@shared/interceptors/http-auth.interceptor';
import {loadingBarInterceptor} from '@shared/interceptors/loading-bar.interceptor';
import {environment} from '@env/environment';

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
  ]
};

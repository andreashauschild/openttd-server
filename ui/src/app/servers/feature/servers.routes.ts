import {Routes} from '@angular/router';

import {RP_ID} from '@shared/model/constants';

export const SERVERS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./servers-overview/servers-overview.component').then(m => m.ServersOverviewComponent)
  },
  {
    path: `:${RP_ID}`,
    loadComponent: () => import('./servers-detail/servers-detail.component').then(m => m.ServersDetailComponent)
  }
];

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: 'servers',
    loadChildren: () =>
      import('./servers/feature/servers-overview/servers-overview.module').then((m) => m.ServersOverviewModule),
  },
  {
    path: 'login',
    loadChildren: () =>
      import('./auth/feature/login/login.module').then((m) => m.LoginModule),
  },
  {
    path: 'logout',
    loadChildren: () =>
      import('./auth/feature/logout/logout.module').then((m) => m.LogoutModule),
  },
  {
    path: '',
    redirectTo: 'servers',
    pathMatch: 'full',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

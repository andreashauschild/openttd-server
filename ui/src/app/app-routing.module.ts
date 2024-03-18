import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from './shared/guards/auth.guard';
import {FileExplorerComponent} from './file-explorer/file-explorer/file-explorer.component';

const routes: Routes = [
  {
    path: 'servers',
    canActivate:[AuthGuard],
    loadChildren: () =>
      import('./servers/feature/servers-shell/servers-shell.module').then((m) => m.ServersShellModule),
  },
  {
    path: 'settings',
    canActivate:[AuthGuard],
    loadChildren: () =>
      import('./settings/feature/settings/settings.module').then((m) => m.SettingsModule),
  },
  {
    path: 'explorer',
    canActivate:[AuthGuard],
    component:FileExplorerComponent
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

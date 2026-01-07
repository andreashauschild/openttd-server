import {Routes} from '@angular/router';

import {authGuard} from '@shared/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'servers',
    canActivate: [authGuard],
    loadChildren: () => import('./servers/feature/servers.routes').then(m => m.SERVERS_ROUTES)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () => import('./settings/feature/settings/settings.component').then(m => m.SettingsComponent)
  },
  {
    path: 'explorer',
    canActivate: [authGuard],
    loadComponent: () => import('./file-explorer/file-explorer.component').then(m => m.FileExplorerComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./auth/feature/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'logout',
    loadComponent: () => import('./auth/feature/logout/logout.component').then(m => m.LogoutComponent)
  },
  {
    path: '',
    redirectTo: 'servers',
    pathMatch: 'full'
  }
];

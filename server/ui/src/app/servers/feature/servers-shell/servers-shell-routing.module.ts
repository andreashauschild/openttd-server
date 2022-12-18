import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('../servers-overview/servers-overview.module').then(
        (m) => m.ServersOverviewModule
      ),
  },
  {
    path: ':name',
    loadChildren: () =>
      import('../servers-detail/servers-detail.module').then(
        (m) => m.ServersDetailModule
      ),
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServersShellRoutingModule {
}

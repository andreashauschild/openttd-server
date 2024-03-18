import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {RP_ID} from '../../../shared/model/constants';


const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('../servers-overview/servers-overview.module').then(
        (m) => m.ServersOverviewModule
      ),
  },
  {
    path: `:${RP_ID}`,
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

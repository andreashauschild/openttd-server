import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ServersOverviewComponent} from './servers-overview.component';

const routes: Routes = [
  {
    path: '',
    component: ServersOverviewComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServersOverviewRoutingModule {
}

import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ServersDetailComponent} from './servers-detail.component';

const routes: Routes = [
  {
    path: '',
    component: ServersDetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ServersDetailRoutingModule {
}

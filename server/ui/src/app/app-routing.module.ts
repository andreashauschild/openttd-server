import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ManagementViewComponent} from './views/management-view/management-view.component';
import {LoginViewComponent} from './views/login-view/login-view.component';

const routes: Routes = [
  {path: '', component: ManagementViewComponent},
  {path: 'manager', component: ManagementViewComponent},
  {path: 'login', component: LoginViewComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

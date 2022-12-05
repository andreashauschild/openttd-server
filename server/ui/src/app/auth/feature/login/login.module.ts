import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LoginComponent} from './login.component';
import {LoginRoutingModuleRoutingModule} from './login-routing.module';


@NgModule({
  declarations: [
    LoginComponent
  ],
  imports: [
    CommonModule,
    LoginRoutingModuleRoutingModule
  ],
  exports: [LoginComponent]
})
export class LoginModule {
}

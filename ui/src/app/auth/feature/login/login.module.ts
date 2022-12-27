import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LoginComponent} from './login.component';
import {LoginRoutingModuleRoutingModule} from './login-routing.module';
import {FormsModule} from '@angular/forms';


@NgModule({
  declarations: [
    LoginComponent
  ],
    imports: [
        CommonModule,
        LoginRoutingModuleRoutingModule,
        FormsModule
    ],
  exports: [LoginComponent]
})
export class LoginModule {
}

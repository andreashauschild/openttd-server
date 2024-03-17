import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SettingsComponent} from './settings.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatLegacyFormFieldModule as MatFormFieldModule} from '@angular/material/legacy-form-field';
import {MatLegacyInputModule as MatInputModule} from '@angular/material/legacy-input';
import {MatIconModule} from '@angular/material/icon';
import {SettingsRoutingModule} from './settings-routing.module';


@NgModule({
  declarations: [
    SettingsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    SettingsRoutingModule
  ],
  exports: [
    SettingsComponent
  ]
})
export class SettingsModule {
}

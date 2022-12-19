import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ServersDetailRoutingModule} from './servers-detail-routing.module';
import {ServersDetailComponent} from './servers-detail.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {ServerFileSelectModule} from '../../../shared/ui/server-file-select/server-file-select.module';


@NgModule({
  declarations: [
    ServersDetailComponent
  ],
  imports: [
    CommonModule,
    ServersDetailRoutingModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    ServerFileSelectModule,
  ]
})
export class ServersDetailModule { }

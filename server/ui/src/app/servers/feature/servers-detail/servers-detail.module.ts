import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ServersDetailRoutingModule } from './servers-detail-routing.module';
import { ServersDetailComponent } from './servers-detail.component';
import {MatFormFieldModule, MatLabel} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';


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
  ]
})
export class ServersDetailModule { }

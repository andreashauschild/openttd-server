import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ServersDetailRoutingModule} from './servers-detail-routing.module';
import {ServersDetailComponent} from './servers-detail.component';
import {MatLegacyFormFieldModule as MatFormFieldModule} from '@angular/material/legacy-form-field';
import {MatLegacyInputModule as MatInputModule} from '@angular/material/legacy-input';
import {ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {ServerFileSelectModule} from '../../../shared/ui/server-file-select/server-file-select.module';
import {MatLegacySlideToggleModule as MatSlideToggleModule} from '@angular/material/legacy-slide-toggle';


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
        MatSlideToggleModule,
    ]
})
export class ServersDetailModule { }

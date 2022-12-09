import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseDialogComponent } from './base-dialog.component';
import {MatIconModule} from '@angular/material/icon';



@NgModule({
  declarations: [
    BaseDialogComponent
  ],
  exports: [
    BaseDialogComponent
  ],
  imports: [
    CommonModule,
    MatIconModule
  ]
})
export class BaseDialogModule { }

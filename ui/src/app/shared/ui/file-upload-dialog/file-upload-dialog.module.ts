import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FileUploadDialogComponent} from './file-upload-dialog.component';
import {JustUploadModule} from '@andreashauschild/just-upload';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogContent, MatDialogModule} from '@angular/material/dialog';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {BaseDialogModule} from '../base-dialog/base-dialog.module';
import {MatProgressBarModule} from '@angular/material/progress-bar';


@NgModule({
  declarations: [
    FileUploadDialogComponent
  ],
  imports: [
    CommonModule,
    JustUploadModule,
    MatIconModule,
    MatDialogModule,
    FormsModule,
    MatIconModule,
    MatButtonModule,
    BaseDialogModule,
    MatProgressBarModule
  ],
  exports: [FileUploadDialogComponent, MatDialogContent]
})
export class FileUploadDialogModule {
}

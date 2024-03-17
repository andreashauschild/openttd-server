import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FileUploadDialogComponent} from './file-upload-dialog.component';
import {JustUploadModule} from '@andreashauschild/just-upload';
import {MatIconModule} from '@angular/material/icon';
import {MatLegacyDialogContent as MatDialogContent, MatLegacyDialogModule as MatDialogModule} from '@angular/material/legacy-dialog';
import {FormsModule} from '@angular/forms';
import {MatLegacyButtonModule as MatButtonModule} from '@angular/material/legacy-button';
import {BaseDialogModule} from '../base-dialog/base-dialog.module';
import {MatLegacyProgressBarModule as MatProgressBarModule} from '@angular/material/legacy-progress-bar';


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

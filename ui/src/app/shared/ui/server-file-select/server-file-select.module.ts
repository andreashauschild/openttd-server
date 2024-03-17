import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {JustUploadModule} from '@andreashauschild/just-upload';
import {MatIconModule} from '@angular/material/icon';
import {MatLegacyDialogModule as MatDialogModule} from '@angular/material/legacy-dialog';
import {ServerFileSelectComponent} from './server-file-select.component';
import {MatLegacySelectModule as MatSelectModule} from '@angular/material/legacy-select';
import {MatLegacyInputModule as MatInputModule} from '@angular/material/legacy-input';
import {MatLegacyFormFieldModule as MatFormFieldModule} from '@angular/material/legacy-form-field';
import {MatLegacyAutocompleteModule as MatAutocompleteModule} from '@angular/material/legacy-autocomplete';
import {ReactiveFormsModule} from '@angular/forms';
import {MatLegacyTooltipModule as MatTooltipModule} from '@angular/material/legacy-tooltip';


@NgModule({
  declarations: [
    ServerFileSelectComponent
  ],
  imports: [
    CommonModule,
    JustUploadModule,
    MatIconModule,
    MatDialogModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    MatTooltipModule,
  ],

  exports:[ServerFileSelectComponent]
})
export class ServerFileSelectModule {
}

import {NgModule} from '@angular/core';
import {CommonModule, DatePipe} from '@angular/common';
import {JustUploadModule} from '@andreashauschild/just-upload';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material/dialog';
import {ServerFileSelectComponent} from './server-file-select.component';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {ReactiveFormsModule} from '@angular/forms';
import {MatTooltipModule} from '@angular/material/tooltip';


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

import {NgModule} from '@angular/core';
import {CreateServerDialogComponent} from './create-server-dialog.component';
import {ServerFileSelectModule} from '../../../shared/ui/server-file-select/server-file-select.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';


@NgModule({
  declarations: [
    CreateServerDialogComponent
  ],
  imports: [
    ServerFileSelectModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule

  ],
  exports: [CreateServerDialogComponent]
})
export class CreateServerDialogModule {
}

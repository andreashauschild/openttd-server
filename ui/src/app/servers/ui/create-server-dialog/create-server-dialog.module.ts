import {NgModule} from '@angular/core';
import {CreateServerDialogComponent} from './create-server-dialog.component';
import {ServerFileSelectModule} from '../../../shared/ui/server-file-select/server-file-select.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatLegacyFormFieldModule as MatFormFieldModule} from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import {MatIconModule} from '@angular/material/icon';
import {BaseDialogModule} from '../../../shared/ui/base-dialog/base-dialog.module';
import {NgIf} from "@angular/common";


@NgModule({
  declarations: [
    CreateServerDialogComponent
  ],
    imports: [
        ServerFileSelectModule,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        BaseDialogModule,
        NgIf

    ],
  exports: [CreateServerDialogComponent]
})
export class CreateServerDialogModule {
}

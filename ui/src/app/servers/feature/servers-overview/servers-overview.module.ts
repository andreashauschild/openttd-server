import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ServersOverviewRoutingModule} from './servers-overview-routing.module';
import {ServersOverviewComponent} from './servers-overview.component';
import {MatLegacyFormFieldModule as MatFormFieldModule} from '@angular/material/legacy-form-field';
import {MatLegacySelectModule as MatSelectModule} from '@angular/material/legacy-select';
import {FormsModule} from '@angular/forms';
import {OpenttdProcessTerminalDialogModule} from '../../ui/openttd-process-terminal/openttd-process-terminal-dialog.module';
import {FileUploadDialogModule} from '../../../shared/ui/file-upload-dialog/file-upload-dialog.module';
import {CreateServerDialogModule} from '../../ui/create-server-dialog/create-server-dialog.module';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import {OpenttdServerGridModule} from '../../ui/openttd-server-grid/openttd-server-grid.module';
import {MatIconModule} from "@angular/material/icon";


@NgModule({
  declarations: [
    ServersOverviewComponent
  ],
    imports: [
        CommonModule,
        ServersOverviewRoutingModule,
        MatButtonModule,
        MatFormFieldModule,
        MatSelectModule,
        FormsModule,
        OpenttdProcessTerminalDialogModule,
        FileUploadDialogModule,
        CreateServerDialogModule,
        OpenttdServerGridModule,
        MatIconModule
    ]
})
export class ServersOverviewModule {
}

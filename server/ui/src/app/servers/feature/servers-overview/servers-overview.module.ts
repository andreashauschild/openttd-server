import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ServersOverviewRoutingModule} from './servers-overview-routing.module';
import {ServersOverviewComponent} from './servers-overview.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {OpenttdProcessTerminalDialogModule} from '../../ui/openttd-process-terminal/openttd-process-terminal-dialog.module';
import {FileUploadDialogModule} from '../../../shared/ui/file-upload-dialog/file-upload-dialog.module';
import {CreateServerDialogModule} from '../../ui/create-server-dialog/create-server-dialog.module';
import { MatButtonModule } from '@angular/material/button';
import {OpenttdServerGridModule} from '../../ui/openttd-server-grid/openttd-server-grid.module';


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
        OpenttdServerGridModule
    ]
})
export class ServersOverviewModule {
}

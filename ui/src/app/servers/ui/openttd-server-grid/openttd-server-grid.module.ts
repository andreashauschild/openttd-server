import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdServerGridComponent} from './openttd-server-grid.component';
import {MatLegacyTableModule as MatTableModule} from '@angular/material/legacy-table';
import {MatIconModule} from '@angular/material/icon';
import {OpenttdProcessTerminalDialogModule} from '../openttd-process-terminal/openttd-process-terminal-dialog.module';
import {MatLegacyDialogModule as MatDialogModule} from "@angular/material/legacy-dialog";
import {MatLegacyTooltipModule as MatTooltipModule} from '@angular/material/legacy-tooltip';
import {RouterLink} from '@angular/router';


@NgModule({
  declarations: [
    OpenttdServerGridComponent
  ],
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    OpenttdProcessTerminalDialogModule,
    MatDialogModule,
    MatTooltipModule,
    RouterLink
  ],
  exports: [OpenttdServerGridComponent]
})
export class OpenttdServerGridModule {
}

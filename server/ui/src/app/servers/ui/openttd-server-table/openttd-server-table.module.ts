import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdServerTableComponent} from './openttd-server-table.component';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {OpenttdProcessTerminalDialogModule} from '../openttd-process-terminal/openttd-process-terminal-dialog.module';
import {MatDialogModule} from "@angular/material/dialog";


@NgModule({
  declarations: [
    OpenttdServerTableComponent
  ],
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    OpenttdProcessTerminalDialogModule,
    MatDialogModule
  ],
  exports: [OpenttdServerTableComponent]
})
export class OpenttdServerTableModule {
}

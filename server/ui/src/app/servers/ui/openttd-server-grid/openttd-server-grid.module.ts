import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdServerGridComponent} from './openttd-server-grid.component';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {OpenttdProcessTerminalDialogModule} from '../openttd-process-terminal/openttd-process-terminal-dialog.module';
import {MatDialogModule} from "@angular/material/dialog";
import {MatTooltipModule} from '@angular/material/tooltip';


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
    MatTooltipModule
  ],
  exports: [OpenttdServerGridComponent]
})
export class OpenttdServerGridModule {
}

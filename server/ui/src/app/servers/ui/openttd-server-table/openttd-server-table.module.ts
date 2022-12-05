import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdServerTableComponent} from './openttd-server-table.component';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {OpenttdProcessTerminalModule} from '../openttd-process-terminal/openttd-process-terminal.module';


@NgModule({
  declarations: [
    OpenttdServerTableComponent
  ],
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    OpenttdProcessTerminalModule,
  ],
  exports: [OpenttdServerTableComponent]
})
export class OpenttdServerTableModule {
}

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdProcessTerminalDialogComponent} from './openttd-process-terminal-dialog.component';
import {TerminalModule} from '../../../shared/ui/terminal/terminal.module';
import {BaseDialogModule} from "../../../shared/ui/base-dialog/base-dialog.module";


@NgModule({
  declarations: [
    OpenttdProcessTerminalDialogComponent
  ],
  imports: [
    CommonModule,
    TerminalModule,
    BaseDialogModule
  ],
  exports: [OpenttdProcessTerminalDialogComponent]
})
export class OpenttdProcessTerminalDialogModule {
}

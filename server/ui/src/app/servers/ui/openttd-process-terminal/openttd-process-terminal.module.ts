import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OpenttdProcessTerminalComponent} from './openttd-process-terminal.component';
import {TerminalModule} from '../../../shared/ui/terminal/terminal.module';


@NgModule({
  declarations: [
    OpenttdProcessTerminalComponent
  ],
  imports: [
    CommonModule,
    TerminalModule,
  ],
  exports: [OpenttdProcessTerminalComponent]
})
export class OpenttdProcessTerminalModule {
}

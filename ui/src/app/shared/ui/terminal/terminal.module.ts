import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TerminalComponent} from './terminal.component';
import {NgTerminalModule} from 'ng-terminal';


@NgModule({
  declarations: [
    TerminalComponent
  ],
  imports: [
    CommonModule,
    NgTerminalModule
  ],
  exports: [TerminalComponent]
})
export class TerminalModule {
}

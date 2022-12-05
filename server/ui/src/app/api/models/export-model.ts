/* tslint:disable */
/* eslint-disable */
import { OpenttdTerminalUpdateEvent } from './openttd-terminal-update-event';
import { PauseCommand } from './pause-command';
import { UnpauseCommand } from './unpause-command';
export interface ExportModel {
  openttdTerminalUpdateEvent?: OpenttdTerminalUpdateEvent;
  pauseCommand?: PauseCommand;
  unpauseCommand?: UnpauseCommand;
}

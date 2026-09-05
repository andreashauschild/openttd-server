/* tslint:disable */
/* eslint-disable */
import { OpenttdTerminalSnapshotEvent } from './openttd-terminal-snapshot-event';
import { OpenttdTerminalUpdateEvent } from './openttd-terminal-update-event';
import { PauseCommand } from './pause-command';
import { ServiceError } from './service-error';
import { UnpauseCommand } from './unpause-command';
export interface ExportModel {
  openttdTerminalSnapshotEvent?: OpenttdTerminalSnapshotEvent;
  openttdTerminalUpdateEvent?: OpenttdTerminalUpdateEvent;
  pauseCommand?: PauseCommand;
  serviceError?: ServiceError;
  unpauseCommand?: UnpauseCommand;
}

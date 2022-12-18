/* tslint:disable */
/* eslint-disable */
import { BaseProcess } from './base-process';
export interface OpenttdProcess {
  config?: string;
  name?: string;
  port?: number;
  process?: BaseProcess;
  saveGame?: string;
  startServerCommand?: Array<string>;
  uiTerminalOpenedByClient?: boolean;
}

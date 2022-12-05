/* tslint:disable */
/* eslint-disable */
import { OpenttdProcess } from './openttd-process';
import { ServerFile } from './server-file';
export interface OpenttdServer {
  autoSaveGame?: ServerFile;
  config?: ServerFile;
  name?: string;
  port?: number;
  process?: OpenttdProcess;
  saveGame?: ServerFile;
  startSaveGame?: ServerFile;
}

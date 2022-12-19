/* tslint:disable */
/* eslint-disable */
import { OpenttdProcess } from './openttd-process';
import { ServerFile } from './server-file';
export interface OpenttdServer {
  config?: ServerFile;
  id?: string;
  name?: string;
  port?: number;
  process?: OpenttdProcess;
  saveGame?: ServerFile;
}

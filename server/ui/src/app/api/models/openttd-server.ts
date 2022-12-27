/* tslint:disable */
/* eslint-disable */
import { OpenttdProcess } from './openttd-process';
import { ServerFile } from './server-file';
export interface OpenttdServer {
  autoPause?: boolean;
  autoSave?: boolean;
  id?: string;
  name?: string;
  openttdConfig?: ServerFile;
  openttdPrivateConfig?: ServerFile;
  openttdSecretsConfig?: ServerFile;
  password?: string;
  paused?: boolean;
  port?: number;
  process?: OpenttdProcess;
  saveGame?: ServerFile;
}

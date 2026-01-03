/* tslint:disable */
/* eslint-disable */
import { OpenttdProcess } from './openttd-process';
import { ServerFile } from './server-file';
export interface OpenttdServer {
  adminPassword?: string;
  autoPause?: boolean;
  autoSave?: boolean;
  currentClients?: number;
  currentCompanies?: number;
  currentSpectators?: number;
  id?: string;
  inviteCode?: string;
  maxClients?: number;
  maxCompanies?: number;
  name?: string;
  openttdConfig?: ServerFile;
  openttdPrivateConfig?: ServerFile;
  openttdSecretsConfig?: ServerFile;
  password?: string;
  paused?: boolean;
  port?: number;
  process?: OpenttdProcess;
  saveGame?: ServerFile;
  serverAdminPort?: number;
}

/* tslint:disable */
/* eslint-disable */
import { OpenttdServer } from './openttd-server';
export interface InternalOpenttdServerConfig {
  autoSaveMinutes?: number;
  numberOfAutoSaveFilesToKeep?: number;
  numberOfManuallySaveFilesToKeep?: number;
  passwordSha256Hash?: string;
  path?: string;
  servers?: Array<OpenttdServer>;
}

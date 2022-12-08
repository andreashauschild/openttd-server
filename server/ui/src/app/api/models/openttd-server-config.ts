/* tslint:disable */
/* eslint-disable */
import { OpenttdServer } from './openttd-server';
export interface OpenttdServerConfig {
  autoSaveMinutes?: number;
  passwordSha256Hash?: string;
  path?: string;
  servers?: Array<OpenttdServer>;
}

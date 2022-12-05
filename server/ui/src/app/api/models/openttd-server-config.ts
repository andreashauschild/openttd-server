/* tslint:disable */
/* eslint-disable */
import { OpenttdServer } from './openttd-server';
export interface OpenttdServerConfig {
  autoSaveMinutes?: number;
  path?: string;
  servers?: Array<OpenttdServer>;
}

/* tslint:disable */
/* eslint-disable */
import { OpenttdServer } from './openttd-server';
export interface OpenttdServerConfigGet {
  autoSaveMinutes?: number;
  numberOfAutoSaveFilesToKeep?: number;
  numberOfManuallySaveFilesToKeep?: number;
  servers?: Array<OpenttdServer>;
}

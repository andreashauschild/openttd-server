/* tslint:disable */
/* eslint-disable */
import { ServerFileType } from './server-file-type';
export interface ServerFile {
  created?: number;
  exists?: boolean;
  lastModified?: number;
  name?: string;
  path?: string;
  type?: ServerFileType;
}

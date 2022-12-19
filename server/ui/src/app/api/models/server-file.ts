/* tslint:disable */
/* eslint-disable */
import { ServerFileType } from './server-file-type';
export interface ServerFile {
  created?: number;
  exists?: boolean;
  lastModified?: number;
  name?: string;
  ownerId?: string;
  path?: string;
  type?: ServerFileType;
}

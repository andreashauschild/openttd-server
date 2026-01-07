/* tslint:disable */
/* eslint-disable */
import { ExplorerFile } from './explorer-file';
export interface ExplorerDirectory {
  absolutePath?: string;
  dirs?: Array<ExplorerDirectory>;
  files?: Array<ExplorerFile>;
  id?: string;
  name?: string;
  relativePath?: string;
}

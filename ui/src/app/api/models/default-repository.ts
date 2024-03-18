/* tslint:disable */
/* eslint-disable */
import { InternalOpenttdServerConfig } from './internal-openttd-server-config';
import { OpenttdServerMapper } from './openttd-server-mapper';
import { Path } from './path';
import { ServerFile } from './server-file';
export interface DefaultRepository {
  configFile?: Path;
  openttdConfigDir?: string;
  openttdConfigDirPath?: Path;
  openttdConfigs?: Array<ServerFile>;
  openttdSaveDir?: string;
  openttdSaveDirPath?: Path;
  openttdSaveGames?: Array<ServerFile>;
  openttdServerConfig?: InternalOpenttdServerConfig;
  openttdServerMapper?: OpenttdServerMapper;
  serverConfigDir?: string;
}

import {createAction, props} from '@ngrx/store';
import {OpenttdProcess} from '../../../api/models/openttd-process';
import {OpenttdTerminalUpdateEvent} from '../../../api/models/openttd-terminal-update-event';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {ServerFile} from '../../../api/models/server-file';
import {AppAlert} from '../reducers/app.reducer';
import {OpenttdServerConfigGet} from '../../../api/models/openttd-server-config-get';
import {OpenttdServerConfigUpdate} from '../../../api/models/openttd-server-config-update';
import {ExplorerData} from '../../../api/models/explorer-data';

export const createAlert = createAction('[App] Creates an alert', props<{ src: string; alert: AppAlert }>());

export const removeAlert = createAction('[App] Removes the given alert', props<{ src: string; alertId: string }>());

export const loadProcesses = createAction(
  '[App] loadProcesses', props<{ src: string }>()
);

export const loadProcessesSuccess = createAction(
  '[App] loadProcessesSuccess', props<{ src: string, result: OpenttdProcess[] }>()
);

export const processUpdateEvent = createAction(
  '[App] processUpdateEvent', props<{ src: string, event: OpenttdTerminalUpdateEvent }>()
);


export const loadServer = createAction(
  '[App] loadServer', props<{ src: string; id: string }>()
);

export const loadServerSuccess = createAction(
  '[App] loadServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const addServer = createAction(
  '[App] addServer', props<{ src: string; server: OpenttdServer }>()
);

export const addServerSuccess = createAction(
  '[App] addServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const updateServer = createAction(
  '[App] updateServer', props<{ src: string; id: string, server: OpenttdServer }>()
);

export const updateServerSuccess = createAction(
  '[App] updateServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const deleteServer = createAction(
  '[App] deleteServer', props<{ src: string; id: string }>()
);

export const deleteServerSuccess = createAction(
  '[App] deleteServerSuccess', props<{ src: string, id: string }>()
);

export const stopServer = createAction(
  '[App] stopServer', props<{ src: string; id: string }>()
);

export const stopServerSuccess = createAction(
  '[App] stopServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const pauseUnpauseServer = createAction(
  '[App] pauseUnpauseServer', props<{ src: string; id: string }>()
);

export const pauseUnpauseServerSuccess = createAction(
  '[App] pauseUnpauseServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const saveServer = createAction(
  '[App] saveServer', props<{ src: string; id: string }>()
);

export const saveServerSuccess = createAction(
  '[App] saveServerSuccess', props<{ src: string; id: string }>()
);

export const startServer = createAction(
  '[App] startServer', props<{ src: string; id: string }>()
);

export const startServerSuccess = createAction(
  '[App] startServerSuccess', props<{ src: string, server: OpenttdServer }>()
);


export const loadServerConfig = createAction(
  '[App] loadServerConfig', props<{ src: string }>()
);

export const loadServerConfigSuccess = createAction(
  '[App] loadServerConfigSuccess', props<{ src: string, config: OpenttdServerConfigGet }>()
);

export const patchServerConfig = createAction(
  '[App] patchServerConfig', props<{ src: string, patch: OpenttdServerConfigUpdate }>()
);

export const patchServerConfigSuccess = createAction(
  '[App] patchServerConfigSuccess', props<{ src: string, config: OpenttdServerConfigGet }>()
);


export const loadServerFiles = createAction(
  '[App] loadServerFiles', props<{ src: string }>()
);

export const loadServerFilesSuccess = createAction(
  '[App] loadServerFilesSuccess', props<{ src: string, files: ServerFile[] }>()
);

export const loadExplorerData = createAction(
  '[App] loadExplorerData', props<{ src: string }>()
);

export const loadExplorerDataSuccess = createAction(
  '[App] loadExplorerDataSuccess', props<{ src: string, data: ExplorerData }>()
);

export const deleteExplorerFile = createAction(
  '[App] deleteExplorerFile', props<{ src: string, relativePath: string }>()
);

export const deleteExplorerFileSuccess = createAction(
  '[App] deleteExplorerFileSuccess', props<{ src: string, data: ExplorerData }>()
);

export const createExplorerDir = createAction(
  '[App] createExplorerDir', props<{ src: string, relativeDirPath: string }>()
);

export const createExplorerDirSuccess = createAction(
  '[App] createExplorerDirSuccess', props<{ src: string, data: ExplorerData }>()
);

export const renameExplorerFile = createAction(
  '[App] renameExplorerFile', props<{ src: string, relativePath: string, newName: string }>()
);

export const renameExplorerFileSuccess = createAction(
  '[App] renameExplorerFileSuccess', props<{ src: string, data: ExplorerData }>()
);

export const moveExplorerFile = createAction(
  '[App] moveExplorerFile', props<{ src: string, sourcePath: string, destinationPath: string, overwrite?: boolean }>()
);

export const moveExplorerFileSuccess = createAction(
  '[App] moveExplorerFileSuccess', props<{ src: string, data: ExplorerData }>()
);

export const copyExplorerFile = createAction(
  '[App] copyExplorerFile', props<{ src: string, sourcePath: string, destinationPath: string }>()
);

export const copyExplorerFileSuccess = createAction(
  '[App] copyExplorerFileSuccess', props<{ src: string, data: ExplorerData }>()
);

export const downloadExplorerZip = createAction(
  '[App] downloadExplorerZip', props<{ src: string, directoryPath: string }>()
);

export const downloadSelectedExplorerZip = createAction(
  '[App] downloadSelectedExplorerZip', props<{ src: string, directoryPath: string, fileNames: string[] }>()
);

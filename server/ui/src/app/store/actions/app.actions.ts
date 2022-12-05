import {createAction, props} from '@ngrx/store';
import {OpenttdTerminalUpdateEvent} from '../../api/models/openttd-terminal-update-event';
import {OpenttdProcess} from "../../api/models/openttd-process";
import {OpenttdServer} from "../../api/models/openttd-server";
import {OpenttdServerConfig} from "../../api/models/openttd-server-config";
import {ServerFile} from "../../api/models/server-file";

export const loadProcesses = createAction(
  '[App] loadProcesses', props<{ src: string }>()
);

export const loadProcessesSuccess = createAction(
  '[App] loadProcessesSuccess', props<{ src: string, result: OpenttdProcess[] }>()
);


export const startProcess = createAction(
  '[App] startProcess', props<{ src: string; setup: { name?: string, port?: number, savegame?: string, config?: string } }>()
);

export const startProcessSuccess = createAction(
  '[App] startProcessSuccess', props<{ src: string, result: OpenttdProcess }>()
);

export const processUpdateEvent = createAction(
  '[App] processUpdateEvent', props<{ src: string, event: OpenttdTerminalUpdateEvent }>()
);


export const loadServer = createAction(
  '[App] loadServer', props<{ src: string; name: string }>()
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
  '[App] updateServer', props<{ src: string; server: OpenttdServer }>()
);

export const updateServerSuccess = createAction(
  '[App] updateServerSuccess', props<{ src: string, server: OpenttdServer }>()
);

export const deleteServer = createAction(
  '[App] deleteServer', props<{ src: string; name: string }>()
);

export const deleteServerSuccess = createAction(
  '[App] deleteServerSuccess', props<{ src: string, name: string }>()
);

export const saveServer = createAction(
  '[App] saveServer', props<{ src: string; name: string }>()
);

export const saveServerSuccess = createAction(
  '[App] saveServerSuccess', props<{ src: string; name: string }>()
);

export const startServer = createAction(
  '[App] startServer', props<{ src: string;  name: string, saveGame?: string  }>()
);

export const startServerSuccess = createAction(
  '[App] startServerSuccess', props<{ src: string, server: OpenttdServer }>()
);



export const loadServerConfig = createAction(
  '[App] loadServerConfig', props<{ src: string}>()
);

export const loadServerConfigSuccess = createAction(
  '[App] loadServerConfigSuccess', props<{ src: string, config: OpenttdServerConfig }>()
);

export const loadServerFiles = createAction(
  '[App] loadServerFiles', props<{ src: string}>()
);

export const loadServerFilesSuccess = createAction(
  '[App] loadServerFilesSuccess', props<{ src: string, files: ServerFile[] }>()
);

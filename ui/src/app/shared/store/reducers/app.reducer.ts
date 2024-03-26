import {createReducer, on} from '@ngrx/store';
import * as AppActions from '../actions/app.actions';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {OpenttdProcess} from '../../../api/models/openttd-process';
import {ServerFile} from '../../../api/models/server-file';
import {OpenttdTerminalUpdateEvent} from '../../../api/models/openttd-terminal-update-event';
import {OpenttdServerConfigGet} from '../../../api/models/openttd-server-config-get';
import {ExplorerData} from '../../../api/models/explorer-data';

export interface AppAlert {
  id: string;
  type: 'warning' | 'info' | 'success' | 'error';
  message: string;
  stacktrace?: string;
}


export const appFeatureKey = 'app';


export interface State {
  config?: OpenttdServerConfigGet
  servers: OpenttdServer[]
  server?: OpenttdServer
  explorer?: ExplorerData
  processes: OpenttdProcess[];
  files: ServerFile[];
  processUpdateEvent?: OpenttdTerminalUpdateEvent;
  alerts: AppAlert[];
}

export const initialState: State = {
  servers: [],
  processes: [],
  files: [],
  alerts: []
};

export const reducer = createReducer(
  initialState,
  on(AppActions.createAlert, (state, action): State => {
    const newState = {...state, alerts: state.alerts.concat()};
    newState.alerts.push(action.alert);
    return newState;
  }),
  on(AppActions.removeAlert, (state, action): State => {
    return {
      ...state,
      alerts: state.alerts.filter((a) => a.id !== action.alertId),
    };
  }),

  on(AppActions.processUpdateEvent, (state, action): State => {
    return {
      ...state,
      processUpdateEvent: action.event
    }
  }),

  on(AppActions.loadProcessesSuccess, (state, action): State => {


    return {
      ...state,
      processes: action.result
    }

  }),

  on(AppActions.loadServerFilesSuccess, (state, action): State => {


    return {
      ...state,
      files: action.files
    }

  }),

  on(AppActions.loadServerConfigSuccess, AppActions.patchServerConfigSuccess, (state, action): State => {
    return {
      ...state,
      config: action.config,
      servers: action.config.servers!

    }
  }),
  on(AppActions.loadExplorerDataSuccess, AppActions.deleteExplorerFileSuccess
    , AppActions.createExplorerDirSuccess, AppActions.renameExplorerFileSuccess, (state: State, action): State => {
      return {
        ...state,
        explorer: action.data,
      }
    }),

  on(AppActions.addServerSuccess, (state, action): State => {
    return {
      ...state,
      servers: state.servers.concat(action.server)
    }
  }),

  on(AppActions.updateServerSuccess, AppActions.startServerSuccess, AppActions.loadServerSuccess
    , AppActions.stopServerSuccess, AppActions.pauseUnpauseServerSuccess, (state, action): State => {
      return {
        ...state,
        server: action.server,
        servers: state.servers.map(s => {
          if (s.id === action.server.id) {
            return action.server;
          }
          return s;
        })
      }
    }),

  on(AppActions.deleteServerSuccess, (state, action): State => {
    return {
      ...state,
      servers: state.servers.filter(s => {
        return (s.id !== action.id)
      })
    }
  }),

  on(AppActions.startServerSuccess, (state, action): State => {
    return {
      ...state,
      processes: state.processes.concat(action.server.process!)
    }
  }),
);

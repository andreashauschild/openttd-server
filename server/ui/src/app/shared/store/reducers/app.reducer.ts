import {createReducer, on} from '@ngrx/store';
import * as AppActions from '../actions/app.actions';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {OpenttdProcess} from '../../../api/models/openttd-process';
import {ServerFile} from '../../../api/models/server-file';
import {OpenttdTerminalUpdateEvent} from '../../../api/models/openttd-terminal-update-event';
import {OpenttdServerConfigGet} from '../../../api/models/openttd-server-config-get';

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
  processes: OpenttdProcess[];
  files: ServerFile[];
  processUpdateEvent: OpenttdTerminalUpdateEvent[];
  alerts: AppAlert[];
}

export const initialState: State = {
  servers: [],
  processes: [],
  processUpdateEvent: [],
  files: [],
  alerts: [],
};

export const reducer = createReducer(
  initialState,
  on(AppActions.createAlert, (state, action) => {
    const newState = {...state, alerts: state.alerts.concat()};
    newState.alerts.push(action.alert);
    return newState;
  }),
  on(AppActions.removeAlert, (state, action) => {
    return {
      ...state,
      alerts: state.alerts.filter((a) => a.id !== action.alertId),
    };
  }),

  on(AppActions.processUpdateEvent, (state, action) => {

    const event = state.processUpdateEvent.find((e) => action.event.processId === e.processId);
    if (!event) {
      return {
        ...state,
        processUpdateEvent: state.processUpdateEvent.concat(action.event)
      }
    } else {
      return {
        ...state,
        processUpdateEvent: state.processUpdateEvent.map(e => {
          if (e.processId !== action.event.processId) {
            return e;
          } else {
            return action.event;
          }
        }) as OpenttdTerminalUpdateEvent[],
      }
    }
  }),

  on(AppActions.loadProcessesSuccess, (state, action) => {


    return {
      ...state,
      processes: action.result
    }

  }),

  on(AppActions.loadServerFilesSuccess, (state, action) => {


    return {
      ...state,
      files: action.files
    }

  }),

  on(AppActions.loadServerConfigSuccess, AppActions.patchServerConfigSuccess, (state, action) => {
    return {
      ...state,
      config: action.config,
      servers: action.config.servers!

    }
  }),

  on(AppActions.addServerSuccess, (state, action) => {
    return {
      ...state,
      servers: state.servers.concat(action.server)
    }
  }),

  on(AppActions.updateServerSuccess, AppActions.startServerSuccess, AppActions.loadServerSuccess, (state, action) => {
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

  on(AppActions.deleteServerSuccess, (state, action) => {
    return {
      ...state,
      servers: state.servers.filter(s => {
        return (s.id !== action.id)
      })
    }
  }),

  on(AppActions.startServerSuccess, (state, action) => {
    return {
      ...state,
      processes: state.processes.concat(action.server.process!)
    }

  }),
);

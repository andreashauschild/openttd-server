import {createReducer, on} from '@ngrx/store';
import * as AppActions from '../actions/app.actions';
import {OpenttdTerminalUpdateEvent} from '../../api/models/openttd-terminal-update-event';
import {OpenttdProcess} from "../../api/models/openttd-process";
import {OpenttdServerConfig} from "../../api/models/openttd-server-config";
import {OpenttdServer} from "../../api/models/openttd-server";
import {ServerFile} from "../../api/models/server-file";

export const appFeatureKey = 'app';


export interface State {
  config?: OpenttdServerConfig
  servers: OpenttdServer[]
  processes: OpenttdProcess[];
  files: ServerFile[];
  processUpdateEvent: OpenttdTerminalUpdateEvent[];
}

export const initialState: State = {
  servers: [],
  processes: [],
  processUpdateEvent: [],
  files: []
};

export const reducer = createReducer(
  initialState,

  on(AppActions.processUpdateEvent, (state, action) => {

    const event = state.processUpdateEvent.find((e) => action.event.processId === e.processId);
    console.log("UPDATE ", event, action)
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

  on(AppActions.loadServerConfigSuccess, (state, action) => {
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
      servers: state.servers.map(s => {
        if (s.name === action.server.name) {
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
        return (s.name !== action.name)
      })
    }
  }),

  on(AppActions.startProcessSuccess, (state, action) => {
    return {
      ...state,
      processes: state.processes.concat(action.result)
    }

  }),
);

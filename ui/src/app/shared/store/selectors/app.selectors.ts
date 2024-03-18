import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as fromApp from '../reducers/app.reducer';

export const selectAppState = createFeatureSelector<fromApp.State>(
  fromApp.appFeatureKey
);

export const selectAlerts = createSelector(selectAppState, (state) => state.alerts);


export const selectProcesses = createSelector(selectAppState, (state) => state.processes);
export const selectProcessUpdateEvent = createSelector(selectAppState, (state) => state.processUpdateEvent);
export const selectServers = createSelector(selectAppState, (state) => state.servers);
export const selectServer = createSelector(selectAppState, (state) => state.server);
export const selectConfig = createSelector(selectAppState, (state) => state.config);

export const selectFiles = createSelector(selectAppState, (state) => state.files);
export const selectExplorerData = createSelector(selectAppState, (state) => state.explorer);

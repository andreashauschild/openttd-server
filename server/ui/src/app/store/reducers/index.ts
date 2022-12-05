import {
  ActionReducer,
  ActionReducerMap,
  createFeatureSelector,
  createSelector,
  MetaReducer
} from '@ngrx/store';
import { environment } from '../../../environments/environment';
import * as fromWorkers from './app.reducer';


export interface State {

  [fromWorkers.appFeatureKey]: fromWorkers.State;


}

export const reducers: ActionReducerMap<State> = {

  [fromWorkers.appFeatureKey]: fromWorkers.reducer,

};


export const metaReducers: MetaReducer<State>[] = !environment.production ? [] : [];

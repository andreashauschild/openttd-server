import {ActionReducerMap, MetaReducer} from '@ngrx/store';
import * as fromWorkers from './app.reducer';
import {environment} from '../../../../environments/environment';


export interface State {

  [fromWorkers.appFeatureKey]: fromWorkers.State;


}

export const reducers: ActionReducerMap<State> = {

  [fromWorkers.appFeatureKey]: fromWorkers.reducer,

};


export const metaReducers: MetaReducer<State>[] = !environment.production ? [] : [];

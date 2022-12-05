import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {OpenttdServerResourceService} from '../../api/services/openttd-server-resource.service';
import * as AppActions from '../actions/app.actions'
import {
  addServer, addServerSuccess, deleteServer, deleteServerSuccess,
  loadProcessesSuccess,
  loadServerConfig,
  loadServerConfigSuccess, loadServerFilesSuccess, loadServerSuccess, saveServerSuccess,
  startProcessSuccess, startServer, startServerSuccess, updateServer, updateServerSuccess
} from '../actions/app.actions'
import {catchError, EMPTY, mergeMap} from 'rxjs';
import {map} from 'rxjs/operators';

@Injectable()
export class AppEffects {
  constructor(private actions$: Actions, private service: OpenttdServerResourceService) {
  }


  loadProcesses = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadProcesses),
      mergeMap(() => this.service.processes()
        .pipe(
          map(result => loadProcessesSuccess({src: AppEffects.name, result})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  startProcess = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.startProcess),
      mergeMap((a) => this.service.start({
          config: a.setup.config,
          name: a.setup.name,
          port: a.setup.port,
          savegame: a.setup.savegame
        })
          .pipe(
            map(result => startProcessSuccess({src: AppEffects.name, result})),
            catchError((err) => {
              console.log(err)
              return EMPTY;
            })
          )
      )
    );
  });

  loadServerConfig = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadServerConfig),
      mergeMap((a) => this.service.getOpenttdServerConfig()
        .pipe(
          map(config => loadServerConfigSuccess({src: AppEffects.name, config})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  loadServerFiles = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadServerFiles),
      mergeMap((a) => this.service.getServerFiles()
        .pipe(
          map(files => loadServerFilesSuccess({src: AppEffects.name, files})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });


  addServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.addServer),
      mergeMap((a) => this.service.addServer({body: a.server})
        .pipe(
          map(server => addServerSuccess({src: AppEffects.name, server})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  startServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.startServer),
      mergeMap((a) => this.service.startServer({name: a.name, savegame: a.saveGame})
        .pipe(
          map(server => startServerSuccess({src: AppEffects.name, server})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  updateServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.updateServer),
      mergeMap((a) => this.service.updateServer({body: a.server})
        .pipe(
          map(server => updateServerSuccess({src: AppEffects.name, server})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  loadServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadServer),
      mergeMap((a) => this.service.getServer({name: a.name})
        .pipe(
          map(server => loadServerSuccess({src: AppEffects.name, server})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });


  deleteServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.deleteServer),
      mergeMap((a) => this.service.deleteServer({name: a.name})
        .pipe(
          map(name => deleteServerSuccess({src: AppEffects.name, name: a.name})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });

  saveServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.saveServer),
      mergeMap((a) => this.service.save({name: a.name})
        .pipe(
          map(name => saveServerSuccess({src: AppEffects.name, name: a.name})),
          catchError((err) => {
            console.log(err)
            return EMPTY;
          })
        )
      )
    );
  });


}

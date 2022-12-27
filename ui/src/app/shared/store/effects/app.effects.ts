import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import * as AppActions from '../actions/app.actions'
import {
  addServerSuccess,
  deleteServerSuccess,
  loadProcessesSuccess,
  loadServerConfigSuccess,
  loadServerFilesSuccess,
  loadServerSuccess,
  patchServerConfigSuccess, pauseUnpauseServerSuccess,
  saveServerSuccess,
  startServerSuccess, stopServerSuccess,
  updateServerSuccess
} from '../actions/app.actions'
import {catchError, EMPTY, mergeMap} from 'rxjs';
import {map} from 'rxjs/operators';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {ApplicationService} from '../../services/application.service';

@Injectable()
export class AppEffects {
  constructor(private app: ApplicationService, private actions$: Actions, private service: OpenttdServerResourceService) {
  }


  loadProcesses = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadProcesses),
      mergeMap(() => this.service.processes()
        .pipe(
          map(result => loadProcessesSuccess({src: AppEffects.name, result})),
          catchError((err) => {
            this.app.handleError(err);
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
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  patchServerConfig = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.patchServerConfig),
      mergeMap((a) => this.service.updateOpenttdServerConfig({body: a.patch})
        .pipe(
          map(config => {
            this.app.createInfoMessage("Settings updated", 2000)
            return patchServerConfigSuccess({src: AppEffects.name, config})
          }),
          catchError((err) => {
            this.app.handleError(err);
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
            this.app.handleError(err);
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
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  startServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.startServer),
      mergeMap((a) => this.service.startServer({id: a.id})
        .pipe(
          map(server => {
            this.app.createInfoMessage("Server started!", 2000)
            return startServerSuccess({src: AppEffects.name, server})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  stopServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.stopServer),
      mergeMap((a) => this.service.stop({id: a.id})
        .pipe(
          map(server => {
            this.app.createInfoMessage("Server stopped!", 2000)
            return stopServerSuccess({src: AppEffects.name, server})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  pauseUnpauseServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.pauseUnpauseServer),
      mergeMap((a) => this.service.pauseUnpause({id: a.id})
        .pipe(
          map(server => {
            if (server.paused) {
              this.app.createInfoMessage("Server paused!",2000)
            } else {
              this.app.createInfoMessage("Server unpaused!",2000)
            }
            return pauseUnpauseServerSuccess({src: AppEffects.name, server})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });


  updateServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.updateServer),
      mergeMap((a) => this.service.updateServer({id: a.id, body: a.server})
        .pipe(
          map(server => {
            this.app.createInfoMessage("Server updated", 2000)
            return updateServerSuccess({src: AppEffects.name, server})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  loadServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.loadServer),
      mergeMap((a) => this.service.getServer({id: a.id})
        .pipe(
          map(server => loadServerSuccess({src: AppEffects.name, server})),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });


  deleteServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.deleteServer),
      mergeMap((a) => this.service.deleteServer({id: a.id})
        .pipe(
          map(name => {
            this.app.createInfoMessage("Server deleted!", 2000)
            return deleteServerSuccess({src: AppEffects.name, id: a.id})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });

  saveServer = createEffect(() => {
    return this.actions$.pipe(
      ofType(AppActions.saveServer),
      mergeMap((a) => this.service.save({id: a.id})
        .pipe(
          map(name => {
            this.app.createInfoMessage("Game saved!", 2000)
            return saveServerSuccess({src: AppEffects.name, id: a.id})
          }),
          catchError((err) => {
            this.app.handleError(err);
            return EMPTY;
          })
        )
      )
    );
  });


}

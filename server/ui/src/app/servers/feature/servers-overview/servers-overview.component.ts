import {Component, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {Actions, ofType} from "@ngrx/effects";
import {FormBuilder} from '@angular/forms';
import {MatDialog} from "@angular/material/dialog";
import {OpenttdProcess} from '../../../api/models/openttd-process';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import * as AppActions from '../../../shared/store/actions/app.actions';
import {loadProcesses, loadServer, startProcess} from '../../../shared/store/actions/app.actions';
import {selectProcesses} from '../../../shared/store/selectors/app.selectors';
import {CreateServerDialogComponent} from '../../ui/create-server-dialog/create-server-dialog.component';
import {FileUploadDialogComponent} from '../../../shared/ui/file-upload-dialog/file-upload-dialog.component';
import {ServerFileType} from '../../../api/models/server-file-type';

@Component({
  selector: 'app-servers-overview',
  templateUrl: './servers-overview.component.html',
  styleUrls: ['./servers-overview.component.scss']
})
export class ServersOverviewComponent implements OnInit {


  openttdProcesses: OpenttdProcess[] = [];
  openttdProcess: OpenttdProcess | undefined;
  init = false;


  constructor(private actions$: Actions, private store: Store<{}>, private openttd: OpenttdServerResourceService, private fb: FormBuilder, public dialog: MatDialog) {
  }

  ngOnInit(): void {

    this.store.dispatch(loadProcesses({src: ServersOverviewComponent.name}))
    this.actions$.pipe(
      ofType(AppActions.startProcessSuccess)
    ).subscribe(a => {
      this.openttdProcess = a.result;
    })

    this.actions$.pipe(
      ofType(AppActions.saveServerSuccess)
    ).subscribe(a => {
      this.store.dispatch(loadServer({src: ServersOverviewComponent.name, name: a.name}))
    })


    this.store.select(selectProcesses).subscribe(p => {
      this.openttdProcesses = p;
    })


  }

  startServer() {
    this.store.dispatch(startProcess({src: ServersOverviewComponent.name, setup: {}}))
  }

  selectProcess($event: OpenttdProcess | Event) {
    console.log(">>>>>>>>>>>><", $event)
  }


  createServer() {
    const dialogRef = this.dialog.open(CreateServerDialogComponent);

    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });

  }

  uploadSaveGame() {
    const dialogRef = this.dialog.open(FileUploadDialogComponent);
    dialogRef.componentInstance.fileType = ServerFileType.SaveGame;
    dialogRef.componentInstance.dialogTitle = "Upload OpenTTD Savegames";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  uploadConfig() {
    const dialogRef = this.dialog.open(FileUploadDialogComponent);
    dialogRef.componentInstance.fileType = ServerFileType.Config;
    dialogRef.componentInstance.dialogTitle = "Upload OpenTTD Configs";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }
}

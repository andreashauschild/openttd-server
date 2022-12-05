import {Component, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {selectProcesses} from '../../store/selectors/app.selectors';
import {OpenttdServerResourceService} from '../../api/services/openttd-server-resource.service';
import * as AppActions from '../../store/actions/app.actions';
import {loadProcesses, loadServer, startProcess} from '../../store/actions/app.actions';
import {OpenttdProcess} from "../../api/models/openttd-process";
import {Actions, ofType} from "@ngrx/effects";
import {FormBuilder} from '@angular/forms';
import {ServerFileType} from "../../api/models/server-file-type";
import {MatDialog} from "@angular/material/dialog";
import {CreateServerDialogComponent} from "./create-server-dialog/create-server-dialog.component";
import {FileUploadDialogComponent} from "../../components/file-upload-dialog/file-upload-dialog.component";

@Component({
  selector: 'app-management-view',
  templateUrl: './management-view.component.html',
  styleUrls: ['./management-view.component.scss']
})
export class ManagementViewComponent implements OnInit {


  openttdProcesses: OpenttdProcess[] = [];
  openttdProcess: OpenttdProcess | undefined;
  init = false;


  constructor(private actions$: Actions, private store: Store<{}>, private openttd: OpenttdServerResourceService, private fb: FormBuilder, public dialog: MatDialog) {
  }

  ngOnInit(): void {

    this.store.dispatch(loadProcesses({src: ManagementViewComponent.name}))
    this.actions$.pipe(
      ofType(AppActions.startProcessSuccess)
    ).subscribe(a => {
      this.openttdProcess = a.result;
    })

    this.actions$.pipe(
      ofType(AppActions.saveServerSuccess)
    ).subscribe(a => {
      this.store.dispatch(loadServer({src: ManagementViewComponent.name, name: a.name}))
    })


    this.store.select(selectProcesses).subscribe(p => {
      this.openttdProcesses = p;
    })


  }

  startServer() {
    this.store.dispatch(startProcess({src: ManagementViewComponent.name, setup: {}}))
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

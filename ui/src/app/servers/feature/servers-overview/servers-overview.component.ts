import {Component, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {Actions, ofType} from "@ngrx/effects";
import {MatDialog} from "@angular/material/dialog";
import * as AppActions from '../../../shared/store/actions/app.actions';
import {loadProcesses, loadServer} from '../../../shared/store/actions/app.actions';
import {CreateServerDialogComponent} from '../../ui/create-server-dialog/create-server-dialog.component';
import {FileUploadDialogComponent} from '../../../shared/ui/file-upload-dialog/file-upload-dialog.component';
import {ServerFileType} from '../../../api/models/server-file-type';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {OpenttdServerGridComponent} from '../../ui/openttd-server-grid/openttd-server-grid.component';

@Component({
    selector: 'app-servers-overview',
    templateUrl: './servers-overview.component.html',
    styleUrls: ['./servers-overview.component.scss'],
    standalone: true,
    imports: [MatButton, MatIcon, OpenttdServerGridComponent]
})
export class ServersOverviewComponent implements OnInit {
  init = false;

  constructor(private actions$: Actions, private store: Store<{}>, public dialog: MatDialog) {
  }

  ngOnInit(): void {

    this.store.dispatch(loadProcesses({src: ServersOverviewComponent.name}))

    this.actions$.pipe(
      ofType(AppActions.saveServerSuccess)
    ).subscribe(a => {
      this.store.dispatch(loadServer({src: ServersOverviewComponent.name, id: a.id}))
    })


  }

  createServer() {
    const dialogRef = this.dialog.open(CreateServerDialogComponent, {
      minWidth: '60%'
    });

    dialogRef.componentInstance.dialogRef = dialogRef;
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });

  }

  uploadSaveGame() {
    const dialogRef = this.dialog.open(FileUploadDialogComponent, {minWidth: "800px"});
    dialogRef.componentInstance.dialogRef = dialogRef;
    dialogRef.componentInstance.fileType = ServerFileType.SaveGame;
    dialogRef.componentInstance.dialogTitle = "UPLOAD OPENTTD SAVEGAMES";
    dialogRef.componentInstance.subTitle = "Info: Don't upload files where the filename contains single/double quotes: ' or \" . This will cause problems!";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  uploadConfig() {
    const dialogRef = this.dialog.open(FileUploadDialogComponent, {minWidth: "800px"});
    dialogRef.componentInstance.dialogRef = dialogRef;
    dialogRef.componentInstance.fileType = ServerFileType.Config;
    dialogRef.componentInstance.dialogTitle = "UPLOAD OPENTTD CONFIGS";
    dialogRef.componentInstance.subTitle = "Info: Don't upload files where the filename contains single/double quotes: ' or \" . This will cause problems!";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }
}

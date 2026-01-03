import {Component, OnInit} from '@angular/core';
import {ServerFile} from "../../../api/models/server-file";
import {OpenttdServerResourceService} from "../../../api/services/openttd-server-resource.service";
import {FormBuilder} from "@angular/forms";
import {Store} from "@ngrx/store";
import {ServerFileType} from "../../../api/models/server-file-type";
import {addServer, loadServerFiles} from '../../../shared/store/actions/app.actions';
import {selectFiles} from '../../../shared/store/selectors/app.selectors';
import {MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'app-create-server-dialog',
    templateUrl: './create-server-dialog.component.html',
    styleUrls: ['./create-server-dialog.component.scss'],
    standalone: false
})
export class CreateServerDialogComponent implements OnInit {
  configFiles: ServerFile[] = []
  saveGameFiles: ServerFile[] = []
  showPassword=false;
  createServerForm = this.fb.group({
    name: [''],
    port: [3979],
    password: [''],
    saveGame: <ServerFile>[{}],
    config: <ServerFile>[{}],
    configSecret: <ServerFile>[{}],
    configPrivate: <ServerFile>[{}]
  });

  public dialogRef: MatDialogRef<CreateServerDialogComponent, boolean> | null = null;

  constructor(private store: Store<{}>, private openttd: OpenttdServerResourceService, private fb: FormBuilder) {
  }

  ngOnInit(): void {
    this.store.dispatch(loadServerFiles({src: CreateServerDialogComponent.name}))
    this.store.select(selectFiles).subscribe(files => {
      this.configFiles = [];
      this.saveGameFiles = [];

      files.forEach(f => {
        if (f.type === ServerFileType.SaveGame) {
          this.saveGameFiles.push(f)
        }
        if (f.type === ServerFileType.Config) {
          this.configFiles.push(f)
        }
      })
    })
  }

  createServer() {
    this.store.dispatch(addServer({
      src: CreateServerDialogComponent.name, server: {
        name: this.createServerForm.controls.name.value!,
        port: this.createServerForm.controls.port.value!,
        password: this.createServerForm.controls.password.value!,
        openttdConfig: this.createServerForm.controls.config.value!,
        openttdSecretsConfig: this.createServerForm.controls.configSecret.value!,
        openttdPrivateConfig: this.createServerForm.controls.configPrivate.value!,
        saveGame: this.createServerForm.controls.saveGame.value!,
      }
    }))
    this.dialogRef?.close(true);
  }

  selectSaveGame(file: ServerFile) {
    if (file && file.name!.length > 0) {
      this.createServerForm.controls.saveGame.patchValue(file);
    }
  }

  selectConfig(file: ServerFile) {
    if (file && file.name!.length > 0) {
      this.createServerForm.controls.config.patchValue(file);
    }
  }

  selectSecretConfig(file: ServerFile) {
    if (file && file.name!.length > 0) {
      this.createServerForm.controls.configSecret.patchValue(file);
    }
  }

  selectPrivateConfig(file: ServerFile) {
    if (file && file.name!.length > 0) {
      this.createServerForm.controls.configPrivate.patchValue(file);
    }
  }

}

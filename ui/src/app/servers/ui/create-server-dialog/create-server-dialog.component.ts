import {Component, OnInit} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Store} from "@ngrx/store";
import {MatDialogRef} from '@angular/material/dialog';
import {NgIf} from '@angular/common';
import {MatFormField, MatLabel, MatHint, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';

import {ServerFile} from "@api/models/server-file";
import {OpenttdServerResourceService} from "@api/services/openttd-server-resource.service";
import {ServerFileType} from "@api/models/server-file-type";
import {addServer, loadServerFiles} from '@store/actions/app.actions';
import {selectFiles} from '@store/selectors/app.selectors';
import {BaseDialogComponent} from '@shared/ui/base-dialog/base-dialog.component';
import {ServerFileSelectComponent} from '@shared/ui/server-file-select/server-file-select.component';

@Component({
    selector: 'app-create-server-dialog',
    templateUrl: './create-server-dialog.component.html',
    styleUrls: ['./create-server-dialog.component.scss'],
    standalone: true,
    imports: [
      NgIf, ReactiveFormsModule,
      MatFormField, MatLabel, MatHint, MatSuffix, MatInput, MatIcon, MatButton,
      BaseDialogComponent, ServerFileSelectComponent
    ]
})
export class CreateServerDialogComponent implements OnInit {
  configFiles: ServerFile[] = []
  saveGameFiles: ServerFile[] = []
  showPassword=false;
  showAdminPassword=false;
  createServerForm = this.fb.group({
    name: [''],
    port: [3979],
    adminPort: [3977],
    password: [''],
    adminPassword: [''],
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
        serverAdminPort: this.createServerForm.controls.adminPort.value!,
        adminPassword: this.createServerForm.controls.adminPassword.value!,
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

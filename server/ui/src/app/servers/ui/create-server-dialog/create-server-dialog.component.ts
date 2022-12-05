import {Component, OnInit} from '@angular/core';
import {ServerFile} from "../../../api/models/server-file";
import {OpenttdServerResourceService} from "../../../api/services/openttd-server-resource.service";
import {FormBuilder} from "@angular/forms";
import {Store} from "@ngrx/store";
import {ServerFileType} from "../../../api/models/server-file-type";
import {addServer, loadServerFiles} from '../../../shared/store/actions/app.actions';
import {selectFiles} from '../../../shared/store/selectors/app.selectors';

@Component({
  selector: 'app-create-server-dialog',
  templateUrl: './create-server-dialog.component.html',
  styleUrls: ['./create-server-dialog.component.scss']
})
export class CreateServerDialogComponent implements OnInit {
  configFiles: ServerFile[] = []
  saveGameFiles: ServerFile[] = []

  createServerForm = this.fb.group({
    name: [''],
    port: [3979],
    saveGame: <ServerFile>[{}],
    config:<ServerFile>[{}]
  });

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
        config: this.createServerForm.controls.config.value!,
        startSaveGame: this.createServerForm.controls.saveGame.value!,

      }
    }))
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
}

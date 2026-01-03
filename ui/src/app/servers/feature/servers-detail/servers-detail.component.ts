import {Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {loadServer, loadServerFiles, updateServer} from '../../../shared/store/actions/app.actions';
import {Subscription} from 'rxjs';
import {selectFiles, selectServer} from '../../../shared/store/selectors/app.selectors';
import {ServerFile} from '../../../api/models/server-file';
import {ServerFileType} from '../../../api/models/server-file-type';
import {ActivatedRoute} from '@angular/router';
import {RP_ID} from '../../../shared/model/constants';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {filter} from 'rxjs/operators';
import {clone} from '../../../shared/services/utils.service';
import {FormBuilder, Validators} from '@angular/forms';

@Component({
    selector: 'app-servers-detail',
    templateUrl: './servers-detail.component.html',
    styleUrls: ['./servers-detail.component.scss'],
    standalone: false
})
export class ServersDetailComponent implements OnInit, OnDestroy {
  serverForm;

  showPassword=false;
  showAdminPassword=false;
  server: OpenttdServer | undefined;
  openttdConfigs: ServerFile[] = [];
  openttdPrivateConfigs: ServerFile[] = [];
  openttdSecretConfigs: ServerFile[] = [];
  openttdSavegames: ServerFile[] = [];

  private sub = new Subscription();

  constructor(private store: Store<{}>, private fb: FormBuilder, private api: OpenttdServerResourceService, private activeRoute: ActivatedRoute) {
    this.serverForm = this.fb.group({
      port: ['', [Validators.required, Validators.min(1)]],
      adminPort: ['', [Validators.required, Validators.min(1)]],
      name: ['', [Validators.required]],
      password: [''],
      adminPassword: [''],
      autoSave: [true, [Validators.required]],
      autoPause: [true, [Validators.required]],
    });
  }

  ngOnInit(): void {


    this.sub.add(this.store.select(selectServer).pipe(filter(s => s != null)).subscribe(s => {
      this.store.dispatch(loadServerFiles({src: ServersDetailComponent.name}));
      this.server = clone(s!);
      this.serverForm.controls.name.patchValue(this.server.name || '')
      this.serverForm.controls.port.patchValue(`${this.server.port}` || '')
      this.serverForm.controls.adminPort.patchValue(`${this.server.serverAdminPort}` || '')
      this.serverForm.controls.adminPassword.patchValue(`${this.server.adminPassword}` || '')
      this.serverForm.controls.password.patchValue(`${this.server.password}` || '')
      this.serverForm.controls.autoSave.patchValue(this.server.autoSave!)
      this.serverForm.controls.autoPause.patchValue(this.server.autoPause!)
      this.sub.add(this.store.select(selectFiles).subscribe(files => {
        this.openttdConfigs = files.filter(f => f.type === ServerFileType.Config);
        this.openttdSecretConfigs = files.filter(f => f.type === ServerFileType.Config);
        this.openttdPrivateConfigs = files.filter(f => f.type === ServerFileType.Config);
        this.openttdSavegames = files.filter(f => f.type === ServerFileType.SaveGame);
      }));
    }));

    this.activeRoute.params.subscribe(p => {
      if (p[RP_ID]) {
        this.store.dispatch(loadServer({src: ServersDetailComponent.name, id: p[RP_ID]}));
      }
    })


  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  save() {
    if (this.server && this.serverForm.valid) {
      const update = {
        ...this.server,
        port: parseInt(this.serverForm.controls.port.value!),
        name: this.serverForm.controls.name.value!,
        password: this.serverForm.controls.password.value!,
        adminPassword: this.serverForm.controls.adminPassword.value!,
        serverAdminPort: parseInt(this.serverForm.controls.adminPort.value!),
        autoSave: this.serverForm.controls.autoSave.value!,
        autoPause: this.serverForm.controls.autoPause.value!,
      }
      this.store.dispatch(updateServer({src: ServersDetailComponent.name, id: update.id!, server: update}))
    }
  }

  saveGameSelected(file: ServerFile) {
    if (this.server) {
      this.server.saveGame = file;
    }
  }

  configSelected(file: ServerFile) {
    if (this.server) {
      this.server.openttdConfig = file;
    }
  }

  secretConfigSelected(file: ServerFile) {
    if (this.server) {
      this.server.openttdSecretsConfig = file;
    }
  }

  privateConfigSelected(file: ServerFile) {
    if (this.server) {
      this.server.openttdPrivateConfig = file;
    }
  }
}

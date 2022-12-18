import { Component, OnInit } from '@angular/core';
import {Store} from '@ngrx/store';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {FormBuilder, Validators} from '@angular/forms';
import {ApplicationService} from '../../../shared/services/application.service';

@Component({
  selector: 'app-servers-detail',
  templateUrl: './servers-detail.component.html',
  styleUrls: ['./servers-detail.component.scss']
})
export class ServersDetailComponent implements OnInit {

  adminFormGroup;

  basicSettingsFormGroup;

  constructor(private store: Store<{}>, private api: OpenttdServerResourceService, private fb: FormBuilder, private app: ApplicationService) {
    this.adminFormGroup = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required]],
      newPasswordRepeat: ['', [Validators.required]]
    }, {
      // validators: this.passwordsEqualValidator
    });

    this.basicSettingsFormGroup = this.fb.group({
      autoSaveMinutes: ['', [Validators.required, Validators.min(-1)]],
    });

  }

  ngOnInit(): void {
  }

  updateConfig() {

  }

  updatePassword() {

  }
}

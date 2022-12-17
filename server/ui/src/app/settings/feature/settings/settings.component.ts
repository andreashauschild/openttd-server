import {Component, OnInit} from '@angular/core';
import {FormBuilder, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {ApplicationService} from '../../../shared/services/application.service';
import {HttpErrorResponse} from '@angular/common/http';
import {patchServerConfig} from '../../../shared/store/actions/app.actions';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {


  adminFormGroup;

  basicSettingsFormGroup;

  constructor(private store: Store<{}>, private api: OpenttdServerResourceService, private fb: FormBuilder, private app: ApplicationService) {
    this.adminFormGroup = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required]],
      newPasswordRepeat: ['', [Validators.required]]
    }, {
      validators: this.passwordsEqualValidator
    });

    this.basicSettingsFormGroup = this.fb.group({
      autoSaveMinutes: ['', [Validators.required, Validators.min(-1)]],
    });

  }

  ngOnInit(): void {
  }

  updatePassword() {
    if (this.adminFormGroup.valid) {
      this.api.updateOpenttdServerConfig({body: {password: this.adminFormGroup.value.newPassword!, oldPassword: this.adminFormGroup.value.oldPassword!}}).subscribe(
        {
          next: (v) => {
            this.app.createInfoMessage("Password changed successfully!")
          },
          error: (e: HttpErrorResponse) => {
            this.app.handleError(e);
          }
        }
      );
    }
  }

  passwordsEqualValidator: ValidatorFn = (): ValidationErrors | null => {
    if (this.adminFormGroup && this.adminFormGroup.value.newPassword !== this.adminFormGroup.value.newPasswordRepeat) {
      return {'not-equal': true}
    }
    return null;
  };

  updateConfig() {
    if (this.basicSettingsFormGroup.valid) {
      this.store.dispatch(patchServerConfig({
        src: SettingsComponent.name,
        patch: {
          autoSaveMinutes: parseInt(this.basicSettingsFormGroup.value.autoSaveMinutes!)
        }
      }))
    }

  }
}

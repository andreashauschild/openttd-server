import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {ServerFile} from '../../../api/models/server-file';
import {Store} from '@ngrx/store';
import {patchServerConfig, patchServerConfigSuccess} from '../../../shared/store/actions/app.actions';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {ApplicationService} from '../../../shared/services/application.service';
import {HttpErrorResponse} from '@angular/common/http';
import {ServiceError} from '../../../api/models/service-error';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {


  adminFormGroup;

  constructor(private store: Store<{}>, private api: OpenttdServerResourceService, private fb: FormBuilder, private app: ApplicationService) {
    this.adminFormGroup = this.fb.group({
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required]],
      newPasswordRepeat: ['', [Validators.required]]
    }, {
      validators: this.passwordsEqualValidator
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
            if (e.error) {
              let error = (e.error as ServiceError);
              this.app.createErrorMessage(`Password changed failed! ${error.message}`)
            } else {
              this.app.createErrorMessage(`Password changed failed!`)
            }
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
}

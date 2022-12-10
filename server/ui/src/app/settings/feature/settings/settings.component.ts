import { Component, OnInit } from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {ServerFile} from '../../../api/models/server-file';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  adminFormGroup = this.fb.group({
    oldPassword: [''],
    newPassword: [''],
    newPasswordRepeat: ['']
  });


  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
  }

  updatePassword() {

  }
}

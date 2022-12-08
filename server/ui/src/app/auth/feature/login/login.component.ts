import { Component, OnInit } from '@angular/core';
import {AuthenticationService} from '../../../shared/services/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  username=""
  password=""

  constructor(private auth:AuthenticationService) { }

  ngOnInit(): void {
  }

  async login() {
    await this.auth.login(this.username, this.password);
  }


}

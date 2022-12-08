import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {AuthResourceService} from '../../api/services/auth-resource.service';
import {environment} from '../../../environments/environment';
import {firstValueFrom} from 'rxjs';
import {HEADER_OPENTTD_SERVER_SESSION_ID} from '../model/constants';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {


  constructor(private router: Router, private http: HttpClient) {

  }

  async login(username: string, password: string): Promise<void> {

    let httpResponse = await firstValueFrom(this.http.post<any>(`${environment.baseUrl}${AuthResourceService.ApiAuthLoginPostPath}`, null, {
      observe: 'response',
      headers: {Authorization: "Basic " + window.btoa(`${username}:${password}`)}
    }));
    const session = httpResponse.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)
    if (session && session.length > 0) {
      localStorage.setItem(HEADER_OPENTTD_SERVER_SESSION_ID, session)
      this.router.navigateByUrl("/")
    }
  }

  getBasicAuth() {
    // const authData = JSON.parse(sessionStorage.getItem('currentUser')).authData;
    // if (authData) {
    //   return "Basic " + authData;
    // }
    // return null;

  }

  logout() {
    if (localStorage.getItem(HEADER_OPENTTD_SERVER_SESSION_ID)) {
      const headers: any = {}
      headers[HEADER_OPENTTD_SERVER_SESSION_ID] = localStorage.getItem(HEADER_OPENTTD_SERVER_SESSION_ID);
      this.http.post<any>(`${environment.baseUrl}${AuthResourceService.ApiAuthLogoutPostPath}`, null, {
        headers
      }).subscribe(_ => {
        this.router.navigateByUrl("/login")
        localStorage.removeItem(HEADER_OPENTTD_SERVER_SESSION_ID)
      });
    } else {
      this.router.navigateByUrl("/login")
    }

  }
}

import {Injectable} from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpResponse } from "@angular/common/http";
import {AuthResourceService} from '../../api/services/auth-resource.service';
import {environment} from '../../../environments/environment';
import {firstValueFrom} from 'rxjs';
import {HEADER_OPENTTD_SERVER_SESSION_ID, LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID} from '../model/constants';
import {clearSessionId, readSessionId, writeSessionId} from './session-storage';
import {Router} from '@angular/router';
import {BackendWebsocketService} from './backend-websocket.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {


  constructor(private router: Router, private http: HttpClient, private backendWebsocket: BackendWebsocketService) {
  }

  async login(username: string, password: string): Promise<void> {

    const httpResponse: HttpResponse<unknown> = await firstValueFrom(this.http.post<unknown>(`${environment.baseUrl}${AuthResourceService.ApiAuthLoginPostPath}`, null, {
      observe: 'response',
      headers: {Authorization: "Basic " + window.btoa(`${username}:${password}`)}
    }));
    // The legacy header is only read here, so a new UI keeps working against a backend of the previous release.
    const session = httpResponse.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID) ?? httpResponse.headers.get(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)
    if (session && session.length > 0) {
      writeSessionId(session)
      this.backendWebsocket.connect()
      await this.router.navigateByUrl("/")
    }
  }

  async isLoggedIn(): Promise<boolean> {
    const sessionId = readSessionId();
    if (sessionId) {
      const headers: any = {}
      headers[HEADER_OPENTTD_SERVER_SESSION_ID] = sessionId;
      let httpResponse: HttpResponse<any | HttpErrorResponse> = await firstValueFrom(this.http.post<any>(`${environment.baseUrl}${AuthResourceService.ApiAuthVerifyLoginPostPath}`, null, {
        observe: 'response',
        headers
      })).catch(e => {
        return e;
      })

      if (httpResponse?.status === 200) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }

  }

  logout() {
    this.backendWebsocket.disconnect();
    const sessionId = readSessionId();
    if (sessionId) {
      const headers: any = {}
      headers[HEADER_OPENTTD_SERVER_SESSION_ID] = sessionId;
      this.http.post<any>(`${environment.baseUrl}${AuthResourceService.ApiAuthLogoutPostPath}`, null, {
        headers
      }).subscribe(_ => {
        this.router.navigateByUrl("/login")
        clearSessionId()
      });
    } else {
      this.router.navigateByUrl("/login")
    }
  }
}

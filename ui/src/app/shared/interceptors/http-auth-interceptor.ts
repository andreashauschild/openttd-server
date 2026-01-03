import {Injectable} from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import {Observable} from 'rxjs';
import {HEADER_OPENTTD_SERVER_SESSION_ID} from '../model/constants';

@Injectable({
  providedIn: 'root'
})
export class HttpAuthInterceptor implements HttpInterceptor {
  intercept(httpRequest: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (localStorage.getItem(HEADER_OPENTTD_SERVER_SESSION_ID)) {
      const headers: any = {}
      headers[HEADER_OPENTTD_SERVER_SESSION_ID] = localStorage.getItem(HEADER_OPENTTD_SERVER_SESSION_ID);
      return next.handle(httpRequest.clone({setHeaders: headers}));
    } else {
      return next.handle(httpRequest);
    }

  }
}

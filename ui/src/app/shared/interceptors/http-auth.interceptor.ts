import {HttpInterceptorFn} from '@angular/common/http';
import {HEADER_OPENTTD_SERVER_SESSION_ID} from '../model/constants';

export const httpAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionId = localStorage.getItem(HEADER_OPENTTD_SERVER_SESSION_ID);
  if (sessionId) {
    const headers: Record<string, string> = {};
    headers[HEADER_OPENTTD_SERVER_SESSION_ID] = sessionId;
    return next(req.clone({setHeaders: headers}));
  }
  return next(req);
};

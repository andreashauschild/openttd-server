import {TestBed} from '@angular/core/testing';
import {HttpHeaders, provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {Router} from '@angular/router';

import {AuthenticationService} from './authentication.service';
import {BackendWebsocketService} from './backend-websocket.service';
import {
  HEADER_OPENTTD_SERVER_SESSION_ID,
  LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID,
  SESSION_STORAGE_KEY
} from '../model/constants';

describe('AuthenticationService', () => {

  let service: AuthenticationService;
  let httpMock: HttpTestingController;
  let router: { navigateByUrl: jasmine.Spy };
  let backendWebsocket: { connect: jasmine.Spy; disconnect: jasmine.Spy };

  beforeEach(() => {
    localStorage.clear();
    router = {navigateByUrl: jasmine.createSpy('navigateByUrl').and.resolveTo(true)};
    backendWebsocket = {connect: jasmine.createSpy('connect'), disconnect: jasmine.createSpy('disconnect')};
    TestBed.configureTestingModule({
      providers: [
        AuthenticationService,
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: Router, useValue: router},
        {provide: BackendWebsocketService, useValue: backendWebsocket}
      ]
    });
    service = TestBed.inject(AuthenticationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should store the session id of the new response header and navigate to the dashboard', async () => {
    const login = service.login('admin', 'Password_1');

    const req = httpMock.expectOne(r => r.url.endsWith('/api/auth/login'));
    expect(req.request.headers.get('Authorization')).toBe('Basic ' + window.btoa('admin:Password_1'));
    req.flush(null, {status: 200, statusText: 'OK', headers: new HttpHeaders({[HEADER_OPENTTD_SERVER_SESSION_ID]: 'session-1'})});
    await login;

    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBe('session-1');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
    expect(backendWebsocket.connect).toHaveBeenCalled();
  });

  it('should accept the legacy response header of an old backend', async () => {
    const login = service.login('admin', 'Password_1');

    const req = httpMock.expectOne(r => r.url.endsWith('/api/auth/login'));
    req.flush(null, {status: 200, statusText: 'OK', headers: new HttpHeaders({[LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID]: 'legacy-session'})});
    await login;

    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBe('legacy-session');
  });

  it('should migrate a session stored under the legacy key on isLoggedIn', async () => {
    localStorage.setItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, 'legacy-session');

    const loggedIn = service.isLoggedIn();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/auth/verifyLogin'));
    expect(req.request.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)).toBe('legacy-session');
    expect(req.request.headers.get(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)).toBeNull();
    req.flush(null, {status: 200, statusText: 'OK'});

    expect(await loggedIn).toBeTrue();
    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBe('legacy-session');
    expect(localStorage.getItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)).toBeNull();
  });

  it('should not call the backend when no session is stored', async () => {
    expect(await service.isLoggedIn()).toBeFalse();
    httpMock.expectNone(r => r.url.endsWith('/api/auth/verifyLogin'));
  });

  it('should remove the session id on logout', () => {
    localStorage.setItem(SESSION_STORAGE_KEY, 'session-1');

    service.logout();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/auth/logout'));
    expect(req.request.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)).toBe('session-1');
    req.flush(null, {status: 200, statusText: 'OK'});

    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBeNull();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    expect(backendWebsocket.disconnect).toHaveBeenCalled();
  });
});

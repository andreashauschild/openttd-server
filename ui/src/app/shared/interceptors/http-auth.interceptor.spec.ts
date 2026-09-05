import {TestBed} from '@angular/core/testing';
import {HttpClient, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';

import {httpAuthInterceptor} from './http-auth.interceptor';
import {
  HEADER_OPENTTD_SERVER_SESSION_ID,
  LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID,
  SESSION_STORAGE_KEY
} from '../model/constants';

describe('httpAuthInterceptor', () => {

  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([httpAuthInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should send only the new session header when a session is stored', () => {
    localStorage.setItem(SESSION_STORAGE_KEY, 'session-1');

    http.get('/api/openttd-server/server').subscribe();

    const req = httpMock.expectOne('/api/openttd-server/server');
    expect(req.request.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)).toBe('session-1');
    expect(req.request.headers.get(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)).toBeNull();
    req.flush({});
  });

  it('should send no session header when nothing is stored', () => {
    http.get('/api/openttd-server/server').subscribe();

    const req = httpMock.expectOne('/api/openttd-server/server');
    expect(req.request.headers.has(HEADER_OPENTTD_SERVER_SESSION_ID)).toBeFalse();
    expect(req.request.headers.has(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)).toBeFalse();
    req.flush({});
  });

  it('should migrate a session stored under the legacy key', () => {
    localStorage.setItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID, 'legacy-session');

    http.get('/api/openttd-server/server').subscribe();

    const req = httpMock.expectOne('/api/openttd-server/server');
    expect(req.request.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)).toBe('legacy-session');
    expect(localStorage.getItem(SESSION_STORAGE_KEY)).toBe('legacy-session');
    expect(localStorage.getItem(LEGACY_HEADER_OPENTTD_SERVER_SESSION_ID)).toBeNull();
    req.flush({});
  });
});

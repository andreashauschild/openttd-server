import {TestBed} from '@angular/core/testing';
import {HttpHeaders, provideHttpClient, withInterceptors} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';

import {DownloadService, FILE_SAVER, FileSaver} from './download.service';
import {httpAuthInterceptor} from '../interceptors/http-auth.interceptor';
import {HEADER_OPENTTD_SERVER_SESSION_ID, SESSION_STORAGE_KEY} from '../model/constants';

describe('DownloadService', () => {

  let service: DownloadService;
  let httpMock: HttpTestingController;
  let saver: jasmine.Spy<FileSaver>;

  beforeEach(() => {
    localStorage.clear();
    saver = jasmine.createSpy<FileSaver>('saveData');
    TestBed.configureTestingModule({
      providers: [
        DownloadService,
        provideHttpClient(withInterceptors([httpAuthInterceptor])),
        provideHttpClientTesting(),
        {provide: FILE_SAVER, useValue: saver}
      ]
    });
    service = TestBed.inject(DownloadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should download a file as a blob and save it under the name of the Content-Disposition header', () => {
    service.downloadFile('save/my game.sav').subscribe();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('fileName')).toBe('save/my game.sav');
    expect(req.request.urlWithParams).toContain('my%20game.sav');
    expect(req.request.responseType).toBe('blob');

    const blob = new Blob(['a save game']);
    req.flush(blob, {headers: new HttpHeaders({'Content-Disposition': 'attachment; filename="my game.sav"'})});

    expect(saver).toHaveBeenCalledWith(blob, 'my game.sav');
  });

  it('should send the session header on a download', () => {
    localStorage.setItem(SESSION_STORAGE_KEY, 'session-1');

    service.downloadFile('save/autosave.sav').subscribe();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download'));
    expect(req.request.headers.get(HEADER_OPENTTD_SERVER_SESSION_ID)).toBe('session-1');
    req.flush(new Blob(['a save game']));
  });

  it('should download a directory as a zip', () => {
    service.downloadDirectoryZip('save/uploads').subscribe();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download-zip'));
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('dir')).toBe('save/uploads');
    expect(req.request.responseType).toBe('blob');

    const blob = new Blob(['a zip']);
    req.flush(blob, {headers: new HttpHeaders({'Content-Disposition': 'attachment; filename="uploads.zip"'})});

    expect(saver).toHaveBeenCalledWith(blob, 'uploads.zip');
  });

  it('should post the selected file names to download them as a zip', () => {
    service.downloadSelectedZip('save', ['a.sav', 'b.sav']).subscribe();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download-zip'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({directoryPath: 'save', fileNames: ['a.sav', 'b.sav']});
    expect(req.request.responseType).toBe('blob');

    const blob = new Blob(['a zip']);
    req.flush(blob, {headers: new HttpHeaders({'Content-Disposition': 'attachment; filename="selection.zip"'})});

    expect(saver).toHaveBeenCalledWith(blob, 'selection.zip');
  });

  it('should fall back to the name of the requested path when the response has no Content-Disposition header', () => {
    service.downloadFile('save/autosave/autosave1.sav').subscribe();

    const fileRequest = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download'));
    fileRequest.flush(new Blob(['a save game']));
    expect(saver).toHaveBeenCalledWith(jasmine.any(Blob), 'autosave1.sav');

    service.downloadDirectoryZip('save/autosave').subscribe();

    const dirRequest = httpMock.expectOne(r => r.method === 'GET' && r.url.endsWith('/api/openttd-server/explorer/download-zip'));
    dirRequest.flush(new Blob(['a zip']));
    expect(saver).toHaveBeenCalledWith(jasmine.any(Blob), 'autosave.zip');

    service.downloadSelectedZip('save', ['a.sav']).subscribe();

    const selectionRequest = httpMock.expectOne(r => r.method === 'POST' && r.url.endsWith('/api/openttd-server/explorer/download-zip'));
    selectionRequest.flush(new Blob(['a zip']));
    expect(saver).toHaveBeenCalledWith(jasmine.any(Blob), 'download.zip');
  });

  it('should decode the file name of a RFC 5987 encoded Content-Disposition header', () => {
    service.downloadFile('save/spiel.sav').subscribe();

    const req = httpMock.expectOne(r => r.url.endsWith('/api/openttd-server/explorer/download'));
    req.flush(new Blob(['a save game']), {
      headers: new HttpHeaders({'Content-Disposition': "attachment; filename*=UTF-8''gro%C3%9Fes%20spiel.sav"})
    });

    expect(saver).toHaveBeenCalledWith(jasmine.any(Blob), 'großes spiel.sav');
  });
});

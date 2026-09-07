import {HttpClient, HttpResponse} from '@angular/common/http';
import {inject, Injectable, InjectionToken} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

import {FileExplorerResourceService} from '@api/services/file-explorer-resource.service';
import {MultiFileDownloadRequest} from '@api/models/multi-file-download-request';
import {environment} from '@env/environment';
import {saveData} from '@shared/services/utils.service';

export type FileSaver = (data: Blob, fileName: string) => void;

/**
 * Indirection over {@link saveData}, so tests can replace the DOM download with a spy.
 */
export const FILE_SAVER = new InjectionToken<FileSaver>('FILE_SAVER', {
  providedIn: 'root',
  factory: () => saveData
});

const FILENAME_STAR_PATTERN = /filename\*=\s*UTF-8''([^;]+)/i;
const FILENAME_PATTERN = /filename\s*=\s*"([^"]*)"|filename\s*=\s*([^;]+)/i;

const lastPathSegment = (path: string): string => path.split('/').filter(segment => !!segment).pop() || '';

const decode = (value: string): string => {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
};

/**
 * Reads the file name of the `Content-Disposition` header (`attachment; filename="..."`). The header is not readable
 * on a cross origin response unless the backend exposes it, so a fall back name is always required.
 */
const fileNameOf = (response: HttpResponse<Blob>, fallback: string): string => {
  const disposition = response.headers.get('Content-Disposition');
  if (disposition) {
    const starMatch = FILENAME_STAR_PATTERN.exec(disposition);
    if (starMatch && starMatch[1].trim()) {
      return decode(starMatch[1].trim());
    }
    const match = FILENAME_PATTERN.exec(disposition);
    const fileName = (match ? (match[1] ?? match[2]) : '').trim();
    if (fileName) {
      return fileName;
    }
  }
  return fallback;
};

/**
 * Downloads of the file explorer. The requests go through {@link HttpClient} on purpose: only then the session id
 * interceptor adds the authentication header, a plain `window.open` or `fetch` is answered with HTTP 403.
 */
@Injectable({
  providedIn: 'root'
})
export class DownloadService {

  private http = inject(HttpClient);
  private saver = inject(FILE_SAVER);

  downloadFile(relativePath: string): Observable<void> {
    const request = this.http.get(environment.baseUrl + FileExplorerResourceService.DownloadFilePath, {
      params: {fileName: relativePath},
      responseType: 'blob',
      observe: 'response'
    });
    return this.save(request, lastPathSegment(relativePath) || 'download');
  }

  downloadDirectoryZip(relativeDirPath: string): Observable<void> {
    const request = this.http.get(environment.baseUrl + FileExplorerResourceService.DownloadDirectoryZipPath, {
      params: {dir: relativeDirPath},
      responseType: 'blob',
      observe: 'response'
    });
    const directoryName = lastPathSegment(relativeDirPath);
    return this.save(request, directoryName ? `${directoryName}.zip` : 'download.zip');
  }

  downloadSelectedZip(directoryPath: string, fileNames: string[]): Observable<void> {
    const body: MultiFileDownloadRequest = {directoryPath, fileNames};
    const request = this.http.post(environment.baseUrl + FileExplorerResourceService.DownloadSelectedZipPath, body, {
      responseType: 'blob',
      observe: 'response'
    });
    return this.save(request, 'download.zip');
  }

  private save(request: Observable<HttpResponse<Blob>>, fallbackFileName: string): Observable<void> {
    return request.pipe(
      map(response => {
        this.saver(response.body ?? new Blob(), fileNameOf(response, fallbackFileName));
      })
    );
  }
}

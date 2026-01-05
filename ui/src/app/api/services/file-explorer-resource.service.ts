/* tslint:disable */
/* eslint-disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpContext } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { RequestBuilder } from '../request-builder';
import { Observable } from 'rxjs';
import { map, filter } from 'rxjs/operators';

import { ExplorerData } from '../models/explorer-data';
import { FileOperationRequest } from '../models/file-operation-request';
import { MultiFileDownloadRequest } from '../models/multi-file-download-request';

@Injectable({
  providedIn: 'root',
})
export class FileExplorerResourceService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation copyFile
   */
  static readonly CopyFilePath = '/api/openttd-server/explorer/copy';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `copyFile()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  copyFile$Response(params?: {
    context?: HttpContext
    body?: FileOperationRequest
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.CopyFilePath, 'post');
    if (params) {
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `copyFile$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  copyFile(params?: {
    context?: HttpContext
    body?: FileOperationRequest
  }
): Observable<ExplorerData> {

    return this.copyFile$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

  /**
   * Path part for operation explorerList
   */
  static readonly ExplorerListPath = '/api/openttd-server/explorer/data';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `explorerList()` instead.
   *
   * This method doesn't expect any request body.
   */
  explorerList$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.ExplorerListPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `explorerList$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  explorerList(params?: {
    context?: HttpContext
  }
): Observable<ExplorerData> {

    return this.explorerList$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

  /**
   * Path part for operation createDirectory
   */
  static readonly CreateDirectoryPath = '/api/openttd-server/explorer/directory';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `createDirectory()` instead.
   *
   * This method doesn't expect any request body.
   */
  createDirectory$Response(params?: {
    dir?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.CreateDirectoryPath, 'post');
    if (params) {
      rb.query('dir', params.dir, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `createDirectory$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  createDirectory(params?: {
    dir?: string;
    context?: HttpContext
  }
): Observable<ExplorerData> {

    return this.createDirectory$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

  /**
   * Path part for operation downloadFile
   */
  static readonly DownloadFilePath = '/api/openttd-server/explorer/download';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `downloadFile()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadFile$Response(params?: {
    downloadName?: string;
    fileName?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<{
}>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.DownloadFilePath, 'get');
    if (params) {
      rb.query('downloadName', params.downloadName, {});
      rb.query('fileName', params.fileName, {});
    }

    return this.http.request(rb.build({
      responseType: 'blob',
      accept: 'application/octet-stream',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<{
        }>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `downloadFile$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadFile(params?: {
    downloadName?: string;
    fileName?: string;
    context?: HttpContext
  }
): Observable<{
}> {

    return this.downloadFile$Response(params).pipe(
      map((r: StrictHttpResponse<{
}>) => r.body as {
})
    );
  }

  /**
   * Path part for operation downloadDirectoryZip
   */
  static readonly DownloadDirectoryZipPath = '/api/openttd-server/explorer/download-zip';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `downloadDirectoryZip()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadDirectoryZip$Response(params?: {
    dir?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.DownloadDirectoryZipPath, 'get');
    if (params) {
      rb.query('dir', params.dir, {});
    }

    return this.http.request(rb.build({
      responseType: 'text',
      accept: '*/*',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `downloadDirectoryZip$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadDirectoryZip(params?: {
    dir?: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.downloadDirectoryZip$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation downloadSelectedZip
   */
  static readonly DownloadSelectedZipPath = '/api/openttd-server/explorer/download-zip';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `downloadSelectedZip()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  downloadSelectedZip$Response(params?: {
    context?: HttpContext
    body?: MultiFileDownloadRequest
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.DownloadSelectedZipPath, 'post');
    if (params) {
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'text',
      accept: '*/*',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `downloadSelectedZip$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  downloadSelectedZip(params?: {
    context?: HttpContext
    body?: MultiFileDownloadRequest
  }
): Observable<void> {

    return this.downloadSelectedZip$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation deleteFile
   */
  static readonly DeleteFilePath = '/api/openttd-server/explorer/file';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `deleteFile()` instead.
   *
   * This method doesn't expect any request body.
   */
  deleteFile$Response(params?: {
    file?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.DeleteFilePath, 'delete');
    if (params) {
      rb.query('file', params.file, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `deleteFile$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  deleteFile(params?: {
    file?: string;
    context?: HttpContext
  }
): Observable<ExplorerData> {

    return this.deleteFile$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

  /**
   * Path part for operation moveFile
   */
  static readonly MoveFilePath = '/api/openttd-server/explorer/move';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `moveFile()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  moveFile$Response(params?: {
    context?: HttpContext
    body?: FileOperationRequest
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.MoveFilePath, 'post');
    if (params) {
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `moveFile$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  moveFile(params?: {
    context?: HttpContext
    body?: FileOperationRequest
  }
): Observable<ExplorerData> {

    return this.moveFile$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

  /**
   * Path part for operation renameFile
   */
  static readonly RenameFilePath = '/api/openttd-server/explorer/rename';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `renameFile()` instead.
   *
   * This method doesn't expect any request body.
   */
  renameFile$Response(params?: {
    file?: string;
    newName?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ExplorerData>> {

    const rb = new RequestBuilder(this.rootUrl, FileExplorerResourceService.RenameFilePath, 'post');
    if (params) {
      rb.query('file', params.file, {});
      rb.query('newName', params.newName, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExplorerData>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `renameFile$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  renameFile(params?: {
    file?: string;
    newName?: string;
    context?: HttpContext
  }
): Observable<ExplorerData> {

    return this.renameFile$Response(params).pipe(
      map((r: StrictHttpResponse<ExplorerData>) => r.body as ExplorerData)
    );
  }

}

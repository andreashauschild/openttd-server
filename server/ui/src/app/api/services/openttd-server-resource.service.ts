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

import { Command } from '../models/command';
import { OpenttdProcess } from '../models/openttd-process';
import { OpenttdServer } from '../models/openttd-server';
import { OpenttdServerConfigGet } from '../models/openttd-server-config-get';
import { OpenttdServerConfigUpdate } from '../models/openttd-server-config-update';
import { ServerFile } from '../models/server-file';

@Injectable({
  providedIn: 'root',
})
export class OpenttdServerResourceService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation getOpenttdServerConfig
   */
  static readonly GetOpenttdServerConfigPath = '/api/openttd-server/config';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getOpenttdServerConfig()` instead.
   *
   * This method doesn't expect any request body.
   */
  getOpenttdServerConfig$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<OpenttdServerConfigGet>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.GetOpenttdServerConfigPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<OpenttdServerConfigGet>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `getOpenttdServerConfig$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getOpenttdServerConfig(params?: {
    context?: HttpContext
  }
): Observable<OpenttdServerConfigGet> {

    return this.getOpenttdServerConfig$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServerConfigGet>) => r.body as OpenttdServerConfigGet)
    );
  }

  /**
   * Path part for operation updateOpenttdServerConfig
   */
  static readonly UpdateOpenttdServerConfigPath = '/api/openttd-server/config';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `updateOpenttdServerConfig()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  updateOpenttdServerConfig$Response(params?: {
    context?: HttpContext
    body?: OpenttdServerConfigUpdate
  }
): Observable<StrictHttpResponse<OpenttdServerConfigGet>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.UpdateOpenttdServerConfigPath, 'patch');
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
        return r as StrictHttpResponse<OpenttdServerConfigGet>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `updateOpenttdServerConfig$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  updateOpenttdServerConfig(params?: {
    context?: HttpContext
    body?: OpenttdServerConfigUpdate
  }
): Observable<OpenttdServerConfigGet> {

    return this.updateOpenttdServerConfig$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServerConfigGet>) => r.body as OpenttdServerConfigGet)
    );
  }

  /**
   * Path part for operation downloadOpenttdConfig
   */
  static readonly DownloadOpenttdConfigPath = '/api/openttd-server/download/openttd-config';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `downloadOpenttdConfig()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadOpenttdConfig$Response(params?: {
    fileName?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<{
}>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.DownloadOpenttdConfigPath, 'get');
    if (params) {
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
   * To access the full response (for headers, for example), `downloadOpenttdConfig$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadOpenttdConfig(params?: {
    fileName?: string;
    context?: HttpContext
  }
): Observable<{
}> {

    return this.downloadOpenttdConfig$Response(params).pipe(
      map((r: StrictHttpResponse<{
}>) => r.body as {
})
    );
  }

  /**
   * Path part for operation downloadSaveGame
   */
  static readonly DownloadSaveGamePath = '/api/openttd-server/download/save-game';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `downloadSaveGame()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadSaveGame$Response(params?: {
    downloadName?: string;
    fileName?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<{
}>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.DownloadSaveGamePath, 'get');
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
   * To access the full response (for headers, for example), `downloadSaveGame$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  downloadSaveGame(params?: {
    downloadName?: string;
    fileName?: string;
    context?: HttpContext
  }
): Observable<{
}> {

    return this.downloadSaveGame$Response(params).pipe(
      map((r: StrictHttpResponse<{
}>) => r.body as {
})
    );
  }

  /**
   * Path part for operation getServerFiles
   */
  static readonly GetServerFilesPath = '/api/openttd-server/files';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getServerFiles()` instead.
   *
   * This method doesn't expect any request body.
   */
  getServerFiles$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<Array<ServerFile>>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.GetServerFilesPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Array<ServerFile>>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `getServerFiles$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getServerFiles(params?: {
    context?: HttpContext
  }
): Observable<Array<ServerFile>> {

    return this.getServerFiles$Response(params).pipe(
      map((r: StrictHttpResponse<Array<ServerFile>>) => r.body as Array<ServerFile>)
    );
  }

  /**
   * Path part for operation processes
   */
  static readonly ProcessesPath = '/api/openttd-server/processes';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `processes()` instead.
   *
   * This method doesn't expect any request body.
   */
  processes$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<Array<OpenttdProcess>>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.ProcessesPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Array<OpenttdProcess>>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `processes$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  processes(params?: {
    context?: HttpContext
  }
): Observable<Array<OpenttdProcess>> {

    return this.processes$Response(params).pipe(
      map((r: StrictHttpResponse<Array<OpenttdProcess>>) => r.body as Array<OpenttdProcess>)
    );
  }

  /**
   * Path part for operation addServer
   */
  static readonly AddServerPath = '/api/openttd-server/server';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `addServer()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  addServer$Response(params?: {
    context?: HttpContext
    body?: OpenttdServer
  }
): Observable<StrictHttpResponse<OpenttdServer>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.AddServerPath, 'post');
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
        return r as StrictHttpResponse<OpenttdServer>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `addServer$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  addServer(params?: {
    context?: HttpContext
    body?: OpenttdServer
  }
): Observable<OpenttdServer> {

    return this.addServer$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServer>) => r.body as OpenttdServer)
    );
  }

  /**
   * Path part for operation getServer
   */
  static readonly GetServerPath = '/api/openttd-server/server/{id}';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getServer()` instead.
   *
   * This method doesn't expect any request body.
   */
  getServer$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<OpenttdServer>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.GetServerPath, 'get');
    if (params) {
      rb.path('id', params.id, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<OpenttdServer>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `getServer$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getServer(params: {
    id: string;
    context?: HttpContext
  }
): Observable<OpenttdServer> {

    return this.getServer$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServer>) => r.body as OpenttdServer)
    );
  }

  /**
   * Path part for operation updateServer
   */
  static readonly UpdateServerPath = '/api/openttd-server/server/{id}';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `updateServer()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  updateServer$Response(params: {
    id: string;
    context?: HttpContext
    body?: OpenttdServer
  }
): Observable<StrictHttpResponse<OpenttdServer>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.UpdateServerPath, 'put');
    if (params) {
      rb.path('id', params.id, {});
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<OpenttdServer>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `updateServer$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  updateServer(params: {
    id: string;
    context?: HttpContext
    body?: OpenttdServer
  }
): Observable<OpenttdServer> {

    return this.updateServer$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServer>) => r.body as OpenttdServer)
    );
  }

  /**
   * Path part for operation deleteServer
   */
  static readonly DeleteServerPath = '/api/openttd-server/server/{id}';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `deleteServer()` instead.
   *
   * This method doesn't expect any request body.
   */
  deleteServer$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.DeleteServerPath, 'delete');
    if (params) {
      rb.path('id', params.id, {});
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
   * To access the full response (for headers, for example), `deleteServer$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  deleteServer(params: {
    id: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.deleteServer$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation executeCommand
   */
  static readonly ExecuteCommandPath = '/api/openttd-server/server/{id}/command';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `executeCommand()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  executeCommand$Response(params: {
    id: string;
    context?: HttpContext
    body?: Command
  }
): Observable<StrictHttpResponse<Command>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.ExecuteCommandPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Command>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `executeCommand$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  executeCommand(params: {
    id: string;
    context?: HttpContext
    body?: Command
  }
): Observable<Command> {

    return this.executeCommand$Response(params).pipe(
      map((r: StrictHttpResponse<Command>) => r.body as Command)
    );
  }

  /**
   * Path part for operation dumpProcessData
   */
  static readonly DumpProcessDataPath = '/api/openttd-server/server/{id}/dump-process-data';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `dumpProcessData()` instead.
   *
   * This method doesn't expect any request body.
   */
  dumpProcessData$Response(params: {
    id: string;
    dir?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.DumpProcessDataPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
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
   * To access the full response (for headers, for example), `dumpProcessData$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  dumpProcessData(params: {
    id: string;
    dir?: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.dumpProcessData$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation save
   */
  static readonly SavePath = '/api/openttd-server/server/{id}/save';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `save()` instead.
   *
   * This method doesn't expect any request body.
   */
  save$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.SavePath, 'post');
    if (params) {
      rb.path('id', params.id, {});
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
   * To access the full response (for headers, for example), `save$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  save(params: {
    id: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.save$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation sendTerminalCommand
   */
  static readonly SendTerminalCommandPath = '/api/openttd-server/server/{id}/send-terminal-command';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `sendTerminalCommand()` instead.
   *
   * This method sends `text/plain` and handles request body of type `text/plain`.
   */
  sendTerminalCommand$Response(params: {
    id: string;
    context?: HttpContext
    body?: string
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.SendTerminalCommandPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
      rb.body(params.body, 'text/plain');
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
   * To access the full response (for headers, for example), `sendTerminalCommand$Response()` instead.
   *
   * This method sends `text/plain` and handles request body of type `text/plain`.
   */
  sendTerminalCommand(params: {
    id: string;
    context?: HttpContext
    body?: string
  }
): Observable<void> {

    return this.sendTerminalCommand$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation startServer
   */
  static readonly StartServerPath = '/api/openttd-server/server/{id}/start';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `startServer()` instead.
   *
   * This method doesn't expect any request body.
   */
  startServer$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<OpenttdServer>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.StartServerPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<OpenttdServer>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `startServer$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  startServer(params: {
    id: string;
    context?: HttpContext
  }
): Observable<OpenttdServer> {

    return this.startServer$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdServer>) => r.body as OpenttdServer)
    );
  }

  /**
   * Path part for operation stop
   */
  static readonly StopPath = '/api/openttd-server/server/{id}/stop';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `stop()` instead.
   *
   * This method doesn't expect any request body.
   */
  stop$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.StopPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
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
   * To access the full response (for headers, for example), `stop$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  stop(params: {
    id: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.stop$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation terminalOpenInUi
   */
  static readonly TerminalOpenInUiPath = '/api/openttd-server/server/{id}/terminal/ui-open';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `terminalOpenInUi()` instead.
   *
   * This method doesn't expect any request body.
   */
  terminalOpenInUi$Response(params: {
    id: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.TerminalOpenInUiPath, 'put');
    if (params) {
      rb.path('id', params.id, {});
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
   * To access the full response (for headers, for example), `terminalOpenInUi$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  terminalOpenInUi(params: {
    id: string;
    context?: HttpContext
  }
): Observable<void> {

    return this.terminalOpenInUi$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation start
   */
  static readonly StartPath = '/api/openttd-server/start-server';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `start()` instead.
   *
   * This method doesn't expect any request body.
   */
  start$Response(params?: {
    config?: string;
    name?: string;
    port?: number;
    savegame?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<OpenttdProcess>> {

    const rb = new RequestBuilder(this.rootUrl, OpenttdServerResourceService.StartPath, 'post');
    if (params) {
      rb.query('config', params.config, {});
      rb.query('name', params.name, {});
      rb.query('port', params.port, {});
      rb.query('savegame', params.savegame, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<OpenttdProcess>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `start$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  start(params?: {
    config?: string;
    name?: string;
    port?: number;
    savegame?: string;
    context?: HttpContext
  }
): Observable<OpenttdProcess> {

    return this.start$Response(params).pipe(
      map((r: StrictHttpResponse<OpenttdProcess>) => r.body as OpenttdProcess)
    );
  }

}

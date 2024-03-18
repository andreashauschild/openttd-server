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

}

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

import { ServerFileType } from '../models/server-file-type';

@Injectable({
  providedIn: 'root',
})
export class ChunkUploadResourceService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation apiChunkUploadPost
   */
  static readonly ApiChunkUploadPostPath = '/api/chunk-upload';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `apiChunkUploadPost()` instead.
   *
   * This method sends `application/octet-stream` and handles request body of type `application/octet-stream`.
   */
  apiChunkUploadPost$Response(params?: {
    fileName?: string;
    fileSize?: number;
    offset?: number;
    type?: ServerFileType;
    context?: HttpContext
    body?: Blob
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, ChunkUploadResourceService.ApiChunkUploadPostPath, 'post');
    if (params) {
      rb.query('fileName', params.fileName, {});
      rb.query('fileSize', params.fileSize, {});
      rb.query('offset', params.offset, {});
      rb.query('type', params.type, {});
      rb.body(params.body, 'application/octet-stream');
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
   * To access the full response (for headers, for example), `apiChunkUploadPost$Response()` instead.
   *
   * This method sends `application/octet-stream` and handles request body of type `application/octet-stream`.
   */
  apiChunkUploadPost(params?: {
    fileName?: string;
    fileSize?: number;
    offset?: number;
    type?: ServerFileType;
    context?: HttpContext
    body?: Blob
  }
): Observable<void> {

    return this.apiChunkUploadPost$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

}

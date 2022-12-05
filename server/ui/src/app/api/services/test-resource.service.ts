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

import { ExportModel } from '../models/export-model';

@Injectable({
  providedIn: 'root',
})
export class TestResourceService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation apiTestExportModelGet
   */
  static readonly ApiTestExportModelGetPath = '/api/test/exportModel';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `apiTestExportModelGet()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestExportModelGet$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ExportModel>> {

    const rb = new RequestBuilder(this.rootUrl, TestResourceService.ApiTestExportModelGetPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ExportModel>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `apiTestExportModelGet$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestExportModelGet(params?: {
    context?: HttpContext
  }
): Observable<ExportModel> {

    return this.apiTestExportModelGet$Response(params).pipe(
      map((r: StrictHttpResponse<ExportModel>) => r.body as ExportModel)
    );
  }

  /**
   * Path part for operation apiTestHelloGet
   */
  static readonly ApiTestHelloGetPath = '/api/test/hello';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `apiTestHelloGet()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestHelloGet$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<string>> {

    const rb = new RequestBuilder(this.rootUrl, TestResourceService.ApiTestHelloGetPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'text',
      accept: 'text/plain',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<string>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `apiTestHelloGet$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestHelloGet(params?: {
    context?: HttpContext
  }
): Observable<string> {

    return this.apiTestHelloGet$Response(params).pipe(
      map((r: StrictHttpResponse<string>) => r.body as string)
    );
  }

  /**
   * Path part for operation apiTestHello2Get
   */
  static readonly ApiTestHello2GetPath = '/api/test/hello2';

  /**
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `apiTestHello2Get()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestHello2Get$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<string>> {

    const rb = new RequestBuilder(this.rootUrl, TestResourceService.ApiTestHello2GetPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'text',
      accept: 'text/plain',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<string>;
      })
    );
  }

  /**
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `apiTestHello2Get$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  apiTestHello2Get(params?: {
    context?: HttpContext
  }
): Observable<string> {

    return this.apiTestHello2Get$Response(params).pipe(
      map((r: StrictHttpResponse<string>) => r.body as string)
    );
  }

}

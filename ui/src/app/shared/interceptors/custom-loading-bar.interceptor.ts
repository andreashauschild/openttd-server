import {LoadingBarService} from '@ngx-loading-bar/core';
import {Injectable} from '@angular/core';
import {HttpContextToken, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {finalize, tap} from 'rxjs/operators';
import {AuthResourceService} from "../../api/services/auth-resource.service";
import {OpenttdServerResourceService} from '../../api/services/openttd-server-resource.service';

export const NGX_LOADING_BAR_IGNORED = new HttpContextToken<boolean>(() => false);
declare const ngDevMode: boolean;

@Injectable()
export class CustomLoadingBarInterceptor implements HttpInterceptor {
  constructor(private loader: LoadingBarService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // https://github.com/angular/angular/issues/18155
    if (req.headers.has('ignoreLoadingBar')) {
      if (typeof ngDevMode === 'undefined' || ngDevMode) {
        console.warn(
          `Using http headers ('ignoreLoadingBar') to ignore loading bar is deprecated. Use HttpContext instead: 'context: new HttpContext().set(NGX_LOADING_BAR_IGNORED, true)'`,
        );
      }

      return next.handle(req.clone({headers: req.headers.delete('ignoreLoadingBar')}));
    }
    if (req.context.get(NGX_LOADING_BAR_IGNORED) === true) {
      return next.handle(req);
    }

    // Ignore loading bar if a specific url is requested
    if (
      // ignore on auth and terminal check
      req.url.endsWith(AuthResourceService.ApiAuthVerifyLoginPostPath)
      || req.url.endsWith(OpenttdServerResourceService.TerminalOpenInUiPath)
    ) {
      return next.handle(req);
    }


    let started = false;
    const ref = this.loader.useRef('http');
    return next.handle(req).pipe(
      tap(() => {
        if (!started) {
          ref.start();
          started = true;
        }
      }),
      finalize(() => started && ref.complete()),
    );
  }
}

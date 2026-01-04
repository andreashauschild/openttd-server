import {HttpContextToken, HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {LoadingBarService} from '@ngx-loading-bar/core';
import {finalize, tap} from 'rxjs/operators';
import {AuthResourceService} from '../../api/services/auth-resource.service';
import {OpenttdServerResourceService} from '../../api/services/openttd-server-resource.service';

export const NGX_LOADING_BAR_IGNORED = new HttpContextToken<boolean>(() => false);

export const loadingBarInterceptor: HttpInterceptorFn = (req, next) => {
  const loader = inject(LoadingBarService);

  // Check for deprecated header usage
  if (req.headers.has('ignoreLoadingBar')) {
    console.warn(
      `Using http headers ('ignoreLoadingBar') to ignore loading bar is deprecated. Use HttpContext instead: 'context: new HttpContext().set(NGX_LOADING_BAR_IGNORED, true)'`,
    );
    return next(req.clone({headers: req.headers.delete('ignoreLoadingBar')}));
  }

  if (req.context.get(NGX_LOADING_BAR_IGNORED) === true) {
    return next(req);
  }

  // Ignore loading bar for specific URLs
  if (
    req.url.endsWith(AuthResourceService.ApiAuthVerifyLoginPostPath) ||
    req.url.endsWith(OpenttdServerResourceService.TerminalOpenInUiPath)
  ) {
    return next(req);
  }

  let started = false;
  const ref = loader.useRef('http');

  return next(req).pipe(
    tap(() => {
      if (!started) {
        ref.start();
        started = true;
      }
    }),
    finalize(() => started && ref.complete()),
  );
};

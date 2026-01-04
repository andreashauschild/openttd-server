import {inject} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {AuthenticationService} from '../services/authentication.service';
import {interval} from 'rxjs';

let sessionCheckInitialized = false;

export const authGuard: CanActivateFn = async () => {
  const router = inject(Router);
  const authService = inject(AuthenticationService);

  // Initialize session check on first guard activation
  if (!sessionCheckInitialized) {
    sessionCheckInitialized = true;
    interval(30000).subscribe(async () => {
      const loggedIn = await authService.isLoggedIn();
      console.log(loggedIn);
      if (!loggedIn) {
        router.navigate(['/login']);
      }
    });
  }

  if (await authService.isLoggedIn()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

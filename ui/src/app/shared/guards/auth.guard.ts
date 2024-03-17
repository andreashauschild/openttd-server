import {Injectable} from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import {AuthenticationService} from "../services/authentication.service";
import {interval} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthGuard  {
  constructor(
    private router: Router,
    private authenticationService: AuthenticationService
  ) {
    interval(30000).subscribe(_ => {
      this.authenticationService.isLoggedIn().then(loggedIn => {
        console.log(loggedIn)
        if (!loggedIn) {
          this.router.navigate(['/login']);
        }
      })
    });
  }

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (await this.authenticationService.isLoggedIn()) {
      // logged in so return true
      return true;
    }
    // not logged in so redirect to login page with the return url
    this.router.navigate(['/login']);
    return false;
  }

}

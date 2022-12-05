import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {


  constructor(private http: HttpClient) {
    // this.currentUserSubject = new BehaviorSubject<LoginUser>(JSON.parse(sessionStorage.getItem('currentUser')) );
    // this.currentUser = this.currentUserSubject.asObservable();
  }

  // public get currentUserValue(): void {
  //   // return this.currentUserSubject.value;
  // }

  login(username: string, password: string) {

    // return this.http.post<any>(`${environment.apiServerRoot}/api/user/authenticate`, null, {
    //   headers: {Authorization: "Basic " + btoa(`${username}:${password}`)}
    // })
    //   .pipe(map(user => {
    //     // store user details and basic auth credentials in local storage to keep user logged in between page refreshes
    //     console.log(user)
    //     user.authData = window.btoa(username + ':' + password);
    //     sessionStorage.setItem('currentUser', JSON.stringify(user));
    //     this.currentUserSubject.next(user);
    //     return user;
    //   }));
  }

  getBasicAuth() {
    // const authData = JSON.parse(sessionStorage.getItem('currentUser')).authData;
    // if (authData) {
    //   return "Basic " + authData;
    // }
    // return null;

  }

  logout() {
    // // remove user from local storage to log user out
    // sessionStorage.removeItem('currentUser');
    // this.currentUserSubject.next(null);
  }
}

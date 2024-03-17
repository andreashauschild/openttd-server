import { Injectable } from '@angular/core';
import {IDataService, NAME_FUNCTION} from 'ngx-explorer';
import {Observable} from 'rxjs';

export interface MyDataType {
  name: string;
  path: string;
  createdOn: Date;
}

@Injectable({
  providedIn: 'root'
})
export class FileExplorerDataService  implements IDataService<MyDataType> {

  constructor() { }

  createDir(parent: MyDataType, name: string): Observable<MyDataType> {

    return Observable.create(null) ;
  }

  delete(target: MyDataType[]): Observable<MyDataType> {
    return Observable.create(null) ;
  }

  downloadFile(target: MyDataType): Observable<MyDataType> {
    return Observable.create(null) ;
  }

  getContent(target: MyDataType): Observable<{ files: MyDataType[]; dirs: MyDataType[] }> {
    return Observable.create(null) ;
  }

  rename(target: MyDataType, newName: string): Observable<MyDataType> {
    return Observable.create(null) ;
  }

  uploadFiles(parent: MyDataType, files: FileList): Observable<MyDataType> {
    return Observable.create(null) ;
  }
}

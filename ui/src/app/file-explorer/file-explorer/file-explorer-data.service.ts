import {Injectable} from '@angular/core';
import {Data, ExplorerService, IDataService} from 'ngx-explorer';
import {from, Observable, of, Subject} from 'rxjs';
import {Store} from '@ngrx/store';
import {loadExplorerData} from '../../shared/store/actions/app.actions';
import {selectExplorerData} from '../../shared/store/selectors/app.selectors';
import {filter, map} from 'rxjs/operators';

export interface MyDataType extends Data {
  id: number;
  name: string;
  path: string;
  content?: string;
}

@Injectable({
  providedIn: 'root'
})
export class FileExplorerDataService implements IDataService<MyDataType> {

  files: MyDataType[] = [];
  dirs: MyDataType[] = [];
  fileSeperator = '/';

  public currentPath:string|null = null;


  constructor(store: Store<{}>) {
    store.dispatch(loadExplorerData({src: FileExplorerDataService.name}))
    store.select(selectExplorerData).pipe(filter(d => d != null)).subscribe(d => {
      this.fileSeperator = d?.fileSeperator!
      for (let dir of d?.directories!) {
        this.dirs.push({
          id: parseInt(dir.id!),
          name: dir.name!,
          path: dir.absolutePath!
        })
        for (let file of dir.files!) {
          this.files.push({
            id: parseInt(file.id!),
            name: file.name!,
            path: file.absolutePath!
          })
        }
      }
    })
  }

  createDir(parent: MyDataType, name: string): Observable<MyDataType> {

    return Observable.create(null);
  }

  delete(target: MyDataType[]): Observable<MyDataType> {
    return Observable.create(null);
  }

  downloadFile(target: MyDataType): Observable<MyDataType> {
    return Observable.create(null);
  }

  getContent(data: MyDataType): Observable<{ files: MyDataType[]; dirs: MyDataType[] }> {
    const folderPath = data.path || 'C:\\App\\openttd-13.4-windows-win64';
    this.currentPath = folderPath;
    const dirs = this.dirs.filter((f) => {
      const paths = f.path.split(this.fileSeperator);
      paths.pop();
      const filteredPath = paths.join(this.fileSeperator);
      return filteredPath === folderPath;
    });

    const files = this.files.filter((f) => {
      const paths = f.path.split(this.fileSeperator);
      paths.pop();
      const filteredPath = paths.join(this.fileSeperator);
      return filteredPath === folderPath;
    });
    console.log(">>>>>>>>>>>>>>>>>>> currentPath",this.currentPath)
    return of({files, dirs});
  }

  rename(target: MyDataType, newName: string): Observable<MyDataType> {
    return Observable.create(null);
  }

  uploadFiles(parent: MyDataType, files: FileList): Observable<MyDataType> {
    return Observable.create(null);
  }
}

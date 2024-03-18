import {Injectable} from '@angular/core';
import {Data, IDataService} from 'ngx-explorer';
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

let MOCK_DIRS = [
  {id: 1, name: 'Music', path: 'music'},
  {id: 2, name: 'Movies', path: 'movies'},
  {id: 3, name: 'Books', path: 'books'},
  {id: 4, name: 'Games', path: 'games'},
  {id: 5, name: 'Rock', path: 'music/rock'},
  {id: 6, name: 'Jazz', path: 'music/jazz'},
  {id: 11, name: 'Very Long Name to display overflow', path: 'long'},

  {id: 7, name: 'Classical', path: 'music/classical'},
  {id: 15, name: 'Aerosmith', path: 'music/rock/aerosmith'},
  {id: 16, name: 'AC/DC', path: 'music/rock/acdc'},
  {id: 17, name: 'Led Zeppelin', path: 'music/rock/ledzeppelin'},
  {id: 18, name: 'The Beatles', path: 'music/rock/thebeatles'},
] as MyDataType[];

let MOCK_FILES = [
  {id: 1312, name: 'notes.txt', path: '', content: 'hi, this is an example'},
  {id: 1212, name: '2.txt', path: '', content: 'hi, this is an example'},
  {id: 28, name: 'Thriller.txt', path: 'music/rock/thebeatles/thriller', content: 'hi, this is an example'},
  {id: 29, name: 'Back in the U.S.S.R.txt', path: 'music/rock/thebeatles', content: 'hi, this is an example'},
  {id: 30, name: 'All You Need Is Love.txt', path: 'music/rock/thebeatles', content: 'hi, this is an example'},
  {id: 31, name: 'Hey Jude.txt', path: 'music/rock/ledzeppelin/heyjude', content: 'hi, this is an example'},
  {id: 32, name: 'Rock And Roll All Nite.txt', path: 'music/rock/ledzeppelin/rockandrollallnight', content: 'hi, this is an example'},
] as MyDataType[];

@Injectable({
  providedIn: 'root'
})
export class FileExplorerDataService implements IDataService<MyDataType> {

  files: MyDataType[] = [];
  dirs: MyDataType[] = [];
  fileSeperator='/';

  constructor(store: Store<{}>) {
    store.dispatch(loadExplorerData({src: FileExplorerDataService.name}))
    store.select(selectExplorerData).pipe(filter(d => d != null)).subscribe(d=>{

      this.fileSeperator = d?.fileSeperator!
      for(let dir of d?.directories!){
        this.dirs.push({
          id:parseInt(dir.id!),
          name:dir.name!,
          path:dir.absolutePath!
        })
        for(let file of dir.files!){
          this.files.push({
            id:parseInt(file.id!),
            name:file.name!,
            path:file.absolutePath!
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
    console.log(">>>>>>>>>>>>>>>>>>> getContent", data)
    const folderPath = data.path || 'C:\\App\\openttd-13.4-windows-win64';

    const dirs = this.dirs.filter((f) => {
      const paths = f.path.split(this.fileSeperator);
      console.log(">>>>>>>>>>>>>>>>>>> paths", paths)
      paths.pop();
      const filteredPath = paths.join(this.fileSeperator);
      console.log(">>>>>>>>>>>>>>>>>>> filteredPath", filteredPath,folderPath)
      return filteredPath === folderPath;
    });

    const files = this.files.filter((f) => {
      const paths = f.path.split(this.fileSeperator);
      paths.pop();
      const filteredPath = paths.join(this.fileSeperator);
      return filteredPath === folderPath;
    });

    return of({files, dirs});
  }

  rename(target: MyDataType, newName: string): Observable<MyDataType> {
    return Observable.create(null);
  }

  uploadFiles(parent: MyDataType, files: FileList): Observable<MyDataType> {
    return Observable.create(null);
  }
}

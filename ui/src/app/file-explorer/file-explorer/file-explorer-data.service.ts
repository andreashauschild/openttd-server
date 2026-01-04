import {Injectable} from '@angular/core';
import {Data, DataNode, ExplorerService, IDataService} from 'ngx-explorer';
import {EMPTY, Observable, of} from 'rxjs';
import {Store} from '@ngrx/store';
import {filter, map} from 'rxjs/operators';

import {createExplorerDir, deleteExplorerFile, loadExplorerData, renameExplorerFile} from '@store/actions/app.actions';
import {selectExplorerData} from '@store/selectors/app.selectors';

export interface MyDataType extends Data {
  id: number;
  name: string;
  path: string;
  relativePath: string;
  content?: string;
}

@Injectable({
  providedIn: 'root'
})
export class FileExplorerDataService implements IDataService<MyDataType> {

  files: MyDataType[] = [];
  dirs: MyDataType[] = [];
  fileSeperator = '/';
  root = '';

  public currentPath: string | null = null;


  constructor(private store: Store<{}>) {
    this.store.dispatch(loadExplorerData({src: FileExplorerDataService.name}))
    this.store.select(selectExplorerData).pipe(filter(d => d != null)).subscribe(d => {
      this.files = [];
      this.dirs = [];
      this.fileSeperator = d?.fileSeperator!
      this.root = d?.root!
      for (let dir of d?.directories!) {
        this.dirs.push({
          id: parseInt(dir.id!),
          name: dir.name!,
          path: dir.absolutePath!,
          relativePath: dir.relativePath!
        })
        for (let file of dir.files!) {
          this.files.push({
            id: parseInt(file.id!),
            name: file.name!,
            path: file.absolutePath!,
            relativePath: file.relativePath!
          })
        }
      }
    })
  }

  createDir(parent: MyDataType, name: string): Observable<MyDataType> {
    if (parent.relativePath) {
      this.store.dispatch(createExplorerDir({
        src: FileExplorerDataService.name,
        relativeDirPath: parent.relativePath + '/' + name
      }))
    } else {
      this.store.dispatch(createExplorerDir({
        src: FileExplorerDataService.name,
        relativeDirPath: name
      }))
    }
    console.log("createDir", parent, name)
    return EMPTY;
  }

  delete(targets: MyDataType[]): Observable<MyDataType> {
    for (let target of targets) {
      console.log(target)
      this.store.dispatch(deleteExplorerFile({
        src: FileExplorerDataService.name,
        relativePath: target.name,
      }))

    }
    return EMPTY;
  }

  downloadFile(target: MyDataType): Observable<MyDataType> {
    return EMPTY;
  }

  getContent(data: MyDataType): Observable<{ files: MyDataType[]; dirs: MyDataType[] }> {
    const folderPath = data.path || this.root;
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
    return of({files, dirs});
  }

  rename(target: MyDataType, newName: string): Observable<MyDataType> {
    console.log("---->", target, newName)
    if (target.relativePath) {
      this.store.dispatch(renameExplorerFile({
        src: FileExplorerDataService.name,
        relativePath: target.relativePath,
        newName
      }))
    } else {
      this.store.dispatch(renameExplorerFile({
        src: FileExplorerDataService.name,
        relativePath: "",
        newName
      }))
    }
    return EMPTY;
  }

  uploadFiles(parent: MyDataType, files: FileList): Observable<MyDataType> {
    // not used since we hackly replaced the orginal upload button with the custom chunk upload dialog
    return of(parent);
  }

  openTree(data: MyDataType): Observable<Array<DataNode<MyDataType>>> {
    // Return the tree structure for the given data node
    // This is used for tree navigation - returning empty array since tree navigation is not fully implemented
    return of([]);
  }
}

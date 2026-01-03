import {AfterViewInit, ApplicationRef, Component, ComponentFactoryResolver, ComponentRef, Injector, OnDestroy, Renderer2} from '@angular/core';
import {DataService, ExplorerComponent, ExplorerService, INode, NAME_FUNCTION} from 'ngx-explorer';
import {FileExplorerDataService} from './file-explorer-data.service';
import {Store} from '@ngrx/store';
import {selectExplorerData} from '../../shared/store/selectors/app.selectors';
import {filter, take} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {FileUploadDialogComponent} from '../../shared/ui/file-upload-dialog/file-upload-dialog.component';
import {ServerFileType} from '../../api/models/server-file-type';
import {MatDialog} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'app-file-explorer',
    imports: [ExplorerComponent, MatButton, MatIcon],
    templateUrl: './file-explorer.component.html',
    styleUrl: './file-explorer.component.scss'
})
export class FileExplorerComponent implements AfterViewInit, OnDestroy{

  private sub = new Subscription();
  private componentRef: ComponentRef<any> | null = null;

  constructor(private store:Store<{}>,private explorerService: ExplorerService,private data: FileExplorerDataService,public dialog: MatDialog, private renderer: Renderer2, private injector: Injector, private appRef: ApplicationRef, private componentFactoryResolver: ComponentFactoryResolver) {

  }


  uploadFiles() {
    console.log(">>>>>>>>>>>>>>>>>",this.data)
    const dialogRef = this.dialog.open(FileUploadDialogComponent, {minWidth: "800px"});
    dialogRef.componentInstance.dialogRef = dialogRef;
    dialogRef.componentInstance.fileType = ServerFileType.OpenttdRoot;
    dialogRef.componentInstance.targetDir = this.data.currentPath!;
    dialogRef.componentInstance.dialogTitle = "UPLOAD OPENTTD CONFIGS";
    dialogRef.componentInstance.subTitle = "Info: Don't upload files where the filename contains single/double quotes: ' or \" . This will cause problems!";
    dialogRef.afterClosed().subscribe(result => {
      console.log(`Dialog result: ${result}`);
    });
  }

  ngAfterViewInit(): void {
    this.sub.add(this.store.select(selectExplorerData).pipe(filter(d=>d!=null)).subscribe(d=>this.explorerService.refresh()));

    // Remove Upload Button
    document.querySelector(".nxe-menu-bar-left .nxe-upload")!.parentElement!.remove();
    const targetElement = document.querySelector(".nxe-menu-bar-left");
    targetElement!.appendChild(document.getElementById("upload-button-explorer")!);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

}

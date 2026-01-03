import {AfterViewInit, Component, ElementRef, Input, ViewChild} from '@angular/core';
import {
  formatBytesToHumanReadable,
  ChunkedUploadConfig,
  AfterChunkSend,
  BeforeChunkSend,
  ChunkedUpload,
  ChunkedUploadFile,
  JustUploadService,
  RequestParams,
  updateFileList,
  UploadState
} from '@andreashauschild/just-upload';
import {environment} from 'src/environments/environment';
import {ServerFileType} from '../../../api/models/server-file-type';
import {MatDialogRef} from '@angular/material/dialog';


@Component({
    selector: 'app-file-upload-dialog',
    templateUrl: './file-upload-dialog.component.html',
    styleUrls: ['./file-upload-dialog.component.scss'],
    standalone: false
})
export class FileUploadDialogComponent implements AfterViewInit {

  @Input()
  dialogTitle= "File Upload";

  @Input()
  subTitle= "";

  @Input()
  fileType: ServerFileType | undefined;

  @Input()
  targetDir: string | undefined;

  @ViewChild("fileUpload", {static: false})
  fileUpload: ElementRef | undefined;

  files: ChunkedUploadFile[] = []

  STATES = UploadState;

  limitHumanReadable: string | undefined;

  chunkedUpload?: ChunkedUpload;

  config: ChunkedUploadConfig | undefined;

  public dialogRef: MatDialogRef<FileUploadDialogComponent, boolean> | null = null;

  constructor(private uploadService: JustUploadService) {

  }

  ngAfterViewInit(): void {
    setTimeout(()=>this.init(),1);



  }

  private init(): void {
    var fileType = this.fileType;
    var targetDir = this.targetDir || '';
    this.config = {
      url: `${environment.baseUrl}/api/chunk-upload`,
      method: "POST",
      multi: true,
      uploadImmediately: false,
      maxFileSize: 100 * 1024 * 1024, // 100MB
      chunkSize: 1024 * 1024,

      beforeChunkSendHook(before: BeforeChunkSend) {
        const chunk = {...before.chunk}

        // Add query parameter to the post request
        const params: RequestParams = {
          query: {
            targetDir,
            type: fileType!.toString(),
            offset: chunk.chunk.offset.toString(),
            fileSize: chunk.uploadFile.size.toString(),
            fileName: chunk.uploadFile.name
          },
          // Add header to the post request, e.g. for Authorization
          header: {...before.requestParams.header}
        }


        return params;
      },
      afterChunkSendHook(after: AfterChunkSend) {
        if (after?.response?.status == 201) {
          // set finished if server response with 201
          after.chunk.uploadFile.state = UploadState.UPLOAD_SUCCESS;
        }
      }
    };
    this.limitHumanReadable = formatBytesToHumanReadable(this.config.maxFileSize!);



    this.chunkedUpload = this.uploadService.createChunkedUpload(this.fileUpload!, this.config!);
    this.chunkedUpload.onChunkProcessed().subscribe(updatedFile => {

      this.files = updateFileList(this.files, updatedFile);
    });

    this.chunkedUpload.onFiledAdded().subscribe(fi => {
      this.files.push(fi);
    });

  }

  remove(file: ChunkedUploadFile) {
    this.files = this.files.filter(f => f.fileId !== file.fileId);
  }

  uploadAll() {
    this.files.forEach(f => this.chunkedUpload?.uploadFile(f));
  }

  openFileDialog() {
    document.getElementById('uploadInput')!.click();
  }
}

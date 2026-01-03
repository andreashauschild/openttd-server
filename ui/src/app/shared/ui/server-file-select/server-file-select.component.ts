import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Observable, tap} from 'rxjs';
import {filter, map, startWith} from 'rxjs/operators';
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {ServerFile} from '../../../api/models/server-file';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {saveData} from '../../services/utils.service';
import {ServerFileType} from '../../../api/models/server-file-type';


@Component({
    selector: 'app-server-file-select',
    templateUrl: './server-file-select.component.html',
    styleUrls: ['./server-file-select.component.scss'],
    standalone: false
})
export class ServerFileSelectComponent implements OnInit, OnChanges {


  myControl = new FormControl<ServerFile | undefined>(undefined);

  @Output()
  selectedFileEvent = new EventEmitter<ServerFile>();

  @Output()
  selectedFileDownloadEvent = new EventEmitter<ServerFile>();

  @Input()
  files: ServerFile[] = [];

  @Input()
  label: string = '';

  @Input()
  hintStart: string = '';

  @Input()
  hintEnd: string = '';

  filteredFiles: Observable<ServerFile[]> | undefined;

  @Input()
  selectedFile: ServerFile | undefined;

  constructor(private openttdService: OpenttdServerResourceService) {

  }

  ngOnInit() {
    this.selectSelectedFile();
  }


  ngOnChanges(changes: SimpleChanges): void {

    this.filteredFiles = this.myControl.valueChanges.pipe(
      tap(v => console.log("FILTER", v)),
      // selected value can be a file, but a input is always a string. if its a file with name property it is filtered out
      filter(value => !value?.hasOwnProperty('name')),
      startWith(''),
      map(value => this._filter(value+'')),
    );

    this.selectSelectedFile();
  }

  select($event: MatAutocompleteSelectedEvent) {
    this.selectedFile = $event.option.value;
    this.selectedFileEvent.emit($event.option.value);
  }

  fileToString(file: any) {
    if (file) {

      return file.name;
    }
    return '';
  }

  private _filter(fileName: string): ServerFile[] {

    const filterValue = fileName.toLowerCase();

    return this.files.filter(files => files.name!.toLowerCase().includes(filterValue) || files.ownerName?.toLowerCase().includes(filterValue));
  }

  private selectSelectedFile() {
    if (this.selectedFile) {
      this.myControl.setValue(this.selectedFile);
    }
  }

  download() {
    if (this.selectedFile) {
      switch (this.selectedFile.type) {
        case ServerFileType.Config: {
          this.openttdService.downloadOpenttdConfig({fileName: this.selectedFile?.name}).subscribe(f => {
            saveData(f, this.selectedFile?.name!);
          })
          break
        }
        case  ServerFileType.SaveGame: {
          this.openttdService.downloadSaveGame({fileName: this.selectedFile?.name}).subscribe(f => {
            saveData(f, this.selectedFile?.name!);
          })
          break;
        }
      }
    }
  }


}

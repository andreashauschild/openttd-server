import {AfterViewInit, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Observable} from 'rxjs';
import {filter, map, startWith} from 'rxjs/operators';
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {ServerFile} from '../../../api/models/server-file';


@Component({
  selector: 'app-server-file-select',
  templateUrl: './server-file-select.component.html',
  styleUrls: ['./server-file-select.component.scss']
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

  ngOnInit() {
    this.selectSelectedFile();
  }


  ngOnChanges(changes: SimpleChanges): void {

    this.filteredFiles = this.myControl.valueChanges.pipe(
      // selected value can be a file, but a input is always a string. if its a file with name property it is filtered out
      filter(value => !value?.hasOwnProperty('name')),
      startWith(''),
      map(value => this._filter((value as ServerFile).name || '')),
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

    return this.files.filter(files => files.name!.toLowerCase().includes(filterValue));
  }

  private selectSelectedFile() {
    if (this.selectedFile) {
      this.myControl.setValue(this.selectedFile);
    }
  }
}

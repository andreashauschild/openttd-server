import {AfterViewInit, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Observable, tap} from 'rxjs';
import {filter, map, startWith} from 'rxjs/operators';
import {ServerFile} from "../../api/models/server-file";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";


@Component({
  selector: 'app-server-file-select',
  templateUrl: './server-file-select.component.html',
  styleUrls: ['./server-file-select.component.scss']
})
export class ServerFileSelectComponent implements OnInit, OnChanges {

  myControl = new FormControl('');

  @Output()
  selectedFile = new EventEmitter<ServerFile>();


  @Input()
  files: ServerFile[] = [];

  @Input()
  label: string = '';

  filteredFiles: Observable<ServerFile[]> | undefined;

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges): void {
    this.filteredFiles = this.myControl.valueChanges.pipe(
      // selected value can be a file, but a input is always a string. if its a file with name property it is filtered out
      filter(value => !value?.hasOwnProperty('name')),
      startWith(''),
      map(value => this._filter(value || '')),
    );
  }

  select($event: MatAutocompleteSelectedEvent) {
    this.selectedFile.emit($event.option.value);
  }

  fileToString(file: any) {
    return file.name;
  }

  private _filter(fileName: string): ServerFile[] {

    const filterValue = fileName.toLowerCase();

    return this.files.filter(files => files.name!.toLowerCase().includes(filterValue));
  }
}

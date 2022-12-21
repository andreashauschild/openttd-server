import {DatePipe} from '@angular/common';
import {Injectable} from '@angular/core';
import {ServerFile} from '../../api/models/server-file';
import {ServerFileType} from '../../api/models/server-file-type';


export const saveData = (data: any, fileName: string) => {
  var a: any = document.createElement("a");
  document.body.appendChild(a);
  a.style = "display: none";
  const blob = new Blob([data], {type: "application/octet-stream"});
  const url = window.URL.createObjectURL(blob);
  a.href = url;
  a.download = fileName;
  a.click();
  window.URL.revokeObjectURL(url);
};

export const clone = <T>(item: T): T => {
  if (item) {
    return JSON.parse(JSON.stringify(item));
  }
  return item;
}

export const truncateString = (value: string, maxLength: number) => {
  if (!value) {
    return value;
  }
  return (value.length > maxLength) ? value.slice(0, maxLength - 1) + '&hellip;' : value;
};


@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  constructor(private datePipe: DatePipe) {

  }

  serverFileNameBeautifier(file: ServerFile) {

    console.log(file)
    if (file && file.type === ServerFileType.SaveGame && file?.lastModified && file.name) {

      const fileEnding = file.name.split('.').pop()
      const truncPrefix = truncateString(file.ownerName || file.name, 16);
      const data = this.datePipe.transform(file.lastModified, 'MMM d, y, HH:mm:ss');
      return `${truncPrefix}-${data}.${fileEnding}`;
    } else if (file && file.type === ServerFileType.Config) {
      file.name;
    }
    return '';

  }
}

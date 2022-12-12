import {Component, EventEmitter, Input, OnInit, Output, TemplateRef} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-base-dialog',
  templateUrl: './base-dialog.component.html',
  styleUrls: ['./base-dialog.component.scss']
})
export class BaseDialogComponent implements OnInit {

  @Input()
  title: string | undefined = "";

  @Input()
  bodyTemplate: TemplateRef<any> | null = null;

  @Input()
  footerTemplate: TemplateRef<any> | null = null

  @Input()
  dialogRef: MatDialogRef<any, boolean> | null = null;

  constructor() {
  }

  ngOnInit(): void {
  }


  close(result: boolean) {
    if (this.dialogRef) {
      this.dialogRef.close(result)
    }
  }
}

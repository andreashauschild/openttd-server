import {Component, Input, OnInit, TemplateRef} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {NgIf, NgTemplateOutlet} from '@angular/common';

@Component({
    selector: 'app-base-dialog',
    templateUrl: './base-dialog.component.html',
    styleUrls: ['./base-dialog.component.scss'],
    standalone: true,
    imports: [NgIf, NgTemplateOutlet, MatIcon, MatButton]
})
export class BaseDialogComponent implements OnInit {

  @Input()
  title: string | undefined = "";

  @Input()
  subTitle: string | undefined = "";

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

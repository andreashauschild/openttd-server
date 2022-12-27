import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarLayoutComponent } from './sidebar-layout.component';
import {MatIconModule} from '@angular/material/icon';
import {RouterLinkWithHref} from '@angular/router';



@NgModule({
  declarations: [
    SidebarLayoutComponent
  ],
  exports: [
    SidebarLayoutComponent,

  ],
  imports: [
    CommonModule,
    MatIconModule,
    RouterLinkWithHref
  ]
})
export class SidebarLayoutModule { }

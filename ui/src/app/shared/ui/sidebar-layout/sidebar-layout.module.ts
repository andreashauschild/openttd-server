import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarLayoutComponent } from './sidebar-layout.component';
import {MatIconModule} from '@angular/material/icon';
import {RouterLink} from '@angular/router';



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
    RouterLink
  ]
})
export class SidebarLayoutModule { }

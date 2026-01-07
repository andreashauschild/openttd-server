import {Component, Input, OnInit} from '@angular/core';
import {AuthenticationService} from '../../services/authentication.service';
import {EventType, Router, RouterLink} from '@angular/router';
import {NgFor} from '@angular/common';
import {MatIcon} from '@angular/material/icon';

export type SidebarLayoutEntryModel = {
  title: string,
  icon: string,
  path: string,
  selected?: boolean
}

export type SidebarLayoutModel = {
  entries: SidebarLayoutEntryModel[],
}

@Component({
    selector: 'app-sidbar-layout',
    templateUrl: './sidebar-layout.component.html',
    styleUrls: ['./sidebar-layout.component.scss'],
    standalone: true,
    imports: [NgFor, MatIcon, RouterLink]
})
export class SidebarLayoutComponent implements OnInit {

  @Input()
  model: SidebarLayoutModel | undefined;
  window: any;

  constructor(public auth: AuthenticationService, private router: Router) {
    this.window = window;
  }

  ngOnInit(): void {
    this.router.events.subscribe(e => {
      if ((e as any).type === EventType.NavigationEnd) {
        const entry = this.model?.entries.find(entry => (e as any).url.toLowerCase().startsWith(entry.path));
        if (entry) {
          this.selectMenuEntry(entry);
        }
      }

    })
  }

  selectMenuEntry(entry: SidebarLayoutEntryModel) {
    this.model?.entries.forEach(e => {
      if (e.path === entry.path) {
        e.selected = true;
      } else {
        e.selected = false;
      }
    })
  }
}

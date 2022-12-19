import {Component, OnDestroy, OnInit} from '@angular/core';
import {Store} from '@ngrx/store';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {loadServer, loadServerFiles} from '../../../shared/store/actions/app.actions';
import {Subscription} from 'rxjs';
import {selectFiles, selectServer} from '../../../shared/store/selectors/app.selectors';
import {ServerFile} from '../../../api/models/server-file';
import {ServerFileType} from '../../../api/models/server-file-type';
import {ActivatedRoute} from '@angular/router';
import {RP_ID} from '../../../shared/model/constants';
import {OpenttdServer} from '../../../api/models/openttd-server';
import {filter} from 'rxjs/operators';
import {clone} from '../../../shared/services/utils';

@Component({
  selector: 'app-servers-detail',
  templateUrl: './servers-detail.component.html',
  styleUrls: ['./servers-detail.component.scss']
})
export class ServersDetailComponent implements OnInit, OnDestroy {

  server: OpenttdServer | undefined;
  openttdConfigs: ServerFile[] = [];
  openttdSavegames: ServerFile[] = [];

  private sub = new Subscription();

  constructor(private store: Store<{}>, private api: OpenttdServerResourceService, private activeRoute: ActivatedRoute) {

  }

  ngOnInit(): void {

    this.store.dispatch(loadServerFiles({src: ServersDetailComponent.name}));


    this.sub.add(this.store.select(selectServer).pipe(filter(s => s != null)).subscribe(s => {
      this.server = clone(s);
      this.sub.add(this.store.select(selectFiles).subscribe(files => {
        this.openttdConfigs = files.filter(f => f.type === ServerFileType.Config);
        this.openttdSavegames = files.filter(f => f.type === ServerFileType.SaveGame);
      }));
    }));

    this.activeRoute.params.subscribe(p => {
      if (p[RP_ID]) {
        this.store.dispatch(loadServer({src: ServersDetailComponent.name, id: p[RP_ID]}));
      }
    })


  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  save() {

  }
}

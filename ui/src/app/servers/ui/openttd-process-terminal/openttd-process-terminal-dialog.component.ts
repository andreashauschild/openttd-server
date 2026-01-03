import {AfterViewInit, Component, OnDestroy} from '@angular/core';
import {interval, Subject, Subscription} from "rxjs";
import {Store} from "@ngrx/store";
import {OpenttdProcess} from 'src/app/api/models';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {selectProcesses, selectProcessUpdateEvent} from '../../../shared/store/selectors/app.selectors';
import {loadProcesses} from "../../../shared/store/actions/app.actions";
import {MatDialogRef} from "@angular/material/dialog";
import {ApplicationService} from '../../../shared/services/application.service';
import {filter} from 'rxjs/operators';

@Component({
    selector: 'app-openttd-process-terminal',
    templateUrl: './openttd-process-terminal-dialog.component.html',
    styleUrls: ['./openttd-process-terminal-dialog.component.scss'],
    standalone: false
})
export class OpenttdProcessTerminalDialogComponent implements AfterViewInit, OnDestroy {


  public dialogTitle = '';

  public dialogRef: MatDialogRef<OpenttdProcessTerminalDialogComponent, boolean> | null = null;

  public openttdProcess!: OpenttdProcess;

  consoleInput = new Subject<string>();

  terminalControl = new Subject<string>();
  showTerminal = false;

  private sub = new Subscription()

  constructor(private app: ApplicationService, private store: Store<{}>, private openttd: OpenttdServerResourceService) {
  }

  sendCommand(cmd: string) {
    this.openttd.sendTerminalCommand({id: this.openttdProcess.id!, body: cmd}).subscribe();
  }

  ngAfterViewInit(): void {

    setTimeout(() => this.showTerminal = true, 500);
    this.store.dispatch(loadProcesses({src: OpenttdProcessTerminalDialogComponent.name}));

    this.sub.add(this.store.select(selectProcesses).subscribe(servers => {
      const process = servers.find(s => s.id === this.openttdProcess?.id)
      if (process) {
        this.openttdProcess = process
        setTimeout(() => {
          this.terminalControl.next("clear");
          this.consoleInput.next(this.openttdProcess!.process?.processData || "")
        }, 1000);
      }
    }));

    this.sub.add(this.store.select(selectProcessUpdateEvent).pipe(filter(e => e != null)).subscribe(event => {
      if (this.openttdProcess && event && event!.processId === this.openttdProcess?.process?.processId) {
        this.consoleInput.next(`${event.text}` || "");
      }
    }));

    // Tell the backend that the client has an open terminal. This prevents the backen from executing automated commands that otherwise would pollute the terminal
    this.sub.add(interval(10000).subscribe(_ => this.openttd.terminalOpenInUi({id: this.openttdProcess.id!}).subscribe({
      error: (e) => {
        this.app.handleError(e);
      }
    })));
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    console.log("DESTORY TERMINAL")

  }
}

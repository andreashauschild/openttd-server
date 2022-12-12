import {AfterViewInit, Component, Input, OnDestroy} from '@angular/core';
import {Subject, Subscription} from "rxjs";
import {Store} from "@ngrx/store";
import {OpenttdProcess} from 'src/app/api/models';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {selectProcesses, selectProcessUpdateEvent} from '../../../shared/store/selectors/app.selectors';
import {loadProcesses} from "../../../shared/store/actions/app.actions";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-openttd-process-terminal',
  templateUrl: './openttd-process-terminal-dialog.component.html',
  styleUrls: ['./openttd-process-terminal-dialog.component.scss']
})
export class OpenttdProcessTerminalDialogComponent implements AfterViewInit, OnDestroy {

  public dialogRef: MatDialogRef<OpenttdProcessTerminalDialogComponent, boolean> | null = null;

  public openttdProcess!: OpenttdProcess;

  consoleInput = new Subject<string>();

  terminalControl = new Subject<string>();
  showTerminal = false;

  private sub = new Subscription()

  constructor(private store: Store<{}>, private openttd: OpenttdServerResourceService) {
  }

  sendCommand(cmd: string) {
    this.openttd.sendTerminalCommand({name: this.openttdProcess?.name, body: cmd}).subscribe();
  }

  ngAfterViewInit(): void {

    setTimeout(() => this.showTerminal = true, 500);
    this.store.dispatch(loadProcesses({src: OpenttdProcessTerminalDialogComponent.name}));

    this.sub.add(this.store.select(selectProcesses).subscribe(servers => {
      const process = servers.find(s => s.name === this.openttdProcess?.name)
      if (process) {
        this.openttdProcess = process
        setTimeout(() => {
          this.terminalControl.next("clear");
          this.consoleInput.next(this.openttdProcess!.process?.processData || "")
        }, 1000);
      }
    }));

    this.sub.add(this.store.select(selectProcessUpdateEvent).pipe().subscribe(events => {
      if (this.openttdProcess) {
        const result = events.find(e => e.processId === this.openttdProcess?.process?.processId)
        if (result) {
          this.consoleInput.next(result.text || "");
        }
      }
    }));
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();

  }
}

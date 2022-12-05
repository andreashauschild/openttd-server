import {AfterViewInit, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges} from '@angular/core';
import {Subject} from "rxjs";
import {Store} from "@ngrx/store";
import {OpenttdProcess} from 'src/app/api/models';
import {OpenttdServerResourceService} from '../../../api/services/openttd-server-resource.service';
import {selectProcessUpdateEvent} from '../../../shared/store/selectors/app.selectors';

@Component({
  selector: 'app-openttd-process-terminal',
  templateUrl: './openttd-process-terminal.component.html',
  styleUrls: ['./openttd-process-terminal.component.scss']
})
export class OpenttdProcessTerminalComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {

  @Input()
  openttdProcess: OpenttdProcess | undefined;

  consoleInput = new Subject<string>();

  terminalControl = new Subject<string>();

  constructor(private store: Store<{}>, private openttd: OpenttdServerResourceService) {
  }

  ngOnDestroy(): void {
    console.log(`TERMINAL DESTROY:${this.openttdProcess?.name} `)
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes["openttdProcess"]) {
      this.terminalControl.next("clear");
      setTimeout(() => this.consoleInput.next(this.openttdProcess!.process?.processData || ""), 1000)
    }
    console.log("CHANGES", changes)
  }

  ngOnInit(): void {

    if (this.openttdProcess) {
      setTimeout(() => this.consoleInput.next(this.openttdProcess!.process?.processData || ""), 1)
    }

  }

  sendCommand(cmd: string) {
    this.openttd.sendTerminalCommand({name: this.openttdProcess?.name, body: cmd}).subscribe();
  }

  ngAfterViewInit(): void {


    this.store.select(selectProcessUpdateEvent).pipe().subscribe(events => {
      if (this.openttdProcess) {
        const result = events.find(e => e.processId === this.openttdProcess?.process?.processId)
        if (result) {
          this.consoleInput.next(result.text || "");
        }
      }
    })
  }
}

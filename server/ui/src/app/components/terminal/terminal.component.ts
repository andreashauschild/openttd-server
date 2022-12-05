import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FunctionsUsingCSI, NgTerminal} from 'ng-terminal';
import {FormControl} from '@angular/forms';
import {Terminal} from 'xterm';
import {Subject} from 'rxjs';
import {filter} from 'rxjs/operators';

@Component({
  selector: 'app-terminal',
  templateUrl: './terminal.component.html',
  styleUrls: ['./terminal.component.scss']
})
export class TerminalComponent implements OnInit, AfterViewInit {
  readonly title = 'openttd server console';
  readonly color = 'accent';
  readonly prompt = '\n' + FunctionsUsingCSI.cursorColumn(1) + '$ ';

  rows: number = 25;
  cols: number = 100;
  draggable: boolean = false;

  public fixed = false;

  disabled = false;
  rowsControl = new FormControl();
  colsControl = new FormControl();
  inputControl = new FormControl();

  underlying: Terminal | undefined;

  keyInput: string | undefined;

  unsendCommand = "";

  @ViewChild('term', {static: false}) child: NgTerminal | undefined;

  @Input()
  consoleInput = new Subject<string>();

  @Output()
  public command = new EventEmitter<string>();

  @Input()
  public terminalControl = new Subject<string>();

  writeSubject = new Subject<string>();


  baseTheme = {
    foreground: '#F8F8F8',
    background: '#2D2E2C',
    selection: '#5DA5D533',
    black: '#1E1E1D',
    brightBlack: '#262625',
    red: '#CE5C5C',
    brightRed: '#FF7272',
    green: '#5BCC5B',
    brightGreen: '#72FF72',
    yellow: '#CCCC5B',
    brightYellow: '#FFFF72',
    blue: '#5D5DD3',
    brightBlue: '#7279FF',
    magenta: '#BC5ED1',
    brightMagenta: '#E572FF',
    cyan: '#5DA5D5',
    brightCyan: '#72F0FF',
    white: '#F8F8F8',
    brightWhite: '#FFFFFF',
    border: '#85858a'
  };

  ngOnInit() {
    if (this.consoleInput) {
      this.consoleInput.pipe(filter(v => v != null)).subscribe(v => this.write(v));
    }

  }

  ngAfterViewInit() {
    if (this.child) {
      if (this.terminalControl) {
        this.terminalControl.subscribe(cmd => {
          if (cmd === "clear") {
            this.child?.underlying.clear();
          }
        })
      }
      this.underlying = this.child.underlying;
      this.underlying.options.fontSize = 20;
      console.debug("example: font apply");
      this.invalidate();
      // this.child.setXtermOptions({
      //   fontFamily: '"Cascadia Code", Menlo, monospace',
      //   theme: this.baseTheme,
      //   cursorBlink: true
      // });
      this.child.write('$ --- Welcome OpenTTD Server ----');
      this.child.write(this.prompt);
      this.child.onData().subscribe((input) => {

        if (input === '\r') { // Carriage Return (When Enter is pressed)
          this.child!.write(this.prompt);
          console.log("Command: ", this.unsendCommand);
          this.command.emit(this.unsendCommand);
          this.unsendCommand = "";
        } else if (input === '\u007f') { // Delete (When Backspace is pressed)

          if (this.unsendCommand.length > 0) {
            this.unsendCommand = this.unsendCommand.substring(0, this.unsendCommand.length - 1);
          }
          if (this.child!.underlying.buffer.active.cursorX > 2) {
            this.child!.write('\b \b');
          }
        } else if (input === '\u0003') { // End of Text (When Ctrl and C are pressed)
          this.unsendCommand = "";
          this.child!.write('^C');
          this.child!.write(this.prompt);
        } else {
          this.child!.write(input);
          this.unsendCommand += input;
        }


      })

      this.child.onKey().subscribe(e => {
        //onData() is used more often.
      });

    }

  }

  invalidate() {
  }


  write(line: string) {

    // handle linux terminal
    line.split(/[\n\r]/g).forEach(l => {
      this.child!.write(FunctionsUsingCSI.cursorColumn(1) + `${l}\n`);
    });


  }


  onKeyInput(event: string) {
    this.keyInput = event;
  }

  get displayOptionForLiveUpdate() {
    return {rows: this.rowsControl.value, cols: this.colsControl.value, draggable: this.draggable};
  }

}

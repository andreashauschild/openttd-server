import {TestBed} from '@angular/core/testing';
import {Observable, Subject} from 'rxjs';

import {TerminalComponent, TerminalIo} from './terminal.component';

const ERASE_LINE = '\r\x1b[2K';
const PROMPT = '$ ';
const BACKSPACE = '\u007f';
const LEFT = '\x1b[D';
const RIGHT = '\x1b[C';
const HOME = '\x1b[H';
const END = '\x1b[F';
const DELETE = '\x1b[3~';

class FakeTerminalIo implements TerminalIo {
  writes: string[] = [];
  scrolls = 0;
  focusCount = 0;
  readonly data = new Subject<string>();

  write(data: string): void {
    this.writes.push(data);
  }

  onData(): Observable<string> {
    return this.data.asObservable();
  }

  scrollToBottom(): void {
    this.scrolls++;
  }

  focus(): void {
    this.focusCount++;
  }

  get text(): string {
    return this.writes.join('');
  }
}

describe('TerminalComponent', () => {

  let component: TerminalComponent;
  let io: FakeTerminalIo;
  let commands: string[];

  /** Creates the component without the real xterm and attaches the fake surface. */
  function create(serverId: string | null = null): void {
    component = TestBed.runInInjectionContext(() => new TerminalComponent());
    component.serverId = serverId;
    commands = [];
    component.command.subscribe(cmd => commands.push(cmd));
    component.ngOnInit();
    io = new FakeTerminalIo();
    component.attachIo(io);
    io.writes = [];
    io.scrolls = 0;
    io.focusCount = 0;
  }

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    create();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should write a batch as whole lines without a trailing blank line', () => {
    component.write('a\nb\n');

    expect(io.text).toBe(`${ERASE_LINE}a\r\nb\r\n${PROMPT}`);
  });

  it('should keep a real blank line inside a batch', () => {
    component.write('a\n\nb\n');

    expect(io.text).toBe(`${ERASE_LINE}a\r\n\r\nb\r\n${PROMPT}`);
  });

  it('should redraw the pending input after output arrived', () => {
    io.data.next('server_i');
    io.writes = [];

    component.write('dbg: something\n');

    expect(io.text).toBe(`${ERASE_LINE}dbg: something\r\n${PROMPT}server_i`);
  });

  it('should colour the synthetic server lines red', () => {
    component.write('[openttd-server] process exited with code 1\n');

    expect(io.text).toContain('\x1b[31m[openttd-server] process exited with code 1\x1b[0m');
  });

  it('should write nothing on backspace with an empty input line', () => {
    io.data.next(BACKSPACE);

    expect(io.writes).toEqual([]);
  });

  it('should erase one character on backspace', () => {
    io.data.next('ab');
    io.writes = [];

    io.data.next(BACKSPACE);

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}a`);

    io.data.next('\r');
    expect(commands).toEqual(['a']);
  });

  it('should ignore unhandled escape sequences instead of echoing them', () => {
    io.data.next('\x1b[5~'); // PageUp
    io.data.next('\x1bOP'); // F1
    io.data.next('\t');

    expect(io.writes).toEqual([]);

    io.data.next('\r');
    expect(commands).toEqual([]);
  });

  it('should not print the command again on enter, so the backend echo is the only copy', () => {
    io.data.next('pause');
    io.writes = [];

    io.data.next('\r');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}`);
    expect(commands).toEqual(['pause']);
    expect(io.scrolls).toBe(1);
  });

  it('should emit nothing for a blank command', () => {
    io.data.next('\r');
    io.data.next('   ');
    io.data.next('\r');

    expect(commands).toEqual([]);
  });

  it('should recall the previous command with the up arrow', () => {
    io.data.next('server_info');
    io.data.next('\r');
    io.writes = [];

    io.data.next('\x1b[A');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}server_info`);

    io.data.next('\r');
    expect(commands).toEqual(['server_info', 'server_info']);
  });

  it('should clear the recalled line with the down arrow', () => {
    io.data.next('pause');
    io.data.next('\r');
    io.data.next('\x1b[A');
    io.writes = [];

    io.data.next('\x1b[B');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}`);
  });

  it('should insert a typed character at the cursor', () => {
    io.data.next('abc');
    io.data.next(LEFT);
    io.data.next(LEFT);
    io.writes = [];

    io.data.next('X');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}aXbc\x1b[2D`);

    io.data.next('\r');
    expect(commands).toEqual(['aXbc']);
  });

  it('should delete the character under the cursor after home', () => {
    io.data.next('abc');
    io.data.next(HOME);
    io.writes = [];

    io.data.next(DELETE);

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}bc\x1b[2D`);

    io.data.next('\r');
    expect(commands).toEqual(['bc']);
  });

  it('should delete the last character on backspace after end', () => {
    io.data.next('abc');
    io.data.next(HOME);
    io.data.next(END);
    io.writes = [];

    io.data.next(BACKSPACE);

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}ab`);

    io.data.next('\r');
    expect(commands).toEqual(['ab']);
  });

  it('should accept ctrl-a and ctrl-e as home and end', () => {
    io.data.next('bc');
    io.data.next('\u0001'); // Ctrl-A
    io.data.next('a');
    io.data.next('\u0005'); // Ctrl-E
    io.data.next('d');
    io.data.next('\r');

    expect(commands).toEqual(['abcd']);
  });

  it('should throw the input line away on ctrl-u', () => {
    io.data.next('paus');
    io.writes = [];

    io.data.next('\u0015'); // Ctrl-U

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}`);

    io.data.next('\r');
    expect(commands).toEqual([]);
  });

  it('should do nothing when the cursor is already at the end', () => {
    io.data.next('ab');
    io.writes = [];

    io.data.next(RIGHT);
    io.data.next(DELETE);

    expect(io.writes).toEqual([]);

    io.data.next('\r');
    expect(commands).toEqual(['ab']);
  });

  it('should do nothing when the cursor is already at the start', () => {
    io.data.next('ab');
    io.data.next(HOME);
    io.writes = [];

    io.data.next(LEFT);
    io.data.next(BACKSPACE);

    expect(io.writes).toEqual([]);

    io.data.next('\r');
    expect(commands).toEqual(['ab']);
  });

  it('should restore the cursor offset when output arrives mid line', () => {
    io.data.next('abc');
    io.data.next(LEFT);
    io.writes = [];

    component.write('line\n');

    expect(io.text).toBe(`${ERASE_LINE}line\r\n${PROMPT}abc\x1b[1D`);
  });

  it('should put the cursor at the end of a recalled command', () => {
    io.data.next('pause');
    io.data.next('\r');
    io.data.next(LEFT);
    io.writes = [];

    io.data.next('\x1b[A');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}pause`);

    io.data.next('X');
    io.data.next('\r');
    expect(commands).toEqual(['pause', 'pauseX']);
  });

  it('should send the whole line after an edit in its middle', () => {
    io.data.next('pase');
    io.data.next(LEFT);
    io.data.next(LEFT);
    io.data.next('u');
    io.data.next('\r');

    expect(commands).toEqual(['pause']);
  });

  it('should paste a block around the cursor', () => {
    io.data.next('cd');
    io.data.next(HOME);

    io.data.next('a\nb');

    expect(commands).toEqual(['a', 'bcd']);
  });

  it('should send every line of a pasted block as its own command', () => {
    io.data.next('pause\nunpause\n');

    expect(commands).toEqual(['pause', 'unpause']);
  });

  it('should prepend what was typed before a multi line paste', () => {
    io.data.next('pau');
    io.data.next('se\nunpause');

    expect(commands).toEqual(['pause', 'unpause']);
  });

  it('should block input while disabled', () => {
    component.disabled = true;

    io.data.next('pause');
    io.data.next('\r');
    io.data.next(BACKSPACE);

    expect(io.writes).toEqual([]);
    expect(commands).toEqual([]);
  });

  it('should not redraw the prompt on output while disabled', () => {
    component.disabled = true;

    component.write('[openttd-server] process exited with code 0\n');

    expect(io.text.endsWith('\r\n')).toBeTrue();
    expect(io.text).not.toContain(PROMPT);
  });

  it('should clear screen and scrollback through the write channel', () => {
    component.terminalControl.next('clear');

    expect(io.text).toBe(`\x1b[2J\x1b[3J\x1b[H${PROMPT}`);
  });

  it('should drop the pending input when the screen is cleared', () => {
    io.data.next('pau');
    io.writes = [];

    component.terminalControl.next('clear');
    io.data.next('\r');

    expect(io.text).toBe(`\x1b[2J\x1b[3J\x1b[H${PROMPT}${ERASE_LINE}${PROMPT}`);
    expect(commands).toEqual([]);
  });

  it('should give the focus back to the terminal after a clear', () => {
    component.terminalControl.next('clear');

    expect(io.focusCount).toBe(1);
  });

  it('should keep the command history after a clear', () => {
    io.data.next('pause');
    io.data.next('\r');
    io.data.next('\x1b[A');
    component.terminalControl.next('clear');
    io.writes = [];

    io.data.next('\x1b[A');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}pause`);
  });

  it('should not render a batch that contains nothing but automation markers', () => {
    component.write('echo @@@@_SaveCommand_4_@@@@\n@@@@_SaveCommand_4_@@@@\n');

    expect(io.writes).toEqual([]);
  });

  it('should render only the real lines of a mixed batch', () => {
    component.write('echo @@@@_SaveCommand_4_@@@@\nsave "autosave"\nSaving map...\n@@@@_SaveCommand_4_@@@@\nMap successfully saved to autosave.sav\n');

    expect(io.text).toBe(`${ERASE_LINE}save "autosave"\r\nSaving map...\r\nMap successfully saved to autosave.sav\r\n${PROMPT}`);
  });

  it('should keep a line that only looks like a marker', () => {
    component.write('echo @@@@_Save Command_@@@@\n');

    expect(io.text).toContain('echo @@@@_Save Command_@@@@');
  });

  it('should scroll to the bottom on the scroll control', () => {
    component.terminalControl.next('scroll');

    expect(io.scrolls).toBe(1);
  });

  it('should buffer output that arrives before the terminal is attached', () => {
    const detached = TestBed.runInInjectionContext(() => new TerminalComponent());
    detached.ngOnInit();
    detached.write('early\n');

    const surface = new FakeTerminalIo();
    detached.attachIo(surface);

    expect(surface.text).toContain('early\r\n');
  });

  it('should persist the command history per server', () => {
    create('server-1');

    io.data.next('pause');
    io.data.next('\r');

    expect(JSON.parse(localStorage.getItem('openttd-terminal-history-server-1') ?? '[]')).toEqual(['pause']);
  });

  it('should recall the persisted history of the server', () => {
    localStorage.setItem('openttd-terminal-history-server-1', JSON.stringify(['unpause']));
    create('server-1');

    io.data.next('\x1b[A');

    expect(io.text).toBe(`${ERASE_LINE}${PROMPT}unpause`);
  });
});

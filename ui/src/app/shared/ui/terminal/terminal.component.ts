import {AfterViewInit, Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output, ViewChild} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {NgTerminal, NgTerminalModule} from 'ng-terminal';
import {ITerminalOptions} from '@xterm/xterm';
import {Observable, Subject} from 'rxjs';
import {filter} from 'rxjs/operators';

/** The terminal surface this component writes to and reads keystrokes from. Kept small so tests can fake it. */
export interface TerminalIo {
  write(data: string): void;

  onData(): Observable<string>;

  scrollToBottom(): void;

  focus(): void;
}

const PROMPT = '$ ';
/** Carriage return + "erase whole line": puts the cursor at column 1 with nothing left of the input line. */
const ERASE_LINE = '\r\x1b[2K';
/** Erase screen, erase scrollback (xterm 5) and home the cursor. Goes through write() to keep the order. */
const CLEAR_SCREEN = '\x1b[2J\x1b[3J\x1b[H';
const RED = '\x1b[31m';
const RESET = '\x1b[0m';
/** Prefix of the lines the server itself adds to the log (exit notice, command errors). */
const SYNTHETIC_PREFIX = '[openttd-server]';
/**
 * Marker of an automated command (`echo @@@@_SaveCommand_4_@@@@`) and OpenTTD's echo of it. Auto saves still run
 * while the terminal is open, so these lines reach the log; they are noise for the admin and are not rendered.
 */
const MARKER_LINE = /^(echo )?@@@@_[A-Za-z]+_\d+_@@@@$/;
const HISTORY_KEY_PREFIX = 'openttd-terminal-history-';
const HISTORY_LIMIT = 50;

/** Drops the surrounding whitespace and the left-to-right marks OpenTTD prefixes some lines with. */
function normalize(line: string): string {
  return line.replace(/^[\s\u200e]+|[\s\u200e]+$/g, '');
}

@Component({
    selector: 'app-terminal',
    templateUrl: './terminal.component.html',
    styleUrls: ['./terminal.component.scss'],
    standalone: true,
    imports: [NgTerminalModule]
})
export class TerminalComponent implements OnInit, AfterViewInit {

  private readonly destroyRef = inject(DestroyRef);

  /** Used as the key of the persisted command history. */
  @Input()
  serverId: string | null = null;

  /** Blocks all input, e.g. after the process exited. */
  @Input()
  disabled = false;

  /** Text coming from the backend. Whole lines, terminated by a single `\n`. */
  @Input()
  consoleInput = new Subject<string>();

  /** `clear` wipes screen and scrollback, `scroll` jumps to the bottom. */
  @Input()
  terminalControl = new Subject<string>();

  @Output()
  public command = new EventEmitter<string>();

  @ViewChild('term', {static: false}) child: NgTerminal | undefined;

  readonly baseTheme = {
    foreground: '#F8F8F8',
    background: '#2D2E2C',
    selectionBackground: '#5DA5D533',
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

  readonly xtermOptions: ITerminalOptions & { theme?: { border?: string } } = {
    fontSize: 16,
    theme: this.baseTheme,
    scrollback: 5000,
    cursorBlink: true
  };

  /** What the user typed but did not send yet. */
  private pending = '';
  /** Where the next character goes: an index into `pending`, between 0 and its length. */
  private cursor = 0;
  private history: string[] = [];
  /** Points into `history`; `history.length` means "not recalling, new line". */
  private historyIndex = 0;
  private io: TerminalIo | null = null;
  /** Output that arrived before the xterm was attached. */
  private buffered: string[] = [];

  ngOnInit(): void {
    this.history = this.loadHistory();
    this.historyIndex = this.history.length;
    this.consoleInput.pipe(filter(v => v != null), takeUntilDestroyed(this.destroyRef)).subscribe(v => this.write(v));
    this.terminalControl.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(cmd => {
      if (cmd === 'clear') {
        this.clear();
      } else if (cmd === 'scroll') {
        this.io?.scrollToBottom();
      }
    });
  }

  ngAfterViewInit(): void {
    if (!this.child) {
      return;
    }
    const child = this.child;
    this.attachIo({
      write: (data) => child.write(data),
      onData: () => child.onData(),
      scrollToBottom: () => child.underlying?.scrollToBottom(),
      focus: () => child.underlying?.focus()
    });
  }

  /** Binds the component to a terminal surface. Called by `ngAfterViewInit`, replaceable in tests. */
  attachIo(io: TerminalIo): void {
    this.io = io;
    io.onData().pipe(takeUntilDestroyed(this.destroyRef)).subscribe(input => this.handleInput(input));
    io.write(`${SYNTHETIC_PREFIX} welcome to the OpenTTD server console\r\n`);
    io.write(PROMPT);
    const buffered = this.buffered;
    this.buffered = [];
    buffered.forEach(text => this.write(text));
  }

  /**
   * Writes backend output as whole lines. The input line is erased first and redrawn afterwards, so incoming
   * output never overwrites what the user is typing.
   */
  write(text: string): void {
    if (!text) {
      return;
    }
    if (!this.io) {
      this.buffered.push(text);
      return;
    }
    // Exactly one trailing line break is the batch terminator, not an empty line.
    const lines = text.replace(/\r?\n$/, '').split(/\r?\n/).filter(line => !MARKER_LINE.test(normalize(line)));
    if (lines.length === 0) {
      // A batch of nothing but automation markers: leave the screen and the input line untouched.
      return;
    }
    let out = ERASE_LINE;
    for (const line of lines) {
      out += (line.startsWith(SYNTHETIC_PREFIX) ? RED + line + RESET : line) + '\r\n';
    }
    if (!this.disabled) {
      out += this.inputLine();
    }
    this.io.write(out);
  }

  clear(): void {
    this.pending = '';
    this.cursor = 0;
    this.historyIndex = this.history.length;
    this.io?.write(CLEAR_SCREEN + (this.disabled ? '' : PROMPT));
    // The click on the clear button took the focus out of the terminal, give it back.
    this.io?.focus();
  }

  /** Right-click pastes the clipboard, if the browser grants access. Otherwise the native menu opens. */
  onContextMenu(event: MouseEvent): void {
    if (this.disabled || !navigator.clipboard || typeof navigator.clipboard.readText !== 'function') {
      return;
    }
    event.preventDefault();
    navigator.clipboard.readText().then(text => {
      if (text) {
        this.handleInput(text);
      }
    }).catch(() => undefined);
  }

  private handleInput(input: string): void {
    if (this.disabled || !this.io || !input) {
      return;
    }
    if (input.length > 1 && /[\r\n]/.test(input)) {
      this.handleMultiLineInput(input);
      return;
    }
    switch (input) {
      case '\r':
        this.submit();
        return;
      case '\u007f': // Backspace
        this.backspace();
        return;
      case '\u0003': // Ctrl-C
        this.cancel();
        return;
      case '\u000c': // Ctrl-L
        this.clear();
        return;
      case '\u0015': // Ctrl-U
        this.clearLine();
        return;
      case '\x1b[A': // Up
        this.recall(-1);
        return;
      case '\x1b[B': // Down
        this.recall(1);
        return;
      case '\x1b[D': // Left
        this.moveCursorTo(this.cursor - 1);
        return;
      case '\x1b[C': // Right
        this.moveCursorTo(this.cursor + 1);
        return;
      case '\x1b[H': // Home, in its three flavours, and Ctrl-A
      case '\x1b[1~':
      case '\x1bOH':
      case '\u0001':
        this.moveCursorTo(0);
        return;
      case '\x1b[F': // End, in its three flavours, and Ctrl-E
      case '\x1b[4~':
      case '\x1bOF':
      case '\u0005':
        this.moveCursorTo(this.pending.length);
        return;
      case '\x1b[3~': // Delete
        this.deleteAtCursor();
        return;
      default:
        break;
    }
    // A still unhandled escape sequence (F-keys, PageUp, ...) is ignored completely: it would otherwise be
    // echoed into the screen and sent to OpenTTD as part of the command.
    if (input.startsWith('\x1b')) {
      return;
    }
    const printable = Array.from(input).filter(c => c >= ' ' && c !== '\u007f').join('');
    if (!printable) {
      return;
    }
    this.pending = this.pending.substring(0, this.cursor) + printable + this.pending.substring(this.cursor);
    this.cursor += printable.length;
    this.redrawInput();
  }

  /** A paste of several lines: every non-empty line is sent as its own command, in order. */
  private handleMultiLineInput(input: string): void {
    const lines = input.split(/\r\n|\r|\n/);
    // The paste lands at the cursor: what stood before it opens the first line, what stood after it closes the last.
    lines[0] = this.pending.substring(0, this.cursor) + lines[0];
    lines[lines.length - 1] += this.pending.substring(this.cursor);
    for (const line of lines) {
      this.pending = line;
      this.cursor = line.length;
      this.submit();
    }
  }

  private submit(): void {
    if (!this.io) {
      return;
    }
    const cmd = this.pending;
    this.pending = '';
    this.cursor = 0;
    // Erase the local copy: the backend echoes every command into the log, and that echo is the single copy.
    this.io.write(ERASE_LINE + PROMPT);
    this.io.scrollToBottom();
    if (cmd.trim().length === 0) {
      return;
    }
    this.pushHistory(cmd);
    this.command.emit(cmd);
  }

  /** Removes the character before the cursor. */
  private backspace(): void {
    if (this.cursor === 0) {
      return;
    }
    this.pending = this.pending.substring(0, this.cursor - 1) + this.pending.substring(this.cursor);
    this.cursor--;
    this.redrawInput();
  }

  /** Removes the character the cursor sits on. */
  private deleteAtCursor(): void {
    if (this.cursor >= this.pending.length) {
      return;
    }
    this.pending = this.pending.substring(0, this.cursor) + this.pending.substring(this.cursor + 1);
    this.redrawInput();
  }

  /** Ctrl-U: throws the whole input line away without sending it. */
  private clearLine(): void {
    if (this.pending.length === 0) {
      return;
    }
    this.pending = '';
    this.cursor = 0;
    this.redrawInput();
  }

  /** Moves the cursor inside the input line, clamped to its ends. A move that changes nothing draws nothing. */
  private moveCursorTo(index: number): void {
    const next = Math.min(Math.max(index, 0), this.pending.length);
    if (next === this.cursor) {
      return;
    }
    this.cursor = next;
    this.redrawInput();
  }

  /**
   * The input line as it has to appear on screen: prompt, pending text, and the terminal cursor walked back to
   * where `cursor` points. Redrawing the whole line is cheaper to reason about than patching it character by
   * character, and it cannot drift out of sync with `pending`.
   */
  private inputLine(): string {
    const back = this.pending.length - this.cursor;
    return PROMPT + this.pending + (back > 0 ? `\x1b[${back}D` : '');
  }

  /** Rewrites the input line in place, after an edit or a cursor move. */
  private redrawInput(): void {
    this.io?.write(ERASE_LINE + this.inputLine());
  }

  private cancel(): void {
    this.pending = '';
    this.cursor = 0;
    this.historyIndex = this.history.length;
    this.io?.write('^C\r\n' + PROMPT);
  }

  private recall(direction: number): void {
    if (this.history.length === 0) {
      return;
    }
    const next = this.historyIndex + direction;
    if (next < 0 || next > this.history.length) {
      return;
    }
    this.historyIndex = next;
    this.pending = next === this.history.length ? '' : this.history[next];
    this.cursor = this.pending.length;
    this.redrawInput();
  }

  private pushHistory(cmd: string): void {
    if (this.history[this.history.length - 1] !== cmd) {
      this.history.push(cmd);
    }
    if (this.history.length > HISTORY_LIMIT) {
      this.history = this.history.slice(this.history.length - HISTORY_LIMIT);
    }
    this.historyIndex = this.history.length;
    this.storeHistory();
  }

  private historyKey(): string | null {
    return this.serverId ? HISTORY_KEY_PREFIX + this.serverId : null;
  }

  private loadHistory(): string[] {
    const key = this.historyKey();
    if (!key) {
      return [];
    }
    try {
      const stored: unknown = JSON.parse(localStorage.getItem(key) ?? '[]');
      return Array.isArray(stored) ? stored.filter((e): e is string => typeof e === 'string') : [];
    } catch {
      return [];
    }
  }

  private storeHistory(): void {
    const key = this.historyKey();
    if (!key) {
      return;
    }
    try {
      localStorage.setItem(key, JSON.stringify(this.history));
    } catch {
      // A full or blocked localStorage must not break the terminal.
    }
  }
}

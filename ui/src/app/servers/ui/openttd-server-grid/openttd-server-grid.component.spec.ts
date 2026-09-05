import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideStore } from '@ngrx/store';
import { metaReducers, reducers } from '@store/reducers';

import { OpenttdServer } from '@api/models/openttd-server';

import { OpenttdServerGridComponent } from './openttd-server-grid.component';

describe('OppttdServerTableComponent', () => {
  let component: OpenttdServerGridComponent;
  let fixture: ComponentFixture<OpenttdServerGridComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ OpenttdServerGridComponent ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
        provideStore(reducers, {metaReducers})
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OpenttdServerGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  const running: OpenttdServer = {id: 'server-1', name: 'Server 1', process: {id: 'server-1', process: {processId: 'p-1'}}};
  const exited: OpenttdServer = {id: 'server-1', name: 'Server 1', process: {id: 'server-1', process: {processId: 'p-1', exitCode: 137}}};
  const offline: OpenttdServer = {id: 'server-1', name: 'Server 1'};

  function render(server: OpenttdServer): void {
    component.dataSource = [server];
    fixture.detectChanges();
  }

  function text(): string {
    return fixture.nativeElement.textContent as string;
  }

  /** The names of the material icons of the card's action row. */
  function icons(): string[] {
    return Array.from(fixture.nativeElement.querySelectorAll('mat-icon'))
      .map(icon => ((icon as HTMLElement).textContent ?? '').trim());
  }

  it('should show the running badge while the process is alive', () => {
    render(running);

    expect(text()).toContain('Running');
    expect(text()).not.toContain('exited (code');
  });

  it('should replace the running badge by the exit code when the process exited', () => {
    render(exited);

    expect(text()).not.toContain('Running');
    expect(text()).toContain('exited (code 137)');
  });

  it('should offer the full control set while the process is alive', () => {
    render(running);

    expect(icons()).toEqual(['stop_circle', 'pause', 'save', 'edit_note', 'terminal', 'delete']);
  });

  it('should offer start and stop but no process actions when the process exited', () => {
    render(exited);

    expect(icons()).toEqual(['play_circle', 'stop_circle', 'edit_note', 'delete']);
  });

  it('should offer only start on an offline server', () => {
    render(offline);

    expect(icons()).toEqual(['play_circle', 'edit_note', 'delete']);
  });

  it('should not claim an exited server is paused', () => {
    render({...exited, paused: true});

    expect(text()).not.toContain('Paused');
  });
});

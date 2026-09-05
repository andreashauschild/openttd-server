import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';
import { ApplicationService } from '@shared/services/application.service';
import { AuthenticationService } from '@shared/services/authentication.service';
import { BackendWebsocketService } from '@shared/services/backend-websocket.service';
import { SESSION_STORAGE_KEY } from '@shared/model/constants';

describe('AppComponent', () => {

  let backendWebsocketService: { connect: jasmine.Spy; disconnect: jasmine.Spy };

  beforeEach(async () => {
    localStorage.clear();
    backendWebsocketService = {connect: jasmine.createSpy('connect'), disconnect: jasmine.createSpy('disconnect')};
    await TestBed.configureTestingModule({
      imports: [
        AppComponent
      ],
      providers: [
        provideRouter([]),
        {provide: ApplicationService, useValue: {}},
        {provide: AuthenticationService, useValue: {}},
        {provide: BackendWebsocketService, useValue: backendWebsocketService}
      ],
    }).compileComponents();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should offer the sidebar entries for servers, settings and file explorer', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const paths = fixture.componentInstance.sidebarLayoutModel.entries.map(e => e.path);
    expect(paths).toEqual(['/servers', '/settings', '/explorer']);
  });

  it('should connect the backend websocket on init when a session is stored', () => {
    localStorage.setItem(SESSION_STORAGE_KEY, 'session-1');

    const fixture = TestBed.createComponent(AppComponent);
    fixture.componentInstance.ngOnInit();

    expect(backendWebsocketService.connect).toHaveBeenCalled();
  });

  it('should not connect the backend websocket without a session', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.componentInstance.ngOnInit();

    expect(backendWebsocketService.connect).not.toHaveBeenCalled();
  });
});

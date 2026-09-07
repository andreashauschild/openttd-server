import {TestBed} from '@angular/core/testing';
import {HttpErrorResponse} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Store} from '@ngrx/store';

import {ApplicationService} from './application.service';

describe('ApplicationService', () => {

  let service: ApplicationService;
  let store: { dispatch: jasmine.Spy };

  const waitForDispatch = async (): Promise<void> => {
    for (let i = 0; i < 50 && !store.dispatch.calls.any(); i++) {
      await new Promise(resolve => setTimeout(resolve, 5));
    }
  };

  const dispatchedMessage = (): string => store.dispatch.calls.mostRecent().args[0].alert.message;

  beforeEach(() => {
    store = {dispatch: jasmine.createSpy('dispatch')};
    TestBed.configureTestingModule({
      providers: [
        ApplicationService,
        {provide: Store, useValue: store},
        {provide: MatSnackBar, useValue: {openFromComponent: jasmine.createSpy('openFromComponent')}}
      ]
    });
    service = TestBed.inject(ApplicationService);
    store.dispatch.calls.reset();
  });

  it('should show the message of a blob error body that holds a service error', async () => {
    const body = new Blob([JSON.stringify({message: 'File not found', stackTrace: 'a stack trace'})], {type: 'application/json'});

    service.handleError(new HttpErrorResponse({status: 500, statusText: 'Internal Server Error', error: body}));
    await waitForDispatch();

    expect(dispatchedMessage()).toBe('Error: HTTP 500 Internal Server Error: File not found');
    expect(store.dispatch.calls.mostRecent().args[0].alert.stacktrace).toBe('a stack trace');
  });

  it('should show the status of a blob error body that is not a service error', async () => {
    const body = new Blob(['Forbidden'], {type: 'text/plain'});

    service.handleError(new HttpErrorResponse({status: 403, statusText: 'Forbidden', error: body}));
    await waitForDispatch();

    expect(dispatchedMessage()).toBe('Error: HTTP 403 Forbidden');
  });

  it('should keep showing the message of a JSON error body', () => {
    service.handleError(new HttpErrorResponse({status: 500, statusText: 'Internal Server Error', error: {message: 'File not found'}}));

    expect(dispatchedMessage()).toBe('Error: File not found');
  });

  it('should show a generic message when the response has no error body', () => {
    service.handleError(new HttpErrorResponse({status: 0, statusText: 'Unknown Error'}));

    expect(dispatchedMessage()).toBe('Error: HTTP Error. See console log');
  });
});

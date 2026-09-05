import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideStore } from '@ngrx/store';
import { metaReducers, reducers } from '@store/reducers';

import { AppNotificationsComponent } from './app-notifications.component';

describe('AppNotificationsComponent', () => {
  let component: AppNotificationsComponent;
  let fixture: ComponentFixture<AppNotificationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ AppNotificationsComponent ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
        provideStore(reducers, {metaReducers})
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AppNotificationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

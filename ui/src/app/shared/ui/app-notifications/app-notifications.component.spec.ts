import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppNotificationsComponent } from './app-notifications.component';

describe('AppNotificationsComponent', () => {
  let component: AppNotificationsComponent;
  let fixture: ComponentFixture<AppNotificationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AppNotificationsComponent ]
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

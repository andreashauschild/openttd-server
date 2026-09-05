import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideStore } from '@ngrx/store';
import { metaReducers, reducers } from '@store/reducers';

import { ServersDetailComponent } from './servers-detail.component';

describe('EditServerComponent', () => {
  let component: ServersDetailComponent;
  let fixture: ComponentFixture<ServersDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ ServersDetailComponent ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideNoopAnimations(),
        provideStore(reducers, {metaReducers})
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServersDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

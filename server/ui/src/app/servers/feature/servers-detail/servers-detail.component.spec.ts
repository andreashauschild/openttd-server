import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServersDetailComponent } from './servers-detail.component';

describe('EditServerComponent', () => {
  let component: ServersDetailComponent;
  let fixture: ComponentFixture<ServersDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServersDetailComponent ]
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

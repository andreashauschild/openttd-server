import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenttdServerGridComponent } from './openttd-server-grid.component';

describe('OppttdServerTableComponent', () => {
  let component: OpenttdServerGridComponent;
  let fixture: ComponentFixture<OpenttdServerGridComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OpenttdServerGridComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OpenttdServerGridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

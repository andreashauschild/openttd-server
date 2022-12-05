import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenttdServerTableComponent } from './openttd-server-table.component';

describe('OppttdServerTableComponent', () => {
  let component: OpenttdServerTableComponent;
  let fixture: ComponentFixture<OpenttdServerTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OpenttdServerTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OpenttdServerTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

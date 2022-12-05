import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OppttdServerTableComponent } from './oppttd-server-table.component';

describe('OppttdServerTableComponent', () => {
  let component: OppttdServerTableComponent;
  let fixture: ComponentFixture<OppttdServerTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OppttdServerTableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OppttdServerTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

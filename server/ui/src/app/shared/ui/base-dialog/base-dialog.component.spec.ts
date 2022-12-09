import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BaseDialogComponent } from './base-dialog.component';

describe('BaseDialogComponent', () => {
  let component: BaseDialogComponent;
  let fixture: ComponentFixture<BaseDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BaseDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BaseDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

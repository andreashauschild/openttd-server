import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SidebarLayoutComponent } from './sidebar-layout.component';

describe('SidbarLayoutComponent', () => {
  let component: SidebarLayoutComponent;
  let fixture: ComponentFixture<SidebarLayoutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SidebarLayoutComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SidebarLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

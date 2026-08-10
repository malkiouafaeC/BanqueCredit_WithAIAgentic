import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { AuthStore } from '../../../core/stores/auth.store';
import { HomePageComponent } from './home-page.component';

describe('HomePageComponent', () => {
  let router: Router;
  let clearSessionSpy: jasmine.Spy;

  beforeEach(async () => {
    clearSessionSpy = jasmine.createSpy('clearSession');

    await TestBed.configureTestingModule({
      imports: [HomePageComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthStore,
          useValue: {
            username: () => 'conseiller1',
            clearSession: clearSessionSpy
          }
        }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  it('should display connected username in menu trigger', () => {
    const fixture = TestBed.createComponent(HomePageComponent);
    fixture.detectChanges();

    const trigger = fixture.nativeElement.querySelector('.menu-trigger') as HTMLButtonElement;
    expect(trigger.textContent).toContain('conseiller1');
  });

  it('should navigate to /clients when clients action is clicked', () => {
    const fixture = TestBed.createComponent(HomePageComponent);
    fixture.detectChanges();

    const trigger = fixture.nativeElement.querySelector('.menu-trigger') as HTMLButtonElement;
    trigger.click();
    fixture.detectChanges();

    const clientsAction = Array.from(fixture.nativeElement.querySelectorAll('.menu-panel button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Clients')
    ) as HTMLButtonElement;

    clientsAction.click();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/clients');
  });

  it('should clear session and navigate to login on logout', () => {
    const fixture = TestBed.createComponent(HomePageComponent);
    fixture.detectChanges();

    const trigger = fixture.nativeElement.querySelector('.menu-trigger') as HTMLButtonElement;
    trigger.click();
    fixture.detectChanges();

    const logoutAction = Array.from(fixture.nativeElement.querySelectorAll('.menu-panel button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Se deconnecter')
    ) as HTMLButtonElement;

    logoutAction.click();

    expect(clearSessionSpy).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});

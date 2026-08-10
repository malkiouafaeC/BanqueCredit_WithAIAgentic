import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { AuthStore } from '../stores/auth.store';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let isAuthenticatedSpy: jasmine.Spy;
  let router: Router;

  beforeEach(() => {
    isAuthenticatedSpy = jasmine.createSpy('isAuthenticated').and.returnValue(true);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthStore,
          useValue: {
            isAuthenticated: isAuthenticatedSpy
          }
        }
      ]
    });

    router = TestBed.inject(Router);
  });

  it('should allow navigation when authenticated', () => {
    isAuthenticatedSpy.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBeTrue();
  });

  it('should redirect to /login when unauthenticated', () => {
    isAuthenticatedSpy.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result instanceof UrlTree).toBeTrue();
    if (result instanceof UrlTree) {
      expect(router.serializeUrl(result)).toBe('/login');
    }
  });
});

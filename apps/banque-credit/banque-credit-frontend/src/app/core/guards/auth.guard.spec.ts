import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../../features/auth/auth.service';

describe('authGuard', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated']);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authServiceMock }]
    });
    router = TestBed.inject(Router);
  });

  function runGuard() {
    return TestBed.runInInjectionContext(() =>
      authGuard({} as any, { url: '/dashboard' } as any)
    );
  }

  it('allows navigation when authenticated', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    expect(runGuard()).toBeTrue();
  });

  it('redirects to /login when not authenticated', () => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    const result = runGuard() as UrlTree;
    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result)).toContain('/login');
  });
});


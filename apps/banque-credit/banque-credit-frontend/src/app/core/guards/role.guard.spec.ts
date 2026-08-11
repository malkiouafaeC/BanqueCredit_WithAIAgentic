import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../../features/auth/auth.service';
import { Role } from '../../features/auth/role.enum';

describe('roleGuard', () => {
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['isAuthenticated', 'hasRole']);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authServiceMock }]
    });
    router = TestBed.inject(Router);
  });

  function runGuard(roles: Role[]) {
    return TestBed.runInInjectionContext(() =>
      roleGuard(...roles)({} as any, { url: '/clients' } as any)
    );
  }

  it('redirects unauthenticated users to /login', () => {
    authServiceMock.isAuthenticated.and.returnValue(false);
    const result = runGuard([Role.CONSEILLER]) as UrlTree;
    expect(router.serializeUrl(result)).toContain('/login');
  });

  it('allows access when role matches', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    authServiceMock.hasRole.and.returnValue(true);
    expect(runGuard([Role.CONSEILLER])).toBeTrue();
  });

  it('redirects to /dashboard when role does not match (E17/E18)', () => {
    authServiceMock.isAuthenticated.and.returnValue(true);
    authServiceMock.hasRole.and.returnValue(false);
    const result = runGuard([Role.RESPONSABLE_CREDIT]) as UrlTree;
    expect(router.serializeUrl(result)).toContain('/dashboard');
  });
});


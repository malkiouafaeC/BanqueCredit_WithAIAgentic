import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
import { Role } from '../../features/auth/role.enum';

/**
 * Factory guard: only lets through users authenticated AND holding one of the required roles
 * (spec-architecture-banque-credit.md §5.3, Q5 role separation, AC-ARCH-003).
 * Unauthenticated users are redirected to /login; authenticated users with the wrong role
 * are redirected to /dashboard (no privilege escalation info leaked).
 */
export function roleGuard(...allowedRoles: Role[]): CanActivateFn {
  return (_route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
    }

    if (authService.hasRole(allowedRoles)) {
      return true;
    }

    return router.createUrlTree(['/dashboard']);
  };
}


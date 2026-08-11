import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { Role } from './features/auth/role.enum';

/**
 * Routing table (spec-architecture-banque-credit.md §5.3). Lazy-loaded standalone
 * components keep the initial bundle small; guards enforce authentication (US-002)
 * and role separation (Q5, AC-ARCH-003) at the route level.
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage)
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/app-layout.component').then((m) => m.AppLayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.page').then((m) => m.DashboardPage)
      },
      {
        path: 'clients',
        canActivate: [roleGuard(Role.CONSEILLER)],
        loadComponent: () => import('./features/clients/client-list.page').then((m) => m.ClientListPage)
      },
      {
        path: 'clients/nouveau',
        canActivate: [roleGuard(Role.CONSEILLER)],
        loadComponent: () => import('./features/clients/client-create.page').then((m) => m.ClientCreatePage)
      },
      {
        path: 'clients/:id',
        loadComponent: () => import('./features/clients/client-detail.page').then((m) => m.ClientDetailPage)
      },
      {
        path: 'demandes',
        loadComponent: () => import('./features/demandes/demande-list.page').then((m) => m.DemandeListPage)
      },
      {
        path: 'demandes/nouvelle',
        canActivate: [roleGuard(Role.CONSEILLER)],
        loadComponent: () => import('./features/demandes/demande-create.page').then((m) => m.DemandeCreatePage)
      },
      {
        path: 'demandes/:id',
        loadComponent: () => import('./features/demandes/demande-detail.page').then((m) => m.DemandeDetailPage)
      },
      {
        path: 'demandes/:id/decision',
        canActivate: [roleGuard(Role.RESPONSABLE_CREDIT)],
        loadComponent: () => import('./features/decisions/decision.page').then((m) => m.DecisionPage)
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

import { authGuard } from './core/guards/auth.guard';
import { routes } from './app.routes';

describe('app routes', () => {
  it('should define /login, /home and /clients routes', () => {
    const routePaths = routes.map((route) => route.path);

    expect(routePaths).toContain('login');
    expect(routePaths).toContain('home');
    expect(routePaths).toContain('clients');
  });

  it('should protect /home and /clients with authGuard', () => {
    const homeRoute = routes.find((route) => route.path === 'home');
    const clientsRoute = routes.find((route) => route.path === 'clients');

    expect(homeRoute?.canActivate).toContain(authGuard);
    expect(clientsRoute?.canActivate).toContain(authGuard);
  });
});

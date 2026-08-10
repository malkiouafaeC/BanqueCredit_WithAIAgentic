import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthApiService } from '../../../core/services/auth-api.service';
import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let authApiService: jasmine.SpyObj<AuthApiService>;
  let router: Router;

  beforeEach(async () => {
    authApiService = jasmine.createSpyObj<AuthApiService>('AuthApiService', ['login']);

    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [{ provide: AuthApiService, useValue: authApiService }, provideRouter([])]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  it('should submit credentials and redirect to /home on success', () => {
    authApiService.login.and.returnValue(of({ token: 'jwt-token', username: 'conseiller1' }));

    const fixture = TestBed.createComponent(LoginPageComponent);
    const component = fixture.componentInstance;

    component.loginForm.setValue({ username: 'conseiller1', password: 'password123' });
    component.submit();

    expect(authApiService.login).toHaveBeenCalledOnceWith({
      username: 'conseiller1',
      password: 'password123'
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/home');
    expect(component.authErrorMessage()).toBeNull();
  });

  it('should display clear message when API returns 401', () => {
    authApiService.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }))
    );

    const fixture = TestBed.createComponent(LoginPageComponent);
    const component = fixture.componentInstance;

    component.loginForm.setValue({ username: 'bad-user', password: 'bad-password' });
    component.submit();

    expect(component.authErrorMessage()).toContain('Identifiants invalides');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('should not call API when form is invalid', () => {
    const fixture = TestBed.createComponent(LoginPageComponent);
    const component = fixture.componentInstance;

    component.loginForm.setValue({ username: '', password: '' });
    component.submit();

    expect(authApiService.login).not.toHaveBeenCalled();
  });
});

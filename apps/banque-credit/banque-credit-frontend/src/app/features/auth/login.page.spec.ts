import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { LoginPage } from './login.page';
import { AuthService } from './auth.service';
import { Role } from './role.enum';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  it('marks the form invalid when fields are empty and blocks submission', () => {
    fixture.detectChanges();
    fixture.componentInstance.submit();
    expect(authServiceMock.login).not.toHaveBeenCalled();
    expect(fixture.componentInstance.form.invalid).toBeTrue();
  });

  it('navigates to /dashboard on successful login (AC-001-1/2)', () => {
    authServiceMock.login.and.returnValue(
      of({ token: 't', username: 'conseiller1', role: Role.CONSEILLER, expiresAt: '2099-01-01' })
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ username: 'conseiller1', password: 'secret' });
    fixture.componentInstance.submit();

    expect(authServiceMock.login).toHaveBeenCalledWith({ username: 'conseiller1', password: 'secret' });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('shows a generic error message on 401 (AC-001-3)', () => {
    authServiceMock.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401 }))
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ username: 'x', password: 'wrong' });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.errorMessage()).toContain('Identifiants incorrects');
  });
});


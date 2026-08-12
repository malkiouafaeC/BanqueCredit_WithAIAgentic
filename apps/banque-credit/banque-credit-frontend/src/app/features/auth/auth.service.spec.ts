import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Role } from './role.enum';
import { environment } from '../../../environments/environment';

const STORAGE_KEY = 'banque-credit-auth';

/** QA-TASK-101 - Couvre auth.service.ts, precedemment sans spec dedie (0% branches). */
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.removeItem(STORAGE_KEY);
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.removeItem(STORAGE_KEY);
  });

  it('is not authenticated and has no role/username by default (empty storage)', () => {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.currentRole()).toBeNull();
    expect(service.currentUsername()).toBeNull();
    expect(service.getToken()).toBeNull();
  });

  it('logs in, stores the session and exposes signals (AC-001-1/2)', () => {
    service.login({ username: 'conseiller1', password: 'Conseiller123!' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'tok', username: 'conseiller1', role: Role.CONSEILLER, expiresAt: '2030-01-01T00:00:00Z' });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.currentRole()).toBe(Role.CONSEILLER);
    expect(service.currentUsername()).toBe('conseiller1');
    expect(service.getToken()).toBe('tok');
    expect(sessionStorage.getItem(STORAGE_KEY)).toContain('conseiller1');
  });

  it('logs out and clears the session', () => {
    service.login({ username: 'responsable1', password: 'Responsable123!' }).subscribe();
    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ token: 'tok', username: 'responsable1', role: Role.RESPONSABLE_CREDIT, expiresAt: '2030-01-01T00:00:00Z' });

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getToken()).toBeNull();
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it('hasRole returns true for a matching single role and false otherwise', () => {
    service.login({ username: 'conseiller1', password: 'x' }).subscribe();
    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ token: 'tok', username: 'conseiller1', role: Role.CONSEILLER, expiresAt: '2030-01-01T00:00:00Z' });

    expect(service.hasRole(Role.CONSEILLER)).toBeTrue();
    expect(service.hasRole(Role.RESPONSABLE_CREDIT)).toBeFalse();
  });

  it('hasRole returns true when the current role is included in an array', () => {
    service.login({ username: 'responsable1', password: 'x' }).subscribe();
    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ token: 'tok', username: 'responsable1', role: Role.RESPONSABLE_CREDIT, expiresAt: '2030-01-01T00:00:00Z' });

    expect(service.hasRole([Role.CONSEILLER, Role.RESPONSABLE_CREDIT])).toBeTrue();
    expect(service.hasRole([Role.CONSEILLER])).toBeFalse();
  });

  it('hasRole returns false when nobody is authenticated', () => {
    expect(service.hasRole(Role.CONSEILLER)).toBeFalse();
    expect(service.hasRole([Role.CONSEILLER, Role.RESPONSABLE_CREDIT])).toBeFalse();
  });

  it('restores an authenticated session from sessionStorage on construction', () => {
    sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ token: 'restored', username: 'conseiller1', role: Role.CONSEILLER, expiresAt: '2030-01-01T00:00:00Z' })
    );

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    const restoredService = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    expect(restoredService.isAuthenticated()).toBeTrue();
    expect(restoredService.getToken()).toBe('restored');
    expect(restoredService.currentUsername()).toBe('conseiller1');
  });

  it('ignores corrupted sessionStorage content and starts unauthenticated', () => {
    sessionStorage.setItem(STORAGE_KEY, '{not-valid-json');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ imports: [HttpClientTestingModule] });
    const freshService = TestBed.inject(AuthService);

    expect(freshService.isAuthenticated()).toBeFalse();
    httpMock = TestBed.inject(HttpTestingController);
  });
});


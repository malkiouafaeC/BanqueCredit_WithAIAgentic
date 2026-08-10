import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { LoginRequest, LoginResponse } from '../models/auth.model';
import { AuthStore } from '../stores/auth.store';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;
  let authStore: AuthStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
    authStore = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should post login payload and return typed response', () => {
    const payload: LoginRequest = { username: 'conseiller1', password: 'password123' };
    const expectedResponse: LoginResponse = { token: 'jwt-token', username: 'conseiller1' };

    service.login(payload).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(expectedResponse);
  });

  it('should persist session when login succeeds', () => {
    const payload: LoginRequest = { username: 'conseiller1', password: 'password123' };
    const expectedResponse: LoginResponse = { token: 'jwt-token', username: 'conseiller1' };

    service.login(payload).subscribe();

    const req = httpMock.expectOne('/api/auth/login');
    req.flush(expectedResponse);

    expect(authStore.isAuthenticated()).toBeTrue();
    expect(authStore.token()).toBe('jwt-token');
    expect(authStore.username()).toBe('conseiller1');
  });
});

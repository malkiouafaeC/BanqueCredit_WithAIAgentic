import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthStore } from '../stores/auth.store';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authStore: AuthStore;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authStore = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should add Authorization header when token exists', () => {
    authStore.setSession({ token: 'jwt-token', username: 'conseiller1' });

    httpClient.get('/api/clients').subscribe();

    const request = httpMock.expectOne('/api/clients');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('should not add Authorization header when token is missing', () => {
    httpClient.get('/api/clients').subscribe();

    const request = httpMock.expectOne('/api/clients');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush([]);
  });

  it('should clear session on 401 response', () => {
    authStore.setSession({ token: 'jwt-token', username: 'conseiller1' });

    let responseStatus = 0;
    httpClient.get('/api/clients').subscribe({
      next: () => {
        fail('Expected 401 error');
      },
      error: (error) => {
        responseStatus = error.status;
      }
    });

    const request = httpMock.expectOne('/api/clients');
    request.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(responseStatus).toBe(401);
    expect(authStore.isAuthenticated()).toBeFalse();
    expect(sessionStorage.getItem('mini-banque.auth.session')).toBeNull();
  });
});

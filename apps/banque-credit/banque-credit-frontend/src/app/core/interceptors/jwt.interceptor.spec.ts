import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../../features/auth/auth.service';

describe('jwtInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting()
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('adds Authorization header when a token is present', () => {
    authServiceMock.getToken.and.returnValue('abc123');
    httpClient.get('/api/v1/dashboard').subscribe();
    const req = httpMock.expectOne('/api/v1/dashboard');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc123');
    req.flush({});
  });

  it('does not add Authorization header when no token', () => {
    authServiceMock.getToken.and.returnValue(null);
    httpClient.get('/api/v1/dashboard').subscribe();
    const req = httpMock.expectOne('/api/v1/dashboard');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});


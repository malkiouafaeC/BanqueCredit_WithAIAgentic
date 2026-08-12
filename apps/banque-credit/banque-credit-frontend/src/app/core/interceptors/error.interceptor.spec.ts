import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../../features/auth/auth.service';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting()
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  afterEach(() => httpMock.verify());

  it('logs out and redirects to /login on 401', () => {
    httpClient.get('/api/v1/clients').subscribe({ error: () => {} });
    const req = httpMock.expectOne('/api/v1/clients');
    req.flush({ message: 'unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('forwards non-401 errors without logging out', () => {
    let received: any;
    httpClient.get('/api/v1/clients').subscribe({ error: (err) => (received = err) });
    const req = httpMock.expectOne('/api/v1/clients');
    req.flush({ code: 'RESSOURCE_INTROUVABLE' }, { status: 404, statusText: 'Not Found' });

    expect(authServiceMock.logout).not.toHaveBeenCalled();
    expect(received.status).toBe(404);
  });
});


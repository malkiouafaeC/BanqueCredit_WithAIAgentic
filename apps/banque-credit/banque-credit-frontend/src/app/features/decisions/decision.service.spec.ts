import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DecisionService } from './decision.service';
import { environment } from '../../../environments/environment';

describe('DecisionService', () => {
  let service: DecisionService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/demandes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DecisionService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DecisionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs analyser (AC-012-1)', () => {
    service.analyser(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/analyser`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('POSTs accepter (AC-013-1)', () => {
    service.accepter(1).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/accepter`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('POSTs refuser with the commentaire (AC-014-1)', () => {
    service.refuser(1, 'Risque trop élevé').subscribe();
    const req = httpMock.expectOne(`${baseUrl}/1/refuser`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ commentaire: 'Risque trop élevé' });
    req.flush({});
  });
});


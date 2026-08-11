import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DemandeCreditService } from './demande-credit.service';
import { environment } from '../../../environments/environment';

describe('DemandeCreditService', () => {
  let service: DemandeCreditService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/demandes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DemandeCreditService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DemandeCreditService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs a new demande (AC-007-1)', () => {
    service.create({ clientId: 1, montantDemande: 1001, dureeMois: 12, tauxFictif: 2 }).subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('POSTs simuler and returns the result (AC-009-1)', () => {
    service.simuler(7).subscribe((res) => expect(res.scoreSimplifie).toBe('EXCELLENT' as any));
    const req = httpMock.expectOne(`${baseUrl}/7/simuler`);
    expect(req.request.method).toBe('POST');
    req.flush({ mensualiteEstimee: 100, tauxEndettement: 10, scoreSimplifie: 'EXCELLENT' });
  });

  it('POSTs soumettre (AC-010-1)', () => {
    service.soumettre(7).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/7/soumettre`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('POSTs annuler (AC-011-1)', () => {
    service.annuler(7).subscribe();
    const req = httpMock.expectOne(`${baseUrl}/7/annuler`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('GETs the list with a statut filter only (QA-TASK-106)', () => {
    service.list({ statut: 'SOUMISE' as any }).subscribe();
    const req = httpMock.expectOne((r) => r.url === baseUrl && r.params.get('statut') === 'SOUMISE' && !r.params.has('clientId'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs the list with a clientId filter only (QA-TASK-106)', () => {
    service.list({ clientId: 42 }).subscribe();
    const req = httpMock.expectOne((r) => r.url === baseUrl && r.params.get('clientId') === '42' && !r.params.has('statut'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs the list without any filter (QA-TASK-106)', () => {
    service.list().subscribe();
    const req = httpMock.expectOne((r) => r.url === baseUrl && !r.params.has('statut') && !r.params.has('clientId'));
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});


import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { environment } from '../../../environments/environment';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DashboardService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the dashboard synthesis (AC-017-1)', () => {
    service.getSynthese().subscribe((dash) => expect(dash.nbSoumises).toBe(3));
    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard`);
    expect(req.request.method).toBe('GET');
    req.flush({ nbSoumises: 3, nbEnAnalyse: 1, nbAcceptees: 2, nbRefusees: 0, montantTotalDemande: 10000, tauxMoyenEndettement: 25 });
  });
});


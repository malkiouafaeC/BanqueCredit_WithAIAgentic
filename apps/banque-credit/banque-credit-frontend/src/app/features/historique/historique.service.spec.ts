import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HistoriqueService } from './historique.service';
import { environment } from '../../../environments/environment';

describe('HistoriqueService', () => {
  let service: HistoriqueService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HistoriqueService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(HistoriqueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the transition history for a demande in chronological order (AC-016-1)', () => {
    service.listerParDemande(3).subscribe((entries) => expect(entries.length).toBe(3));
    const req = httpMock.expectOne(`${environment.apiUrl}/demandes/3/historique`);
    expect(req.request.method).toBe('GET');
    req.flush([{}, {}, {}]);
  });
});


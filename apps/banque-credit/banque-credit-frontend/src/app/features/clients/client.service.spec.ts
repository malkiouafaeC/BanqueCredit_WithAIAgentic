import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClientService } from './client.service';
import { environment } from '../../../environments/environment';
import { Statut } from '../demandes/statut.enum';

describe('ClientService', () => {
  let service: ClientService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/clients`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ClientService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('POSTs a new client (AC-003-1)', () => {
    service.create({ nom: 'Dupont', revenuMensuel: 2000, chargesMensuelles: 300 }).subscribe();
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1 });
  });

  it('GETs the client list (AC-005-1/2)', () => {
    service.list().subscribe((clients) => expect(clients).toEqual([]));
    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('GETs a client detail with its demandes (AC-006-1/2)', () => {
    service.getById(5).subscribe((detail) => expect(detail.demandes.length).toBe(1));
    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('GET');
    req.flush({
      id: 5,
      nom: 'Dupont',
      demandes: [{ id: 1, montantDemande: 2000, dureeMois: 12, statut: Statut.BROUILLON }]
    });
  });
});


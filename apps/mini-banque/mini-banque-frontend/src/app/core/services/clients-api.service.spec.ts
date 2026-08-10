import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';

import { Client, CreateClientRequest } from '../models/client.model';
import { ClientsApiService } from './clients-api.service';

describe('ClientsApiService', () => {
  let service: ClientsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ClientsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should list clients from /api/clients', () => {
    const expectedClients: Client[] = [
      { id: 1, nom: 'Client A', email: 'a@bank.test' },
      { id: 2, nom: 'Client B', email: 'b@bank.test' }
    ];

    service.listClients().subscribe((clients) => {
      expect(clients).toEqual(expectedClients);
    });

    const req = httpMock.expectOne('/api/clients');
    expect(req.request.method).toBe('GET');

    req.flush(expectedClients);
  });

  it('should create a client via /api/clients/new', () => {
    const payload: CreateClientRequest = { nom: 'Client C', email: 'c@bank.test' };
    const expectedClient: Client = { id: 3, nom: 'Client C', email: 'c@bank.test' };

    service.createClient(payload).subscribe((client) => {
      expect(client).toEqual(expectedClient);
    });

    const req = httpMock.expectOne('/api/clients/new');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);

    req.flush(expectedClient);
  });
});

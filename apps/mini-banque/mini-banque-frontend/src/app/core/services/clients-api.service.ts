import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Client, CreateClientRequest } from '../models/client.model';

@Injectable({
  providedIn: 'root'
})
export class ClientsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/clients';

  listClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this.baseUrl);
  }

  createClient(payload: CreateClientRequest): Observable<Client> {
    return this.http.post<Client>(`${this.baseUrl}/new`, payload);
  }
}

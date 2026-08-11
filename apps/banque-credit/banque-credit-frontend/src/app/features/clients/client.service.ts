import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Client, ClientCreate, ClientDetail } from './client.model';

/** HTTP access for the Clients domain (spec-architecture-banque-credit.md §7.2). */
@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly baseUrl = `${environment.apiUrl}/clients`;

  constructor(private readonly http: HttpClient) {}

  create(payload: ClientCreate): Observable<Client> {
    return this.http.post<Client>(this.baseUrl, payload);
  }

  list(): Observable<Client[]> {
    return this.http.get<Client[]>(this.baseUrl);
  }

  getById(id: number): Observable<ClientDetail> {
    return this.http.get<ClientDetail>(`${this.baseUrl}/${id}`);
  }
}


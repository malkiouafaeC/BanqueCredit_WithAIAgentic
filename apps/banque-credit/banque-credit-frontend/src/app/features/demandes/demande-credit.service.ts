import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DemandeCredit, DemandeCreditCreate, DemandeCreditSummary } from './demande-credit.model';
import { SimulationResult } from './simulation-result.model';
import { Statut } from './statut.enum';

/** HTTP access for the Demandes de crédit domain (spec-architecture-banque-credit.md §7.3, §7.4). */
@Injectable({ providedIn: 'root' })
export class DemandeCreditService {
  private readonly baseUrl = `${environment.apiUrl}/demandes`;

  constructor(private readonly http: HttpClient) {}

  create(payload: DemandeCreditCreate): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(this.baseUrl, payload);
  }

  list(filters?: { statut?: Statut; clientId?: number }): Observable<DemandeCreditSummary[]> {
    let params: Record<string, string> = {};
    if (filters?.statut) params['statut'] = filters.statut;
    if (filters?.clientId) params['clientId'] = String(filters.clientId);
    return this.http.get<DemandeCreditSummary[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<DemandeCredit> {
    return this.http.get<DemandeCredit>(`${this.baseUrl}/${id}`);
  }

  simuler(id: number): Observable<SimulationResult> {
    return this.http.post<SimulationResult>(`${this.baseUrl}/${id}/simuler`, {});
  }

  soumettre(id: number): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(`${this.baseUrl}/${id}/soumettre`, {});
  }

  annuler(id: number): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(`${this.baseUrl}/${id}/annuler`, {});
  }
}


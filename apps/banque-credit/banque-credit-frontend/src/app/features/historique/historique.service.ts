import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HistoriqueDecision } from './historique-decision.model';

/** HTTP access for the Historique domain (spec-architecture-banque-credit.md §7.5). */
@Injectable({ providedIn: 'root' })
export class HistoriqueService {
  constructor(private readonly http: HttpClient) {}

  listerParDemande(demandeId: number): Observable<HistoriqueDecision[]> {
    return this.http.get<HistoriqueDecision[]>(`${environment.apiUrl}/demandes/${demandeId}/historique`);
  }
}


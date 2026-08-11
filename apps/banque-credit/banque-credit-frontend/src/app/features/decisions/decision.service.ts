import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DemandeCredit } from '../demandes/demande-credit.model';

/** HTTP access for the Décisions domain, reserved to RESPONSABLE_CREDIT (spec-architecture §7.4). */
@Injectable({ providedIn: 'root' })
export class DecisionService {
  private readonly baseUrl = `${environment.apiUrl}/demandes`;

  constructor(private readonly http: HttpClient) {}

  analyser(id: number): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(`${this.baseUrl}/${id}/analyser`, {});
  }

  accepter(id: number): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(`${this.baseUrl}/${id}/accepter`, {});
  }

  refuser(id: number, commentaire: string): Observable<DemandeCredit> {
    return this.http.post<DemandeCredit>(`${this.baseUrl}/${id}/refuser`, { commentaire });
  }
}


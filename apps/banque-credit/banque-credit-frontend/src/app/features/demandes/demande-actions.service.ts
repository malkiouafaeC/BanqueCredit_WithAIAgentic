import { Injectable } from '@angular/core';
import { Statut } from './statut.enum';
import { Role } from '../auth/role.enum';
import { DemandeCredit } from './demande-credit.model';

/**
 * Centralizes the statut/role → allowed-action visibility rules (RG-BANK-05) so both
 * demande-detail (Conseiller actions) and decision (Responsable crédit actions) pages
 * stay consistent (mitigates RISK-CREDIT-003: duplicated/divergent visibility logic).
 */
@Injectable({ providedIn: 'root' })
export class DemandeActionsService {
  canSoumettre(demande: DemandeCredit, role: Role | null): boolean {
    return role === Role.CONSEILLER && demande.statut === Statut.BROUILLON;
  }

  canAnnuler(demande: DemandeCredit, role: Role | null): boolean {
    return (
      role === Role.CONSEILLER &&
      (demande.statut === Statut.BROUILLON || demande.statut === Statut.SOUMISE)
    );
  }

  canAnalyser(demande: DemandeCredit, role: Role | null): boolean {
    return role === Role.RESPONSABLE_CREDIT && demande.statut === Statut.SOUMISE;
  }

  canAccepter(demande: DemandeCredit, role: Role | null): boolean {
    return role === Role.RESPONSABLE_CREDIT && demande.statut === Statut.EN_ANALYSE;
  }

  canRefuser(demande: DemandeCredit, role: Role | null): boolean {
    return role === Role.RESPONSABLE_CREDIT && demande.statut === Statut.EN_ANALYSE;
  }
}


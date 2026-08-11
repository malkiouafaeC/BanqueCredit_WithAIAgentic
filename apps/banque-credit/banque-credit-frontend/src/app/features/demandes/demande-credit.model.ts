import { Statut } from './statut.enum';
import { ScoreSimplifie } from './score-simplifie.enum';

/** Request body for POST /demandes (RG-BANK-02, RG-BANK-03). */
export interface DemandeCreditCreate {
  clientId: number;
  montantDemande: number;
  dureeMois: number;
  tauxFictif: number;
}

/** Full DemandeCredit representation, incl. recalculated simulation (DEC-ENT-002). */
export interface DemandeCredit {
  id: number;
  clientId: number;
  clientNom: string;
  montantDemande: number;
  dureeMois: number;
  tauxFictif: number;
  statut: Statut;
  mensualiteEstimee: number | null;
  tauxEndettement: number | null;
  scoreSimplifie: ScoreSimplifie | null;
  commentaireDecision: string | null;
  dateSoumission: string | null;
  dateDecision: string | null;
}

/** Lightweight summary used in lists (client detail, demandes list). */
export interface DemandeCreditSummary {
  id: number;
  montantDemande: number;
  dureeMois: number;
  statut: Statut;
}


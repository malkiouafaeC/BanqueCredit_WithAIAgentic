import { Statut } from '../demandes/statut.enum';

/** Historique de transition d'une demande (US-016, spec-architecture-banque-credit.md §7.5). */
export interface HistoriqueDecision {
  id: number;
  ancienStatut: Statut | null;
  nouveauStatut: Statut;
  commentaire: string | null;
  tauxEndettementSnapshot: number | null;
  auteur: string;
  date: string;
}


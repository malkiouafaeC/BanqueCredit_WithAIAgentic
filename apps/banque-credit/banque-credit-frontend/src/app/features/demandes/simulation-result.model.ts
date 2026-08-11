import { ScoreSimplifie } from './score-simplifie.enum';

/** Response body for POST /demandes/{id}/simuler (RG-BANK-04, RG-BANK-08). */
export interface SimulationResult {
  mensualiteEstimee: number;
  tauxEndettement: number;
  scoreSimplifie: ScoreSimplifie;
}


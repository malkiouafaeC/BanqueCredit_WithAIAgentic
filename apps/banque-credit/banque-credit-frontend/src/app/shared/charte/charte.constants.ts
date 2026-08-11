/**
 * Centralized visual charter mapping (spec-feature-banque-credit.md §5.8):
 * bleu = neutre/info, vert = succès/ACCEPTEE/EXCELLENT-BON, rouge = échec/REFUSEE,
 * orange = avertissement/EN_ANALYSE/MOYEN-FAIBLE.
 * Every mapping carries both a CSS class/color AND a textual label so status/score
 * is never conveyed by color alone (accessibility baseline).
 */
import { Statut } from '../../features/demandes/statut.enum';
import { ScoreSimplifie } from '../../features/demandes/score-simplifie.enum';

export interface CharteEntry {
  label: string;
  cssClass: string;
}

export const STATUT_CHARTE: Record<Statut, CharteEntry> = {
  [Statut.BROUILLON]: { label: 'Brouillon', cssClass: 'charte-neutral' },
  [Statut.SOUMISE]: { label: 'Soumise', cssClass: 'charte-info' },
  [Statut.EN_ANALYSE]: { label: 'En analyse', cssClass: 'charte-warning' },
  [Statut.ACCEPTEE]: { label: 'Acceptée', cssClass: 'charte-success' },
  [Statut.REFUSEE]: { label: 'Refusée', cssClass: 'charte-danger' },
  [Statut.ANNULEE]: { label: 'Annulée', cssClass: 'charte-neutral' }
};

export const SCORE_CHARTE: Record<ScoreSimplifie, CharteEntry> = {
  [ScoreSimplifie.EXCELLENT]: { label: 'Excellent', cssClass: 'charte-success' },
  [ScoreSimplifie.BON]: { label: 'Bon', cssClass: 'charte-success' },
  [ScoreSimplifie.MOYEN]: { label: 'Moyen', cssClass: 'charte-warning' },
  [ScoreSimplifie.FAIBLE]: { label: 'Faible', cssClass: 'charte-danger' }
};

/** Scores requiring a pedagogical warning callout (AC-009-6). */
export const SCORES_AVERTISSEMENT: ScoreSimplifie[] = [ScoreSimplifie.MOYEN, ScoreSimplifie.FAIBLE];


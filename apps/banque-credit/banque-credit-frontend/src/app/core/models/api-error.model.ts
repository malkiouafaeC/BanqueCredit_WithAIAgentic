/** Standardized backend error body (spec-architecture-banque-credit.md DEC-005). */
export interface ApiErrorResponse {
  code:
    | 'CHAMP_OBLIGATOIRE'
    | 'VALEUR_HORS_BORNES'
    | 'TRANSITION_INVALIDE'
    | 'ROLE_NON_AUTORISE'
    | 'COMMENTAIRE_OBLIGATOIRE'
    | 'ELIGIBILITE_NON_RESPECTEE'
    | 'RESSOURCE_INTROUVABLE'
    | string;
  message: string;
  champ: string | null;
  timestamp: string;
  /** Real backend shape (see ErrorResponseDto.ChampErreur on the backend): a JSON array of { champ, message }. */
  erreurs?: { champ: string; message: string }[] | null;
}


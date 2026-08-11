/** Synthèse agence (US-017, spec-architecture §7.6, Q4: agrégats hors BROUILLON/ANNULEE). */
export interface Dashboard {
  nbSoumises: number;
  nbEnAnalyse: number;
  nbAcceptees: number;
  nbRefusees: number;
  montantTotalDemande: number;
  tauxMoyenEndettement: number;
}


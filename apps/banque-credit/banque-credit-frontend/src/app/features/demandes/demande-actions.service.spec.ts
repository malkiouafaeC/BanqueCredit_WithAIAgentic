import { DemandeActionsService } from './demande-actions.service';
import { Statut } from './statut.enum';
import { Role } from '../auth/role.enum';
import { DemandeCredit } from './demande-credit.model';

function demande(statut: Statut): DemandeCredit {
  return {
    id: 1,
    clientId: 1,
    clientNom: 'Dupont',
    montantDemande: 2000,
    dureeMois: 12,
    tauxFictif: 2,
    statut,
    mensualiteEstimee: null,
    tauxEndettement: null,
    scoreSimplifie: null,
    commentaireDecision: null,
    dateSoumission: null,
    dateDecision: null
  };
}

describe('DemandeActionsService', () => {
  const service = new DemandeActionsService();

  it('allows CONSEILLER to soumettre only from BROUILLON (AC-010-1/2)', () => {
    expect(service.canSoumettre(demande(Statut.BROUILLON), Role.CONSEILLER)).toBeTrue();
    expect(service.canSoumettre(demande(Statut.SOUMISE), Role.CONSEILLER)).toBeFalse();
    expect(service.canSoumettre(demande(Statut.BROUILLON), Role.RESPONSABLE_CREDIT)).toBeFalse();
  });

  it('allows CONSEILLER to annuler from BROUILLON or SOUMISE only (AC-011-1..4)', () => {
    expect(service.canAnnuler(demande(Statut.BROUILLON), Role.CONSEILLER)).toBeTrue();
    expect(service.canAnnuler(demande(Statut.SOUMISE), Role.CONSEILLER)).toBeTrue();
    expect(service.canAnnuler(demande(Statut.EN_ANALYSE), Role.CONSEILLER)).toBeFalse();
    expect(service.canAnnuler(demande(Statut.SOUMISE), Role.RESPONSABLE_CREDIT)).toBeFalse();
  });

  it('allows RESPONSABLE_CREDIT to analyser only from SOUMISE (AC-012-1..3)', () => {
    expect(service.canAnalyser(demande(Statut.SOUMISE), Role.RESPONSABLE_CREDIT)).toBeTrue();
    expect(service.canAnalyser(demande(Statut.BROUILLON), Role.RESPONSABLE_CREDIT)).toBeFalse();
    expect(service.canAnalyser(demande(Statut.SOUMISE), Role.CONSEILLER)).toBeFalse();
  });

  it('allows RESPONSABLE_CREDIT to accepter/refuser only from EN_ANALYSE (AC-013-5, AC-015-3)', () => {
    expect(service.canAccepter(demande(Statut.EN_ANALYSE), Role.RESPONSABLE_CREDIT)).toBeTrue();
    expect(service.canAccepter(demande(Statut.SOUMISE), Role.RESPONSABLE_CREDIT)).toBeFalse();
    expect(service.canRefuser(demande(Statut.EN_ANALYSE), Role.RESPONSABLE_CREDIT)).toBeTrue();
    expect(service.canRefuser(demande(Statut.ACCEPTEE), Role.RESPONSABLE_CREDIT)).toBeFalse();
  });
});


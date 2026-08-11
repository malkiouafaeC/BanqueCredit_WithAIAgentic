import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { DecisionPage } from './decision.page';
import { DemandeCreditService } from '../demandes/demande-credit.service';
import { DecisionService } from './decision.service';
import { AuthService } from '../auth/auth.service';
import { Role } from '../auth/role.enum';
import { Statut } from '../demandes/statut.enum';

function baseDemande(statut: Statut) {
  return {
    id: 1,
    clientId: 1,
    clientNom: 'Dupont',
    montantDemande: 2000,
    dureeMois: 12,
    tauxFictif: 2,
    statut,
    mensualiteEstimee: 100,
    tauxEndettement: 40,
    scoreSimplifie: null,
    commentaireDecision: null,
    dateSoumission: null,
    dateDecision: null
  };
}

describe('DecisionPage', () => {
  let fixture: ComponentFixture<DecisionPage>;
  let demandeServiceMock: jasmine.SpyObj<DemandeCreditService>;
  let decisionServiceMock: jasmine.SpyObj<DecisionService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    demandeServiceMock = jasmine.createSpyObj<DemandeCreditService>('DemandeCreditService', ['getById']);
    decisionServiceMock = jasmine.createSpyObj<DecisionService>('DecisionService', ['analyser', 'accepter', 'refuser']);
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole']);
    (authServiceMock as any).currentRole = () => Role.RESPONSABLE_CREDIT;

    await TestBed.configureTestingModule({
      imports: [DecisionPage],
      providers: [
        provideRouter([]),
        { provide: DemandeCreditService, useValue: demandeServiceMock },
        { provide: DecisionService, useValue: decisionServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DecisionPage);
  });

  it('blocks refus submission without a commentaire (RG-BANK-07, AC-015-1)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    fixture.detectChanges();
    fixture.componentInstance.toggleRefusForm();
    fixture.componentInstance.refuser();
    expect(decisionServiceMock.refuser).not.toHaveBeenCalled();
  });

  it('blocks refus submission with a whitespace-only commentaire (AC-015-2)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    fixture.detectChanges();
    fixture.componentInstance.toggleRefusForm();
    fixture.componentInstance.refusForm.setValue({ commentaire: '   ' });
    fixture.componentInstance.refuser();
    expect(decisionServiceMock.refuser).not.toHaveBeenCalled();
  });

  it('submits refus with a valid commentaire', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.refuser.and.returnValue(of(baseDemande(Statut.REFUSEE) as any));
    fixture.detectChanges();
    fixture.componentInstance.toggleRefusForm();
    fixture.componentInstance.refusForm.setValue({ commentaire: 'Endettement trop élevé' });
    fixture.componentInstance.refuser();
    expect(decisionServiceMock.refuser).toHaveBeenCalledWith(1, 'Endettement trop élevé');
  });

  it('shows an eligibility error on 422 when accepting (AC-013-2..4)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.accepter.and.returnValue(
      throwError(
        () => new HttpErrorResponse({ status: 422, error: { message: "Acceptation impossible : taux d'endettement supérieur à 35%" } })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.accepter();
    expect(fixture.componentInstance.actionError()).toContain('Acceptation impossible');
  });

  it('shows a default eligibility message on 422 when body has no message (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.accepter.and.returnValue(throwError(() => new HttpErrorResponse({ status: 422 })));
    fixture.detectChanges();
    fixture.componentInstance.accepter();
    expect(fixture.componentInstance.actionError()).toContain("Éligibilité non respectée");
  });

  it('shows a transition invalide error on 409 when analysing (AC-012-2, QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.analyser.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'Transition invalide.' } }))
    );
    fixture.detectChanges();
    fixture.componentInstance.analyser();
    expect(fixture.componentInstance.actionError()).toBe('Transition invalide.');
  });

  it('shows a default transition invalide message on 409 when body has no message (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.analyser.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    fixture.detectChanges();
    fixture.componentInstance.analyser();
    expect(fixture.componentInstance.actionError()).toBe('Transition invalide.');
  });

  it('shows a commentaire obligatoire error on 400 when refusing (AC-015-1, QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.refuser.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'Un commentaire est obligatoire pour refuser une demande' } }))
    );
    fixture.detectChanges();
    fixture.componentInstance.toggleRefusForm();
    fixture.componentInstance.refusForm.setValue({ commentaire: 'motif' });
    fixture.componentInstance.refuser();
    expect(fixture.componentInstance.actionError()).toContain('commentaire est obligatoire');
  });

  it('shows a role non autorise error on 403 (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.accepter.and.returnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    fixture.detectChanges();
    fixture.componentInstance.accepter();
    expect(fixture.componentInstance.actionError()).toContain("n'êtes pas autorisé");
  });

  it('shows a generic error message for unmapped HTTP status codes (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    decisionServiceMock.accepter.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.detectChanges();
    fixture.componentInstance.accepter();
    expect(fixture.componentInstance.actionError()).toContain('échoué');
  });

  it('toggles the refus form visibility (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.EN_ANALYSE) as any));
    fixture.detectChanges();
    expect(fixture.componentInstance.showRefusForm()).toBeFalse();
    fixture.componentInstance.toggleRefusForm();
    expect(fixture.componentInstance.showRefusForm()).toBeTrue();
    fixture.componentInstance.toggleRefusForm();
    expect(fixture.componentInstance.showRefusForm()).toBeFalse();
  });

  it('sets the state to error when loading the demande fails (QA-TASK-102)', () => {
    demandeServiceMock.getById.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


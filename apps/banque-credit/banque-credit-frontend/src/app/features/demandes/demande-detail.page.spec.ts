import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeDetailPage } from './demande-detail.page';
import { DemandeCreditService } from './demande-credit.service';
import { HistoriqueService } from '../historique/historique.service';
import { AuthService } from '../auth/auth.service';
import { Role } from '../auth/role.enum';
import { Statut } from './statut.enum';
import { ScoreSimplifie } from './score-simplifie.enum';

function baseDemande(statut: Statut) {
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

describe('DemandeDetailPage', () => {
  let fixture: ComponentFixture<DemandeDetailPage>;
  let demandeServiceMock: jasmine.SpyObj<DemandeCreditService>;
  let historiqueServiceMock: jasmine.SpyObj<HistoriqueService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    demandeServiceMock = jasmine.createSpyObj<DemandeCreditService>('DemandeCreditService', [
      'getById',
      'simuler',
      'soumettre',
      'annuler'
    ]);
    historiqueServiceMock = jasmine.createSpyObj<HistoriqueService>('HistoriqueService', ['listerParDemande']);
    historiqueServiceMock.listerParDemande.and.returnValue(of([]));
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole']);
    (authServiceMock as any).currentRole = () => Role.CONSEILLER;
    authServiceMock.hasRole.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [DemandeDetailPage],
      providers: [
        provideRouter([]),
        { provide: DemandeCreditService, useValue: demandeServiceMock },
        { provide: HistoriqueService, useValue: historiqueServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DemandeDetailPage);
  });

  it('shows the Soumettre button for CONSEILLER when statut=BROUILLON', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    fixture.detectChanges();
    const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('button'));
    expect(buttons.some((b) => b.textContent?.includes('Soumettre'))).toBeTrue();
  });

  it('hides the Soumettre button when statut is not BROUILLON', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.SOUMISE) as any));
    fixture.detectChanges();
    const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('button'));
    expect(buttons.some((b) => b.textContent?.includes('Soumettre'))).toBeFalse();
  });

  it('displays the score badge and pedagogical warning after simulation (AC-009-6)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.simuler.and.returnValue(
      of({ mensualiteEstimee: 500, tauxEndettement: 60, scoreSimplifie: ScoreSimplifie.FAIBLE })
    );
    fixture.detectChanges();
    fixture.componentInstance.simuler();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
  });

  it('shows a transition-invalid error message on 409 (AC-010-2)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.soumettre.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'Transition invalide' } }))
    );
    fixture.detectChanges();
    fixture.componentInstance.soumettre();
    expect(fixture.componentInstance.actionError()).toContain('Transition invalide');
  });

  it('shows a role-not-authorized error message on 403 (AC-011-4)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.annuler.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 403 }))
    );
    fixture.detectChanges();
    fixture.componentInstance.annuler();
    expect(fixture.componentInstance.actionError()).toContain('autorisé');
  });

  it('sets state to error when loading the demande fails (QA-TASK-107)', () => {
    demandeServiceMock.getById.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });

  it('resets simulating flag without setting a result when simulation fails (QA-TASK-107)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.simuler.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.detectChanges();
    fixture.componentInstance.simuler();
    expect(fixture.componentInstance.simulating()).toBeFalse();
    expect(fixture.componentInstance.simulation()).toBeNull();
  });

  it('shows a default transition-invalid message on 409 when body has no message (QA-TASK-107)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.soumettre.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    fixture.detectChanges();
    fixture.componentInstance.soumettre();
    expect(fixture.componentInstance.actionError()).toBe('Transition invalide.');
  });

  it('shows a generic failure message for unmapped HTTP status codes (QA-TASK-107)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.annuler.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.detectChanges();
    fixture.componentInstance.annuler();
    expect(fixture.componentInstance.actionError()).toContain('échoué');
  });

  it('updates the demande and clears pending state on a successful action (QA-TASK-107)', () => {
    demandeServiceMock.getById.and.returnValue(of(baseDemande(Statut.BROUILLON) as any));
    demandeServiceMock.soumettre.and.returnValue(of(baseDemande(Statut.SOUMISE) as any));
    fixture.detectChanges();
    fixture.componentInstance.soumettre();
    expect(fixture.componentInstance.actionPending()).toBeFalse();
    expect(fixture.componentInstance.demande()?.statut).toBe(Statut.SOUMISE);
  });
});


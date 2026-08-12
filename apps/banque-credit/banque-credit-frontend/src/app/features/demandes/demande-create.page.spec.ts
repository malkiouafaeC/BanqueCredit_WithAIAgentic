import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeCreatePage } from './demande-create.page';
import { DemandeCreditService } from './demande-credit.service';
import { ClientService } from '../clients/client.service';

describe('DemandeCreatePage', () => {
  let fixture: ComponentFixture<DemandeCreatePage>;
  let demandeServiceMock: jasmine.SpyObj<DemandeCreditService>;
  let clientServiceMock: jasmine.SpyObj<ClientService>;
  let router: Router;

  beforeEach(async () => {
    demandeServiceMock = jasmine.createSpyObj<DemandeCreditService>('DemandeCreditService', ['create']);
    clientServiceMock = jasmine.createSpyObj<ClientService>('ClientService', ['list']);
    clientServiceMock.list.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [DemandeCreatePage],
      providers: [
        provideRouter([]),
        { provide: DemandeCreditService, useValue: demandeServiceMock },
        { provide: ClientService, useValue: clientServiceMock },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DemandeCreatePage);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  it('rejects missing client (AC-007-3)', () => {
    fixture.detectChanges();
    fixture.componentInstance.submit();
    expect(demandeServiceMock.create).not.toHaveBeenCalled();
    expect(fixture.componentInstance.form.get('clientId')!.invalid).toBeTrue();
  });

  it('rejects montant = 1000 (borne exclue, AC-008-1/E1)', () => {
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 1000, dureeMois: 12, tauxFictif: 1 });
    expect(fixture.componentInstance.form.get('montantDemande')!.hasError('montantMin')).toBeTrue();
  });

  it('accepts montant = 100000 and duree = 84 (bornes hautes incluses, AC-007-2/E2/E6)', () => {
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 100000, dureeMois: 84, tauxFictif: 1 });
    expect(fixture.componentInstance.form.valid).toBeTrue();
  });

  it('rejects duree = 11 and 85 (E4/E7)', () => {
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 2000, dureeMois: 11, tauxFictif: 1 });
    expect(fixture.componentInstance.form.get('dureeMois')!.invalid).toBeTrue();
    fixture.componentInstance.form.patchValue({ dureeMois: 85 });
    expect(fixture.componentInstance.form.get('dureeMois')!.invalid).toBeTrue();
  });

  it('creates the demande and navigates to its detail on success', () => {
    demandeServiceMock.create.and.returnValue(of({ id: 9 } as any));
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 2000, dureeMois: 12, tauxFictif: 2 });
    fixture.componentInstance.submit();
    expect(router.navigate).toHaveBeenCalledWith(['/demandes', 9]);
  });

  it('shows a general error message on server failure', () => {
    demandeServiceMock.create.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'Le client est obligatoire' } }))
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 2000, dureeMois: 12, tauxFictif: 2 });
    fixture.componentInstance.submit();
    expect(fixture.componentInstance.generalError()).toContain('obligatoire');
  });

  it('maps multi-field 400 validation errors to per-field messages (RG-BANK-02/03)', () => {
    demandeServiceMock.create.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              erreurs: [
                { champ: 'montantDemande', message: 'Le montant doit être strictement supérieur à 1000' },
                { champ: 'dureeMois', message: 'La durée doit être comprise entre 12 et 84 mois' }
              ]
            }
          })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 2000, dureeMois: 12, tauxFictif: 2 });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.serverErrors()['montantDemande']).toContain('supérieur');
    expect(fixture.componentInstance.serverErrors()['dureeMois']).toContain('durée');
  });

  it('renders the mapped server errors in the template', () => {
    demandeServiceMock.create.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              erreurs: [{ champ: 'montantDemande', message: 'Le montant doit être strictement supérieur à 1000' }]
            }
          })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({ clientId: 1, montantDemande: 2000, dureeMois: 12, tauxFictif: 2 });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Le montant doit être strictement supérieur à 1000');
  });
});


import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DemandeListPage } from './demande-list.page';
import { DemandeCreditService } from './demande-credit.service';
import { AuthService } from '../auth/auth.service';
import { Statut } from './statut.enum';

describe('DemandeListPage', () => {
  let fixture: ComponentFixture<DemandeListPage>;
  let demandeServiceMock: jasmine.SpyObj<DemandeCreditService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    demandeServiceMock = jasmine.createSpyObj<DemandeCreditService>('DemandeCreditService', ['list']);
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole']);
    authServiceMock.hasRole.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [DemandeListPage],
      providers: [
        provideRouter([]),
        { provide: DemandeCreditService, useValue: demandeServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DemandeListPage);
  });

  it('shows an explicit empty state', () => {
    demandeServiceMock.list.and.returnValue(of([]));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucune demande');
  });

  it('lists demandes with statut badges', () => {
    demandeServiceMock.list.and.returnValue(of([{ id: 1, montantDemande: 2000, dureeMois: 12, statut: Statut.SOUMISE }]));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('shows an error state on failure', () => {
    demandeServiceMock.list.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


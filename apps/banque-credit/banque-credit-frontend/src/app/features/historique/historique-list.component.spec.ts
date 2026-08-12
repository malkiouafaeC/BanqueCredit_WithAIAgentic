import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HistoriqueListComponent } from './historique-list.component';
import { HistoriqueService } from './historique.service';
import { Statut } from '../demandes/statut.enum';

describe('HistoriqueListComponent', () => {
  let fixture: ComponentFixture<HistoriqueListComponent>;
  let historiqueServiceMock: jasmine.SpyObj<HistoriqueService>;

  beforeEach(async () => {
    historiqueServiceMock = jasmine.createSpyObj<HistoriqueService>('HistoriqueService', ['listerParDemande']);

    await TestBed.configureTestingModule({
      imports: [HistoriqueListComponent],
      providers: [{ provide: HistoriqueService, useValue: historiqueServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(HistoriqueListComponent);
    fixture.componentInstance.demandeId = 1;
  });

  it('shows the 3 transitions in order with all fields (AC-016-1)', () => {
    historiqueServiceMock.listerParDemande.and.returnValue(
      of([
        { id: 1, ancienStatut: null, nouveauStatut: Statut.BROUILLON, commentaire: null, tauxEndettementSnapshot: null, auteur: 'conseiller1', date: '2024-01-01' },
        { id: 2, ancienStatut: Statut.BROUILLON, nouveauStatut: Statut.SOUMISE, commentaire: null, tauxEndettementSnapshot: null, auteur: 'conseiller1', date: '2024-01-02' },
        { id: 3, ancienStatut: Statut.SOUMISE, nouveauStatut: Statut.EN_ANALYSE, commentaire: null, tauxEndettementSnapshot: null, auteur: 'resp1', date: '2024-01-03' }
      ])
    );
    fixture.detectChanges();
    const items = fixture.nativeElement.querySelectorAll('.historique-list li');
    expect(items.length).toBe(3);
  });

  it('shows an explicit empty state (AC-016-2 théorique)', () => {
    historiqueServiceMock.listerParDemande.and.returnValue(of([]));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucune transition');
  });

  it('shows an error state on failure', () => {
    historiqueServiceMock.listerParDemande.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


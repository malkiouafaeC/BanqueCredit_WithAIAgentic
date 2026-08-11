import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ClientDetailPage } from './client-detail.page';
import { ClientService } from './client.service';
import { Statut } from '../demandes/statut.enum';

describe('ClientDetailPage', () => {
  let fixture: ComponentFixture<ClientDetailPage>;
  let clientServiceMock: jasmine.SpyObj<ClientService>;

  beforeEach(async () => {
    clientServiceMock = jasmine.createSpyObj<ClientService>('ClientService', ['getById']);

    await TestBed.configureTestingModule({
      imports: [ClientDetailPage],
      providers: [
        provideRouter([]),
        { provide: ClientService, useValue: clientServiceMock },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '5' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientDetailPage);
  });

  it('shows an explicit empty state when the client has no demandes (AC-006-2)', () => {
    clientServiceMock.getById.and.returnValue(
      of({
        id: 5,
        nom: 'Dupont',
        email: null,
        revenuMensuel: 2000,
        chargesMensuelles: 200,
        situationProfessionnelle: null,
        createdAt: '2024-01-01T00:00:00Z',
        demandes: []
      })
    );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('aucune demande');
  });

  it('lists demandes with their statut badge (AC-006-1)', () => {
    clientServiceMock.getById.and.returnValue(
      of({
        id: 5,
        nom: 'Dupont',
        email: null,
        revenuMensuel: 2000,
        chargesMensuelles: 200,
        situationProfessionnelle: null,
        createdAt: '2024-01-01T00:00:00Z',
        demandes: [
          { id: 1, montantDemande: 2000, dureeMois: 12, statut: Statut.SOUMISE },
          { id: 2, montantDemande: 5000, dureeMois: 24, statut: Statut.ACCEPTEE }
        ]
      })
    );
    fixture.detectChanges();
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
  });

  it('shows an error state on failure', () => {
    clientServiceMock.getById.and.returnValue(throwError(() => new Error('404')));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


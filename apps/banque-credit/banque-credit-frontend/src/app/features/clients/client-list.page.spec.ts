import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ClientListPage } from './client-list.page';
import { ClientService } from './client.service';
import { AuthService } from '../auth/auth.service';

describe('ClientListPage', () => {
  let fixture: ComponentFixture<ClientListPage>;
  let clientServiceMock: jasmine.SpyObj<ClientService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    clientServiceMock = jasmine.createSpyObj<ClientService>('ClientService', ['list']);
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole']);
    authServiceMock.hasRole.and.returnValue(true);

    await TestBed.configureTestingModule({
      imports: [ClientListPage],
      providers: [
        provideRouter([]),
        { provide: ClientService, useValue: clientServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientListPage);
  });

  it('shows the loading state first', () => {
    clientServiceMock.list.and.returnValue(of([]));
    expect(fixture.componentInstance.state()).toBe('loading');
  });

  it('shows an explicit empty state (AC-005-2)', () => {
    clientServiceMock.list.and.returnValue(of([]));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('loaded');
    expect(fixture.nativeElement.textContent).toContain('Aucun client');
  });

  it('shows the client list (AC-005-1)', () => {
    clientServiceMock.list.and.returnValue(
      of([{ id: 1, nom: 'Dupont', email: null, revenuMensuel: 2000, chargesMensuelles: 200, situationProfessionnelle: null, createdAt: '' }])
    );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Dupont');
  });

  it('shows an error state on failure', () => {
    clientServiceMock.list.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


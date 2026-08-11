import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ClientCreatePage } from './client-create.page';
import { ClientService } from './client.service';

describe('ClientCreatePage', () => {
  let fixture: ComponentFixture<ClientCreatePage>;
  let clientServiceMock: jasmine.SpyObj<ClientService>;
  let router: Router;

  beforeEach(async () => {
    clientServiceMock = jasmine.createSpyObj<ClientService>('ClientService', ['create']);

    await TestBed.configureTestingModule({
      imports: [ClientCreatePage],
      providers: [provideRouter([]), { provide: ClientService, useValue: clientServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientCreatePage);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  it('blocks submission when required fields are missing (AC-004-1..3)', () => {
    fixture.detectChanges();
    fixture.componentInstance.submit();
    expect(clientServiceMock.create).not.toHaveBeenCalled();
    expect(fixture.componentInstance.form.get('nom')!.invalid).toBeTrue();
  });

  it('creates the client and navigates to its detail page on success (AC-003-1)', () => {
    clientServiceMock.create.and.returnValue(of({ id: 42 } as any));
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nom: 'Dupont',
      email: '',
      revenuMensuel: 2000,
      chargesMensuelles: 300,
      situationProfessionnelle: ''
    });
    fixture.componentInstance.submit();

    expect(clientServiceMock.create).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/clients', 42]);
  });

  it('maps multi-field 400 validation errors (AC-004-4)', () => {
    clientServiceMock.create.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              erreurs: [
                { champ: 'nom', message: 'Le nom est obligatoire' },
                { champ: 'revenuMensuel', message: 'Le revenu mensuel est obligatoire' }
              ]
            }
          })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nom: 'x',
      email: '',
      revenuMensuel: 1,
      chargesMensuelles: 0,
      situationProfessionnelle: ''
    });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.serverErrors()['nom']).toContain('obligatoire');
    expect(fixture.componentInstance.serverErrors()['revenuMensuel']).toContain('obligatoire');
  });

  it('renders the mapped server errors in the template (AC-004-4)', () => {
    clientServiceMock.create.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              erreurs: [
                { champ: 'nom', message: 'Le nom est obligatoire' },
                { champ: 'revenuMensuel', message: 'Le revenu mensuel est obligatoire' }
              ]
            }
          })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nom: 'x',
      email: '',
      revenuMensuel: 1,
      chargesMensuelles: 0,
      situationProfessionnelle: ''
    });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Le nom est obligatoire');
    expect(text).toContain('Le revenu mensuel est obligatoire');
  });

  it('maps a single-field 400 error (champ+message, no erreurs[] array) (QA-TASK-105)', () => {
    clientServiceMock.create.and.returnValue(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { champ: 'email', message: "Le format de l'email est invalide" } })
      )
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nom: 'Dupont',
      email: 'valide@example.com',
      revenuMensuel: 2000,
      chargesMensuelles: 300,
      situationProfessionnelle: ''
    });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.serverErrors()['email']).toContain("format de l'email");
  });

  it('shows a general error message when the 400 body has neither erreurs[] nor champ (QA-TASK-105)', () => {
    clientServiceMock.create.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 500, error: { message: 'Erreur serveur inattendue' } }))
    );
    fixture.detectChanges();
    fixture.componentInstance.form.setValue({
      nom: 'Dupont',
      email: '',
      revenuMensuel: 2000,
      chargesMensuelles: 300,
      situationProfessionnelle: ''
    });
    fixture.componentInstance.submit();

    expect(fixture.componentInstance.generalError()).toBe('Erreur serveur inattendue');
  });
});


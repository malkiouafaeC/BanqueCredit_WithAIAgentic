import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ClientsApiService } from '../../../core/services/clients-api.service';
import { ClientsPageComponent } from './clients-page.component';

describe('ClientsPageComponent', () => {
  let clientsApiService: jasmine.SpyObj<ClientsApiService>;
  let router: Router;

  beforeEach(async () => {
    clientsApiService = jasmine.createSpyObj<ClientsApiService>('ClientsApiService', ['listClients', 'createClient']);

    await TestBed.configureTestingModule({
      imports: [ClientsPageComponent],
      providers: [{ provide: ClientsApiService, useValue: clientsApiService }, provideRouter([])]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
  });

  it('should load and display clients on success', () => {
    clientsApiService.listClients.and.returnValue(
      of([
        { id: 1, nom: 'Client A', email: 'a@bank.test' },
        { id: 2, nom: 'Client B', email: 'b@bank.test' }
      ])
    );

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const titles = Array.from(fixture.nativeElement.querySelectorAll('.client-item h2')).map((node) =>
      (node as HTMLElement).textContent?.trim()
    );

    expect(clientsApiService.listClients).toHaveBeenCalled();
    expect(titles).toEqual(['Client A', 'Client B']);
    expect(fixture.nativeElement.querySelector('.loading')).toBeFalsy();
  });

  it('should display top Nouveau client button and show hint after click', () => {
    clientsApiService.listClients.and.returnValue(of([]));

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const newClientButton = Array.from(fixture.nativeElement.querySelectorAll('.header-actions button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Nouveau client')
    ) as HTMLButtonElement;

    expect(newClientButton).toBeTruthy();

    newClientButton.click();
    fixture.detectChanges();

    const formTitle = fixture.nativeElement.querySelector('.create-client-card h2') as HTMLElement;
    expect(formTitle.textContent).toContain('Nouveau client');
  });

  it('should create client and refresh list on success', () => {
    clientsApiService.listClients.and.returnValues(
      of([{ id: 1, nom: 'Client A', email: 'a@bank.test' }]),
      of([
        { id: 1, nom: 'Client A', email: 'a@bank.test' },
        { id: 2, nom: 'Client C', email: 'c@bank.test' }
      ])
    );
    clientsApiService.createClient.and.returnValue(of({ id: 2, nom: 'Client C', email: 'c@bank.test' }));

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const newClientButton = Array.from(fixture.nativeElement.querySelectorAll('.header-actions button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Nouveau client')
    ) as HTMLButtonElement;

    newClientButton.click();
    fixture.detectChanges();

    const nomInput = fixture.nativeElement.querySelector('input[formControlName="nom"]') as HTMLInputElement;
    nomInput.value = 'Client C';
    nomInput.dispatchEvent(new Event('input'));

    const emailInput = fixture.nativeElement.querySelector('input[formControlName="email"]') as HTMLInputElement;
    emailInput.value = 'c@bank.test';
    emailInput.dispatchEvent(new Event('input'));

    const submitButton = Array.from(fixture.nativeElement.querySelectorAll('.form-actions button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Creer client')
    ) as HTMLButtonElement;

    submitButton.click();
    fixture.detectChanges();

    expect(clientsApiService.createClient).toHaveBeenCalledWith({ nom: 'Client C', email: 'c@bank.test' });
    expect(clientsApiService.listClients).toHaveBeenCalledTimes(2);

    const titles = Array.from(fixture.nativeElement.querySelectorAll('.client-item h2')).map((node) =>
      (node as HTMLElement).textContent?.trim()
    );
    expect(titles).toEqual(['Client A', 'Client C']);
  });

  it('should display validation message when create returns CLIENT_VALIDATION_ERROR', () => {
    clientsApiService.listClients.and.returnValue(of([]));
    clientsApiService.createClient.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            statusText: 'Bad Request',
            error: { code: 'CLIENT_VALIDATION_ERROR', message: 'Donnees client invalides' }
          })
      )
    );

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const newClientButton = Array.from(fixture.nativeElement.querySelectorAll('.header-actions button')).find(
      (button) => (button as HTMLButtonElement).textContent?.includes('Nouveau client')
    ) as HTMLButtonElement;

    newClientButton.click();
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.createClientForm.setValue({ nom: 'Client', email: 'client@bank.test' });
    component.submitNewClient();
    fixture.detectChanges();

    const errorText = fixture.nativeElement.querySelector('.create-client-card .state.error') as HTMLElement;
    expect(clientsApiService.createClient).toHaveBeenCalledWith({ nom: 'Client', email: 'client@bank.test' });
    expect(errorText.textContent).toContain('Donnees client invalides');
    expect(clientsApiService.listClients).toHaveBeenCalledTimes(1);
  });

  it('should show reconnect action when API returns 401', () => {
    clientsApiService.listClients.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }))
    );

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const errorText = (fixture.nativeElement.querySelector('.error p') as HTMLElement).textContent;
    const reconnectButton = Array.from(fixture.nativeElement.querySelectorAll('.error-actions button')).find((button) =>
      (button as HTMLButtonElement).textContent?.includes('Se reconnecter')
    );

    expect(errorText).toContain('session a expire');
    expect(reconnectButton).toBeTruthy();
  });

  it('should show retry action when API returns non-401 error', () => {
    clientsApiService.listClients.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 500, statusText: 'Server Error' }))
    );

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const retryButton = Array.from(fixture.nativeElement.querySelectorAll('.error-actions button')).find((button) =>
      (button as HTMLButtonElement).textContent?.includes('Reessayer')
    );

    expect(retryButton).toBeTruthy();
  });

  it('should navigate to login when reconnect action is clicked', () => {
    clientsApiService.listClients.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, statusText: 'Unauthorized' }))
    );

    const fixture = TestBed.createComponent(ClientsPageComponent);
    fixture.detectChanges();

    const reconnectButton = Array.from(fixture.nativeElement.querySelectorAll('.error-actions button')).find((button) =>
      (button as HTMLButtonElement).textContent?.includes('Se reconnecter')
    ) as HTMLButtonElement;

    reconnectButton.click();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});

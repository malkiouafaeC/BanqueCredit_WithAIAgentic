import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { Client } from '../../../core/models/client.model';
import { ClientsApiService } from '../../../core/services/clients-api.service';

interface ApiErrorResponse {
  code?: string;
  message?: string;
}

@Component({
  selector: 'app-clients-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clients-page.component.html',
  styleUrl: './clients-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClientsPageComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly clientsApiService = inject(ClientsApiService);
  private readonly formBuilder = inject(FormBuilder);

  readonly clients = signal<Client[]>([]);
  readonly isLoading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly unauthorized = signal(false);
  readonly showCreateClientForm = signal(false);
  readonly createClientError = signal<string | null>(null);
  readonly isCreatingClient = signal(false);

  readonly createClientForm = this.formBuilder.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]]
  });

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.unauthorized.set(false);

    this.clientsApiService
      .listClients()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (clients) => {
          this.clients.set(clients);
        },
        error: (error: unknown) => {
          this.clients.set([]);
          if (error instanceof HttpErrorResponse && error.status === 401) {
            this.unauthorized.set(true);
            this.errorMessage.set('Votre session a expire. Veuillez vous reconnecter.');
            return;
          }
          this.errorMessage.set('Impossible de charger les clients. Veuillez reessayer.');
        }
      });
  }

  backToHome(): void {
    void this.router.navigateByUrl('/home');
  }

  goToLogin(): void {
    void this.router.navigateByUrl('/login');
  }

  startNewClient(): void {
    this.showCreateClientForm.set(true);
    this.createClientError.set(null);
  }

  cancelNewClient(): void {
    this.showCreateClientForm.set(false);
    this.createClientError.set(null);
    this.createClientForm.reset({ nom: '', email: '' });
  }

  submitNewClient(): void {
    if (this.createClientForm.invalid || this.isCreatingClient()) {
      this.createClientForm.markAllAsTouched();
      return;
    }

    this.createClientError.set(null);
    this.isCreatingClient.set(true);

    this.clientsApiService
      .createClient(this.createClientForm.getRawValue())
      .pipe(finalize(() => this.isCreatingClient.set(false)))
      .subscribe({
        next: () => {
          this.cancelNewClient();
          this.loadClients();
        },
        error: (error: unknown) => {
          if (error instanceof HttpErrorResponse && error.status === 400) {
            const payload = error.error as ApiErrorResponse | null;
            if (payload?.code === 'CLIENT_VALIDATION_ERROR') {
              this.createClientError.set(payload.message ?? 'Donnees client invalides.');
              return;
            }
            this.createClientError.set('Donnees client invalides.');
            return;
          }

          if (error instanceof HttpErrorResponse && error.status === 401) {
            this.createClientError.set('Session invalide. Veuillez vous reconnecter.');
            return;
          }

          this.createClientError.set('Creation impossible pour le moment. Veuillez reessayer.');
        }
      });
  }

  get nomControl() {
    return this.createClientForm.controls.nom;
  }

  get emailControl() {
    return this.createClientForm.controls.email;
  }
}

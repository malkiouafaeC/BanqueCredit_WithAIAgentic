import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClientService } from './client.service';
import { ApiErrorResponse } from '../../core/models/api-error.model';

/** Client creation form (US-003, US-004) — one explicit error message per missing field. */
@Component({
  selector: 'app-client-create-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './client-create.page.html'
})
export class ClientCreatePage {
  private readonly fb = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    nom: ['', [Validators.required]],
    email: ['', [Validators.email]],
    revenuMensuel: [null as number | null, [Validators.required, Validators.min(0.01)]],
    chargesMensuelles: [null as number | null, [Validators.required, Validators.min(0)]],
    situationProfessionnelle: ['']
  });

  readonly submitting = signal(false);
  readonly serverErrors = signal<Record<string, string>>({});
  readonly generalError = signal<string | null>(null);


  submit(): void {
    this.generalError.set(null);
    this.serverErrors.set({});

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();

    this.clientService
      .create({
        nom: raw.nom!,
        email: raw.email || null,
        revenuMensuel: raw.revenuMensuel!,
        chargesMensuelles: raw.chargesMensuelles!,
        situationProfessionnelle: raw.situationProfessionnelle || null
      })
      .subscribe({
        next: (client) => {
          this.submitting.set(false);
          this.router.navigate(['/clients', client.id]);
        },
        error: (error: HttpErrorResponse) => {
          this.submitting.set(false);
          this.applyServerError(error);
        }
      });
  }

  private applyServerError(error: HttpErrorResponse): void {
    const body = error.error as ApiErrorResponse | undefined;
    if (error.status === 400 && body?.erreurs?.length) {
      this.serverErrors.set(Object.fromEntries(body.erreurs.map((e) => [e.champ, e.message])));
      return;
    }
    if (error.status === 400 && body?.champ && body.message) {
      this.serverErrors.set({ [body.champ]: body.message });
      return;
    }
    this.generalError.set(body?.message ?? 'La création du client a échoué. Veuillez réessayer.');
  }
}


import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeCreditService } from './demande-credit.service';
import { ClientService } from '../clients/client.service';
import { Client } from '../clients/client.model';
import { ApiErrorResponse } from '../../core/models/api-error.model';

/** Montant must be strictly > 1000 (RG-BANK-02). */
function montantMinExclusive(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null =>
    control.value != null && Number(control.value) <= 1000 ? { montantMin: true } : null;
}

/** Creation form for a DemandeCredit in BROUILLON (US-007, US-008). */
@Component({
  selector: 'app-demande-create-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './demande-create.page.html'
})
export class DemandeCreatePage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly demandeService = inject(DemandeCreditService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly form = this.fb.group({
    clientId: [null as number | null, [Validators.required]],
    montantDemande: [null as number | null, [Validators.required, montantMinExclusive(), Validators.max(100000)]],
    dureeMois: [null as number | null, [Validators.required, Validators.min(12), Validators.max(84)]],
    tauxFictif: [null as number | null, [Validators.required, Validators.min(0)]]
  });

  readonly clients = signal<Client[]>([]);
  readonly submitting = signal(false);
  readonly serverErrors = signal<Record<string, string>>({});
  readonly generalError = signal<string | null>(null);


  ngOnInit(): void {
    this.clientService.list().subscribe((clients) => this.clients.set(clients));
    const preselected = this.route.snapshot.queryParamMap.get('clientId');
    if (preselected) {
      this.form.patchValue({ clientId: Number(preselected) });
    }
  }

  submit(): void {
    this.generalError.set(null);
    this.serverErrors.set({});
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const raw = this.form.getRawValue();
    this.demandeService
      .create({
        clientId: raw.clientId!,
        montantDemande: raw.montantDemande!,
        dureeMois: raw.dureeMois!,
        tauxFictif: raw.tauxFictif!
      })
      .subscribe({
        next: (demande) => {
          this.submitting.set(false);
          this.router.navigate(['/demandes', demande.id]);
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
    this.generalError.set(body?.message ?? 'La création de la demande a échoué.');
  }
}


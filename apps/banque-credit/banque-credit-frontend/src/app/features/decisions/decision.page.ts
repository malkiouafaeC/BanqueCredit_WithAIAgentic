import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeCreditService } from '../demandes/demande-credit.service';
import { DecisionService } from './decision.service';
import { DemandeCredit } from '../demandes/demande-credit.model';
import { DemandeActionsService } from '../demandes/demande-actions.service';
import { AuthService } from '../auth/auth.service';
import { BadgeStatutComponent } from '../../shared/charte/badge-statut.component';
import { ApiErrorResponse } from '../../core/models/api-error.model';

type LoadState = 'loading' | 'loaded' | 'error';

/** Non-empty, non-whitespace-only commentaire (RG-BANK-07, mirrors AC-015-1/2). */
function commentaireObligatoire(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null =>
    !control.value || !String(control.value).trim() ? { commentaireVide: true } : null;
}

/**
 * Décision page (analyser / accepter / refuser), reserved to RESPONSABLE_CREDIT (US-012..015).
 */
@Component({
  selector: 'app-decision-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ReactiveFormsModule, BadgeStatutComponent],
  templateUrl: './decision.page.html'
})
export class DecisionPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly demandeService = inject(DemandeCreditService);
  private readonly decisionService = inject(DecisionService);
  readonly actions = inject(DemandeActionsService);
  readonly authService = inject(AuthService);

  readonly state = signal<LoadState>('loading');
  readonly demande = signal<DemandeCredit | null>(null);
  readonly actionPending = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly showRefusForm = signal(false);

  readonly refusForm = this.fb.group({
    commentaire: ['', [Validators.required, commentaireObligatoire()]]
  });


  ngOnInit(): void {
    this.load();
  }

  private get id(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  load(): void {
    this.state.set('loading');
    this.demandeService.getById(this.id).subscribe({
      next: (demande) => {
        this.demande.set(demande);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }

  analyser(): void {
    this.run(this.decisionService.analyser(this.id));
  }

  accepter(): void {
    this.run(this.decisionService.accepter(this.id));
  }

  toggleRefusForm(): void {
    this.showRefusForm.set(!this.showRefusForm());
  }

  refuser(): void {
    if (this.refusForm.invalid) {
      this.refusForm.markAllAsTouched();
      return;
    }
    const commentaire = this.refusForm.getRawValue().commentaire!;
    this.run(this.decisionService.refuser(this.id, commentaire));
  }

  private run(observable: ReturnType<DecisionService['analyser']>): void {
    this.actionError.set(null);
    this.actionPending.set(true);
    observable.subscribe({
      next: (demande) => {
        this.demande.set(demande);
        this.actionPending.set(false);
        this.showRefusForm.set(false);
        this.refusForm.reset();
      },
      error: (error: HttpErrorResponse) => {
        this.actionPending.set(false);
        const body = error.error as ApiErrorResponse | undefined;
        if (error.status === 422) {
          this.actionError.set(body?.message ?? "Éligibilité non respectée : l'acceptation est impossible.");
        } else if (error.status === 409) {
          this.actionError.set(body?.message ?? 'Transition invalide.');
        } else if (error.status === 400) {
          this.actionError.set(body?.message ?? 'Un commentaire est obligatoire pour refuser une demande.');
        } else if (error.status === 403) {
          this.actionError.set("Vous n'êtes pas autorisé à effectuer cette action.");
        } else {
          this.actionError.set(body?.message ?? "L'action a échoué. Veuillez réessayer.");
        }
      }
    });
  }
}


import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { DemandeCreditService } from './demande-credit.service';
import { DemandeCredit } from './demande-credit.model';
import { SimulationResult } from './simulation-result.model';
import { DemandeActionsService } from './demande-actions.service';
import { AuthService } from '../auth/auth.service';
import { Role } from '../auth/role.enum';
import { BadgeStatutComponent } from '../../shared/charte/badge-statut.component';
import { BadgeScoreComponent } from '../../shared/charte/badge-score.component';
import { HistoriqueListComponent } from '../historique/historique-list.component';
import { ApiErrorResponse } from '../../core/models/api-error.model';

type LoadState = 'loading' | 'loaded' | 'error';

/**
 * Demande detail: simulation (US-009), soumission/annulation actions (US-010/011),
 * and embedded historique (US-016).
 */
@Component({
  selector: 'app-demande-detail-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BadgeStatutComponent, BadgeScoreComponent, HistoriqueListComponent],
  templateUrl: './demande-detail.page.html'
})
export class DemandeDetailPage implements OnInit {
  readonly state = signal<LoadState>('loading');
  readonly demande = signal<DemandeCredit | null>(null);
  readonly simulation = signal<SimulationResult | null>(null);
  readonly simulating = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly actionPending = signal(false);
  readonly Role = Role;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly demandeService: DemandeCreditService,
    readonly actions: DemandeActionsService,
    readonly authService: AuthService
  ) {}

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

  simuler(): void {
    this.simulating.set(true);
    this.demandeService.simuler(this.id).subscribe({
      next: (result) => {
        this.simulation.set(result);
        this.simulating.set(false);
      },
      error: () => this.simulating.set(false)
    });
  }

  soumettre(): void {
    this.runAction(this.demandeService.soumettre(this.id));
  }

  annuler(): void {
    this.runAction(this.demandeService.annuler(this.id));
  }

  private runAction(observable: ReturnType<DemandeCreditService['soumettre']>): void {
    this.actionError.set(null);
    this.actionPending.set(true);
    observable.subscribe({
      next: (demande) => {
        this.demande.set(demande);
        this.actionPending.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.actionPending.set(false);
        const body = error.error as ApiErrorResponse | undefined;
        if (error.status === 409) {
          this.actionError.set(body?.message ?? 'Transition invalide.');
        } else if (error.status === 403) {
          this.actionError.set("Vous n'êtes pas autorisé à effectuer cette action.");
        } else {
          this.actionError.set(body?.message ?? "L'action a échoué. Veuillez réessayer.");
        }
      }
    });
  }
}


import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DemandeCreditService } from './demande-credit.service';
import { DemandeCreditSummary } from './demande-credit.model';
import { BadgeStatutComponent } from '../../shared/charte/badge-statut.component';
import { AuthService } from '../auth/auth.service';
import { Role } from '../auth/role.enum';

type LoadState = 'loading' | 'loaded' | 'error';

/** Demandes list — read access for both roles (Responsable crédit needs to find dossiers to instruct). */
@Component({
  selector: 'app-demande-list-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BadgeStatutComponent],
  templateUrl: './demande-list.page.html'
})
export class DemandeListPage implements OnInit {
  readonly state = signal<LoadState>('loading');
  readonly demandes = signal<DemandeCreditSummary[]>([]);
  readonly Role = Role;

  constructor(
    private readonly demandeService: DemandeCreditService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.demandeService.list().subscribe({
      next: (demandes) => {
        this.demandes.set(demandes);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }
}


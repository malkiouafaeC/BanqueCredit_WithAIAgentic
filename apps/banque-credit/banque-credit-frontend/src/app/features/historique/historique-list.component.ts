import { ChangeDetectionStrategy, Component, Input, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HistoriqueService } from './historique.service';
import { HistoriqueDecision } from './historique-decision.model';
import { BadgeStatutComponent } from '../../shared/charte/badge-statut.component';

type LoadState = 'loading' | 'loaded' | 'error';

/**
 * Reusable, presentational chronological list of statut transitions (US-016).
 * Embedded in demande-detail.page (per spec-architecture-banque-credit.md §5.1: "ou section
 * intégrée à demande-detail"), avoiding a dedicated route.
 */
@Component({
  selector: 'app-historique-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, BadgeStatutComponent],
  templateUrl: './historique-list.component.html'
})
export class HistoriqueListComponent implements OnInit {
  @Input({ required: true }) demandeId!: number;

  readonly state = signal<LoadState>('loading');
  readonly entries = signal<HistoriqueDecision[]>([]);

  constructor(private readonly historiqueService: HistoriqueService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.historiqueService.listerParDemande(this.demandeId).subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }
}


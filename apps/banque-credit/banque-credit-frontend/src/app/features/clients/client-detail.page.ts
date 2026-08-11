import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ClientService } from './client.service';
import { ClientDetail } from './client.model';
import { BadgeStatutComponent } from '../../shared/charte/badge-statut.component';

type LoadState = 'loading' | 'loaded' | 'error';

/** Client detail incl. associated demandes (US-006) — explicit empty state (AC-006-2). */
@Component({
  selector: 'app-client-detail-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, BadgeStatutComponent, DatePipe],
  templateUrl: './client-detail.page.html'
})
export class ClientDetailPage implements OnInit {
  readonly state = signal<LoadState>('loading');
  readonly client = signal<ClientDetail | null>(null);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly clientService: ClientService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.state.set('loading');
    this.clientService.getById(id).subscribe({
      next: (client) => {
        this.client.set(client);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }
}



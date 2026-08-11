import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClientService } from './client.service';
import { Client } from './client.model';
import { AuthService } from '../auth/auth.service';
import { Role } from '../auth/role.enum';

type LoadState = 'loading' | 'loaded' | 'error';

/** Client list (US-005) — explicit loading/empty/error states (AC-005-1/2). */
@Component({
  selector: 'app-client-list-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  templateUrl: './client-list.page.html'
})
export class ClientListPage implements OnInit {
  readonly state = signal<LoadState>('loading');
  readonly clients = signal<Client[]>([]);
  readonly Role = Role;

  constructor(
    private readonly clientService: ClientService,
    readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.clientService.list().subscribe({
      next: (clients) => {
        this.clients.set(clients);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }
}


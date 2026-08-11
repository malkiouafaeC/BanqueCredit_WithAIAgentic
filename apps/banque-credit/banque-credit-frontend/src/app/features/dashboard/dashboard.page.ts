import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { DashboardService } from './dashboard.service';
import { Dashboard } from './dashboard.model';

type LoadState = 'loading' | 'loaded' | 'error';

/** Agency-wide synthesis (US-017) — handles empty base explicitly (AC-017-4). */
@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
  templateUrl: './dashboard.page.html'
})
export class DashboardPage implements OnInit {
  readonly state = signal<LoadState>('loading');
  readonly dashboard = signal<Dashboard | null>(null);

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.state.set('loading');
    this.dashboardService.getSynthese().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
        this.state.set('loaded');
      },
      error: () => this.state.set('error')
    });
  }
}


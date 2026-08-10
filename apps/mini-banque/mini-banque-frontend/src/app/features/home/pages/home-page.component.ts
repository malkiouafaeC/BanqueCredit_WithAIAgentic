import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { AuthStore } from '../../../core/stores/auth.store';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomePageComponent {
  private readonly router = inject(Router);
  readonly authStore = inject(AuthStore);
  readonly menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update((state) => !state);
  }

  openClientsPage(): void {
    this.menuOpen.set(false);
    void this.router.navigateByUrl('/clients');
  }

  logout(): void {
    this.menuOpen.set(false);
    this.authStore.clearSession();
    void this.router.navigateByUrl('/login');
  }
}

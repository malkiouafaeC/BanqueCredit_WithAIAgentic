import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { Statut } from '../../features/demandes/statut.enum';
import { STATUT_CHARTE } from './charte.constants';

/**
 * Reusable, accessible status badge: always renders a text label + color class,
 * never color alone (accessibility baseline, spec §5.8).
 */
@Component({
  selector: 'app-badge-statut',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="charte-badge" [ngClass]="cssClass" role="status">{{ label }}</span>`,
  styleUrl: './badges.scss',
  imports: [NgClass]
})
export class BadgeStatutComponent {
  @Input({ required: true }) statut!: Statut;

  get label(): string {
    return STATUT_CHARTE[this.statut]?.label ?? this.statut;
  }

  get cssClass(): string {
    return STATUT_CHARTE[this.statut]?.cssClass ?? 'charte-neutral';
  }
}



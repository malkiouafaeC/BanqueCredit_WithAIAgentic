import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { ScoreSimplifie } from '../../features/demandes/score-simplifie.enum';
import { SCORE_CHARTE, SCORES_AVERTISSEMENT } from './charte.constants';

/**
 * Reusable, accessible score badge with an explicit pedagogical warning for
 * MOYEN/FAIBLE scores (AC-009-6), never relying on color alone.
 */
@Component({
  selector: 'app-badge-score',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="charte-badge" [ngClass]="cssClass" role="status">Score : {{ label }}</span>
    @if (showWarning) {
      <p class="charte-warning-text" role="alert">
        ⚠ Avertissement pédagogique : ce score suggère un risque plus élevé pour ce dossier fictif.
      </p>
    }
  `,
  styleUrl: './badges.scss',
  imports: [NgClass]
})
export class BadgeScoreComponent {
  @Input({ required: true }) score!: ScoreSimplifie;

  get label(): string {
    return SCORE_CHARTE[this.score]?.label ?? this.score;
  }

  get cssClass(): string {
    return SCORE_CHARTE[this.score]?.cssClass ?? 'charte-neutral';
  }

  get showWarning(): boolean {
    return SCORES_AVERTISSEMENT.includes(this.score);
  }
}



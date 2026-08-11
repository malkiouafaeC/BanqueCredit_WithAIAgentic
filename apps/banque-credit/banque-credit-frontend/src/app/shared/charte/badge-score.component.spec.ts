import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BadgeScoreComponent } from './badge-score.component';
import { ScoreSimplifie } from '../../features/demandes/score-simplifie.enum';

describe('BadgeScoreComponent', () => {
  let fixture: ComponentFixture<BadgeScoreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [BadgeScoreComponent] }).compileComponents();
    fixture = TestBed.createComponent(BadgeScoreComponent);
  });

  it('shows no pedagogical warning for EXCELLENT', () => {
    fixture.componentInstance.score = ScoreSimplifie.EXCELLENT;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows no pedagogical warning for BON (AC-009-4, borne 35% incluse)', () => {
    fixture.componentInstance.score = ScoreSimplifie.BON;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeNull();
  });

  it('shows a pedagogical warning for MOYEN (AC-009-6)', () => {
    fixture.componentInstance.score = ScoreSimplifie.MOYEN;
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')).not.toBeNull();
  });

  it('shows a pedagogical warning for FAIBLE (AC-009-6)', () => {
    fixture.componentInstance.score = ScoreSimplifie.FAIBLE;
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]');
    expect(alert).not.toBeNull();
    expect(alert.textContent).toContain('Avertissement pédagogique');
  });

  it('falls back to a neutral class/raw label for an unmapped score value (QA-TASK-103)', () => {
    fixture.componentInstance.score = 'INCONNU' as ScoreSimplifie;
    fixture.detectChanges();
    expect(fixture.componentInstance.cssClass).toBe('charte-neutral');
    expect(fixture.componentInstance.label).toBe('INCONNU');
  });
});


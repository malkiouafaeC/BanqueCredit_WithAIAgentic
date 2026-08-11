import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BadgeStatutComponent } from './badge-statut.component';
import { Statut } from '../../features/demandes/statut.enum';

describe('BadgeStatutComponent', () => {
  let fixture: ComponentFixture<BadgeStatutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [BadgeStatutComponent] }).compileComponents();
    fixture = TestBed.createComponent(BadgeStatutComponent);
  });

  it('renders the French label for ACCEPTEE with success class', () => {
    fixture.componentInstance.statut = Statut.ACCEPTEE;
    fixture.detectChanges();
    const span: HTMLElement = fixture.nativeElement.querySelector('span');
    expect(span.textContent).toContain('Acceptée');
    expect(span.classList).toContain('charte-success');
  });

  it('renders the French label for REFUSEE with danger class', () => {
    fixture.componentInstance.statut = Statut.REFUSEE;
    fixture.detectChanges();
    const span: HTMLElement = fixture.nativeElement.querySelector('span');
    expect(span.textContent).toContain('Refusée');
    expect(span.classList).toContain('charte-danger');
  });

  it('renders EN_ANALYSE with warning class', () => {
    fixture.componentInstance.statut = Statut.EN_ANALYSE;
    fixture.detectChanges();
    const span: HTMLElement = fixture.nativeElement.querySelector('span');
    expect(span.classList).toContain('charte-warning');
  });

  it('falls back to a neutral class/raw label for an unmapped statut value (QA-TASK-104)', () => {
    fixture.componentInstance.statut = 'INCONNU' as Statut;
    fixture.detectChanges();
    expect(fixture.componentInstance.cssClass).toBe('charte-neutral');
    expect(fixture.componentInstance.label).toBe('INCONNU');
  });
});


import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DashboardPage } from './dashboard.page';
import { DashboardService } from './dashboard.service';

describe('DashboardPage', () => {
  let fixture: ComponentFixture<DashboardPage>;
  let dashboardServiceMock: jasmine.SpyObj<DashboardService>;

  beforeEach(async () => {
    dashboardServiceMock = jasmine.createSpyObj<DashboardService>('DashboardService', ['getSynthese']);

    await TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [{ provide: DashboardService, useValue: dashboardServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPage);
  });

  it('shows nominal counters (AC-017-1)', () => {
    dashboardServiceMock.getSynthese.and.returnValue(
      of({ nbSoumises: 2, nbEnAnalyse: 1, nbAcceptees: 3, nbRefusees: 1, montantTotalDemande: 15000, tauxMoyenEndettement: 22.5 })
    );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('15000');
  });

  it('shows neutral zero values for an empty base (AC-017-4)', () => {
    dashboardServiceMock.getSynthese.and.returnValue(
      of({ nbSoumises: 0, nbEnAnalyse: 0, nbAcceptees: 0, nbRefusees: 0, montantTotalDemande: 0, tauxMoyenEndettement: 0 })
    );
    fixture.detectChanges();
    const values: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.dashboard-card__value'));
    expect(values.every((v) => v.textContent?.trim() === '0')).toBeTrue();
  });

  it('shows an error state on failure', () => {
    dashboardServiceMock.getSynthese.and.returnValue(throwError(() => new Error('fail')));
    fixture.detectChanges();
    expect(fixture.componentInstance.state()).toBe('error');
  });
});


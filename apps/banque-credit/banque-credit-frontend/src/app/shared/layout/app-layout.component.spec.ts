import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppLayoutComponent } from './app-layout.component';
import { AuthService } from '../../features/auth/auth.service';
import { Role } from '../../features/auth/role.enum';

describe('AppLayoutComponent', () => {
  let fixture: ComponentFixture<AppLayoutComponent>;
  let authServiceMock: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole', 'logout']);
    (authServiceMock as any).currentUsername = () => 'jdupont';
    (authServiceMock as any).currentRole = () => Role.CONSEILLER;

    await TestBed.configureTestingModule({
      imports: [AppLayoutComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(AppLayoutComponent);
  });

  it('shows the Clients link for CONSEILLER', () => {
    authServiceMock.hasRole.and.returnValue(true);
    fixture.detectChanges();
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('a'));
    expect(links.some((l) => l.getAttribute('routerLink') === '/clients')).toBeTrue();
  });

  it('hides the Clients link for RESPONSABLE_CREDIT', () => {
    authServiceMock.hasRole.and.returnValue(false);
    fixture.detectChanges();
    const links: HTMLAnchorElement[] = Array.from(fixture.nativeElement.querySelectorAll('a'));
    expect(links.some((l) => l.getAttribute('routerLink') === '/clients')).toBeFalse();
  });

  it('calls logout on button click', () => {
    authServiceMock.hasRole.and.returnValue(true);
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('.app-header__logout');
    button.click();
    expect(authServiceMock.logout).toHaveBeenCalled();
  });
});


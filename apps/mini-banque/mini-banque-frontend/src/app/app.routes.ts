import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { HomePageComponent } from './features/home/pages/home-page.component';
import { ClientsPageComponent } from './features/clients/pages/clients-page.component';
import { LoginPageComponent } from './features/auth/pages/login-page.component';

export const routes: Routes = [
	{ path: '', pathMatch: 'full', redirectTo: 'login' },
	{ path: 'login', component: LoginPageComponent },
	{ path: 'home', component: HomePageComponent, canActivate: [authGuard] },
	{ path: 'clients', component: ClientsPageComponent, canActivate: [authGuard] },
	{ path: '**', redirectTo: 'login' }
];

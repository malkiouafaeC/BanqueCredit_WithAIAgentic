import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest } from './auth-request.model';
import { AuthResponse } from './auth-response.model';
import { Role } from './role.enum';

const STORAGE_KEY = 'banque-credit-auth';

interface StoredAuth {
  token: string;
  username: string;
  role: Role;
  expiresAt: string;
}

/**
 * Handles authentication lifecycle: login, client-side JWT storage, role/username exposure.
 * Storage: sessionStorage (cleared when the browser tab closes) to limit token leakage window.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly _auth = signal<StoredAuth | null>(this.readFromStorage());

  readonly isAuthenticated = computed(() => !!this._auth());
  readonly currentRole = computed<Role | null>(() => this._auth()?.role ?? null);
  readonly currentUsername = computed<string | null>(() => this._auth()?.username ?? null);

  constructor(private readonly http: HttpClient) {}

  login(request: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => {
        const stored: StoredAuth = {
          token: response.token,
          username: response.username,
          role: response.role,
          expiresAt: response.expiresAt
        };
        this._auth.set(stored);
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
      })
    );
  }

  logout(): void {
    this._auth.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  getToken(): string | null {
    return this._auth()?.token ?? null;
  }

  hasRole(role: Role | Role[]): boolean {
    const current = this.currentRole();
    if (!current) {
      return false;
    }
    return Array.isArray(role) ? role.includes(current) : current === role;
  }

  private readFromStorage(): StoredAuth | null {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as StoredAuth;
    } catch {
      return null;
    }
  }
}


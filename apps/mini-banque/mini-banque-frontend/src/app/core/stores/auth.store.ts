import { Injectable, computed, signal } from '@angular/core';

import { LoginResponse } from '../models/auth.model';

interface AuthSession {
  token: string;
  username: string;
}

const SESSION_STORAGE_KEY = 'mini-banque.auth.session';

@Injectable({
  providedIn: 'root'
})
export class AuthStore {
  private readonly session = signal<AuthSession | null>(this.readSessionFromStorage());

  readonly token = computed(() => this.session()?.token ?? null);
  readonly username = computed(() => this.session()?.username ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);

  setSession(loginResponse: LoginResponse): void {
    const nextSession: AuthSession = {
      token: loginResponse.token,
      username: loginResponse.username
    };

    this.session.set(nextSession);
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(nextSession));
  }

  clearSession(): void {
    this.session.set(null);
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
  }

  private readSessionFromStorage(): AuthSession | null {
    const rawSession = sessionStorage.getItem(SESSION_STORAGE_KEY);
    if (!rawSession) {
      return null;
    }

    try {
      const parsed = JSON.parse(rawSession) as Partial<AuthSession>;
      if (typeof parsed.token === 'string' && parsed.token.length > 0 && typeof parsed.username === 'string') {
        return { token: parsed.token, username: parsed.username };
      }
    } catch {
      // Invalid JSON should not break app bootstrap.
    }

    sessionStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
}

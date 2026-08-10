import { TestBed } from '@angular/core/testing';

import { LoginResponse } from '../models/auth.model';
import { AuthStore } from './auth.store';

describe('AuthStore', () => {
  let store: AuthStore;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    store = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should persist and expose session data', () => {
    const loginResponse: LoginResponse = {
      token: 'jwt-token',
      username: 'conseiller1'
    };

    store.setSession(loginResponse);

    expect(store.isAuthenticated()).toBeTrue();
    expect(store.token()).toBe('jwt-token');
    expect(store.username()).toBe('conseiller1');
    expect(sessionStorage.getItem('mini-banque.auth.session')).toContain('jwt-token');
  });

  it('should clear session data', () => {
    store.setSession({ token: 'jwt-token', username: 'conseiller1' });

    store.clearSession();

    expect(store.isAuthenticated()).toBeFalse();
    expect(store.token()).toBeNull();
    expect(sessionStorage.getItem('mini-banque.auth.session')).toBeNull();
  });
});

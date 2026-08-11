import { Role } from './role.enum';

/** Response body for POST /auth/login (spec-architecture-banque-credit.md §7.1). */
export interface AuthResponse {
  token: string;
  username: string;
  role: Role;
  expiresAt: string;
}


/** Request body for POST /auth/login (spec-architecture-banque-credit.md §7.1). */
export interface AuthRequest {
  username: string;
  password: string;
}


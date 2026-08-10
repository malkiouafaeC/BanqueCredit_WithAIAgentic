# Frontend Handoff - FE-004

Agent cible: AgentFrontendDeveloper
Task ID: FE-004

## Objective
Implementer page login et gestion des erreurs credentials invalides.

## Linked requirements
- REQ-001

## Preconditions
- FE-003 complete

## API contract
- Endpoint: POST /api/auth/login
- Request: { username, password }
- Success 200: { token, username }
- Error 401: { code: AUTH_INVALID_CREDENTIALS, message }

## Expected deliverables
- UI login (username/password).
- Soumission vers API login.
- Message d'erreur visible en cas de 401.

## Validation expectations
- Login valide redirige vers /home.
- Login invalide affiche une erreur claire.

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Flux login conforme au comportement attendu.

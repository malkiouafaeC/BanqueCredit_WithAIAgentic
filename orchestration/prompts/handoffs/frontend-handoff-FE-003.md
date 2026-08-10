# Frontend Handoff - FE-003

Agent cible: AgentFrontendDeveloper
Task ID: FE-003

## Objective
Implementer gestion token (stockage session + ajout header Authorization).

## Linked requirements
- REQ-003
- SEC-001

## Preconditions
- FE-002 complete
- BE-002 complete
- BE-003 complete

## Expected deliverables
- Stockage token/session.
- Interceptor HTTP (ou mecanisme equivalent) ajoutant Bearer token.
- Gestion du cas token manquant/invalide (401).

## Validation expectations
- Les appels API clients envoient Authorization: Bearer <token>.
- Les erreurs 401 sont gerees proprement.

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Requetes protegees fonctionnent apres login.

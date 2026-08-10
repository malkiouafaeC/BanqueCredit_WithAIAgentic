# Backend Handoff - BE-005

Agent cible: AgentBackendDeveloper
Task ID: BE-005

## Objective
Uniformiser les erreurs auth/validation et ajouter les tests backend.

## Linked requirements
- SEC-002

## Preconditions
- BE-004 complete

## Expected deliverables
- Format erreur coherent (401/400)
- Tests unitaires (auth, clients)
- Tests integration:
  - POST /api/auth/login (success + invalid)
  - GET /api/clients (authorized + unauthorized)
  - POST /api/clients/new (success + validation error)

## Validation expectations
- Tous tests backend passent
- Erreurs standards conformes a la spec

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Couverture scenarios critiques login/clients/unauthorized
- Regressions backend non detectees

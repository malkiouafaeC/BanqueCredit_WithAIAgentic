# Backend Handoff - BE-002

Agent cible: AgentBackendDeveloper
Task ID: BE-002

## Objective
Implementer login conseiller `POST /api/auth/login` avec credentials hardcodes.

## Linked requirements
- REQ-001
- REQ-002

## Preconditions
- BE-001 complete

## API contract
- Endpoint: POST /api/auth/login
- Request:
```json
{ "username": "conseiller1", "password": "password123" }
```
- Success 200:
```json
{ "token": "<jwt>", "username": "conseiller1" }
```
- Error 401:
```json
{ "code": "AUTH_INVALID_CREDENTIALS", "message": "Username ou password invalide" }
```

## Expected deliverables
- Controller login
- Service auth avec credentials hardcodes
- Generation JWT
- DTO request/response

## Validation expectations
- Test succes credentials valides
- Test erreur credentials invalides

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Retour 200 + token si valide
- Retour 401 standardise si invalide

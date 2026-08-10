# Backend Handoff - BE-004

Agent cible: AgentBackendDeveloper
Task ID: BE-004

## Objective
Implementer APIs clients `GET /api/clients` et `POST /api/clients/new` avec donnees hardcodees.

## Linked requirements
- REQ-006
- REQ-008
- CON-001

## Preconditions
- BE-003 complete

## API contracts
1) GET /api/clients
- Success 200:
```json
[
  { "id": 1, "nom": "Client A", "email": "a@bank.test" },
  { "id": 2, "nom": "Client B", "email": "b@bank.test" }
]
```

2) POST /api/clients/new
- Request:
```json
{ "nom": "Client C", "email": "c@bank.test" }
```
- Success 201:
```json
{ "id": 3, "nom": "Client C", "email": "c@bank.test" }
```
- Error 400:
```json
{ "code": "CLIENT_VALIDATION_ERROR", "message": "Donnees client invalides" }
```

## Expected deliverables
- Endpoint liste clients
- Endpoint creation client
- Validation payload minimal

## Validation expectations
- GET retourne la liste attendue
- POST valide retourne 201
- POST invalide retourne 400

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Contrats API conformes a la specification

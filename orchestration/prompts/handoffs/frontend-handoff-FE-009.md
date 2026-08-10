# Frontend Handoff - FE-009

Agent cible: AgentFrontendDeveloper
Task ID: FE-009

## Objective
Connecter action nouveau client vers POST /api/clients/new.

## Linked requirements
- REQ-008

## Preconditions
- FE-008 complete
- BE-004 complete

## API contract
- Endpoint: POST /api/clients/new
- Request: { nom, email }
- Success 201: client cree
- Error 400: CLIENT_VALIDATION_ERROR

## Expected deliverables
- Action UI de creation client.
- Appel API create client.
- Rafraichissement de la liste apres succes.
- Affichage erreurs validation.

## Validation expectations
- Creation valide visible dans la liste.
- Erreur 400 affichee sans casser le flux.

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Creation client fonctionnelle cote UI.

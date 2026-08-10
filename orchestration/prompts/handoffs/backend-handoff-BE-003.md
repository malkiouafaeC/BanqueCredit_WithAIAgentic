# Backend Handoff - BE-003

Agent cible: AgentBackendDeveloper
Task ID: BE-003

## Objective
Configurer Spring Security + JWT pour proteger `/api/clients` et `/api/clients/new`.

## Linked requirements
- SEC-001

## Preconditions
- BE-002 complete

## Mandatory constraints
- Endpoints clients proteges par bearer token.
- Endpoint login public.

## Expected deliverables
- SecurityFilterChain
- JWT filter/validation
- Configuration des routes protegees

## Validation expectations
- Sans token: endpoints clients renvoient 401
- Avec token valide: endpoints clients accessibles

## Required output format
1) Task understanding
2) Assumptions
3) Technical plan
4) Code changes (by file)
5) Tests added/updated
6) Risks and follow-ups
7) Skills used

## Done criteria
- Protection JWT fonctionnelle sur endpoints clients

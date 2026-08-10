---
goal: Implementer le mini projet banque avec JWT backend et interface Angular connectee
version: 1.0
date_created: 2026-08-07
last_updated: 2026-08-07
owner: AgentOrchestrator
status: Planned
tags: [feature, backend, frontend, integration]
---

# Introduction

Plan d'implementation executable pour livrer le mini projet banque decrit dans la specification `spec/spec-feature-mini-banque-auth-clients.md`.

## 1. Requirements & Constraints

- **REQ-001**: Auth conseiller via username/password.
- **REQ-002**: Retour JWT en cas de login valide.
- **REQ-003**: Stockage token cote frontend.
- **REQ-004**: Home avec menu username/client/se deconnecter.
- **REQ-005**: Navigation home -> clients.
- **REQ-006**: Liste clients affichee.
- **REQ-007**: Bouton nouveau client visible en haut.
- **REQ-008**: API ajout nouveau client.
- **SEC-001**: Endpoints clients proteges par JWT.
- **SEC-002**: Erreurs auth standardisees.
- **CON-001**: Donnees hardcodees autorisees.
- **CON-002**: Sources applicatives uniquement dans:
  - apps/mini-banque/mini-banque-backend
  - apps/mini-banque/mini-banque-frontend

## 2. Requirements Traceability Matrix

| Requirement | Planned Tasks |
|---|---|
| REQ-001, REQ-002, SEC-002 | BE-002, BE-005 |
| REQ-003 | FE-003, FE-004 |
| REQ-004, REQ-005 | FE-005, FE-006 |
| REQ-006, REQ-007 | FE-007, FE-008 |
| REQ-008 | BE-004, FE-009 |
| SEC-001 | BE-003, BE-005, FE-003 |
| CON-001 | BE-001 |
| CON-002 | BE-001, FE-001 |

## 3. Implementation Steps

### Implementation Phase 1 - Project Setup

- GOAL-001: Initialiser structure parent + apps backend/frontend dans les chemins imposes.

| Task | Description | Owner | Completed | Date |
|------|-------------|-------|-----------|------|
| BE-001 | Creer app Spring Boot dans apps/mini-banque/mini-banque-backend avec structure de base | AgentBackendDeveloper |  |  |
| FE-001 | Creer app Angular dans apps/mini-banque/mini-banque-frontend avec routing de base | AgentFrontendDeveloper |  |  |

### Implementation Phase 2 - Backend Security and APIs

- GOAL-002: Fournir authentification JWT et APIs clients protegees.

| Task | Description | Owner | Completed | Date |
|------|-------------|-------|-----------|------|
| BE-002 | Implementer endpoint POST /api/auth/login avec credentials hardcodes | AgentBackendDeveloper |  |  |
| BE-003 | Configurer Spring Security + JWT filter + protection des endpoints /api/clients* | AgentBackendDeveloper |  |  |
| BE-004 | Implementer GET /api/clients et POST /api/clients/new avec donnees hardcodees | AgentBackendDeveloper |  |  |
| BE-005 | Uniformiser erreurs auth/validation (401/400) + tests integration API | AgentBackendDeveloper |  |  |

### Implementation Phase 3 - Frontend Auth and Navigation

- GOAL-003: Construire le flux UI login -> home -> clients -> logout.

| Task | Description | Owner | Completed | Date |
|------|-------------|-------|-----------|------|
| FE-002 | Creer models et services HTTP types pour auth et clients | AgentFrontendDeveloper |  |  |
| FE-003 | Implementer gestion token (store session + attach Authorization header) | AgentFrontendDeveloper |  |  |
| FE-004 | Implementer page login + gestion erreur credentials invalides | AgentFrontendDeveloper |  |  |
| FE-005 | Implementer page home avec menu username, client, se deconnecter | AgentFrontendDeveloper |  |  |
| FE-006 | Implementer routes et guard (redirections auth) | AgentFrontendDeveloper |  |  |
| FE-007 | Implementer page clients avec chargement liste depuis API | AgentFrontendDeveloper |  |  |
| FE-008 | Ajouter bouton nouveau client en haut de page clients | AgentFrontendDeveloper |  |  |
| FE-009 | Connecter action nouveau client a POST /api/clients/new (mode simple) | AgentFrontendDeveloper |  |  |

### Implementation Phase 4 - Integration and Final Quality Gate

- GOAL-004: Verifier alignement FE/BE et readiness de livraison.

| Task | Description | Owner | Completed | Date |
|------|-------------|-------|-----------|------|
| INT-001 | Verifier alignement contrats API login/clients/new-client entre FE et BE | AgentOrchestrator |  |  |
| INT-002 | Executer checklist de securite, erreurs, navigation et tests | AgentOrchestrator |  |  |
| INT-003 | Produire verdict READY/NOT_READY avec blockers et actions | AgentOrchestrator |  |  |

## 4. Dependencies

- **DEP-001**: FE-002 depend de FE-001.
- **DEP-002**: FE-003 depend de BE-002 et BE-003.
- **DEP-003**: FE-007 depend de BE-004 et BE-005.
- **DEP-004**: FE-009 depend de BE-004.
- **DEP-005**: INT-001 depend de BE-005 et FE-009.

## 5. Files

- **FILE-001**: apps/mini-banque/mini-banque-backend/*
- **FILE-002**: apps/mini-banque/mini-banque-frontend/*
- **FILE-003**: spec/spec-feature-mini-banque-auth-clients.md
- **FILE-004**: plan/plan-feature-mini-banque-auth-clients.md
- **FILE-005**: tasks/tasks-feature-mini-banque-auth-clients.md
- **FILE-006**: orchestration/prompts/handoffs/*
- **FILE-007**: orchestration/status/progress-board.md

## 6. Testing

- **TEST-001**: Backend unit tests pour service auth et service clients.
- **TEST-002**: Backend integration tests endpoints login, clients list, new client.
- **TEST-003**: Frontend unit tests login/home/clients components.
- **TEST-004**: Frontend tests routing/guard et comportement logout.
- **TEST-005**: Verification manuelle E2E login -> home -> clients -> nouveau client -> logout.

## 7. Risks & Assumptions

- **RISK-001**: Desalignement format payload entre FE et BE.
- **RISK-002**: Mauvaise gestion du token frontend (expiration/non suppression logout).
- **RISK-003**: Redirections de guard inconsistantes selon etat auth.

- **ASSUMPTION-001**: Les versions locales Java/Node/Angular CLI sont disponibles.
- **ASSUMPTION-002**: Le mode hardcode des donnees est acceptable pour cette iteration.
- **ASSUMPTION-003**: Aucun besoin de persistence base de donnees pour cette phase.

## 8. Related Specifications / Further Reading

- spec/spec-feature-mini-banque-auth-clients.md

---
goal: Implementer le suivi des demandes de credit client (clients, demandes, simulation, scoring, decision, historique, dashboard) en backend Spring Boot (WAR/Tomcat) et frontend Angular (Apache), conformement a spec/spec-feature-banque-credit.md et spec/spec-architecture-banque-credit.md
version: 1.0
date_created: 2026-08-11
owner: AgentOrchestrator
status: Planned
tags: [feature, backend, frontend, security, angular, spring-boot, jpa]
---

# Introduction

Plan d'implementation executable pour livrer l'application "banque-credit" decrite dans :
- `spec/spec-feature-banque-credit.md` (v1.1, approuve, US-001..017, RG-BANK-01..08)
- `spec/spec-architecture-banque-credit.md` (v1.0, approuve, ENT-001..004, DEC-001..009, 16 endpoints)

## 1. Requirements & Constraints (rappel)

- **REQ-CLIENT-01..04** : creation client (RG-BANK-01), liste clients, detail client avec demandes associees.
- **REQ-DEMANDE-01..05** : creation demande (RG-BANK-02/03), simulation (RG-BANK-04/08), soumission, annulation.
- **REQ-DECISION-01..04** : passage en analyse, acceptation (RG-BANK-06), refus (RG-BANK-07), toutes transitions historisees (RG-BANK-05).
- **REQ-HISTO-01** : consultation historique complet par demande.
- **REQ-DASHBOARD-01** : synthese agence (compteurs par statut, montant total, taux moyen d'endettement, hors BROUILLON/ANNULEE).
- **REQ-AUTH-01** : connexion JWT avec 2 roles (CONSEILLER, RESPONSABLE_CREDIT), comptes pre-provisionnes.
- **SEC-001** : toutes les routes hors `/auth/login` protegees par JWT (401 si absent/invalide).
- **SEC-002** : autorisation par role sur les routes d'ecriture (403 si role non autorise), verification cote service (DEC-009) en plus du controller.
- **CON-001** : code applicatif uniquement dans :
  - apps/banque-credit/banque-credit-backend
  - apps/banque-credit/banque-credit-frontend
- **CON-002** : package racine backend `formulAI.project.bank.banqueCredit` ; packaging WAR pour Tomcat ; frontend build statique pour Apache HTTP.
- **CON-003** : persistance H2 mode fichier (DEC-007) ; Swagger UI via springdoc (DEC-008).

## 2. Requirements Traceability Matrix

| Requirement / RG-BANK | Planned Tasks |
|---|---|
| RG-BANK-01 (client obligatoire) | BE-CREDIT-002, BE-CREDIT-005, FE-CREDIT-005 |
| RG-BANK-02, RG-BANK-03 (bornes montant/duree) | BE-CREDIT-002, BE-CREDIT-006, FE-CREDIT-006 |
| RG-BANK-04 (formule mensualite/taux) | BE-CREDIT-006, FE-CREDIT-007 |
| RG-BANK-05 (statuts/transitions/historique) | BE-CREDIT-002, BE-CREDIT-007, BE-CREDIT-008, FE-CREDIT-008, FE-CREDIT-010 |
| RG-BANK-06 (eligibilite acceptation) | BE-CREDIT-007, FE-CREDIT-009 |
| RG-BANK-07 (commentaire refus obligatoire) | BE-CREDIT-007, FE-CREDIT-009 |
| RG-BANK-08 (score simplifie) | BE-CREDIT-006, FE-CREDIT-007 |
| REQ-DASHBOARD-01 | BE-CREDIT-009, FE-CREDIT-011 |
| REQ-AUTH-01, SEC-001, SEC-002 | BE-CREDIT-003, BE-CREDIT-004, FE-CREDIT-002, FE-CREDIT-003 |
| DEC-008 (Swagger) | BE-CREDIT-010 |
| CON-001, CON-002 | BE-CREDIT-001, FE-CREDIT-001 |

## 3. Implementation Steps

### Implementation Phase 1 - Project Setup

- GOAL-001: Initialiser les squelettes backend et frontend dans les chemins imposes, avec dependances et configuration de base.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| BE-CREDIT-001 | Creer projet Spring Boot (WAR packaging) dans apps/banque-credit/banque-credit-backend, package racine `formulAI.project.bank.banqueCredit`, dependances (web, data-jpa, security, validation, h2, springdoc, jjwt), application.properties (H2 fichier), structure de packages (model/dto/repository/service/service.impl/controller/security/config/exception) | AgentBackendDeveloper | — |
| FE-CREDIT-001 | Creer projet Angular standalone dans apps/banque-credit/banque-credit-frontend, routing de base, structure features/{auth,dashboard,clients,demandes,decisions,historique} + shared + core vides | AgentFrontendDeveloper | — |

### Implementation Phase 2 - Backend Domain, Security & Error Handling

- GOAL-002: Fournir le modele de donnees JPA, l'authentification JWT par role et la gestion d'erreurs centralisee.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| BE-CREDIT-002 | Implementer entites JPA Client, DemandeCredit, HistoriqueDecision, User + enums Statut/ScoreSimplifie/Role (ENT-001..004) + repositories Spring Data JPA + table de transitions (DEC-ENT-003) | AgentBackendDeveloper | BE-CREDIT-001 |
| BE-CREDIT-003 | Implementer Spring Security + JWT (JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, CustomUserDetailsService), seed data.sql (comptes CONSEILLER/RESPONSABLE_CREDIT), endpoint `POST /auth/login` (DEC-002) | AgentBackendDeveloper | BE-CREDIT-002 |
| BE-CREDIT-004 | Implementer GlobalExceptionHandler (`@RestControllerAdvice`) + exceptions metier dediees + codes d'erreur standardises (DEC-005) ; configurer Bean Validation globale (DEC-004) | AgentBackendDeveloper | BE-CREDIT-002 |

### Implementation Phase 3 - Backend Business Logic & APIs

- GOAL-003: Livrer l'ensemble des endpoints metier (clients, demandes, simulation, decisions, historique, dashboard) avec les regles de gestion.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| BE-CREDIT-005 | Implementer ClientService/ClientServiceImpl/ClientController : `POST /clients`, `GET /clients`, `GET /clients/{id}` avec validation RG-BANK-01 (messages par champ) | AgentBackendDeveloper | BE-CREDIT-003, BE-CREDIT-004 |
| BE-CREDIT-006 | Implementer SimulationService (formule mensualite/taux/score normative RG-BANK-04/08) + DemandeCreditService/Controller : `POST /demandes`, `GET /demandes`, `GET /demandes/{id}`, `POST /demandes/{id}/simuler` avec validation RG-BANK-02/03 | AgentBackendDeveloper | BE-CREDIT-005 |
| BE-CREDIT-007 | Implementer EligibiliteService (RG-BANK-06) + DecisionService/Controller : `POST /demandes/{id}/soumettre`, `/annuler`, `/analyser`, `/accepter`, `/refuser` avec verification de transition par role (DEC-ENT-003, DEC-009), commentaire obligatoire refus (RG-BANK-07), creation atomique HistoriqueDecision a chaque transition (DEC-006), snapshot tauxEndettement (DEC-ENT-002) | AgentBackendDeveloper | BE-CREDIT-006 |
| BE-CREDIT-008 | Implementer HistoriqueService/Controller : `GET /demandes/{id}/historique` (ordre chronologique, etat vide explicite) | AgentBackendDeveloper | BE-CREDIT-007 |
| BE-CREDIT-009 | Implementer DashboardService/Controller : `GET /dashboard` (compteurs par statut, montant total, taux moyen, exclusion BROUILLON/ANNULEE — Q4/AC-ARCH-006) | AgentBackendDeveloper | BE-CREDIT-007 |
| BE-CREDIT-010 | Integrer springdoc-openapi (Swagger UI + `/v3/api-docs`), annoter tous les DTO/controllers (DEC-008, AC-ARCH-005) | AgentBackendDeveloper | BE-CREDIT-009 |
| BE-CREDIT-011 | Finaliser couverture de tests backend (unitaires services + integration endpoints), couvrir cas limites E1-E25 de la spec fonctionnelle | AgentBackendDeveloper | BE-CREDIT-010 |

### Implementation Phase 4 - Frontend Core, Auth & Shell

- GOAL-004: Fournir l'authentification, les guards par role, la charte graphique partagee et la navigation.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| FE-CREDIT-002 | Implementer core/services partages : modeles/services API generiques, JWT interceptor, error interceptor, auth.guard, role.guard | AgentFrontendDeveloper | FE-CREDIT-001 |
| FE-CREDIT-003 | Implementer feature `auth` : login.page, auth.service (login, stockage token, decodage role), routing `/login` + redirections (US-001, US-002) | AgentFrontendDeveloper | FE-CREDIT-002 |
| FE-CREDIT-004 | Implementer `shared/layout` (header/menu role-aware) + `shared/charte` (mapping couleur statut/score : bleu/vert/rouge/orange, composants badge-statut/badge-score, avertissements pedagogiques) | AgentFrontendDeveloper | FE-CREDIT-003 |

### Implementation Phase 5 - Frontend Clients & Demandes

- GOAL-005: Construire les ecrans clients et creation/simulation de demandes.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| FE-CREDIT-005 | Implementer feature `clients` : client.model/service, client-list.page, client-create.page, client-detail.page (US-003..006), consommant `POST/GET /clients`, `GET /clients/{id}` | AgentFrontendDeveloper | FE-CREDIT-004, BE-CREDIT-005 |
| FE-CREDIT-006 | Implementer feature `demandes` (creation) : demande-credit.model/service, demande-create.page avec selection client + validation cote UI des bornes RG-BANK-02/03 (US-007, US-008), consommant `POST /demandes` | AgentFrontendDeveloper | FE-CREDIT-005, BE-CREDIT-006 |
| FE-CREDIT-007 | Implementer simulation dans demande-detail.page : appel `POST /demandes/{id}/simuler`, affichage mensualite/taux/score avec charte couleur et avertissement pedagogique si score FAIBLE/MOYEN (US-009) | AgentFrontendDeveloper | FE-CREDIT-006 |
| FE-CREDIT-008 | Implementer actions soumission/annulation sur demande-detail.page (US-010, US-011), consommant `POST /demandes/{id}/soumettre` et `/annuler` | AgentFrontendDeveloper | FE-CREDIT-007, BE-CREDIT-007 |

### Implementation Phase 6 - Frontend Decision, Historique & Dashboard

- GOAL-006: Construire les ecrans de decision (role RESPONSABLE_CREDIT), historique et tableau de bord.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| FE-CREDIT-009 | Implementer feature `decisions` : decision.page (passer en analyse / accepter / refuser), guard role RESPONSABLE_CREDIT, formulaire commentaire obligatoire pour refus (US-012..015), consommant `/analyser`, `/accepter`, `/refuser` | AgentFrontendDeveloper | FE-CREDIT-008, BE-CREDIT-007 |
| FE-CREDIT-010 | Implementer feature `historique` : historique.page (ou section demande-detail) affichant les transitions chronologiques (US-016), consommant `GET /demandes/{id}/historique` | AgentFrontendDeveloper | FE-CREDIT-009, BE-CREDIT-008 |
| FE-CREDIT-011 | Implementer feature `dashboard` : dashboard.page (compteurs, montant total, taux moyen, etats vides) avec charte couleur (US-017), consommant `GET /dashboard` | AgentFrontendDeveloper | FE-CREDIT-010, BE-CREDIT-009 |
| FE-CREDIT-012 | Finaliser couverture de tests frontend (unitaires composants/services, guards, routing) et verification accessibilite/lisibilite (charte) | AgentFrontendDeveloper | FE-CREDIT-011 |

### Implementation Phase 7 - Integration and Final Quality Gate

- GOAL-007: Verifier l'alignement FE/BE et la readiness de livraison avant QA/Security/Deployment.

| Task | Description | Owner | Depends On |
|------|-------------|-------|------------|
| INT-CREDIT-001 | Verifier alignement des contrats API (DTO, codes HTTP, codes d'erreur) entre BE-CREDIT-* et FE-CREDIT-* pour les 16 endpoints | AgentOrchestrator | BE-CREDIT-011, FE-CREDIT-012 |
| INT-CREDIT-002 | Executer checklist securite/erreurs/navigation/tests (RG-BANK-01..08, workflow, roles) | AgentOrchestrator | INT-CREDIT-001 |
| INT-CREDIT-003 | Produire verdict READY/NOT_READY avec blockers avant dispatch AgentQA/AgentSecurity | AgentOrchestrator | INT-CREDIT-002 |

## 4. Dependencies

- **DEP-CREDIT-001**: FE-CREDIT-002 depend de FE-CREDIT-001.
- **DEP-CREDIT-002**: BE-CREDIT-003 depend de BE-CREDIT-002 (entites/enums avant securite/seed).
- **DEP-CREDIT-003**: FE-CREDIT-003 depend de BE-CREDIT-003 (contrat `/auth/login` disponible pour integration reelle, sinon developpement contre contrat documente en attendant).
- **DEP-CREDIT-004**: FE-CREDIT-005 depend de BE-CREDIT-005 (endpoints clients disponibles).
- **DEP-CREDIT-005**: FE-CREDIT-006/007 dependent de BE-CREDIT-006 (simulation).
- **DEP-CREDIT-006**: FE-CREDIT-008/009 dependent de BE-CREDIT-007 (workflow decision).
- **DEP-CREDIT-007**: FE-CREDIT-010 depend de BE-CREDIT-008 (historique).
- **DEP-CREDIT-008**: FE-CREDIT-011 depend de BE-CREDIT-009 (dashboard).
- **DEP-CREDIT-009**: INT-CREDIT-001 depend de BE-CREDIT-011 et FE-CREDIT-012 (fin des deux chaines + tests).

Note d'execution parallele : BE-CREDIT-* et FE-CREDIT-* demarrent en parallele des que l'architecture est validee (deja fait). Le frontend peut developper contre le contrat documente (spec architecture §7) avant que chaque endpoint backend correspondant soit termine, mais l'integration reelle (tests end-to-end manuels) de chaque ecran depend de l'endpoint backend associe (voir DEP-CREDIT-003..008).

## 5. Files

- **FILE-CREDIT-001**: apps/banque-credit/banque-credit-backend/*
- **FILE-CREDIT-002**: apps/banque-credit/banque-credit-frontend/*
- **FILE-CREDIT-003**: spec/spec-feature-banque-credit.md
- **FILE-CREDIT-004**: spec/spec-architecture-banque-credit.md
- **FILE-CREDIT-005**: plan/plan-feature-banque-credit.md
- **FILE-CREDIT-006**: tasks/tasks-feature-banque-credit.md
- **FILE-CREDIT-007**: orchestration/handoffs/*
- **FILE-CREDIT-008**: orchestration/status/progress-board.md

## 6. Testing Strategy

- **TEST-CREDIT-001**: Tests unitaires backend (SimulationService, EligibiliteService, table de transitions, ClientService, DashboardService), incluant bornes RG-BANK-02/03/06 et formule RG-BANK-04/08.
- **TEST-CREDIT-002**: Tests d'integration backend (MockMvc/@SpringBootTest) pour les 16 endpoints : succes, 400/401/403/404/409/422 selon cas.
- **TEST-CREDIT-003**: Tests unitaires frontend (composants pages, services HTTP, guards auth/role, interceptors JWT/erreur).
- **TEST-CREDIT-004**: Tests de routing frontend (redirection non authentifie, blocage route par role).
- **TEST-CREDIT-005**: Verification manuelle E2E (Swagger UI + frontend) du workflow complet : creation client -> creation demande -> simulation -> soumission -> analyse -> acceptation/refus -> historique -> dashboard.
- **TEST-CREDIT-006**: Couverture des cas limites E1-E25 de la spec fonctionnelle (bornes montant/duree, refus sans commentaire, transitions invalides, endettement/revenu/montant hors criteres, annulation, acces par role).

Cible de couverture : ces tests seront detailles et implementes par AgentQA en phase QA (skills qa-create-plan/qa-create-tasks/qa-implement-tests-junit-mockito), sur la base de cette strategie.

## 7. Rollback and Migration Notes

- Base H2 mode fichier : suppression du fichier `./data/banquecredit*` permet un reset complet en environnement de demonstration (pas de migration de donnees reelles a prevoir, projet pedagogique).
- Aucune version anterieure de "banque-credit" n'existe dans ce depot : pas de migration de schema a gerer, uniquement une creation initiale (DDL auto via `spring.jpa.hibernate.ddl-auto=update` ou `create` en dev).
- En cas de rollback d'une tache backend, revenir au dernier commit valide du module `apps/banque-credit/banque-credit-backend` sans impact sur `apps/mini-banque` (perimetre physiquement isole, CON-001).

## 8. Risks & Assumptions

- **RISK-CREDIT-001**: Desalignement entre le contrat documente (spec architecture §7) et l'implementation reelle des DTO — mitigation : INT-CREDIT-001 verifie explicitement chaque endpoint avant la QA/Security.
- **RISK-CREDIT-002**: Divergence sur la formule de mensualite (amortissement) si mal implementee — mitigation : formule normative figee dans RG-BANK-04 (spec fonctionnelle), a tester unitairement en priorite (TEST-CREDIT-001).
- **RISK-CREDIT-003**: Incoherence de la table de transitions cote frontend (boutons d'action affiches a tort) — mitigation : centraliser la logique d'affichage conditionnel des actions dans un service partage alimente par le statut courant, teste (FE-CREDIT-012).
- **RISK-CREDIT-004**: Oubli de l'historisation sur une transition — mitigation : DEC-006 (transaction atomique statut+historique) verifiee par tests d'integration dedies (TEST-CREDIT-002).
- **RISK-CREDIT-005**: Incoherence de la charte couleur entre statuts et scores selon les pages — mitigation : composants partages badge-statut/badge-score centralisant le mapping (FE-CREDIT-004).

- **ASSUMPTION-CREDIT-001**: Java 17+, Node/Angular CLI recents et Maven disponibles localement pour le build.
- **ASSUMPTION-CREDIT-002**: H2 mode fichier est acceptable pour cette iteration pedagogique (pas de cible de production reelle).
- **ASSUMPTION-CREDIT-003**: Comptes CONSEILLER/RESPONSABLE_CREDIT pre-provisionnes via seed data, aucun flux d'inscription requis.
- **ASSUMPTION-CREDIT-004**: Les arbitrages Q1-Q5 de la spec fonctionnelle (v1.1) restent valides pour toute la duree de l'implementation.

## 9. Related Specifications / Further Reading

- spec/spec-feature-banque-credit.md
- spec/spec-architecture-banque-credit.md


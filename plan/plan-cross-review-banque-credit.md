# Plan de revue croisée (cross-review) — Feature "banque-credit"

Auteur : AgentReviewer
Statut : Exécuté (voir review/cross-review-banque-credit.md pour les constats détaillés et le verdict de gate)
Sources : spec/spec-feature-banque-credit.md (v1.1, US-001..017, RG-BANK-01..08, AC-*, edge cases E1-E25), spec/spec-architecture-banque-credit.md (v1.0, ENT-001..004, DEC-001..009, §7 API, §8 traçabilité), livrables AgentBackendDeveloper/AgentFrontendDeveloper, plan/plan-tests-banque-credit.md + tasks/tasks-tests-banque-credit.md (QA-CREDIT-001), plan/plan-security-review-banque-credit.md + review/security-review-banque-credit.md (SEC-CREDIT-001), plan/plan-deployment-banque-credit.md (DEPLOY-CREDIT-001), orchestration/status/progress-board.md (INT-CREDIT-001).

---

## 1. Périmètre de la revue croisée

### Backend (`apps/banque-credit/banque-credit-backend/src/main/java/formulAI/project/bank/banqueCredit`)
- `model/Statut.java`, `model/ScoreSimplifie.java`, `model/Role.java`, `model/TransitionRules.java`, `model/Client.java`, `model/DemandeCredit.java`, `model/HistoriqueDecision.java`, `model/User.java`
- `dto/*` (`ClientCreateDto`, `ClientDto`, `ClientDetailDto`, `DemandeCreditCreateDto`, `DemandeCreditDto`, `DemandeCreditSummaryDto`, `SimulationResultDto`, `DecisionRefusDto`, `HistoriqueDecisionDto`, `DashboardDto`, `AuthRequestDto`/`AuthResponseDto`, `ErrorResponseDto`)
- `controller/*` (`AuthController`, `ClientController`, `DemandeCreditController`, `DecisionController`, `HistoriqueController`, `DashboardController`)
- `service/impl/*` (`ClientServiceImpl`, `DemandeCreditServiceImpl`, `DecisionServiceImpl`, `EligibiliteServiceImpl`, `SimulationServiceImpl`, `DashboardServiceImpl`, `AuthServiceImpl`)
- `security/CustomUserDetailsService.java`, `security/JwtTokenProvider.java`
- `exception/GlobalExceptionHandler.java`

### Frontend (`apps/banque-credit/banque-credit-frontend/src/app`)
- `features/auth/role.enum.ts`, `auth.service.ts`, `auth-response.model.ts`
- `features/clients/client.model.ts`, `client-create.page.ts`, `client.service.ts`
- `features/demandes/statut.enum.ts`, `score-simplifie.enum.ts`, `demande-credit.model.ts`, `simulation-result.model.ts`, `demande-create.page.ts`, `demande-detail.page.ts`, `demande-actions.service.ts`
- `features/decisions/decision-request.model.ts`, `decision.page.ts`, `decision.service.ts`
- `features/dashboard/dashboard.page.ts`, `dashboard.model.ts`
- `core/models/api-error.model.ts`, `core/guards/role.guard.ts`, `core/guards/auth.guard.ts`
- `shared/charte/charte.constants.ts`, `badge-score.component.ts`, `badge-statut.component.ts`
- `app.routes.ts`

### Hors périmètre (rappel)
- Re-test fonctionnel ou re-exécution des suites AgentQA (133 backend / 105 frontend déjà vertes).
- Re-revue de sécurité approfondie (déjà couverte par SEC-CREDIT-001) — seules les incohérences transverses backend/frontend sont reprises ici si elles recoupent un constat sécurité (ex. `TransitionRules`).
- Modification de code (constat uniquement, remédiation renvoyée à l'agent propriétaire).

---

## 2. Points de comparaison contrat backend/frontend (COMPARE-xxx)

| ID | Point de comparaison | Artefacts backend | Artefacts frontend | RG-BANK / US associée |
|---|---|---|---|---|
| COMPARE-01 | Enum `Statut` — orthographe/casse exactes | `model/Statut.java` | `features/demandes/statut.enum.ts` | RG-BANK-05 |
| COMPARE-02 | Enum `ScoreSimplifie` — orthographe/casse exactes | `model/ScoreSimplifie.java` | `features/demandes/score-simplifie.enum.ts` | RG-BANK-08 |
| COMPARE-03 | Enum `Role` — orthographe/casse exactes + claim JWT | `model/Role.java`, `AuthServiceImpl.login`, `CustomUserDetailsService` | `features/auth/role.enum.ts`, `auth.service.ts` | Acteurs §2.1, Q5 |
| COMPARE-04 | Forme du corps d'erreur (`ErrorResponseDto.erreurs`) — tableau vs dictionnaire | `dto/ErrorResponseDto.java` | `core/models/api-error.model.ts`, `client-create.page.ts`, `demande-create.page.ts` | AC-004-4, AC-008-x (déjà fixé par INT-CREDIT-001) |
| COMPARE-05 | Validation Client (nom/revenu/charges) — bornes et obligation | `dto/ClientCreateDto.java` (Bean Validation) | `client-create.page.ts` (Reactive Forms Validators) | RG-BANK-01 |
| COMPARE-06 | Validation montant demandé — bornes 1000 (exclusif) / 100000 (inclusif) | `dto/DemandeCreditCreateDto.java` (`@DecimalMin`/`@DecimalMax`) | `demande-create.page.ts` (`montantMinExclusive`, `Validators.max`) | RG-BANK-02 |
| COMPARE-07 | Validation durée — bornes 12/84 inclusives | `dto/DemandeCreditCreateDto.java` (`@Min`/`@Max`) | `demande-create.page.ts` (`Validators.min/max`) | RG-BANK-03 |
| COMPARE-08 | Formule mensualité/taux d'endettement — source unique de vérité | `service/impl/SimulationServiceImpl.java` | Aucune réimplémentation attendue côté frontend (affichage seul) | RG-BANK-04 |
| COMPARE-09 | Transitions de statut (table RG-BANK-05) — enforcement réel vs code mort vs visibilité UI | `model/TransitionRules.java` (non appelé), `service/impl/DecisionServiceImpl.java` (if/else dupliqués) | `features/demandes/demande-actions.service.ts` (visibilité boutons) | RG-BANK-05, US-010..014 |
| COMPARE-10 | Éligibilité cumulative à l'acceptation — source unique de vérité | `service/impl/EligibiliteServiceImpl.java` | Aucune réimplémentation attendue côté frontend (lecture du 422 uniquement) | RG-BANK-06, US-013 |
| COMPARE-11 | Commentaire obligatoire au refus — cohérence de la règle de "vide" (trim) | `service/impl/DecisionServiceImpl.refuser` | `decision.page.ts` (`commentaireObligatoire`) | RG-BANK-07, US-014, US-015 |
| COMPARE-12 | Score simplifié par bandes — source unique de vérité | `service/impl/SimulationServiceImpl.calculerScore` | `shared/charte/badge-score.component.ts` (affichage seul) | RG-BANK-08, US-009 |
| COMPARE-13 | Rôles requis par endpoint vs guards de route | `controller/*`, `service/impl/*` (`@PreAuthorize`) | `app.routes.ts` (`roleGuard`) | Q5, AC-ARCH-003 |
| COMPARE-14 | Périmètre des agrégats Dashboard (exclusion BROUILLON/ANNULEE) | `service/impl/DashboardServiceImpl.java` | `features/dashboard/dashboard.page.ts` (affichage seul) | Q4, AC-ARCH-006, US-017 |
| COMPARE-15 | Traçabilité US-001..017 / RG-BANK-01..08 → artefact livré concret | Controllers + services | Pages + services | Toutes |

---

## 3. Checklist de cohérence des règles métier (RG-BANK-01 à RG-BANK-08)

| RG-BANK | Backend (artefact) | Frontend (artefact) | Tests (AgentQA) | À vérifier |
|---|---|---|---|---|
| RG-BANK-01 | `ClientCreateDto` + `ClientServiceImpl` | `client-create.page.ts` | Tests unitaires backend + spec frontend | Cohérence obligatoire/bornes, messages |
| RG-BANK-02 | `DemandeCreditCreateDto` | `demande-create.page.ts` | idem | Bornes exacte inclusif/exclusif |
| RG-BANK-03 | `DemandeCreditCreateDto` | `demande-create.page.ts` | idem | Bornes inclusives 12/84 |
| RG-BANK-04 | `SimulationServiceImpl` | Affichage seul | idem | Pas de réimplémentation frontend |
| RG-BANK-05 | `DecisionServiceImpl` + `TransitionRules` (mort) | `demande-actions.service.ts` | idem | Enforcement serveur réel vs visibilité UI vs code mort |
| RG-BANK-06 | `EligibiliteServiceImpl` | Aucune (lecture 422) | idem | Pas de contournement/reimplémentation frontend |
| RG-BANK-07 | `DecisionServiceImpl.refuser` | `decision.page.ts` | idem | Règle de "vide" (trim) identique |
| RG-BANK-08 | `SimulationServiceImpl.calculerScore` | `badge-score.component.ts` | idem | Bandes identiques, pas de reimplémentation |

---

## 4. Dette technique et zones de duplication à inspecter (DEBT-xxx)

| ID | Zone | Constat attendu à vérifier | Propriétaire suggéré |
|---|---|---|---|
| DEBT-01 | `model/TransitionRules.java` non appelé (flag SEC-TASK-001) | Décider : brancher dans `DecisionServiceImpl` ou supprimer | AgentBackendDeveloper |
| DEBT-02 | Triple représentation de la table de transitions RG-BANK-05 (`TransitionRules` mort, if/else `DecisionServiceImpl`, `DemandeActionsService` frontend) | Risque de divergence future si une des trois est modifiée sans les autres | AgentBackendDeveloper / AgentFrontendDeveloper |
| DEBT-03 | Absence de handler catch-all `Exception.class` dans `GlobalExceptionHandler` (déjà noté SEC-TASK-004) | Non bloquant, recommandation défensive reprise pour mémoire | AgentBackendDeveloper |
| DEBT-04 | Restriction frontend de la route `/clients` (liste) au seul rôle CONSEILLER alors que l'endpoint `GET /clients` est ouvert aux deux rôles côté backend | Vérifier que c'est un choix documenté (architecture §5.3) et non un oubli | AgentArchitect (confirmation) |
| DEBT-05 | 8 CVE HIGH Angular en production + upgrade majeur non traité (déjà noté SEC-TASK-007) | Repris pour mémoire, hors remédiation directe de cette revue | AgentArchitect/AgentOrchestrator |

---

## 5. Risques et hypothèses (RISK-xxx)

| ID | Risque/hypothèse | Sévérité | Statut |
|---|---|---|---|
| RISK-01 | Une incohérence de contrat non détectée pourrait permettre à un rôle de contourner une règle métier (ex. acceptation sans éligibilité) si le frontend recalculait ses propres règles au lieu de se fier au backend | Blocker si avéré | À vérifier par inspection directe (§2, §3) |
| RISK-02 | Le code mort `TransitionRules` pourrait diverger silencieusement de la logique réelle si un développeur futur modifie l'un sans l'autre (déjà signalé par AgentSecurity) | Low (aujourd'hui), Medium (risque futur) | Décision requise dans cette revue (DEBT-01) |
| RISK-03 | Le format du corps d'erreur (`erreurs`) ayant déjà causé un bug (INT-CREDIT-001), un risque de régression existe si un nouveau endpoint d'erreur est ajouté sans respecter le même contrat | Medium | À vérifier sur l'ensemble des pages consommant `ApiErrorResponse` |
| RISK-04 | AC-013-1 (cas limite mathématiquement infaisable, triple borne simultanée) déjà noté non bloquant par AgentQA — pas re-traité ici, juste repris pour traçabilité | Low, non bloquant | Hérité de QA-CREDIT-001, à adresser par AgentBA |
| RISK-05 | Risques de déploiement réel (RISK-001/002/005 de DEPLOY-CREDIT-001) restent ouverts et non bloquants pour ce gate (hors périmètre de la revue croisée) | Low/Medium, non bloquant pour ce gate | Hérité de DEPLOY-CREDIT-001 |

### Hypothèses (assumptions)
- ASM-REV-01 : les livrables analysés sont ceux actuellement présents sur le dépôt au moment de cette revue (pas de changement en cours en parallèle).
- ASM-REV-02 : les 133 tests backend et 105 tests frontend verts (QA-CREDIT-001) restent valides ; cette revue ne les ré-exécute pas mais vérifie leur couverture déclarée des RG-BANK.
- ASM-REV-03 : les 2 corrections auto-appliquées par AgentSecurity (jwt.secret, CORS) sont considérées acquises et ne sont pas re-vérifiées en détail ici (hors périmètre cross-review).

---

## 6. Tâches de revue croisée à exécuter (REV-TASK)

Voir `review/cross-review-banque-credit.md` pour le détail des constats par tâche, une tâche par groupe de COMPARE-xxx :
- REV-TASK-01 — Enums et rôles (COMPARE-01, COMPARE-02, COMPARE-03, COMPARE-13)
- REV-TASK-02 — Contrat d'erreur API (COMPARE-04)
- REV-TASK-03 — Validation RG-BANK-01/02/03 (COMPARE-05, COMPARE-06, COMPARE-07)
- REV-TASK-04 — RG-BANK-04/08 simulation et score (COMPARE-08, COMPARE-12)
- REV-TASK-05 — RG-BANK-05 transitions et code mort `TransitionRules` (COMPARE-09, DEBT-01, DEBT-02)
- REV-TASK-06 — RG-BANK-06 éligibilité (COMPARE-10)
- REV-TASK-07 — RG-BANK-07 commentaire obligatoire (COMPARE-11)
- REV-TASK-08 — Dashboard Q4 (COMPARE-14)
- REV-TASK-09 — Traçabilité US-001..017 / RG-BANK-01..08 (COMPARE-15)

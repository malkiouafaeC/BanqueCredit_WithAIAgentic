# Plan de tests — Feature "banque-credit"

Statut : Approuvé pour exécution (AgentQA)
Version : 1.0
Sources : spec/spec-feature-banque-credit.md (v1.1), spec/spec-architecture-banque-credit.md (v1.0), tasks/tasks-feature-banque-credit.md, code livré (78 tests backend / 77 tests frontend existants, couverture frontend 85.5% statements / 56.6% branches)

---

## 1. Périmètre et état des lieux

### 1.1 Backend (`apps/banque-credit/banque-credit-backend`, package `formulAI.project.bank.banqueCredit`)
Suite existante (78 tests, verte) :
- `TransitionRulesTest` (unit, pur) — RG-BANK-05
- `SimulationServiceImplTest` (unit, pur) — RG-BANK-04/08
- `GlobalExceptionHandlerTest` (unit, `@RestControllerAdvice` testé isolément) — DEC-005
- `RepositoryIntegrationTest` (`@DataJpaTest`-like) — persistance ENT-001..003
- `AuthControllerIntegrationTest`, `ClientControllerIntegrationTest`, `DemandeCreditControllerIntegrationTest`, `DecisionControllerIntegrationTest`, `HistoriqueControllerIntegrationTest`, `DashboardControllerIntegrationTest`, `OpenApiIntegrationTest` — tous `@SpringBootTest` + `MockMvc` (contexte complet, DB H2 réelle).

**Constat pyramide** : la quasi-totalité des règles métier (RG-BANK-05, 06, 07, agrégats dashboard) ne sont vérifiées qu'au travers de tests d'intégration `@SpringBootTest` complets. Aucun test unitaire Mockito n'isole `ClientServiceImpl`, `DemandeCreditServiceImpl`, `EligibiliteServiceImpl`, `DecisionServiceImpl`, `HistoriqueServiceImpl`, `DashboardServiceImpl`, `AuthServiceImpl` de la couche persistance. Cela alourdit le temps d'exécution, réduit la granularité de diagnostic en cas d'échec, et inverse la pyramide (trop d'intégration, pas assez d'unitaire) par rapport au principe directeur.

### 1.2 Frontend (`apps/banque-credit/banque-credit-frontend`)
Suite existante (77 tests, verte), couverture mesurée localement : Statements 85.39%, Branches 57.54%, Functions 85.95%, Lines 86.2%.

Points chauds de couverture de branches (issus du rapport Istanbul) :
| Fichier | Branches | Cause probable |
|---|---|---|
| `features/auth/auth.service.ts` | 0% (0/10) | **Aucun spec dédié** (`auth.service.spec.ts` absent) |
| `features/decisions/decision.page.ts` | 33.33% (7/21) | Branches d'erreur 422/409/400/403/générique dans `run()` non toutes testées |
| `shared/charte/badge-score.component.ts` | 50% (2/4) | Cas `showWarning=false` (score EXCELLENT/BON) non testé |
| `shared/charte/badge-statut.component.ts` | 50% (2/4) | Cas statut inconnu / fallback `charte-neutral` non testé |
| `features/clients/client-create.page.ts` | 57.14% (8/14) | Branche `body?.champ && body.message` (erreur 400 mono-champ, hors `erreurs[]`) non testée |
| `features/demandes/demande-credit.service.ts` | 0% (0/2) | Branches optionnelles `filters?.statut` / `filters?.clientId` de `list()` non exercées avec filtres réels |
| `features/demandes/demande-create.page.ts` | 73.33% (11/15) | Branche erreur générale (sans `erreurs[]`) partiellement couverte |
| `features/demandes/demande-detail.page.ts` | 50% (4/8) | États d'erreur/branches conditionnelles d'affichage (soumettre/annuler visibles selon rôle+statut) incomplets |

**Constat contrat erreur** : `client-create.page.spec.ts` et `demande-create.page.spec.ts` testent déjà correctement la forme `erreurs: [{champ, message}]` (tableau, pas dictionnaire) — **le bug de contrat est bien couvert**, aucune régression à craindre ici. Confirmé par lecture de `ErrorResponseDto.java` (champ `erreurs: List<ChampErreur>`).

### 1.3 Note spec (AC-013-1 / RG-BANK-06)
Le cas AC-013-1 (35% endettement ET 1500 revenu ET 50000 montant simultanément) est **mathématiquement infaisable** : à montant=50000/durée=84/taux=0 (mensualité minimale ≈595.24), revenu=1500, charges=0, le taux d'endettement est déjà ≈39.7% (> 35%). `DecisionControllerIntegrationTest.accepterAvecMontantExactement50000EstAccepte()` documente ceci explicitement en commentaire et teste chaque borne indépendamment (charges ajustées pour isoler chaque condition). **Ceci est déjà géré correctement dans les tests existants** — aucune action corrective sur les tests, mais l'incohérence spec doit être remontée à AgentBA (voir §5).

---

## 2. Test pyramid breakdown

Cible visée après ce batch (approximatif, sur l'ensemble backend+frontend) :

| Niveau | Backend existant | Backend cible (ajout QA) | Frontend existant | Frontend cible (ajout QA) |
|---|---|---|---|---|
| Unitaire (services purs, composants isolés, Mockito/TestBed+spies) | 3 classes (~22 tests) | +7 classes service (Mockito) | ~60 tests (composants/services avec spies) | +~20 tests ciblés branches |
| Intégration (MockMvc `@SpringBootTest`, DTO contract) | 7 classes (~53 tests) | Pas d'ajout (déjà bien couvert) | Contract shape déjà testé (client/demande create) | Pas d'ajout majeur |
| Non-régression (parcours critiques bout-en-bout) | 0 dédié | +1 classe parcours complet (E2E logique via MockMvc) | 0 dédié | Documenté en plan manuel (§4) |

Rationale : l'essentiel de l'effort QA porte sur le **rééquilibrage vers l'unitaire côté backend** (services Mockito) et le **comblement des branches non testées côté frontend**, sans dupliquer les ~155 tests déjà verts.

## 3. Tools per layer

| Couche | Outil |
|---|---|
| Backend unitaire | JUnit5 + Mockito (`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`) |
| Backend intégration | `@SpringBootTest` + `@AutoConfigureMockMvc` + H2 fichier de test (`application-test.properties`) |
| Backend non-régression | MockMvc, parcours complet multi-étapes dans une seule classe dédiée |
| Frontend unitaire | Jasmine + Karma, `TestBed`, `jasmine.createSpyObj`, `HttpClientTestingModule` où pertinent |
| Frontend couverture | `ng test --code-coverage` (Istanbul), rapport HTML `coverage/banque-credit-frontend` |

## 4. Business rule to test-case mapping plan

| RG-BANK | Couverture existante | TEST-xxx planifiés (nouveaux) |
|---|---|---|
| RG-BANK-01 (champs Client obligatoires) | `ClientControllerIntegrationTest` (400 par champ) | TEST-001 : `ClientServiceImpl` unitaire — création avec champs valides (happy path isolé du repository) |
| RG-BANK-02 (bornes montant 1000 excl./100000 incl.) | `DemandeCreditControllerIntegrationTest` (E1-E3), `demande-create.page.spec.ts` (E1/E2) | TEST-002 : `DemandeCreditServiceImpl` unitaire — délégation correcte au repository/simulation, pas de re-vérification des bornes (déjà couvert Bean Validation), focus sur `lister()` filtres |
| RG-BANK-03 (bornes durée 12-84 incl.) | Idem RG-BANK-02 (E4-E7) | Idem |
| RG-BANK-04 (formule mensualité/taux) | `SimulationServiceImplTest` (unitaire, bornes 20/35/50%) | Pas d'ajout (déjà bien couvert unitairement) |
| RG-BANK-05 (statuts/transitions) | `DecisionControllerIntegrationTest` (intégration complète) | TEST-003 : `DecisionServiceImpl` unitaire (Mockito) — 5 transitions (soumettre/annuler/analyser/accepter/refuser), cas nominal + transition invalide, sans dépendance DB |
| RG-BANK-06 (éligibilité cumulative 35%/1500/50000) | `DecisionControllerIntegrationTest` (bornes exactes + violations isolées), doc AC-013-1 | TEST-004 : `EligibiliteServiceImpl` unitaire — chaque borne exacte (35.00%, 1500, 50000 → OK) et chaque violation isolée (35.01%, 1499, 50001 → exception), combinaisons croisées |
| RG-BANK-07 (commentaire obligatoire refus) | `DecisionControllerIntegrationTest` (absent/espaces/valide) | Couvert dans TEST-003 (refuser) |
| RG-BANK-08 (score simplifié par bandes) | `SimulationServiceImplTest` | Pas d'ajout |
| N/A (Dashboard Q4/AC-017, AC-ARCH-006) | `DashboardControllerIntegrationTest` (2 tests seulement : nominal minimal + vide) | TEST-005 : `DashboardServiceImpl` unitaire — cas vide, cas exclusion BROUILLON/ANNULEE explicite, calcul moyenne arrondie |
| N/A (Historique, US-016) | `HistoriqueControllerIntegrationTest` | TEST-006 : `HistoriqueServiceImpl` unitaire — 404 si demande inexistante, ordre chronologique |
| N/A (Auth, US-001, AC-001-3) | `AuthControllerIntegrationTest` | TEST-007 : `AuthServiceImpl` unitaire — succès, mauvais mot de passe, utilisateur inactif, utilisateur inexistant (4 branches) |
| N/A (Non-régression parcours complet) | Aucun test dédié bout-en-bout | TEST-008 : parcours complet MockMvc login→client→demande→simulation→soumission→analyse→décision→historique→dashboard (non-régression critique) |

Frontend (branches ciblées, mappées aux AC concernées) :

| Cible | AC/RG associée | TEST-xxx |
|---|---|---|
| `auth.service.ts` | US-001, AC-001-x (aucun spec existant) | TEST-101 : login succès (stockage sessionStorage), `hasRole` (rôle unique/array), `logout`, lecture storage invalide (catch JSON.parse), `getToken` null |
| `decision.page.ts` `run()` | AC-012-2/3, AC-013-2..5, AC-015-1..3 | TEST-102 : branches 422/409/400/403/générique de gestion d'erreur |
| `badge-score.component.ts` | AC-009-6 (avertissement pédagogique) | TEST-103 : `showWarning=false` pour EXCELLENT/BON |
| `badge-statut.component.ts` | Charte §5.8 | TEST-104 : statut connu vs fallback |
| `client-create.page.ts` `applyServerError` | AC-004-x (contrat erreur) | TEST-105 : branche mono-champ (`body.champ && body.message`, sans `erreurs[]`) |
| `demande-credit.service.ts` `list()` | US-007 (filtres liste) | TEST-106 : filtres `statut` seul, `clientId` seul, aucun filtre |
| `demande-detail.page.ts` | AC-010-x, AC-011-x (visibilité conditionnelle actions) | TEST-107 : branches d'affichage conditionnel selon statut/rôle, erreurs 409/403 |
| Guards (déjà 100%) | Q5 | Pas d'ajout |

## 5. Coverage targets and critical flows for non-regression

- **Backend** : maintenir 100% des 78 tests existants verts + ajout de 7 classes unitaires Mockito (~35-45 tests supplémentaires estimés) + 1 classe non-régression parcours complet. Aucune régression tolérée.
- **Frontend** : cible **branches ≥ 75%** (actuellement 57.54%), statements maintenu ≥ 85%. Priorité : `auth.service.ts` (0% → ≥90%), `decision.page.ts` (33% → ≥80%), badges (50% → 100%).
- **Flux critique de non-régression** (à documenter comme baseline pour AgentReviewer/AgentDeployment) : login → création client → création demande → simulation → soumission → passage en analyse → décision (accepter ou refuser) → historique → dashboard. Testé logiquement côté backend (TEST-008) ; **aucun test end-to-end réel contre un backend démarré n'existe** (voir §6 risques).

## 6. Risks and assumptions

- **RISK-QA-01** : Pas d'environnement e2e (Cypress/Playwright) disponible dans l'outillage courant → le parcours complet ne peut être vérifié qu'au niveau logique (MockMvc backend) + tests unitaires frontend isolés. **Recommandation** : vérification manuelle scriptée (checklist) à exécuter par AgentDeployment/AgentReviewer avant mise en production, décrite en annexe du rapport final.
- **RISK-QA-02 (spec inconsistency, non bloquant pour les tests)** : AC-013-1 (triple borne simultanée) est mathématiquement infaisable sous RG-BANK-02/03/04. Les tests existants et nouveaux testent chaque condition indépendamment (déjà fait, conforme à la pratique documentée dans le code). **À remonter à AgentBA** pour corriger l'AC dans une prochaine révision de spec (ex : reformuler en 3 AC séparées plutôt qu'une AC combinée).
- **ASSUMPTION-QA-01** : Les tests unitaires Mockito n'ont pas vocation à dupliquer les assertions déjà faites au niveau intégration (codes HTTP, JSON) — ils isolent la logique métier pure (branches, exceptions, délégation) pour accélérer le feedback loop et améliorer la granularité de diagnostic.
- **ASSUMPTION-QA-02** : Les comptes de test (`conseiller1`/`Conseiller123!`, `responsable1`/`Responsable123!`) proviennent du seed `data.sql` existant et restent stables entre exécutions.
- **RISK-QA-03** : `demande-credit.service.ts.list()` a seulement 2 branches (statut/clientId) non testées — risque faible mais rapide à corriger (TEST-106).

## 7. Definition of done pour ce plan

- Chaque RG-BANK-01 à 08 a au moins un test explicite backend et/ou frontend (unitaire ou intégration).
- Aucun test existant n'est modifié de façon à changer son comportement (uniquement ajout).
- Le rapport de gate final (qa-quality-gate) documente pass/fail par zone et le verdict global.


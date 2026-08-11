# Spécification d'architecture — Feature "banque-credit"

Statut : Approuvé — AgentBackendDeveloper et AgentFrontendDeveloper peuvent démarrer l'implémentation
Version : 1.0
Auteur : AgentArchitect (validé par AgentOrchestrator)
Source : spec/spec-feature-banque-credit.md (v1.1, Approuvé, Q1-Q5 arbitrées et définitives)

---

## 1. Purpose and scope

### 1.1 Objectif de ce document
Traduire la spécification fonctionnelle approuvée (US-001..017, RG-BANK-01..08) en architecture technique déterministe, couvrant :
- le découpage des packages backend,
- le modèle de données et ses relations JPA,
- l'architecture des features frontend,
- les décisions techniques transverses (sécurité, packaging, validation, transactions, persistance, documentation API),
- le contrat d'API concret (endpoints, DTO, rôles, codes HTTP),
- la traçabilité complète RG-BANK-01..08 → entités/décisions/endpoints.

Ce document est la référence contraignante pour AgentBackendDeveloper et AgentFrontendDeveloper. Toute déviation en implémentation doit être remontée à AgentArchitect.

### 1.2 Dans le périmètre
- Architecture backend Spring Boot (WAR, Tomcat) : packages, entités, services, sécurité JWT, gestion d'erreurs, Swagger.
- Architecture frontend Angular (standalone components, build statique Apache) : features, routing, guards, charte graphique.
- Modèle de données JPA/H2 et règles de transition d'état.
- Contrat API REST complet (surface fonctionnelle du §4 de la spec fonctionnelle).

### 1.3 Hors périmètre
- Code d'implémentation (repositories, services, composants Angular) — AgentBackendDeveloper / AgentFrontendDeveloper.
- Rédaction de user stories / règles métier — AgentBA (déjà livré et approuvé).
- Tests (unitaires, intégration, e2e) — AgentQA.

### 1.4 Emplacements physiques (contrainte imposée)
- Backend : `apps/banque-credit/banque-credit-backend`
- Frontend : `apps/banque-credit/banque-credit-frontend`
- Application physiquement séparée de `apps/mini-banque`.
- Package racine backend : `formulAI.project.bank.banqueCredit`

---

## 2. Assumptions

- ASM-01 : Les comptes CONSEILLER et RESPONSABLE_CREDIT sont pré-provisionnés (seed data / data.sql), aucun flux d'inscription n'est requis (conforme US-001, hors périmètre signup).
- ASM-02 : H2 est acceptable comme unique base de données pour cette itération pédagogique (pas de cible de production réelle mentionnée dans la spec fonctionnelle).
- ASM-03 : Un seul environnement de déploiement (Tomcat pour le backend WAR, Apache HTTP pour le frontend statique) ; pas de multi-tenant, pas de haute disponibilité.
- ASM-04 : `User` est un compte d'authentification autonome (staff), non lié à un `Client` (aucune US ne demande à un client fictif de se connecter).
- ASM-05 : Les montants (`revenuMensuel`, `chargesMensuelles`, `montantDemande`, `mensualiteEstimee`) sont représentés en `BigDecimal` pour éviter les erreurs d'arrondi flottant sur les calculs financiers normatifs (RG-BANK-04).

---

## 3. Backend package architecture

Package racine : `formulAI.project.bank.banqueCredit`

| Package | Contenu | Rationale (one-line) |
|---|---|---|
| `.model` | Entités JPA : `Client`, `DemandeCredit`, `HistoriqueDecision`, `User`, enums `Statut`, `ScoreSimplifie`, `Role` | Isole le modèle persistant du contrat API pour ne jamais exposer une entité JPA directement (évite fuite de structure interne / cycles de sérialisation). |
| `.dto` | DTO request/response par domaine (`ClientDto`, `DemandeCreditDto`, `SimulationResultDto`, `DecisionRequestDto`, `HistoriqueDecisionDto`, `DashboardDto`, `AuthRequestDto`/`AuthResponseDto`) | Frontière API strictement découplée des entités ; permet de faire évoluer le modèle de données sans casser le contrat public. |
| `.repository` | Interfaces Spring Data JPA (`ClientRepository`, `DemandeCreditRepository`, `HistoriqueDecisionRepository`, `UserRepository`) | Isole l'accès données ; aucune logique métier ne doit y résider (juste requêtes/dérivations). |
| `.service` | Interfaces de service (`ClientService`, `DemandeCreditService`, `EligibiliteService`, `SimulationService`, `DecisionService`, `HistoriqueService`, `DashboardService`, `AuthService`) | Interface+impl : rend chaque service mockable/testable et substituable sans impacter les controllers (cf. DEC-001). |
| `.service.impl` | Implémentations (`ClientServiceImpl`, etc.) | Sépare le contrat (métier exposé) de l'implémentation concrète, cohérent avec DEC-001. |
| `.controller` | Controllers REST (`AuthController`, `ClientController`, `DemandeCreditController`, `DecisionController`, `HistoriqueController`, `DashboardController`) | Couche fine : validation d'entrée (DTO + Bean Validation) + délégation aux services ; aucune règle métier ici. |
| `.security` | `JwtAuthenticationFilter`, `JwtTokenProvider`, `SecurityConfig`, `CustomUserDetailsService` | Isole toute la logique d'authentification/autorisation du reste de la config applicative (limite le rayon d'impact d'un changement de stratégie d'auth). |
| `.config` | `OpenApiConfig` (springdoc), `CorsConfig`, seed/data initializer config si nécessaire | Sépare la configuration technique générique de la configuration sécurité (deux préoccupations distinctes, cycles de vie différents). |
| `.exception` | `GlobalExceptionHandler` (`@ControllerAdvice`), exceptions métier dédiées (`TransitionInvalideException`, `EligibiliteNonRespecteeException`, `CommentaireObligatoireException`, `ValidationMetierException`, `RessourceNotFoundException`) | Centralise la traduction erreur métier → réponse HTTP standardisée ; évite la duplication de gestion d'erreurs par controller. |

**AC-ARCH-001** : chaque controller ne dépend que d'interfaces `.service` (jamais de `.service.impl` ni de `.repository` directement) — vérifiable par revue de code / analyse de dépendances.

---

## 4. Data model and entity relationships

### 4.1 Vue relationnelle

```
Client (1) ────< (N) DemandeCredit (1) ────< (N) HistoriqueDecision
User (staff, indépendant) ── auteur (référence logique) ──> HistoriqueDecision.auteur
```

### 4.2 ENT-001 — Client

Trace : RG-BANK-01, US-003, US-004, US-005, US-006

| Champ | Type | Contrainte | Origine |
|---|---|---|---|
| id | Long | PK, généré (IDENTITY) | technique |
| nom | String | `@NotBlank` | RG-BANK-01 |
| email | String | `@Email` (nullable, optionnel) | US-003 (info complémentaire) |
| revenuMensuel | BigDecimal | `@NotNull`, `@DecimalMin("0.01")` (strictement > 0) | RG-BANK-01 |
| chargesMensuelles | BigDecimal | `@NotNull`, `@DecimalMin("0.00")` (≥ 0) | RG-BANK-01 |
| situationProfessionnelle | String | optionnel, texte libre (Q1 retenu) | Q1 (spec §9) |
| createdAt | Instant | généré serveur, non modifiable | US-003 (AC-003-1) |

Relation : `Client (1) --- (N) DemandeCredit` via `@OneToMany(mappedBy = "client")` côté Client / `@ManyToOne` côté DemandeCredit (obligatoire, `optional = false`).

**DEC-ENT-001** : `situationProfessionnelle` reste un `String` libre (non un enum), conformément à Q1 — pas de liste fermée métier communiquée, éviter la sur-ingénierie d'un enum sans valeurs validées.

### 4.3 ENT-002 — DemandeCredit

Trace : RG-BANK-02, RG-BANK-03, RG-BANK-04, RG-BANK-05, RG-BANK-06, RG-BANK-07, RG-BANK-08, US-007..015

| Champ | Type | Contrainte | Origine |
|---|---|---|---|
| id | Long | PK, généré | technique |
| client | `Client` (`@ManyToOne`, `optional=false`) | `@NotNull` | RG-BANK-02 modèle relationnel §1.4, AC-007-3 |
| montantDemande | BigDecimal | `@NotNull`, `@DecimalMin(value="1000.00", inclusive=false)`, `@DecimalMax(value="100000.00", inclusive=true)` | RG-BANK-02 |
| dureeMois | Integer | `@NotNull`, `@Min(12)`, `@Max(84)` | RG-BANK-03 |
| tauxFictif | BigDecimal | `@NotNull`, `@DecimalMin(value="0.00", inclusive=true)` | RG-BANK-04 (cas particulier taux=0 géré) |
| statut | `Statut` (enum, `@Enumerated(STRING)`) | `@NotNull`, valeur initiale = `BROUILLON` | RG-BANK-05 |
| mensualiteEstimee | BigDecimal | **non persisté en base** (voir DEC-ENT-002) — calculé et retourné en DTO à chaque simulation, et *snapshotté* dans HistoriqueDecision au moment des transitions clés | RG-BANK-04 |
| tauxEndettement | BigDecimal (%) | **non persisté en base** — recalculé à la volée à chaque simulation/décision (voir DEC-ENT-002) | RG-BANK-04 |
| scoreSimplifie | `ScoreSimplifie` (enum) | **non persisté en base** — recalculé à chaque simulation à partir de `tauxEndettement` | RG-BANK-08 |
| commentaireDecision | String | obligatoire **uniquement** si `statut = REFUSEE` (validation manuelle en service, pas de contrainte colonne rigide) | RG-BANK-07 |
| dateSoumission | Instant | renseigné à la transition `BROUILLON → SOUMISE` | AC-010-1 |
| dateDecision | Instant | renseigné à la transition vers `ACCEPTEE`/`REFUSEE` | AC-013-1, AC-014-1 |

**DEC-ENT-002 — Persistance vs recalcul de `mensualiteEstimee` / `tauxEndettement` / `scoreSimplifie` (tranche explicitement RG-BANK-04/RG-BANK-08)** :
- Ces 3 valeurs **ne sont PAS des colonnes persistées** sur `DemandeCredit`.
- Rationale : elles dérivent entièrement de champs source (`montantDemande`, `dureeMois`, `tauxFictif`, `client.revenuMensuel`, `client.chargesMensuelles`). Les persister créerait un risque de désynchronisation si le client est modifié après simulation (ex : `revenuMensuel` changé après une simulation initiale rendrait la valeur stockée obsolète et trompeuse pour une décision).
- Elles sont exposées uniquement dans les DTO de réponse (`SimulationResultDto`, `DemandeCreditDto`) recalculées à la demande (`GET`/`POST simuler`), garantissant qu'elles reflètent toujours l'état courant du client et de la demande.
- **Exception d'audit** : au moment de chaque transition qui déclenche une `HistoriqueDecision` (notamment `EN_ANALYSE → ACCEPTEE/REFUSEE`), la valeur de `tauxEndettement` utilisée pour la décision est **snapshottée** dans `HistoriqueDecision.tauxEndettementSnapshot` (voir ENT-003) — traçabilité exigée pour justifier une décision passée même si le client est modifié ultérieurement.
- Ce choix évite toute redondance de state pouvant diverger de la source de vérité, tout en garantissant l'auditabilité exigée implicitement par RG-BANK-06/07 (justifier une décision).

### 4.4 Enum `Statut` (RG-BANK-05) — transitions explicites

Valeurs : `BROUILLON`, `SOUMISE`, `EN_ANALYSE`, `ACCEPTEE`, `REFUSEE`, `ANNULEE`

Table de transitions autorisées (état actuel → état cible : rôle autorisé) :

| De \ Vers | SOUMISE | EN_ANALYSE | ACCEPTEE | REFUSEE | ANNULEE |
|---|---|---|---|---|---|
| BROUILLON | CONSEILLER | ✗ | ✗ | ✗ | CONSEILLER |
| SOUMISE | ✗ | RESPONSABLE_CREDIT | ✗ | ✗ | CONSEILLER |
| EN_ANALYSE | ✗ | ✗ | RESPONSABLE_CREDIT (si éligible, RG-BANK-06) | RESPONSABLE_CREDIT (commentaire obligatoire, RG-BANK-07) | ✗ |
| ACCEPTEE | ✗ | ✗ | ✗ (terminal) | ✗ | ✗ |
| REFUSEE | ✗ | ✗ | ✗ | ✗ (terminal) | ✗ |
| ANNULEE | ✗ | ✗ | ✗ | ✗ | ✗ (terminal) |

**DEC-ENT-003** : la table de transitions est codée en dur dans `DemandeCreditService`/`EligibiliteService` sous forme de map statut→statuts autorisés (pas une bibliothèque de state machine externe) — rationale : 6 statuts et 6 transitions ne justifient pas une dépendance supplémentaire (évite sur-ingénierie), mais la logique est isolée dans une méthode dédiée `verifierTransitionAutorisee(statutActuel, statutCible, role)` testable indépendamment.

### 4.5 Enum `ScoreSimplifie` (RG-BANK-08)

Valeurs : `EXCELLENT` (≤20%), `BON` (>20% et ≤35%), `MOYEN` (>35% et ≤50%), `FAIBLE` (>50%).
Calcul isolé dans `SimulationService.calculerScore(tauxEndettement)`, pure function, testable indépendamment (pas de dépendance à la couche persistance).

### 4.6 ENT-003 — HistoriqueDecision

Trace : RG-BANK-05 (traçabilité de transition), RG-BANK-07 (commentaire), US-016, Q2

| Champ | Type | Contrainte | Origine |
|---|---|---|---|
| id | Long | PK, généré | technique |
| demandeCredit | `DemandeCredit` (`@ManyToOne`, `optional=false`) | `@NotNull` | US-016 |
| ancienStatut | `Statut` (nullable) | `null` uniquement pour la 1ère entrée à la création (Q2 retenu) | Q2 |
| nouveauStatut | `Statut` | `@NotNull` | RG-BANK-05 |
| commentaire | String | obligatoire si `nouveauStatut = REFUSEE` (validation manuelle service, reprend `commentaireDecision`), optionnel sinon | RG-BANK-07 |
| tauxEndettementSnapshot | BigDecimal (nullable) | renseigné pour les transitions `EN_ANALYSE→ACCEPTEE/REFUSEE` uniquement (voir DEC-ENT-002) | Audit RG-BANK-04/06 |
| auteur | `User` (`@ManyToOne`, `optional=false`) | `@NotNull` | US-016 |
| date | Instant | généré serveur | US-016 |

**DEC-ENT-004** : `auteur` est une relation `@ManyToOne` vers `User` (pas une simple chaîne de caractères) — rationale : garantit l'intégrité référentielle et permet d'afficher le rôle de l'auteur dans l'historique sans dénormalisation fragile.

**DEC-ENT-005 (Q2 appliqué)** : une entrée `HistoriqueDecision` est créée **dès la création de la `DemandeCredit`** avec `ancienStatut = null`, `nouveauStatut = BROUILLON`, `auteur` = le Conseiller créateur — assure que l'historique complet du cycle de vie est toujours disponible dès l'origine (AC-016-1/2).

### 4.7 ENT-004 — User

Trace : US-001, US-002, Acteurs §2.1, Q5

| Champ | Type | Contrainte | Origine |
|---|---|---|---|
| id | Long | PK, généré | technique |
| username | String | `@NotBlank`, unique | US-001 |
| password | String | `@NotBlank`, haché (BCrypt) | US-001 (sécurité) |
| role | `Role` (enum : `CONSEILLER`, `RESPONSABLE_CREDIT`) | `@NotNull` | Acteurs §2.1, RG rôle unique |
| actif | boolean | défaut `true` | ASM-01 (comptes pré-provisionnés, pas de désactivation exigée par la spec mais champ technique standard) |

**DEC-ENT-006** : `User` n'a **aucune relation** vers `Client` (ASM-04) — un `Client` est une personne fictive gérée par le Conseiller, jamais un principal d'authentification. Relation `User → HistoriqueDecision` uniquement via `auteur`.

**AC-ARCH-002** : le modèle de données ci-dessus (ENT-001 à ENT-004) est implémentable sans décision architecturale supplémentaire — vérifiable par relecture croisée AgentBackendDeveloper.

---

## 5. Frontend feature architecture

Structure : `src/app/features/{domaine}/{models,pages,services}` + `src/app/shared` + `src/app/core`.

### 5.1 Arborescence par feature

| Feature | models | pages | services | Rationale |
|---|---|---|---|---|
| `auth` | `auth-request.model.ts`, `auth-response.model.ts`, `role.enum.ts` | `login.page.ts` | `auth.service.ts` (login, stockage token, décodage rôle) | Isole tout le cycle d'authentification, réutilisable par les guards. |
| `dashboard` | `dashboard.model.ts` | `dashboard.page.ts` | `dashboard.service.ts` | Vue de synthèse agence (US-017), lecture seule, aucune dépendance vers les autres features métier. |
| `clients` | `client.model.ts` | `client-list.page.ts`, `client-create.page.ts`, `client-detail.page.ts` | `client.service.ts` | Domaine CRUD client (US-003..006), isolé pour être lazy-loadable indépendamment des demandes. |
| `demandes` | `demande-credit.model.ts`, `statut.enum.ts`, `simulation-result.model.ts` | `demande-create.page.ts`, `demande-detail.page.ts`, `demande-list.page.ts` | `demande-credit.service.ts`, `simulation.service.ts` | Domaine création/simulation/soumission/annulation (US-007..011), séparé de `decisions` pour respecter la frontière de rôle (Conseiller vs Responsable). |
| `decisions` | `decision-request.model.ts` | `decision.page.ts` (passer en analyse / accepter / refuser) | `decision.service.ts` | Actions réservées RESPONSABLE_CREDIT (US-012..015) ; séparé de `demandes` pour appliquer le guard de rôle au niveau route et non au niveau composant partagé. |
| `historique` | `historique-decision.model.ts` | `historique.page.ts` (ou section intégrée à `demande-detail`) | `historique.service.ts` | Lecture transverse (US-016), consommée par les deux rôles, sans logique de décision. |

### 5.2 Shared / Core

| Dossier | Contenu | Rationale |
|---|---|---|
| `shared/layout` | Header, sidebar, layout applicatif | Cohérence visuelle unique, évite duplication entre features. |
| `shared/charte` | `charte-couleurs.service.ts` ou `charte.constants.ts` (bleu = neutre/info, vert = succès/ACCEPTEE/EXCELLENT, rouge = échec/REFUSEE, orange = avertissement/MOYEN-FAIBLE) + composants `badge-statut`, `badge-score` | Centralise le mapping statut/score → couleur pour garantir la cohérence visuelle (AC-009-6) et éviter la duplication de logique de couleur dans chaque page. |
| `core/guards` | `auth.guard.ts` (authentification requise, US-002), `role.guard.ts` (paramétrable par rôle requis, Q5) | Sépare la vérification technique (token présent/valide) de la vérification métier (rôle autorisé pour la route), réutilisable sur toutes les routes protégées. |
| `core/interceptors` | `jwt.interceptor.ts` (ajout header Authorization), `error.interceptor.ts` (mapping erreurs API → notifications) | Centralise l'injection du token et la gestion d'erreur HTTP, évite duplication dans chaque service. |

### 5.3 Table de routing (extrait, avec guards)

| Route | Feature/Page | Guard(s) | Rôle requis |
|---|---|---|---|
| `/login` | `auth/login.page` | — | public |
| `/dashboard` | `dashboard/dashboard.page` | `authGuard` | CONSEILLER, RESPONSABLE_CREDIT |
| `/clients` | `clients/client-list.page` | `authGuard`, `roleGuard(CONSEILLER)` | CONSEILLER (Q5) |
| `/clients/nouveau` | `clients/client-create.page` | `authGuard`, `roleGuard(CONSEILLER)` | CONSEILLER |
| `/clients/:id` | `clients/client-detail.page` | `authGuard` | CONSEILLER, RESPONSABLE_CREDIT (lecture, cf. §7 note) |
| `/demandes/nouvelle` | `demandes/demande-create.page` | `authGuard`, `roleGuard(CONSEILLER)` | CONSEILLER |
| `/demandes/:id` | `demandes/demande-detail.page` (inclut simulation + historique) | `authGuard` | CONSEILLER, RESPONSABLE_CREDIT |
| `/demandes/:id/decision` | `decisions/decision.page` | `authGuard`, `roleGuard(RESPONSABLE_CREDIT)` | RESPONSABLE_CREDIT |

Note : la lecture de detail client/demande est ouverte aux deux rôles (nécessaire pour que le Responsable crédit instruise un dossier) ; seules les actions d'écriture sont strictement cloisonnées par rôle (Q5 = séparation des **actions**, pas de la lecture nécessaire à l'instruction).

**AC-ARCH-003** : chaque route d'action d'écriture (création client/demande, décision) est protégée par un `roleGuard` dédié — vérifiable par revue du fichier de routes.

---

## 6. Cross-cutting technical decisions

### DEC-001 — Interface + implémentation pour chaque service
Chaque service métier (`ClientService`, `DemandeCreditService`, `EligibiliteService`, `SimulationService`, `DecisionService`, `HistoriqueService`, `DashboardService`, `AuthService`) est défini comme interface avec une implémentation `*ServiceImpl`.
**Rationale** : permet le mock en test (AgentQA), et l'injection Spring par interface découple les controllers de l'implémentation concrète.

### DEC-002 — Authentification JWT stateless + claim de rôle
Le backend émet un JWT signé (HMAC-SHA256) contenant `sub` (username) et un claim `role` (`CONSEILLER`/`RESPONSABLE_CREDIT`). Aucune session serveur (stateless, `SessionCreationPolicy.STATELESS`). Le filtre `JwtAuthenticationFilter` valide le token à chaque requête et alimente le `SecurityContext`.
**Rationale** : cohérent avec une architecture WAR déployée sur Tomcat sans state partagé, simplifie le scaling horizontal (non requis ici mais bonne pratique par défaut), et le rôle porté dans le token permet l'autorisation via `@PreAuthorize` sans requête DB supplémentaire par appel.

### DEC-003 — Packaging WAR pour déploiement Tomcat
Le module backend est packagé en WAR (`<packaging>war</packaging>`), avec `SpringBootServletInitializer` pour le déploiement sur un Tomcat externe existant.
**Rationale** : contrainte explicite du projet (hébergement Tomcat cible imposé), justifie l'abandon du JAR/embedded par défaut de Spring Boot.

### DEC-004 — Bean Validation vs validation manuelle
- **Bean Validation (`jakarta.validation`)** pour les contraintes statiques et inconditionnelles : champs obligatoires Client (RG-BANK-01), bornes montant/durée (RG-BANK-02/03), format email.
- **Validation manuelle en service** pour les règles conditionnelles/cross-field : commentaire obligatoire uniquement si refus (RG-BANK-07), transitions de statut autorisées selon rôle (RG-BANK-05), critères d'éligibilité cumulatifs à l'acceptation (RG-BANK-06).
**Rationale** : Bean Validation ne peut pas exprimer nativement des règles conditionnelles multi-champs/multi-entités de façon lisible ; les isoler en service (`EligibiliteService`, `DecisionService`) les rend testables unitairement et évite des annotations `@AssertTrue` illisibles.

### DEC-005 — Gestion d'erreurs centralisée
Un unique `GlobalExceptionHandler` (`@RestControllerAdvice`) mappe chaque exception métier vers un code HTTP et un corps d'erreur standardisé :
```
{ "code": "TRANSITION_INVALIDE", "message": "...", "champ": null|"nom-du-champ", "timestamp": "..." }
```
Codes d'erreur métier : `CHAMP_OBLIGATOIRE`, `VALEUR_HORS_BORNES`, `TRANSITION_INVALIDE`, `ROLE_NON_AUTORISE`, `COMMENTAIRE_OBLIGATOIRE`, `ELIGIBILITE_NON_RESPECTEE`, `RESSOURCE_INTROUVABLE`.
**Rationale** : garantit des messages d'erreur explicites et cohérents (exigence forte des AC : "message explicite par champ") sans dupliquer le try/catch dans chaque controller.

### DEC-006 — Frontières transactionnelles
Chaque transition de statut (soumission, passage en analyse, acceptation, refus, annulation) et la création de l'entrée `HistoriqueDecision` associée s'exécutent dans **une seule méthode de service annotée `@Transactional`** (mise à jour `DemandeCredit` + insertion `HistoriqueDecision` atomiques).
**Rationale** : une transition sans trace d'historique (ou inversement) violerait la garantie de traçabilité systématique exigée par RG-BANK-05/US-016 ; l'atomicité évite tout état incohérent en cas d'échec partiel.

### DEC-007 — Choix H2
H2 en mode fichier (`jdbc:h2:file:./data/banquecredit`, pas in-memory pur) est retenu pour la persistance.
**Rationale** : projet pédagogique sans exigence de production réelle (ASM-02) ; le mode fichier (plutôt qu'in-memory) évite la perte de données entre redémarrages du serveur Tomcat pendant les démonstrations/tests manuels, tout en gardant zéro dépendance d'infrastructure externe. La console H2 peut être activée en profil dev uniquement.

### DEC-008 — Documentation API (springdoc-openapi)
Intégration de `springdoc-openapi-starter-webmvc-ui` exposant Swagger UI sur `/swagger-ui.html` et le contrat OpenAPI sur `/v3/api-docs`. Chaque DTO est annoté (`@Schema`) et chaque endpoint documenté (`@Operation`, `@ApiResponse`).
**Rationale** : exigence explicite du périmètre projet (Swagger UI doit refléter l'API) ; springdoc est le standard actuel pour Spring Boot 3 (remplace springfox, incompatible).

### DEC-009 — Rôles Spring Security
Autorisation appliquée via `@PreAuthorize("hasRole('CONSEILLER')")` / `@PreAuthorize("hasRole('RESPONSABLE_CREDIT')")` au niveau des méthodes de service (pas seulement au niveau controller), en complément de la vérification de transition (DEC-004).
**Rationale** : défense en profondeur — même si un controller était mal protégé, le service refuse l'opération ; centralise aussi la règle "rôle unique par transition" du tableau §4.4.

**AC-ARCH-004** : une requête sans JWT valide sur une route protégée retourne HTTP 401 (vérifiable par test d'intégration côté AgentQA).
**AC-ARCH-005** : `/swagger-ui.html` et `/v3/api-docs` sont accessibles et listent tous les endpoints du §7 (vérifiable manuellement après implémentation).

---

## 7. API design

Base path : `/api/v1`. Toutes les routes (sauf `/auth/login`) exigent un JWT valide (`Authorization: Bearer <token>`) → sinon **401**. Rôle non autorisé sur une action permise à un autre rôle → **403**.

### 7.1 Authentification

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| POST | `/auth/login` | public | `{ username, password }` | `{ token, username, role, expiresAt }` | 200, 401 (identifiants invalides, message générique — AC-001-3) |

### 7.2 Clients

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| POST | `/clients` | CONSEILLER | `ClientCreateDto { nom, email?, revenuMensuel, chargesMensuelles, situationProfessionnelle? }` | `ClientDto` (avec `id`, `createdAt`) | 201, 400 (validation, un message par champ), 403 |
| GET | `/clients` | CONSEILLER, RESPONSABLE_CREDIT | — (pagination optionnelle `page`, `size`) | `ClientDto[]` (nom, revenuMensuel min.) | 200 (liste vide → `[]`, AC-005-2) |
| GET | `/clients/{id}` | CONSEILLER, RESPONSABLE_CREDIT | — | `ClientDetailDto { ...client, demandes: DemandeCreditSummaryDto[] }` | 200, 404 |

### 7.3 Demandes de crédit

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| POST | `/demandes` | CONSEILLER | `DemandeCreditCreateDto { clientId, montantDemande, dureeMois, tauxFictif }` | `DemandeCreditDto` (statut=BROUILLON) | 201, 400 (bornes RG-BANK-02/03, client manquant AC-007-3), 403 |
| GET | `/demandes` | CONSEILLER, RESPONSABLE_CREDIT | filtres optionnels `statut`, `clientId` | `DemandeCreditSummaryDto[]` | 200 |
| GET | `/demandes/{id}` | CONSEILLER, RESPONSABLE_CREDIT | — | `DemandeCreditDto` (inclut dernière simulation recalculée) | 200, 404 |
| POST | `/demandes/{id}/simuler` | CONSEILLER, RESPONSABLE_CREDIT | — | `SimulationResultDto { mensualiteEstimee, tauxEndettement, scoreSimplifie }` | 200, 404 |
| POST | `/demandes/{id}/soumettre` | CONSEILLER | — | `DemandeCreditDto` (statut=SOUMISE) | 200, 409 (transition invalide, AC-010-2), 403, 404 |
| POST | `/demandes/{id}/annuler` | CONSEILLER | — | `DemandeCreditDto` (statut=ANNULEE) | 200, 409 (AC-011-3), 403 (AC-011-4), 404 |

### 7.4 Décisions

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| POST | `/demandes/{id}/analyser` | RESPONSABLE_CREDIT | — | `DemandeCreditDto` (statut=EN_ANALYSE) | 200, 409 (AC-012-2), 403 (AC-012-3), 404 |
| POST | `/demandes/{id}/accepter` | RESPONSABLE_CREDIT | — | `DemandeCreditDto` (statut=ACCEPTEE, dateDecision) | 200, 409 (transition invalide AC-013-5), 422 (éligibilité non respectée AC-013-2/3/4), 403, 404 |
| POST | `/demandes/{id}/refuser` | RESPONSABLE_CREDIT | `DecisionRefusDto { commentaire }` | `DemandeCreditDto` (statut=REFUSEE, commentaireDecision, dateDecision) | 200, 400 (commentaire vide/absent AC-015-1/2), 409 (transition invalide AC-015-3), 403, 404 |

### 7.5 Historique

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| GET | `/demandes/{id}/historique` | CONSEILLER, RESPONSABLE_CREDIT | — | `HistoriqueDecisionDto[]` (ordre chronologique, `[]` si vide — AC-016-2) | 200, 404 |

### 7.6 Dashboard

| Méthode | Path | Rôle requis | Request | Response | Codes |
|---|---|---|---|---|---|
| GET | `/dashboard` | CONSEILLER, RESPONSABLE_CREDIT | — | `DashboardDto { nbSoumises, nbEnAnalyse, nbAcceptees, nbRefusees, montantTotalDemande, tauxMoyenEndettement }` (périmètre agrégats hors BROUILLON/ANNULEE, Q4) | 200 (valeurs neutres à 0/N/A si vide, AC-017-4) |

**AC-ARCH-006** : les agrégats `montantTotalDemande` et `tauxMoyenEndettement` du Dashboard excluent explicitement les statuts `BROUILLON` et `ANNULEE` dans `DashboardServiceImpl` (Q4) — vérifiable par test dédié AgentQA.

---

## 8. Business rule traceability table

| RG-BANK | Entités impactées | Décisions techniques | Endpoints |
|---|---|---|---|
| RG-BANK-01 (champs Client obligatoires) | ENT-001 Client | DEC-004 (Bean Validation) | `POST /clients` |
| RG-BANK-02 (bornes montant) | ENT-002 DemandeCredit | DEC-004 (Bean Validation `@DecimalMin`/`@DecimalMax`) | `POST /demandes` |
| RG-BANK-03 (bornes durée) | ENT-002 DemandeCredit | DEC-004 (Bean Validation `@Min`/`@Max`) | `POST /demandes` |
| RG-BANK-04 (formule mensualité/taux) | ENT-002 DemandeCredit (non persisté, DEC-ENT-002) | `SimulationService` (calcul pur, formule normative) | `POST /demandes/{id}/simuler` |
| RG-BANK-05 (statuts et transitions) | ENT-002 (`Statut` enum), ENT-003 HistoriqueDecision | DEC-ENT-003 (table transitions), DEC-006 (transaction atomique statut+historique), DEC-009 (rôles) | `soumettre`, `annuler`, `analyser`, `accepter`, `refuser` |
| RG-BANK-06 (éligibilité cumulative) | ENT-002 DemandeCredit | DEC-004 (validation manuelle en `EligibiliteService`), Q3 (blocage strict, pas de forçage) | `POST /demandes/{id}/accepter` (422 si non éligible) |
| RG-BANK-07 (commentaire obligatoire refus) | ENT-002.commentaireDecision, ENT-003.commentaire | DEC-004 (validation manuelle conditionnelle) | `POST /demandes/{id}/refuser` |
| RG-BANK-08 (score simplifié par bandes) | ENT-002 (non persisté, DEC-ENT-002) | `SimulationService.calculerScore` (pure function, bandes explicites) | `POST /demandes/{id}/simuler` |

---

## 9. Open questions

Aucune question bloquante : les arbitrages Q1-Q5 de la spec fonctionnelle sont définitifs et intégralement reflétés dans ce document (DEC-ENT-001, DEC-ENT-005, DEC-004/RG-BANK-06, Q4 en §7.6/AC-ARCH-006, Q5 en §5.3).

Notes non bloquantes pour l'implémentation :
- NOTE-01 : le format exact des messages d'erreur par champ (ex. libellés français des AC-004-x/AC-008-x) doit être repris **littéralement** par AgentBackendDeveloper depuis la spec fonctionnelle §7, pour garantir la conformité aux AC.
- NOTE-02 : le choix précis de l'algorithme de hachage de mot de passe (BCrypt, coût par défaut Spring Security) est laissé à AgentBackendDeveloper — pas d'exigence de sécurité renforcée dans la spec fonctionnelle (contexte pédagogique).
- NOTE-03 : la pagination sur `GET /clients` et `GET /demandes` est optionnelle pour cette itération (volumétrie pédagogique faible) ; à activer si AgentQA/AgentBA signale un besoin.

---

## 10. Architecture acceptance criteria (résumé)

- **AC-ARCH-001** : controllers ne dépendent que d'interfaces `.service`.
- **AC-ARCH-002** : modèle de données implémentable sans décision supplémentaire.
- **AC-ARCH-003** : routes d'action d'écriture protégées par `roleGuard` dédié.
- **AC-ARCH-004** : requête sans JWT valide → 401.
- **AC-ARCH-005** : Swagger UI / OpenAPI exposent tous les endpoints du §7.
- **AC-ARCH-006** : agrégats Dashboard excluent BROUILLON/ANNULEE (Q4).


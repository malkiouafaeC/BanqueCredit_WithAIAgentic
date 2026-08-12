# Tasks - Banque Credit (Suivi des demandes de credit client)

## Scope
Backlog executable pour implementer l'application "banque-credit" selon :
- spec/spec-feature-banque-credit.md (v1.1, approuve)
- spec/spec-architecture-banque-credit.md (v1.0, approuve)
- plan/plan-feature-banque-credit.md (v1.0)

## Routing Rules
- BE-CREDIT-* -> AgentBackendDeveloper
- FE-CREDIT-* -> AgentFrontendDeveloper
- INT-CREDIT-* -> AgentOrchestrator

## Execution Order (recommended, BE/FE chains run in parallel)
1. BE-CREDIT-001
2. FE-CREDIT-001
3. BE-CREDIT-002
4. FE-CREDIT-002
5. BE-CREDIT-003
6. FE-CREDIT-003
7. BE-CREDIT-004
8. FE-CREDIT-004
9. BE-CREDIT-005
10. FE-CREDIT-005
11. BE-CREDIT-006
12. FE-CREDIT-006
13. FE-CREDIT-007
14. BE-CREDIT-007
15. FE-CREDIT-008
16. FE-CREDIT-009
17. BE-CREDIT-008
18. FE-CREDIT-010
19. BE-CREDIT-009
20. FE-CREDIT-011
21. BE-CREDIT-010
22. BE-CREDIT-011
23. FE-CREDIT-012
24. INT-CREDIT-001
25. INT-CREDIT-002
26. INT-CREDIT-003

## Backend Tasks

### BE-CREDIT-001
- Objective: Initialiser le projet Spring Boot (WAR packaging) dans apps/banque-credit/banque-credit-backend, package racine `formulAI.project.bank.banqueCredit`.
- Files impacted: apps/banque-credit/banque-credit-backend/pom.xml, src/main/java/formulAI/project/bank/banqueCredit/**, src/main/resources/application.properties
- Linked requirements: CON-001, CON-002 (spec architecture §1.4, DEC-003, DEC-007)
- Preconditions: Aucun
- Implementation steps:
  1. Generer le squelette Spring Boot (starters: web, data-jpa, security, validation, h2, springdoc-openapi-starter-webmvc-ui, jjwt).
  2. Configurer packaging `war` + `SpringBootServletInitializer` (DEC-003).
  3. Creer les packages vides avec rationale (model, dto, repository, service, service.impl, controller, security, config, exception).
  4. Configurer `application.properties` : H2 mode fichier (`jdbc:h2:file:./data/banquecredit`), `spring.jpa.hibernate.ddl-auto=update`.
- Test steps:
  - Build Maven (`mvnw clean install`) reussit.
  - Application demarre localement sans erreur.
- Definition of done: Projet compile, demarre, structure de packages conforme a l'architecture, uniquement dans apps/banque-credit/banque-credit-backend.

### BE-CREDIT-002
- Objective: Implementer les entites JPA Client, DemandeCredit, HistoriqueDecision, User + enums Statut/ScoreSimplifie/Role + repositories.
- Files impacted: .model/Client.java, .model/DemandeCredit.java, .model/HistoriqueDecision.java, .model/User.java, .model/Statut.java, .model/ScoreSimplifie.java, .model/Role.java, .repository/*Repository.java
- Linked requirements: RG-BANK-01..08, ENT-001..004, DEC-ENT-001..006
- Preconditions: BE-CREDIT-001
- Implementation steps:
  1. Creer entite `Client` (champs §4.2 architecture, contraintes Bean Validation RG-BANK-01).
  2. Creer entite `DemandeCredit` avec `@ManyToOne Client`, statut enum, champs non persistes exclus (mensualiteEstimee/tauxEndettement/scoreSimplifie non-colonnes, cf DEC-ENT-002).
  3. Creer entite `HistoriqueDecision` avec `@ManyToOne DemandeCredit`, `@ManyToOne User auteur`, `tauxEndettementSnapshot` nullable.
  4. Creer entite `User` (username, password, role enum, actif).
  5. Creer enums `Statut` (6 valeurs), `ScoreSimplifie` (4 valeurs), `Role` (2 valeurs).
  6. Creer repositories Spring Data JPA correspondants.
  7. Implementer la table de transitions autorisees (DEC-ENT-003) sous forme de composant/utilitaire dedie (ex: `TransitionRules`), avec methode `verifierTransitionAutorisee(statutActuel, statutCible, role)`.
- Test steps:
  - Tests unitaires JPA (repository) : sauvegarde/lecture Client, DemandeCredit, HistoriqueDecision avec relations correctes.
  - Test unitaire `TransitionRules` couvrant toutes les transitions autorisees/interdites du tableau §4.4.
- Definition of done: Entites persistees correctement (verifie par tests repository), table de transitions testee unitairement pour les 30 combinaisons du tableau §4.4.

### BE-CREDIT-003
- Objective: Implementer Spring Security + JWT stateless par role + seed des comptes + endpoint `POST /auth/login`.
- Files impacted: .security/JwtTokenProvider.java, .security/JwtAuthenticationFilter.java, .security/SecurityConfig.java, .security/CustomUserDetailsService.java, .controller/AuthController.java, .dto/AuthRequestDto.java, .dto/AuthResponseDto.java, src/main/resources/data.sql
- Linked requirements: REQ-AUTH-01, SEC-001, SEC-002, DEC-002, DEC-009
- Preconditions: BE-CREDIT-002
- Implementation steps:
  1. Implementer `JwtTokenProvider` (generation/validation JWT HMAC-SHA256, claims `sub`+`role`).
  2. Implementer `JwtAuthenticationFilter` + `SecurityConfig` (`SessionCreationPolicy.STATELESS`, routes protegees, `/auth/login` public).
  3. Implementer `CustomUserDetailsService` charge depuis `UserRepository`.
  4. Implementer `AuthController.login` (`POST /auth/login`) : verification credentials (BCrypt), retour `{ token, username, role, expiresAt }`.
  5. Ajouter `data.sql` avec au moins 1 compte CONSEILLER et 1 compte RESPONSABLE_CREDIT (mots de passe haches BCrypt).
  6. Configurer `@PreAuthorize` au niveau service (activation `@EnableMethodSecurity`).
- Test steps:
  - Test integration `POST /auth/login` : credentials valides -> 200 + token; credentials invalides -> 401 message generique (AC-001-3).
  - Test integration : requete sans token sur route protegee -> 401 (AC-ARCH-004).
- Definition of done: Login fonctionnel pour les 2 roles, JWT valide contient le claim role, toute route hors `/auth/login` refuse l'acces sans token valide.

### BE-CREDIT-004
- Objective: Implementer la gestion d'erreurs centralisee et la configuration Bean Validation.
- Files impacted: .exception/GlobalExceptionHandler.java, .exception/TransitionInvalideException.java, .exception/EligibiliteNonRespecteeException.java, .exception/CommentaireObligatoireException.java, .exception/ValidationMetierException.java, .exception/RessourceNotFoundException.java, .dto/ErrorResponseDto.java
- Linked requirements: DEC-004, DEC-005
- Preconditions: BE-CREDIT-002
- Implementation steps:
  1. Creer les exceptions metier dediees listees ci-dessus.
  2. Implementer `GlobalExceptionHandler` (`@RestControllerAdvice`) mappant chaque exception vers un code HTTP + corps standardise (`code`, `message`, `champ`, `timestamp`), codes : `CHAMP_OBLIGATOIRE`, `VALEUR_HORS_BORNES`, `TRANSITION_INVALIDE`, `ROLE_NON_AUTORISE`, `COMMENTAIRE_OBLIGATOIRE`, `ELIGIBILITE_NON_RESPECTEE`, `RESSOURCE_INTROUVABLE`.
  3. Gerer `MethodArgumentNotValidException` (Bean Validation) -> 400 avec un message par champ (AC-004-4).
- Test steps:
  - Test unitaire du handler pour chaque type d'exception -> code HTTP et corps attendus.
  - Test integration : payload avec plusieurs champs obligatoires manquants simultanement -> tous les messages retournes (AC-004-4).
- Definition of done: Toute exception metier produit une reponse HTTP standardisee et testee ; aucune stack trace exposee au client.

### BE-CREDIT-005
- Objective: Implementer ClientService/ClientServiceImpl/ClientController (`POST /clients`, `GET /clients`, `GET /clients/{id}`).
- Files impacted: .service/ClientService.java, .service.impl/ClientServiceImpl.java, .controller/ClientController.java, .dto/ClientCreateDto.java, .dto/ClientDto.java, .dto/ClientDetailDto.java
- Linked requirements: RG-BANK-01, US-003..006, AC-003-x, AC-004-x, AC-005-x, AC-006-x
- Preconditions: BE-CREDIT-003, BE-CREDIT-004
- Implementation steps:
  1. Implementer `ClientService`/`ClientServiceImpl` : creation (validation RG-BANK-01), liste, detail (avec demandes associees via `DemandeCreditSummaryDto`).
  2. Implementer `ClientController` avec les 3 endpoints, roles autorises (CONSEILLER pour creation, les 2 roles en lecture).
  3. Gerer etat vide explicite (liste vide -> `[]`, AC-005-2 ; client sans demande -> section vide, AC-006-2).
- Test steps:
  - Test integration `POST /clients` : succes 201, echec validation 400 (par champ), acces role non autorise 403.
  - Test integration `GET /clients`, `GET /clients/{id}` : cas nominal, cas vide, cas 404.
- Definition of done: 3 endpoints clients fonctionnels et testes, conformes aux AC-003..006.

### BE-CREDIT-006
- Objective: Implementer SimulationService (formule normative RG-BANK-04/08) et DemandeCreditService/Controller (creation + simulation + lecture).
- Files impacted: .service/SimulationService.java, .service.impl/SimulationServiceImpl.java, .service/DemandeCreditService.java, .service.impl/DemandeCreditServiceImpl.java, .controller/DemandeCreditController.java, .dto/DemandeCreditCreateDto.java, .dto/DemandeCreditDto.java, .dto/DemandeCreditSummaryDto.java, .dto/SimulationResultDto.java
- Linked requirements: RG-BANK-02, RG-BANK-03, RG-BANK-04, RG-BANK-08, US-007..009, AC-007-x, AC-008-x, AC-009-x
- Preconditions: BE-CREDIT-005
- Implementation steps:
  1. Implementer `SimulationService.calculerMensualite` avec la formule normative exacte de la spec fonctionnelle RG-BANK-04 (amortissement + cas particulier taux=0).
  2. Implementer `SimulationService.calculerTauxEndettement` et `calculerScore` (bandes RG-BANK-08, bornes inclusives testees a AC-009-2..5).
  3. Implementer `DemandeCreditService.creer` (statut initial BROUILLON, validation bornes RG-BANK-02/03, client obligatoire AC-007-3) + creation de la 1ere entree HistoriqueDecision (`ancienStatut=null`, `nouveauStatut=BROUILLON`, DEC-ENT-005).
  4. Implementer `GET /demandes`, `GET /demandes/{id}`, `POST /demandes/{id}/simuler` (recalcul a la volee, DEC-ENT-002).
- Test steps:
  - Tests unitaires `SimulationService` : valeurs exactes aux bornes 20%/35%/50% (AC-009-2..5), cas taux=0.
  - Test integration `POST /demandes` : bornes 1000/100000/12/84 (E1-E7), client manquant.
  - Test integration `POST /demandes/{id}/simuler` : reponse mensualite/taux/score coherente.
- Definition of done: Formule normative implementee et testee exactement aux bornes, endpoints creation/simulation/lecture fonctionnels.

### BE-CREDIT-007
- Objective: Implementer EligibiliteService (RG-BANK-06) et DecisionService/Controller pour toutes les transitions de statut.
- Files impacted: .service/EligibiliteService.java, .service.impl/EligibiliteServiceImpl.java, .service/DecisionService.java, .service.impl/DecisionServiceImpl.java, .controller/DecisionController.java, .dto/DecisionRefusDto.java
- Linked requirements: RG-BANK-05, RG-BANK-06, RG-BANK-07, US-010..015, AC-010-x..AC-015-x, DEC-006, DEC-ENT-002, DEC-ENT-003, DEC-009
- Preconditions: BE-CREDIT-006
- Implementation steps:
  1. Implementer `POST /demandes/{id}/soumettre` (BROUILLON->SOUMISE, CONSEILLER uniquement, DEC-006 atomique avec HistoriqueDecision).
  2. Implementer `POST /demandes/{id}/annuler` (BROUILLON ou SOUMISE ->ANNULEE, CONSEILLER uniquement, rejet 403 si RESPONSABLE_CREDIT).
  3. Implementer `POST /demandes/{id}/analyser` (SOUMISE->EN_ANALYSE, RESPONSABLE_CREDIT uniquement).
  4. Implementer `EligibiliteService.verifierEligibilite` (3 conditions cumulatives RG-BANK-06, bornes inclusives 35%/1500/50000).
  5. Implementer `POST /demandes/{id}/accepter` (EN_ANALYSE->ACCEPTEE si eligible, sinon 422 `ELIGIBILITE_NON_RESPECTEE`; blocage strict, pas de forcage, Q3).
  6. Implementer `POST /demandes/{id}/refuser` (EN_ANALYSE->REFUSEE, commentaire obligatoire non-vide/non-espaces RG-BANK-07, sinon 400).
  7. A chaque transition : verifier la transition via `TransitionRules`, snapshotter `tauxEndettementSnapshot` pour accepter/refuser (DEC-ENT-002), creer `HistoriqueDecision` (auteur, date) dans la meme transaction `@Transactional` (DEC-006).
- Test steps:
  - Test integration par transition : cas nominal + transition invalide (409) + role non autorise (403).
  - Test integration acceptation : 3 bornes exactes (35%/1500/50000) -> succes ; chaque condition en echec isolement -> 422 (AC-013-2..4).
  - Test integration refus : commentaire absent/espaces -> 400 (AC-015-1/2) ; commentaire valide -> 200.
  - Test integration : verifier qu'une entree HistoriqueDecision est bien creee pour chaque transition reussie (auteur + date corrects).
- Definition of done: Les 5 transitions (soumettre/annuler/analyser/accepter/refuser) fonctionnent, respectent les roles et regles, et sont historisees de maniere atomique et testee.

### BE-CREDIT-008
- Objective: Implementer HistoriqueService/Controller (`GET /demandes/{id}/historique`).
- Files impacted: .service/HistoriqueService.java, .service.impl/HistoriqueServiceImpl.java, .controller/HistoriqueController.java, .dto/HistoriqueDecisionDto.java
- Linked requirements: US-016, AC-016-x
- Preconditions: BE-CREDIT-007
- Implementation steps:
  1. Implementer `HistoriqueService.listerParDemande` (ordre chronologique croissant).
  2. Implementer endpoint avec 404 si demande inexistante, `[]` si aucune transition (cas theorique, cf DEC-ENT-005 qui garantit toujours au moins 1 entree).
- Test steps:
  - Test integration : demande avec 3 transitions -> 3 entrees dans l'ordre avec tous les champs (AC-016-1).
- Definition of done: Endpoint historique fonctionnel, ordonne, teste.

### BE-CREDIT-009
- Objective: Implementer DashboardService/Controller (`GET /dashboard`).
- Files impacted: .service/DashboardService.java, .service.impl/DashboardServiceImpl.java, .controller/DashboardController.java, .dto/DashboardDto.java
- Linked requirements: US-017, AC-017-x, Q4, AC-ARCH-006
- Preconditions: BE-CREDIT-007
- Implementation steps:
  1. Implementer le calcul des compteurs par statut (soumises, en_analyse, acceptees, refusees).
  2. Implementer `montantTotalDemande` et `tauxMoyenEndettement` en excluant explicitement BROUILLON et ANNULEE (Q4).
  3. Gerer le cas base vide : tous compteurs a 0, montants/taux a une valeur neutre (AC-017-4).
- Test steps:
  - Test integration avec jeu de demandes reparties sur tous les statuts -> compteurs exacts (AC-017-1).
  - Test integration base vide -> valeurs neutres sans erreur (AC-017-4).
  - Test dedie verifiant l'exclusion BROUILLON/ANNULEE des agregats (AC-ARCH-006).
- Definition of done: Dashboard reflete exactement les regles d'agregation definies, teste sur cas nominal et vide.

### BE-CREDIT-010
- Objective: Integrer springdoc-openapi (Swagger UI) et documenter tous les endpoints/DTO.
- Files impacted: .config/OpenApiConfig.java, annotations `@Schema`/`@Operation`/`@ApiResponse` sur tous les controllers/DTO
- Linked requirements: DEC-008, AC-ARCH-005
- Preconditions: BE-CREDIT-009
- Implementation steps:
  1. Ajouter/configurer `springdoc-openapi-starter-webmvc-ui`.
  2. Configurer `OpenApiConfig` (titre, version, schema de securite Bearer JWT).
  3. Annoter chaque controller/DTO (`@Operation`, `@ApiResponse`, `@Schema`).
- Test steps:
  - Verification manuelle : `/swagger-ui.html` liste les 16 endpoints avec schemas de requete/reponse corrects.
  - Verification `/v3/api-docs` retourne un JSON OpenAPI valide.
- Definition of done: Swagger UI reflete integralement l'API (AC-ARCH-005).

### BE-CREDIT-011
- Objective: Finaliser la couverture de tests backend (unitaires + integration) sur l'ensemble des cas limites E1-E25.
- Files impacted: src/test/java/formulAI/project/bank/banqueCredit/**
- Linked requirements: Toutes RG-BANK-01..08, edge cases E1-E25 (spec fonctionnelle §8)
- Preconditions: BE-CREDIT-010
- Implementation steps:
  1. Recenser les 25 cas limites de la spec fonctionnelle et verifier leur couverture par les tests existants (BE-CREDIT-002..009).
  2. Ajouter les tests manquants (notamment E17/E18 acces role croise, E20 demande sans client, E21 double soumission).
  3. Consolider un rapport de couverture (liste E1-E25 -> test correspondant).
- Test steps:
  - Execution complete de la suite de tests backend, 100% des E1-E25 couverts par au moins un test.
- Definition of done: Suite de tests backend complete et verte, tracabilite E1-E25 documentee dans le rapport de fin de tache.

## Frontend Tasks

### FE-CREDIT-001
- Objective: Initialiser le projet Angular standalone dans apps/banque-credit/banque-credit-frontend avec structure de features vide.
- Files impacted: apps/banque-credit/banque-credit-frontend/**, src/app/features/{auth,dashboard,clients,demandes,decisions,historique}/, src/app/shared/, src/app/core/
- Linked requirements: CON-001, CON-002
- Preconditions: Aucun
- Implementation steps:
  1. Generer projet Angular (standalone components, routing active).
  2. Creer les dossiers de features vides + shared + core selon l'architecture §5.
  3. Configurer le routing de base (placeholder routes).
- Test steps:
  - Build Angular (`ng build`) reussit.
- Definition of done: Projet cree au bon emplacement, structure conforme a l'architecture, build passe.

### FE-CREDIT-002
- Objective: Implementer les services/guards/interceptors core partages.
- Files impacted: src/app/core/guards/auth.guard.ts, src/app/core/guards/role.guard.ts, src/app/core/interceptors/jwt.interceptor.ts, src/app/core/interceptors/error.interceptor.ts
- Linked requirements: SEC-001, SEC-002, DEC-002
- Preconditions: FE-CREDIT-001
- Implementation steps:
  1. Implementer `jwt.interceptor.ts` (ajout header `Authorization: Bearer <token>`).
  2. Implementer `error.interceptor.ts` (mapping erreurs API -> notifications, gestion 401 -> redirection login).
  3. Implementer `auth.guard.ts` (verifie presence/validite token).
  4. Implementer `role.guard.ts` (parametrable par role requis, verifie claim role du token/etat auth).
- Test steps:
  - Tests unitaires guards (cas authentifie/non authentifie, role correct/incorrect).
  - Tests unitaires interceptors (header ajoute, erreur 401 geree).
- Definition of done: Guards et interceptors testes unitairement, prets a etre branches sur les routes.

### FE-CREDIT-003
- Objective: Implementer la feature `auth` (login, stockage token, redirections).
- Files impacted: src/app/features/auth/auth-request.model.ts, auth-response.model.ts, role.enum.ts, login.page.ts, auth.service.ts, app.routes.ts
- Linked requirements: US-001, US-002, AC-001-x, AC-002-1
- Preconditions: FE-CREDIT-002
- Implementation steps:
  1. Implementer `auth.service.ts` (login via `POST /auth/login`, stockage token+role+username, decodage role).
  2. Implementer `login.page.ts` (formulaire reactif username/password, gestion erreur 401 generique).
  3. Configurer route `/login` (publique) et redirection post-login vers `/dashboard`.
  4. Brancher `authGuard` sur les routes protegees (redirection `/login` si non authentifie, AC-002-1).
- Test steps:
  - Tests unitaires composant login (soumission valide/invalide, affichage erreur).
  - Test routing : acces route protegee sans authentification -> redirection `/login`.
- Definition of done: Flux login fonctionnel pour les 2 roles, redirection non-authentifie operationnelle.

### FE-CREDIT-004
- Objective: Implementer le layout partage et la charte graphique (couleurs statuts/scores, avertissements pedagogiques).
- Files impacted: src/app/shared/layout/*, src/app/shared/charte/charte.constants.ts, src/app/shared/charte/badge-statut.component.ts, src/app/shared/charte/badge-score.component.ts
- Linked requirements: Charte §5.8 spec fonctionnelle, US-009 (AC-009-6)
- Preconditions: FE-CREDIT-003
- Implementation steps:
  1. Implementer header/menu role-aware (affichage username, role, navigation, logout) dans `shared/layout`.
  2. Implementer `charte.constants.ts` : mapping statut->couleur (bleu neutre, vert=ACCEPTEE, rouge=REFUSEE, orange=EN_ANALYSE) et score->couleur (EXCELLENT/BON vert, MOYEN/FAIBLE orange/rouge).
  3. Implementer composants `badge-statut` et `badge-score` reutilisables, accessibles (contraste, texte + couleur, pas couleur seule).
- Test steps:
  - Tests unitaires composants badge (statut/score -> classe CSS/texte attendus).
  - Verification manuelle contraste/accessibilite (lisibilite prioritaire sur decoration).
- Definition of done: Layout et badges partages fonctionnels, coherents avec la charte, reutilisables par toutes les features suivantes.

### FE-CREDIT-005
- Objective: Implementer la feature `clients` (liste, creation, detail).
- Files impacted: src/app/features/clients/client.model.ts, client.service.ts, client-list.page.ts, client-create.page.ts, client-detail.page.ts
- Linked requirements: US-003..006, AC-003-x..AC-006-x
- Preconditions: FE-CREDIT-004, BE-CREDIT-005 (contrat consommable)
- Implementation steps:
  1. Implementer `client.service.ts` (appels `POST/GET /clients`, `GET /clients/{id}`).
  2. Implementer `client-list.page.ts` (etat loading/vide/erreur, AC-005-2).
  3. Implementer `client-create.page.ts` (formulaire reactif, affichage erreurs par champ AC-004-1..4).
  4. Implementer `client-detail.page.ts` (affichage client + liste demandes associees, etat vide AC-006-2).
  5. Brancher `roleGuard(CONSEILLER)` sur les routes d'ecriture (`/clients`, `/clients/nouveau`).
- Test steps:
  - Tests unitaires composants (nominal, vide, erreur validation, 401).
  - Test integration manuelle avec backend reel une fois BE-CREDIT-005 livre.
- Definition of done: 3 pages fonctionnelles, testees, conformes aux AC clients.

### FE-CREDIT-006
- Objective: Implementer la creation de demande de credit (formulaire + validation cote UI).
- Files impacted: src/app/features/demandes/demande-credit.model.ts, statut.enum.ts, demande-credit.service.ts, demande-create.page.ts
- Linked requirements: US-007, US-008, AC-007-x, AC-008-x
- Preconditions: FE-CREDIT-005, BE-CREDIT-006 (contrat consommable)
- Implementation steps:
  1. Implementer `demande-credit.service.ts` (`POST /demandes`, `GET /demandes`, `GET /demandes/{id}`).
  2. Implementer `demande-create.page.ts` : selection client obligatoire, montant (validators bornes 1000 exclu/100000 inclus), duree (12-84 inclus), taux fictif, affichage erreurs explicites par champ (AC-008-1..4).
  3. Brancher `roleGuard(CONSEILLER)` sur la route de creation.
- Test steps:
  - Tests unitaires formulaire (bornes valides/invalides, client manquant AC-007-3).
- Definition of done: Formulaire de creation fonctionnel, validations UI alignees sur RG-BANK-02/03, teste.

### FE-CREDIT-007
- Objective: Implementer la simulation sur la page detail demande.
- Files impacted: src/app/features/demandes/simulation-result.model.ts, demande-detail.page.ts (section simulation)
- Linked requirements: US-009, AC-009-x
- Preconditions: FE-CREDIT-006
- Implementation steps:
  1. Appeler `POST /demandes/{id}/simuler` et afficher mensualite estimee, taux d'endettement, score simplifie.
  2. Utiliser `badge-score` (charte) pour l'affichage du score avec avertissement pedagogique visuel si `FAIBLE`/`MOYEN` (AC-009-6).
- Test steps:
  - Tests unitaires composant simulation (affichage valeurs, avertissement conditionnel selon score).
- Definition of done: Section simulation fonctionnelle, avertissement pedagogique conforme a la charte.

### FE-CREDIT-008
- Objective: Implementer les actions soumission et annulation sur la page detail demande.
- Files impacted: demande-detail.page.ts (actions), demande-credit.service.ts (methodes soumettre/annuler)
- Linked requirements: US-010, US-011, AC-010-x, AC-011-x
- Preconditions: FE-CREDIT-007, BE-CREDIT-007 (contrat consommable)
- Implementation steps:
  1. Ajouter action "Soumettre" visible uniquement si statut=BROUILLON et role=CONSEILLER, appel `POST /demandes/{id}/soumettre`.
  2. Ajouter action "Annuler" visible uniquement si statut in (BROUILLON, SOUMISE) et role=CONSEILLER, appel `POST /demandes/{id}/annuler`.
  3. Centraliser la logique d'affichage conditionnel des actions selon statut/role dans un service partage (mitigation RISK-CREDIT-003).
  4. Gerer messages d'erreur 409 (transition invalide) et 403 (role non autorise).
- Test steps:
  - Tests unitaires : visibilite des boutons selon statut/role, gestion des erreurs 409/403.
- Definition of done: Actions soumission/annulation fonctionnelles, visibilite conditionnelle testee.

### FE-CREDIT-009
- Objective: Implementer la feature `decisions` (analyser/accepter/refuser) reservee RESPONSABLE_CREDIT.
- Files impacted: src/app/features/decisions/decision-request.model.ts, decision.service.ts, decision.page.ts
- Linked requirements: US-012..015, AC-012-x..AC-015-x
- Preconditions: FE-CREDIT-008, BE-CREDIT-007 (contrat consommable)
- Implementation steps:
  1. Implementer `decision.service.ts` (`POST /demandes/{id}/analyser`, `/accepter`, `/refuser`).
  2. Implementer `decision.page.ts` : bouton "Passer en analyse" (si SOUMISE), boutons "Accepter"/"Refuser" (si EN_ANALYSE).
  3. Formulaire de refus avec commentaire obligatoire (validation cote UI non-vide/non-espaces avant envoi, miroir de AC-015-1/2).
  4. Gerer l'affichage de l'erreur 422 (`ELIGIBILITE_NON_RESPECTEE`) lors d'une tentative d'acceptation non eligible.
  5. Brancher `roleGuard(RESPONSABLE_CREDIT)` sur la route `/demandes/:id/decision`.
- Test steps:
  - Tests unitaires : refus sans commentaire bloque cote UI, refus avec commentaire valide envoie la requete, gestion erreur 422.
- Definition of done: Feature decisions complete, guard de role actif, formulaire refus conforme a RG-BANK-07.

### FE-CREDIT-010
- Objective: Implementer la feature `historique`.
- Files impacted: src/app/features/historique/historique-decision.model.ts, historique.service.ts, historique.page.ts
- Linked requirements: US-016, AC-016-x
- Preconditions: FE-CREDIT-009, BE-CREDIT-008 (contrat consommable)
- Implementation steps:
  1. Implementer `historique.service.ts` (`GET /demandes/{id}/historique`).
  2. Implementer affichage chronologique (ancienStatut, nouveauStatut, commentaire, auteur, date) avec badges statut (charte).
  3. Integrer la section dans `demande-detail.page` ou route dediee.
- Test steps:
  - Tests unitaires : affichage liste transitions, etat theorique vide.
- Definition of done: Historique affiche correctement et de maniere lisible pour les 2 roles.

### FE-CREDIT-011
- Objective: Implementer la feature `dashboard`.
- Files impacted: src/app/features/dashboard/dashboard.model.ts, dashboard.service.ts, dashboard.page.ts
- Linked requirements: US-017, AC-017-x
- Preconditions: FE-CREDIT-010, BE-CREDIT-009 (contrat consommable)
- Implementation steps:
  1. Implementer `dashboard.service.ts` (`GET /dashboard`).
  2. Implementer `dashboard.page.ts` : compteurs par statut (badges couleur charte), montant total demande, taux moyen d'endettement.
  3. Gerer etat vide (tous compteurs a 0, valeurs neutres, AC-017-4).
- Test steps:
  - Tests unitaires : affichage nominal, affichage etat vide.
- Definition of done: Dashboard agence fonctionnel, conforme a la charte et aux regles d'agregation Q4.

### FE-CREDIT-012
- Objective: Finaliser la couverture de tests frontend et la verification accessibilite.
- Files impacted: src/app/**/*.spec.ts
- Linked requirements: Toutes US, accessibilite §5.8
- Preconditions: FE-CREDIT-011
- Implementation steps:
  1. Completer les tests unitaires manquants (composants, services, guards, interceptors, routing).
  2. Verifier l'accessibilite : contraste des badges, navigation clavier de base, texte alternatif aux couleurs de statut.
- Test steps:
  - Execution complete de la suite de tests frontend (`ng test`), 100% des specs vertes.
  - Checklist accessibilite manuelle validee.
- Definition of done: Suite de tests frontend complete et verte, checklist accessibilite validee.

## Integration Tasks

### INT-CREDIT-001
- Objective: Verifier l'alignement des contrats API (DTO, codes HTTP, codes d'erreur) entre backend et frontend pour les 16 endpoints.
- Preconditions: BE-CREDIT-011, FE-CREDIT-012
- Validation:
  - Comparaison endpoint par endpoint entre spec architecture §7 et implementation reelle (BE et FE).
  - Verification des codes d'erreur consommes correctement par le frontend (400/401/403/404/409/422).
- Done criteria: Aucun mismatch contractuel bloquant identifie ; ecarts documentes et corriges.

### INT-CREDIT-002
- Objective: Executer la checklist securite/erreurs/navigation/regles metier.
- Preconditions: INT-CREDIT-001
- Validation:
  - Workflow complet teste manuellement (Swagger + frontend) : creation client -> demande -> simulation -> soumission -> analyse -> acceptation/refus -> historique -> dashboard.
  - Verification des roles (acces refuse cote CONSEILLER sur actions decision et inversement).
  - Verification RG-BANK-01..08 couvertes de bout en bout.
- Done criteria: Checklist integration complete sans blocker.

### INT-CREDIT-003
- Objective: Produire le verdict final READY/NOT_READY avant dispatch AgentQA/AgentSecurity.
- Preconditions: INT-CREDIT-002
- Validation:
  - Rapport blockers (le cas echeant) et ameliorations non bloquantes.
- Done criteria: Decision de release explicite documentee dans le progress board.


# Revue de sécurité — Feature "banque-credit"

Auteur : AgentSecurity
Plan de référence : plan/plan-security-review-banque-credit.md
Périmètre : voir plan §1

---

## SEC-TASK-001 — Autorisation et transitions de statut (RG-BANK-05 / RG-BANK-07)

### Résumé
Revue de `@PreAuthorize` sur chaque méthode d'écriture de service, croisée avec la table de transitions §4.4 de l'architecture (BROUILLON→SOUMISE/ANNULEE = CONSEILLER ; SOUMISE→EN_ANALYSE/ANNULEE ; EN_ANALYSE→ACCEPTEE/REFUSEE = RESPONSABLE_CREDIT) et avec la logique métier de contrôle de statut réellement exécutée (pas seulement la présence de l'annotation).

### Constats
- **Aucune méthode d'écriture non protégée trouvée.** Vérification exhaustive :
  - `ClientServiceImpl.creer` → `@PreAuthorize("hasRole('CONSEILLER')")` ✅ (RG-BANK-01, acteurs §2.1)
  - `DemandeCreditServiceImpl.creer` → `@PreAuthorize("hasRole('CONSEILLER')")` ✅
  - `DecisionServiceImpl.soumettre` → `CONSEILLER` ✅, vérifie `statut == BROUILLON` avant transition
  - `DecisionServiceImpl.annuler` → `CONSEILLER` ✅, vérifie `statut ∈ {BROUILLON, SOUMISE}`
  - `DecisionServiceImpl.analyser` → `RESPONSABLE_CREDIT` ✅, vérifie `statut == SOUMISE`
  - `DecisionServiceImpl.accepter` → `RESPONSABLE_CREDIT` ✅, vérifie `statut == EN_ANALYSE` + délègue à `EligibiliteService` (RG-BANK-06)
  - `DecisionServiceImpl.refuser` → `RESPONSABLE_CREDIT` ✅, vérifie `statut == EN_ANALYSE` + commentaire obligatoire (RG-BANK-07, voir ci-dessous)
  - Endpoints de lecture (`GET /clients`, `GET /demandes`, `GET /demandes/{id}/historique`, `GET /dashboard`) : pas de `@PreAuthorize` dédié, mais protégés par `SecurityConfig.anyRequest().authenticated()` → cohérent avec Q5 (lecture ouverte aux deux rôles, seules les actions d'écriture sont cloisonnées).
- **`GlobalExceptionHandler.handleAccessDenied`** mappe correctement `AccessDeniedException` (levée par `@PreAuthorize` en échec) vers HTTP 403 avec code `ROLE_NON_AUTORISE` — pas de fuite d'information sur la raison précise du refus.
- **RG-BANK-07 vérifié en profondeur** : `DecisionServiceImpl.refuser` fait `commentaire == null || commentaire.trim().isEmpty()` → rejette avec `CommentaireObligatoireException`. Ce contrôle est fait **côté serveur, dans le service**, indépendamment de toute validation de formulaire Angular (`decision.page.ts`) — un appel API direct (ex. via `curl`/Postman) sans commentaire est bien rejeté en 400. Conforme.
- **Point mineur (non-sécurité, hygiène de code)** : `model/TransitionRules.java` (table de transitions RG-BANK-05, `DEC-ENT-003`) est définie mais **jamais appelée** — `DecisionServiceImpl` réimplémente la logique de transition par des comparaisons `if (demande.getStatut() != Statut.X)` directement, plutôt que via `TransitionRules.verifierTransitionAutorisee(...)`. Le résultat fonctionnel est correct (vérifié un par un ci-dessus) et n'introduit **aucune faille de sécurité actuelle**, mais crée un risque de divergence future si un développeur modifie une des deux logiques sans l'autre. Recommandation adressée à AgentReviewer/AgentBackendDeveloper (hors périmètre de correction sécurité directe).

### Sévérité des constats
- Aucun blocker, aucun high sur cette tâche : l'autorisation est correctement et complètement appliquée à chaque transition/action d'écriture.
- **Low** : code mort `TransitionRules` non utilisé — remédiation : soit le brancher dans `DecisionServiceImpl`, soit le supprimer, pour éviter toute divergence future entre la doc/l'architecture et l'implémentation réelle.

### Règle(s) métier vérifiée(s)
RG-BANK-05 (transitions + rôle), RG-BANK-07 (commentaire obligatoire serveur), Q5 (séparation lecture/écriture par rôle).

### Résiduel
Aucun.

---

## SEC-TASK-002 — Validation d'entrée serveur (RG-BANK-01/02/03/04/06)

### Résumé
Vérification que les contraintes Bean Validation et les règles métier conditionnelles sont appliquées côté serveur et ne peuvent pas être contournées par un appel API direct sautant le frontend.

### Constats
- `ClientCreateDto` : `@NotBlank` (nom), `@NotNull + @DecimalMin("0.01")` (revenuMensuel), `@NotNull + @DecimalMin("0.00")` (chargesMensuelles), `@Email` (email optionnel) — **RG-BANK-01 appliqué côté serveur**, indépendant du frontend.
- `DemandeCreditCreateDto` : `@NotNull` (clientId), `@DecimalMin("1000.00", inclusive=false) + @DecimalMax("100000.00", inclusive=true)` (montant), `@Min(12) + @Max(84)` (durée) — **RG-BANK-02/RG-BANK-03 appliqués côté serveur**, bornes inclusives/exclusives conformes à la spec.
- `@Valid` présent sur les `@RequestBody` des controllers (`ClientController.creer`, `DemandeCreditController.creer`, `AuthController.login`) → `MethodArgumentNotValidException` interceptée par `GlobalExceptionHandler` avec un message par champ (conforme AC-004/AC-008).
- **RG-BANK-04** : `mensualiteEstimee`/`tauxEndettement`/`scoreSimplifie` ne sont **jamais des champs acceptés en entrée** — aucun DTO de requête n'expose ces champs ; ils sont exclusivement calculés côté serveur par `SimulationService` à partir de `montantDemande`/`dureeMois`/`tauxFictif`/`client.revenuMensuel`/`client.chargesMensuelles`. Impossible pour un client API de les falsifier. Conforme et bien conçu (DEC-ENT-002).
- **RG-BANK-06** : `EligibiliteServiceImpl.verifierEligibilite` recalcule les 3 conditions côté serveur à partir des valeurs de la demande/client courantes (pas des valeurs fournies dans la requête), avant de permettre `accepter()`. Un appel direct à `POST /demandes/{id}/accepter` sans passer par le frontend ne peut donc pas contourner l'éligibilité.
- Pas de construction de requête SQL/JPQL manuelle par concaténation trouvée (Spring Data JPA dérivé/`findBy*` uniquement) → pas de risque d'injection SQL identifié.

### Sévérité des constats
Aucun finding — toutes les règles contrôlées sont appliquées côté serveur et non contournables par appel API direct.

### Règle(s) métier vérifiée(s)
RG-BANK-01, RG-BANK-02, RG-BANK-03, RG-BANK-04, RG-BANK-06.

### Résiduel
Aucun.

---

## SEC-TASK-003 — Secrets (jwt.secret)

### Résumé
Vérification de l'externalisation et de la robustesse du secret de signature JWT.

### Constats (avant correction)
- `application.properties` contenait `app.jwt.secret=ChangeMeInProdBanqueCreditSuperSecretKeyForHS256Signing123456` **en clair, commité dans le dépôt**, utilisé tel quel en l'absence de toute variable d'environnement de substitution.
- Le secret fait ~63 caractères ASCII → force suffisante pour HS256 (>256 bits en UTF-8), donc pas de faiblesse cryptographique en soi.
- Risque réel : dans un contexte pédagogique (ASM-02), l'impact est limité (pas de donnée réelle), mais le secret étant commité et **identique pour tous les déploiements** de ce dépôt, toute personne ayant accès au code source peut forger des JWT valides pour n'importe quel rôle (élévation de privilège complète), sans jamais avoir besoin d'un mot de passe.

### Correction appliquée (AUTO-FIX)
`app.jwt.secret` externalisé via `${JWT_SECRET:<valeur par défaut>}` (Spring property placeholder) dans `application.properties`. Le comportement local/dev est inchangé (valeur par défaut conservée), mais en production il suffit de définir la variable d'environnement `JWT_SECRET` pour utiliser une valeur non commitée. `app.jwt.expiration-ms` externalisé de la même façon (`JWT_EXPIRATION_MS`).
Build vérifié : `mvnw compile` OK après modification.

### Sévérité
**High → corrigé (fix appliqué).** Le risque résiduel (valeur par défaut toujours présente dans le code pour l'usage local) est acceptable et documenté en commentaire dans `application.properties`.

### Règle(s) métier vérifiée(s)
DEC-002 (JWT stateless), NOTE-02 de l'architecture (algorithme de hachage laissé libre — non concerné ici, c'est la clé de signature JWT, pas le hash de mot de passe).

### Résiduel
- Recommandation à AgentDeployment : en environnement de déploiement réel (même pédagogique/démo), définir explicitement `JWT_SECRET` via variable d'environnement ou secret manager, et ne jamais laisser la valeur par défaut du dépôt active au-delà d'un poste de développement local.

---

## SEC-TASK-004 — Logging hygiene

### Résumé
Recherche transverse de `System.out`, `log.`, `logger.`, `printStackTrace` dans le code backend (`src/main`), et revue manuelle de `AuthServiceImpl`, `AuthController`, `GlobalExceptionHandler`.

### Constats
- **Aucune instruction de logging applicatif trouvée dans `src/main`** (pas de `Logger`/`System.out` utilisé nulle part). Aucun mot de passe, token JWT ou donnée personnelle n'est donc loggé par le code applicatif.
- `GlobalExceptionHandler` ne renvoie jamais de stack trace au client (corps d'erreur standardisé `ErrorResponseDto`), conforme DEC-005.
- `AuthServiceImpl.login` utilise un message d'erreur générique unique (`GENERIC_ERROR = "Identifiants invalides"`) pour username inconnu et mot de passe invalide, sans distinguer la cause — bonne pratique anti-énumération de comptes (conforme AC-001-3).
- Aucun `@ExceptionHandler(Exception.class)` générique catch-all n'est défini : une exception inattendue (ex. `NullPointerException`) provoquerait le comportement par défaut de Spring Boot. Par défaut Spring Boot 3 (`server.error.include-stacktrace=never`, `include-message=never`), aucune fuite n'est attendue en configuration standard, et aucune configuration contraire n'a été trouvée dans `application.properties`.

### Sévérité des constats
- **Low** (informationnel) : absence de handler catch-all générique — recommandation d'ajouter un `@ExceptionHandler(Exception.class)` renvoyant un 500 générique, par robustesse défensive plutôt que par nécessité de corriger une fuite constatée aujourd'hui.

### Règle(s) métier vérifiée(s)
Hygiène de logging transverse (hors RG-BANK spécifique).

### Résiduel
Recommandation non bloquante à AgentBackendDeveloper : ajouter un handler générique `Exception.class` → 500 `{"code":"ERREUR_INTERNE", ...}` pour se prémunir de toute évolution future qui introduirait un log de stack trace.

---

## SEC-TASK-005 — Stockage JWT frontend & exposition XSS

### Résumé
Évaluation du choix `sessionStorage` pour le JWT (documenté par AgentFrontendDeveloper) et recherche de points d'injection XSS qui rendraient le vol de token plus probable.

### Constats
- `AuthService` (frontend) stocke le token dans `sessionStorage` (clé `banque-credit-auth`), avec commentaire explicite justifiant ce choix ("cleared when the browser tab closes to limit token leakage window").
- Alternative plus robuste (cookie `httpOnly` + `SameSite`) **non retenue** — écart documenté par rapport à la bonne pratique OWASP de non-stockage du JWT en storage accessible par JS. Cela reste un choix architectural (DEC-002 ne l'impose pas explicitement), donc **hors périmètre de correction directe par AgentSecurity**.
- Recherche transverse `innerHTML`, `bypassSecurityTrust*` dans tout `src/app` : **aucune occurrence trouvée**. Les templates utilisent l'interpolation Angular standard (`{{ }}`), qui applique l'échappement/sanitization par défaut du moteur de rendu Angular. Aucun point d'injection DOM direct identifié dans le code applicatif livré.
- Le risque XSS résiduel provient donc principalement de **vulnérabilités du framework Angular lui-même** (voir SEC-TASK-007 : plusieurs CVE XSS HIGH affectent la version `@angular/core` `^18.2.0` utilisée), pas du code applicatif écrit par AgentFrontendDeveloper.

### Sévérité des constats
- **Medium** : sessionStorage pour un JWT est un risque XSS-dépendant reconnu (OWASP) ; **accepté comme risque proportionné** pour ce projet pédagogique en l'absence de tout point d'injection applicatif trouvé, **à condition que** les CVE XSS d'Angular (SEC-TASK-007) soient traitées avant toute mise en situation réelle/démonstration publique exposée à des utilisateurs non maîtrisés.
- Pas de finding bloquant : aucune preuve d'exploitabilité XSS dans le code livré.

### Règle(s) métier vérifiée(s)
Aucune RG-BANK directe — relève du principe transverse "Transport/stockage" du mandat AgentSecurity.

### Résiduel
- Documenté comme risque accepté sous condition ; à réévaluer si le périmètre passe d'un usage pédagogique à une exposition réelle (cf. recommandation de bascule vers cookie httpOnly si le projet évolue).

---

## SEC-TASK-006 — Configuration CORS

### Résumé
Revue de `SecurityConfig.corsConfigurationSource()`.

### Constats (avant correction)
- `configuration.setAllowedOriginPatterns(List.of("*"))` combiné à `configuration.setAllowCredentials(true)`. Bien que l'authentification se fasse par en-tête `Authorization: Bearer` (pas de cookie automatique, donc le risque CSRF classique est réduit), cette combinaison reste une mauvaise pratique CORS reconnue : elle autorise **n'importe quel site tiers** à effectuer des requêtes cross-origin "credentialed" (ex. si un jour un cookie de session était ajouté, ou si un utilisateur XSS-compromis exécute du JS qui appelle l'API depuis n'importe quel domaine avec le token en mémoire). Combiné aux CVE XSS Angular (SEC-TASK-007), ce wildcard aggrave l'impact potentiel d'une compromission XSS en supprimant une barrière de défense en profondeur.

### Correction appliquée (AUTO-FIX)
`allowedOriginPatterns` externalisé via la propriété `app.cors.allowed-origins` (liste séparée par virgules), avec valeur par défaut restreinte à `http://localhost:4200` (origine du frontend Angular en dev). En production, il suffit de définir `CORS_ALLOWED_ORIGINS` avec le(s) domaine(s) réel(s) du frontend statique. Aucune régression fonctionnelle attendue : le frontend de dev (`ng serve` sur le port 4200 par défaut) continue de fonctionner ; le build de production sert le frontend derrière le même host que le backend (`environment.prod.ts` → chemin relatif `/banque-credit/api/v1`), donc CORS n'intervient même pas en production dans la configuration de déploiement documentée (même origine).

### Sévérité
**High → corrigé (fix appliqué).**

### Règle(s) métier vérifiée(s)
Aucune RG-BANK directe — durcissement transverse "Transport et stockage".

### Résiduel
Si un jour le frontend est servi depuis un domaine distinct du backend en production, s'assurer que `CORS_ALLOWED_ORIGINS` est positionné avec la/les URL(s) exacte(s) de production (pas de wildcard).

---

## SEC-TASK-007 — Dépendances npm (frontend)

### Résumé
Exécution de `npm audit` (frontend) et classification des CVE par sévérité et par exposition runtime (prod) vs outillage (dev).

### Constats
- **Audit complet (prod + dev)** : 54 avisories — 1 **critical**, 32 **high**, 14 **moderate**, 7 **low**.
- **Audit restreint aux dépendances de production (`--omit=dev`)** : **8 vulnérabilités, toutes de sévérité HIGH**, concentrées sur les packages `@angular/core`, `@angular/common`, `@angular/compiler`, `@angular/animations`, `@angular/forms`, `@angular/platform-browser`, `@angular/platform-browser-dynamic`, `@angular/router` (tous en version `^18.2.0`, plage vulnérable `<=18.2.14` / `<=19.2.25` selon le CVE).
- Détail des CVE HIGH en production les plus significatives :
  - `GHSA-g93w-mfhg-p222` — **XSS via bindings i18n** (CVSS 9.0) sur `@angular/core`/`@angular/compiler`
  - `GHSA-jrmj-c5cx-3cw6` — XSS via attributs de script SVG non sanitizés
  - `GHSA-v4hv-rgfq-gp49` — XSS stocké via SVG/MathML
  - `GHSA-jj27-h5hq-8x99` — XSS via attributs event-handler i18n
  - `GHSA-q6f4-qqrg-jv6x` — fuite d'information via cache de requêtes credentialed (`HttpTransferCache`) — non utilisé activement dans cette app SPA classique, mais présent dans le package
  - Plusieurs CVE DoS (OOM) sur le formatage de dates/nombres
- **Ces 8 CVE HIGH affectent du code exécuté en runtime dans le navigateur de l'utilisateur final** (le framework Angular lui-même) — ce n'est **pas** de la dette d'outillage dev uniquement, contrairement à ce qu'une lecture rapide du chiffre "54" pourrait suggérer.
- Le reste des 54 avisories (1 critical + 24 high supplémentaires + 14 moderate + 7 low) provient de `devDependencies` (chaîne `@angular-devkit/build-angular`, `karma`, et leurs transitives) : risque limité à la chaîne de build/CI, pas au bundle livré au navigateur, mais à corriger également par hygiène de chaîne d'approvisionnement (supply chain).
- **Non corrigé directement par AgentSecurity** : la remédiation proposée par `npm audit` (`fixAvailable`) implique un saut de version majeure Angular 18 → 20/21 pour la totalité des packages `@angular/*`, ce qui est un changement d'ampleur architecturale (breaking changes potentiels sur templates/API, nécessite tests de non-régression complets) — hors mandat d'AgentSecurity, à arbitrer par AgentArchitect/AgentOrchestrator.

### Sévérité
**High — non corrigé, remédiation nécessitant arbitrage.**

### Règle(s) métier vérifiée(s)
Aucune RG-BANK directe — OWASP A06:2021 (Vulnerable and Outdated Components).

### Résiduel / recommandation
- Remonter à AgentArchitect/AgentOrchestrator : planifier une montée de version Angular (18 → au moins une version corrigeant les CVE XSS listées) avant toute mise en démonstration exposée publiquement, avec un cycle de tests de non-régression (AgentQA) dédié.
- À court terme (mitigation partielle sans upgrade majeur) : vérifier si un correctif mineur/patch existe dans la plage 18.x (à ce jour, `npm audit` ne propose qu'un saut majeur ; à revalider périodiquement).

---

## SEC-TASK-008 — Exposition de données à une IA

### Résumé
Recherche transverse de tout point d'intégration IA (mots-clés : `openai`, `chatgpt`, `azure.ai`, `chatbot`, `AIService`, etc.) dans `apps/banque-credit/**`.

### Constats
**Aucun résultat.** La spécification fonctionnelle (§1.3 Hors périmètre) confirme explicitement qu'aucune intégration avec un moteur de scoring réel ou un service externe n'est prévue dans cette itération.

### Sévérité
**N/A** — non applicable pour cette itération.

### Règle(s) métier vérifiée(s)
Aucune (hors RG-BANK, mandat transverse AgentSecurity).

### Résiduel
Si un assistant d'éligibilité IA ou un chatbot est introduit dans une itération future, une revue SEC-AREA-005 dédiée devra être déclenchée avant mise en production, avec vérification stricte qu'aucune donnée client réelle/confidentielle n'est transmise sans anonymisation.

---

## Synthèse des findings par sévérité

| Sévérité | Finding | Statut |
|---|---|---|
| High | `app.jwt.secret` commité en clair, non externalisé (SEC-TASK-003) | **Corrigé (auto-fix)** |
| High | CORS `allowedOriginPatterns("*")` + `allowCredentials(true)` (SEC-TASK-006) | **Corrigé (auto-fix)** |
| High | 8 CVE HIGH sur dépendances Angular de production (XSS/DoS/cache poisoning) (SEC-TASK-007) | **Non corrigé — arbitrage requis (upgrade majeur Angular)** |
| Medium | Stockage JWT en `sessionStorage` (SEC-TASK-005) | **Accepté sous condition** (pas de point XSS applicatif trouvé, mais dépend de la résolution des CVE Angular ci-dessus) |
| Low | `TransitionRules` non branché dans `DecisionServiceImpl` (SEC-TASK-001) | **Non corrigé — note d'hygiène de code pour AgentReviewer** |
| Low | Absence de handler `Exception.class` catch-all (SEC-TASK-004) | **Non corrigé — recommandation défensive non bloquante** |
| Low | 46 avisories npm supplémentaires en devDependencies (chaîne de build) (SEC-TASK-007) | **Non corrigé — hygiène supply chain, non bloquant pour le runtime livré** |

Aucun **blocker** identifié.

---

## Security Quality Gate (final)

### 1. Résolution des findings
- Aucun **blocker** ni **high** ne reste sans réponse :
  - Les 2 findings High corrigibles rapidement (secret JWT non externalisé, CORS wildcard+credentials) ont été **corrigés directement** (auto-fix, build backend revérifié OK).
  - Le finding High restant (CVE Angular en production) est **documenté et explicitement transmis pour arbitrage** à AgentArchitect/AgentOrchestrator (nécessite un saut de version majeure, hors mandat de correction directe AgentSecurity) — donc **acquitté**, pas ignoré.
- **PASS**

### 2. Correction de l'autorisation
- Chaque transition de statut (RG-BANK-05) est protégée par le rôle exact attendu, vérifié méthode par méthode (`soumettre`/`annuler` = CONSEILLER ; `analyser`/`accepter`/`refuser` = RESPONSABLE_CREDIT), avec défense en profondeur (`@PreAuthorize` + vérification manuelle du statut courant).
- Le commentaire obligatoire de refus (RG-BANK-07) est appliqué côté serveur dans `DecisionServiceImpl.refuser`, non contournable par appel API direct.
- **PASS**

### 3. Secrets et logging
- `jwt.secret` externalisé via variable d'environnement (fix appliqué) ; mots de passe seed en BCrypt (non plaintext) ; aucun mot de passe/token/JWT loggé (aucune instruction de logging trouvée dans le code applicatif) ; JWT stocké en `sessionStorage` côté frontend — écart documenté et justifié par l'absence de point d'injection XSS trouvé dans le code livré, accepté comme risque proportionné pour ce périmètre pédagogique.
- **PASS avec réserve documentée** (sessionStorage = risque accepté sous condition, cf. SEC-TASK-005)

### 4. Exposition de données à une IA
- Aucun point d'intégration IA dans le périmètre fonctionnel actuel — confirmé par recherche transverse du code et par la spécification fonctionnelle (§1.3 Hors périmètre).
- **PASS (N/A pour cette itération)**

---

## Verdict final : **GO conditionnel**

La feature "banque-credit" peut être mise en release dans son contexte pédagogique actuel. Les 2 findings High corrigibles ont été traités immédiatement. Le finding High restant (dépendances Angular avec CVE XSS/DoS) ne bloque pas ce GO **à condition que** :
1. Ce finding soit formellement remonté à AgentArchitect/AgentOrchestrator pour planification d'une montée de version Angular avant toute exposition publique/démonstration à un public non maîtrisé ;
2. Le risque accepté du stockage JWT en `sessionStorage` reste réévalué si le périmètre applicatif évolue (ajout de contenu utilisateur non maîtrisé, nouvelle dépendance front, etc.) ou si les CVE Angular ne sont pas traitées à moyen terme.

Aucun blocage sur l'autorisation par rôle (RG-BANK-05), le commentaire obligatoire de refus (RG-BANK-07), ni sur l'exposition de données à une IA (non applicable).



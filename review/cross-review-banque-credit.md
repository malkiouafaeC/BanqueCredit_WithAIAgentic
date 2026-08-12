# Revue croisée (cross-review) — Feature "banque-credit"

Auteur : AgentReviewer
Plan de référence : plan/plan-cross-review-banque-credit.md
Périmètre : voir plan §1 (inspection directe du code réel, pas seulement des synthèses des autres agents)

---

## REV-TASK-01 — Enums et rôles (COMPARE-01, COMPARE-02, COMPARE-03, COMPARE-13)

### Résumé
Comparaison caractère par caractère des enums backend (`model/Statut.java`, `model/ScoreSimplifie.java`, `model/Role.java`) avec leurs équivalents frontend (`statut.enum.ts`, `score-simplifie.enum.ts`, `role.enum.ts`), et vérification du claim de rôle transmis par le JWT (`AuthServiceImpl.login` → `user.getRole().name()`) jusqu'à sa consommation frontend (`auth.service.ts`).

### Constats
- **`Statut`** : `BROUILLON, SOUMISE, EN_ANALYSE, ACCEPTEE, REFUSEE, ANNULEE` — identique des deux côtés, aucune divergence d'orthographe/casse.
- **`ScoreSimplifie`** : `EXCELLENT, BON, MOYEN, FAIBLE` — identique des deux côtés.
- **`Role`** : `CONSEILLER, RESPONSABLE_CREDIT` — identique des deux côtés. `AuthServiceImpl.login` renvoie `user.getRole().name()` tel quel (pas de mapping/renommage), et `CustomUserDetailsService` préfixe `"ROLE_"` uniquement pour les `GrantedAuthority` Spring Security internes (jamais exposé au frontend) — cohérent avec les `@PreAuthorize("hasRole('CONSEILLER')")` qui attendent ce préfixe implicitement. Le frontend `auth.service.ts` stocke `response.role` tel quel comme `Role` sans transformation — cohérent.
- **Rôles requis par route vs par endpoint (COMPARE-13)** : correspondance vérifiée exhaustivement entre `app.routes.ts` (`roleGuard`) et les `@PreAuthorize` de service :
  - `/clients/nouveau` (CONSEILLER) ↔ `ClientServiceImpl.creer` (`@PreAuthorize("hasRole('CONSEILLER')")`) — cohérent.
  - `/demandes/nouvelle` (CONSEILLER) ↔ `DemandeCreditServiceImpl.creer` — cohérent.
  - `/demandes/:id/decision` (RESPONSABLE_CREDIT) ↔ `DecisionServiceImpl.analyser/accepter/refuser` — cohérent.
  - Actions `soumettre`/`annuler` (CONSEILLER, `DecisionServiceImpl`) sont exposées sur la page `demande-detail.page.ts` (route sans `roleGuard` dédié) mais leur visibilité est conditionnée par `DemandeActionsService.canSoumettre/canAnnuler` qui vérifie `role === Role.CONSEILLER` — cohérent en pratique (le bouton n'apparaît pas pour un RESPONSABLE_CREDIT), et le backend refuserait de toute façon l'action (403) en défense en profondeur.
  - **Point à noter (non bloquant)** : la route `/clients` (liste) est restreinte à `roleGuard(Role.CONSEILLER)` côté frontend, alors que `ClientController.lister()` (`GET /clients`) n'a **aucune** restriction de rôle côté backend (accessible à `RESPONSABLE_CREDIT` via appel API direct ou Swagger). Ceci correspond exactement à ce que documente `spec-architecture-banque-credit.md` §5.3 (`/clients` → "CONSEILLER (Q5)" alors que `/clients/:id` → "CONSEILLER, RESPONSABLE_CREDIT") : c'est donc un choix architectural assumé (restreindre la page de liste, pas l'API), pas un oubli. Aucune fuite de données sensible n'est en jeu (lecture seule, même périmètre de données que le détail). Classé **low / informationnel**, à confirmer explicitement par AgentArchitect si ce n'est pas l'intention.

### Sévérité des constats
Aucun blocker, aucun high. **Low** : asymétrie documentée frontend (liste clients CONSEILLER uniquement) vs backend (API ouverte aux deux rôles) — cohérente avec l'architecture approuvée mais mérite confirmation explicite.

### Règle(s) métier vérifiée(s)
RG-BANK-05 (casse `Statut`), RG-BANK-08 (casse `ScoreSimplifie`), Q5 (séparation des rôles par route vs par endpoint).

### Gap vs demande initiale
Aucun.

---

## REV-TASK-02 — Contrat d'erreur API (COMPARE-04)

### Résumé
Vérification de la forme réelle de `ErrorResponseDto` (backend) contre `ApiErrorResponse` (frontend) et de sa consommation dans les pages ayant déclenché INT-CREDIT-001.

### Constats
- `dto/ErrorResponseDto.java` sérialise `erreurs` comme `List<ChampErreur>` (donc un **tableau JSON** `[{ "champ": "...", "message": "..." }, ...]`), confirmé par lecture directe du DTO et de `GlobalExceptionHandler.handleValidation` qui construit bien une `List` via `.stream().map(...).toList()`.
- `core/models/api-error.model.ts` type désormais `erreurs?: { champ: string; message: string }[] | null` — **tableau**, conforme au backend réel. Le commentaire inline `/** Real backend shape ... */` documente explicitement la correction.
- `client-create.page.ts` et `demande-create.page.ts` consomment `body.erreurs` via `body?.erreurs?.length` puis `Object.fromEntries(body.erreurs.map((e) => [e.champ, e.message]))` — logique correcte pour un tableau (aurait échoué silencieusement ou levé une exception si `erreurs` avait été traité comme un dictionnaire). Le correctif INT-CREDIT-001 est bien reflété dans le code actuellement livré, sur les deux pages concernées.
- Recherche transverse : aucune autre page frontend ne consomme `body.erreurs` d'une façon incompatible (les autres pages — `decision.page.ts`, `demande-detail.page.ts` — ne lisent que `body?.message`, jamais `body.erreurs`, donc non concernées par ce risque).

### Sévérité des constats
Aucun finding — le contrat est cohérent et le bug INT-CREDIT-001 est bien corrigé sur son périmètre réel.

### Règle(s) métier vérifiée(s)
AC-004-4 (plusieurs erreurs simultanées affichées), AC-008-x (messages par champ).

### Gap vs demande initiale
Aucun.

---

## REV-TASK-03 — Validation RG-BANK-01/02/03 (COMPARE-05, COMPARE-06, COMPARE-07)

### Résumé
Comparaison champ par champ des contraintes Bean Validation backend et des `Validators` Angular associés.

### Constats
- **RG-BANK-01 (Client)** : `nom` → backend `@NotBlank` vs frontend `Validators.required` seul. **Écart mineur** : `Validators.required` accepte une chaîne non vide composée uniquement d'espaces (ex. `"   "`), alors que le backend `@NotBlank` la rejette. Un utilisateur pourrait donc voir son formulaire "valide" côté client puis recevoir un rejet serveur (comportement dégradé mais pas un contournement de règle — le backend reste la source de vérité et rejette bien). `revenuMensuel` → backend `@DecimalMin("0.01")` vs frontend `Validators.min(0.01)` — cohérent. `chargesMensuelles` → backend `@DecimalMin("0.00")` vs frontend `Validators.min(0)` — cohérent.
- **RG-BANK-02 (montant)** : backend `@DecimalMin("1000.00", inclusive=false)` + `@DecimalMax("100000.00", inclusive=true)` vs frontend `montantMinExclusive()` (rejette `<= 1000`, donc strictement `> 1000`) + `Validators.max(100000)` (inclusif par défaut dans Angular) — **cohérence exacte des bornes inclusif/exclusif**, y compris sur les valeurs limites (1000 rejeté des deux côtés, 100000 accepté des deux côtés).
- **RG-BANK-03 (durée)** : backend `@Min(12)` + `@Max(84)` vs frontend `Validators.min(12)` + `Validators.max(84)` — cohérence exacte (12 et 84 acceptés des deux côtés, 11 et 85 rejetés des deux côtés).
- Dans tous les cas, la validation frontend est un **miroir non substitutif** : la création n'aboutit que si le backend valide également (`ClientCreateDto`/`DemandeCreditCreateDto` restent les seules autorités réelles, cf. SEC-TASK-002 déjà vérifié par AgentSecurity côté contournement API direct).

### Sévérité des constats
- **Low** : `nom` — `Validators.required` n'exclut pas les chaînes uniquement composées d'espaces, contrairement au backend `@NotBlank`. Impact limité (le backend rejette quand même la requête, l'utilisateur voit juste une erreur serveur au lieu d'une erreur de formulaire immédiate). Recommandation : ajouter un validator `Validators.pattern(/\S/)` ou équivalent côté `client-create.page.ts` pour un feedback plus rapide. Propriétaire suggéré : AgentFrontendDeveloper.

### Règle(s) métier vérifiée(s)
RG-BANK-01, RG-BANK-02, RG-BANK-03 — cohérence confirmée sur les bornes, écart mineur documenté sur le blanc/`nom`.

### Gap vs demande initiale
Aucun gap bloquant. L'écart `nom`/espaces est un raffinement UX, pas un manquement à une AC (aucune AC ne teste explicitement un nom composé uniquement d'espaces).

---

## REV-TASK-04 — RG-BANK-04/08 simulation et score (COMPARE-08, COMPARE-12)

### Résumé
Vérification qu'aucune réimplémentation frontend des formules normatives (mensualité, taux d'endettement, bandes de score) n'existe, et que le frontend se contente d'afficher les valeurs renvoyées par le backend.

### Constats
- `SimulationServiceImpl.calculerMensualite/calculerTauxEndettement/calculerScore` sont les **seules** implémentations trouvées de ces formules dans tout le dépôt (recherche transverse sur `mensualite`, `tauxEndettement`, `scoreSimplifie` côté frontend limitée aux modèles/composants d'affichage).
- `demande-detail.page.ts.simuler()` appelle `demandeService.simuler(id)` et affiche tel quel le `SimulationResult` reçu — aucun calcul local.
- `shared/charte/badge-score.component.ts` ne fait que mapper la valeur `ScoreSimplifie` reçue vers un libellé/couleur (`SCORE_CHARTE`) — **aucune classification par bande n'est recalculée côté frontend** (les seuils 20%/35%/50% n'apparaissent nulle part côté Angular).
- Source unique de vérité respectée : le backend est la seule autorité pour RG-BANK-04 et RG-BANK-08.

### Sévérité des constats
Aucun finding — pas de duplication détectée, conforme à DEC-ENT-002 de l'architecture (recalcul serveur systématique).

### Règle(s) métier vérifiée(s)
RG-BANK-04, RG-BANK-08.

### Gap vs demande initiale
Aucun.

---

## REV-TASK-05 — RG-BANK-05 transitions et code mort `TransitionRules` (COMPARE-09, DEBT-01, DEBT-02)

### Résumé
Inspection complète de `model/TransitionRules.java`, de son (absence d')utilisation réelle dans `DecisionServiceImpl`, et de la troisième représentation de la même règle côté frontend (`DemandeActionsService`), pour décider explicitement du sort du code mort (mandat explicite de la mission).

### Constats
- **Confirmation directe (lecture du code, pas seulement de la synthèse SEC-CREDIT-001)** : `model/TransitionRules.java` définit `verifierTransitionAutorisee(Statut, Statut, Role)` et `transitionExiste(Statut, Statut)`, une table de transitions statique fidèle à l'architecture §4.4. **Aucun appel** à ces deux méthodes n'existe nulle part dans `service/impl/DecisionServiceImpl.java` (recherche confirmée : les méthodes `soumettre/annuler/analyser/accepter/refuser` réimplémentent chacune leur propre condition `if (demande.getStatut() != Statut.X)`).
- Le résultat fonctionnel des deux implémentations (table `TransitionRules` vs `if/else` de `DecisionServiceImpl`) est **actuellement identique et vérifié transition par transition** (BROUILLON→SOUMISE/ANNULEE = CONSEILLER ; SOUMISE→EN_ANALYSE/ANNULEE ; EN_ANALYSE→ACCEPTEE/REFUSEE = RESPONSABLE_CREDIT) — aucune divergence fonctionnelle aujourd'hui.
- **Troisième représentation** de la même règle métier trouvée côté frontend : `DemandeActionsService` (`canSoumettre`, `canAnnuler`, `canAnalyser`, `canAccepter`, `canRefuser`) réimplémente indépendamment la même table pour piloter la **visibilité** des boutons d'action dans `demande-detail.page.ts` et `decision.page.ts`. Le commentaire du fichier lui-même reconnaît le risque ("mitigates RISK-CREDIT-003: duplicated/divergent visibility logic") mais ne résout que la duplication **interne au frontend** (une seule classe pour les deux pages), pas la duplication **cross-stack** avec le backend.
- Conséquence : la règle RG-BANK-05 existe aujourd'hui sous **trois formes distinctes dans le dépôt** : (1) `TransitionRules` (backend, non utilisé), (2) `if/else` de `DecisionServiceImpl` (backend, réellement exécuté, source de vérité actuelle), (3) `DemandeActionsService` (frontend, visibilité UI uniquement). Le frontend n'a **aucune autorité d'enforcement** (le backend refuse l'action par 409/403 même si un bouton était visible par erreur), donc il n'y a **pas de risque de bypass utilisateur aujourd'hui** — mais le risque de divergence future (une des trois logiques modifiée sans les deux autres) est réel et documenté par AgentSecurity comme "Low" tant qu'il reste non exploité.

### Décision sur `TransitionRules` (mandat explicite de cette revue)
**Décision : recommander de brancher `TransitionRules` dans `DecisionServiceImpl` plutôt que de le supprimer.**
Rationale :
1. `TransitionRules` est déjà testable indépendamment (méthode statique pure, `EnumMap`), documenté (`DEC-ENT-003`), et strictement équivalent fonctionnellement au code actuellement exécuté — le supprimer ferait perdre un artefact conforme à l'architecture sans gain, et laisserait la logique de transition dispersée dans 5 méthodes de service sans point de vérité unique.
2. Le brancher (remplacer chaque `if (demande.getStatut() != Statut.X)` par un appel à `TransitionRules.transitionExiste(...)` et `TransitionRules.verifierTransitionAutorisee(...)`) réduit le nombre de représentations de la règle de 2 à 1 côté backend (la copie frontend `DemandeActionsService` reste nécessaire et légitime car elle sert un objectif différent — visibilité UI, pas enforcement — et n'a pas vocation à disparaître).
3. Alternative rejetée (suppression de `TransitionRules`) : elle supprimerait un code déjà écrit, testé en conformité fonctionnelle par cette revue, et referait perdre le seul point testable isolément de la table de transitions — un pas en arrière par rapport à DEC-ENT-003.
Cette remédiation est **à exécuter par AgentBackendDeveloper**, pas par AgentReviewer (contrainte de mission : constat uniquement, pas de modification de code).

### Sévérité des constats
- **Low** (aucun bug fonctionnel constaté aujourd'hui) : code mort `TransitionRules` — décision documentée ci-dessus (brancher).
- **Low** : triple représentation de la règle RG-BANK-05 dans le dépôt (DEBT-02) — pas de risque de bypass aujourd'hui (le backend est la seule autorité d'enforcement pour les 3 zones), mais risque de divergence future si l'une des trois est modifiée isolément. Recommandation : documenter (commentaire croisé) que `DemandeActionsService` doit rester synchronisé avec la table §4.4 de l'architecture à chaque évolution de RG-BANK-05.

### Règle(s) métier vérifiée(s)
RG-BANK-05 — enforcement réel vérifié transition par transition, cohérent entre les 3 représentations aujourd'hui.

### Gap vs demande initiale
Aucun gap fonctionnel. Dette technique documentée avec décision explicite (mandat rempli).

---

## REV-TASK-06 — RG-BANK-06 éligibilité (COMPARE-10)

### Résumé
Vérification qu'aucune logique d'éligibilité (taux ≤35%, revenu ≥1500, montant ≤50000) n'est dupliquée côté frontend, et que le frontend se contente d'afficher l'erreur 422 renvoyée par le backend.

### Constats
- `EligibiliteServiceImpl.verifierEligibilite` est la **seule** implémentation des 3 conditions cumulatives trouvée dans le dépôt (recherche transverse des seuils `35`, `1500`, `50000` limitée au backend).
- `decision.page.ts.accepter()` délègue entièrement à `decisionService.accepter(id)` et se contente d'intercepter un statut HTTP `422` pour afficher `body?.message` — **aucun calcul ou pré-vérification d'éligibilité côté client** avant l'appel API (pas de blocage optimiste du bouton "Accepter" basé sur une règle réimplémentée).
- Conforme à Q3 (blocage strict serveur, aucune option de forçage) et à DEC-004 (validation manuelle conditionnelle isolée en service).

### Sévérité des constats
Aucun finding — source unique de vérité respectée, aucun risque de contournement ou de divergence.

### Règle(s) métier vérifiée(s)
RG-BANK-06.

### Gap vs demande initiale
Aucun.

---

## REV-TASK-07 — RG-BANK-07 commentaire obligatoire (COMPARE-11)

### Résumé
Comparaison exacte de la règle de "commentaire vide" entre `DecisionServiceImpl.refuser` (backend) et `commentaireObligatoire()` (frontend, `decision.page.ts`).

### Constats
- Backend : `commentaire == null || commentaire.trim().isEmpty()` → rejeté (`CommentaireObligatoireException`, HTTP 400, code `COMMENTAIRE_OBLIGATOIRE`).
- Frontend : `!control.value || !String(control.value).trim()` → invalide (`{ commentaireVide: true }`), combiné à `Validators.required` sur le même contrôle.
- Les deux logiques sont **équivalentes bit à bit** : une chaîne `null`/vide/composée uniquement d'espaces est rejetée dans les deux cas, une chaîne avec au moins un caractère non-espace est acceptée dans les deux cas.
- Le frontend ne fait que **mirorer** la règle pour l'UX (empêcher la soumission du formulaire avant l'appel réseau) ; le backend reste l'autorité (vérifié indépendamment par SEC-TASK-002 : un appel API direct sans commentaire échoue bien).

### Sévérité des constats
Aucun finding — cohérence exacte confirmée.

### Règle(s) métier vérifiée(s)
RG-BANK-07 (AC-015-1, AC-015-2 — y compris le cas "espaces uniquement").

### Gap vs demande initiale
Aucun.

---

## REV-TASK-08 — Dashboard Q4 (COMPARE-14)

### Résumé
Vérification que l'exclusion `BROUILLON`/`ANNULEE` des agrégats (montant total, taux moyen) n'est appliquée qu'une seule fois (backend) et fidèlement affichée côté frontend.

### Constats
- `DashboardServiceImpl.EXCLUS_AGREGATS = List.of(Statut.BROUILLON, Statut.ANNULEE)` puis `findByStatutNotIn(EXCLUS_AGREGATS)` pour `montantTotal` et `tauxMoyen` — conforme à Q4/AC-ARCH-006.
- Les compteurs par statut (`nbSoumises`, `nbEnAnalyse`, `nbAcceptees`, `nbRefusees`) sont calculés séparément par statut exact, sans lien avec le périmètre d'exclusion des agrégats — cohérent avec AC-017-1 (comptage exact par statut) vs AC-017-2/3 (agrégats sur périmètre restreint).
- `dashboard.page.ts` affiche `DashboardDto` tel quel (`dashboard.service.ts` → `GET /dashboard`), sans recalcul d'aucune sorte côté Angular.

### Sévérité des constats
Aucun finding.

### Règle(s) métier vérifiée(s)
Q4 (AC-ARCH-006), US-017 (AC-017-1..4).

### Gap vs demande initiale
Aucun.

---

## REV-TASK-09 — Traçabilité US-001..017 / RG-BANK-01..08 → artefacts livrés (COMPARE-15)

### Résumé
Traçage de chaque user story et règle métier vers un artefact backend + frontend concret (contrôleur/service + page/service Angular), ou exception documentée si non traçable directement.

### Table de traçabilité

| US | Artefact backend | Artefact frontend | Statut |
|---|---|---|---|
| US-001 (connexion) | `AuthController.login`, `AuthServiceImpl` | `login.page.ts`, `auth.service.ts` | Tracé |
| US-002 (redirection non authentifié) | `SecurityConfig`/filtre JWT (401) | `core/guards/auth.guard.ts` | Tracé |
| US-003 (création client) | `ClientController.creer`, `ClientServiceImpl.creer` | `client-create.page.ts` | Tracé |
| US-004 (erreurs par champ) | `ClientCreateDto` + `GlobalExceptionHandler.handleValidation` | `client-create.page.ts` (`serverErrors`) | Tracé |
| US-005 (liste clients) | `ClientController.lister` | `client-list.page.ts` | Tracé |
| US-006 (détail client + demandes) | `ClientController.consulterDetail`, `ClientServiceImpl.consulterDetail` | `client-detail.page.ts` | Tracé |
| US-007 (création demande) | `DemandeCreditController.creer`, `DemandeCreditServiceImpl.creer` | `demande-create.page.ts` | Tracé |
| US-008 (erreurs bornes) | `DemandeCreditCreateDto` (Bean Validation) | `demande-create.page.ts` (Validators) | Tracé |
| US-009 (simulation) | `DemandeCreditController.simuler`, `SimulationServiceImpl` | `demande-detail.page.ts` (`simuler()`) | Tracé |
| US-010 (soumission) | `DecisionController.soumettre`, `DecisionServiceImpl.soumettre` | `demande-detail.page.ts` (`soumettre()`) | Tracé |
| US-011 (annulation) | `DecisionController.annuler`, `DecisionServiceImpl.annuler` | `demande-detail.page.ts` (`annuler()`) | Tracé |
| US-012 (passage en analyse) | `DecisionController.analyser`, `DecisionServiceImpl.analyser` | `decision.page.ts` (`analyser()`) | Tracé |
| US-013 (acceptation) | `DecisionController.accepter`, `DecisionServiceImpl.accepter` + `EligibiliteServiceImpl` | `decision.page.ts` (`accepter()`) | Tracé |
| US-014 (refus + commentaire) | `DecisionController.refuser`, `DecisionServiceImpl.refuser` | `decision.page.ts` (`refuser()`, `refusForm`) | Tracé |
| US-015 (empêché sans commentaire) | `CommentaireObligatoireException` (400) | `decision.page.ts` (`commentaireObligatoire` validator) | Tracé |
| US-016 (historique) | `HistoriqueController`, `HistoriqueServiceImpl` | `features/historique/historique-list.component.ts` (intégré à `demande-detail.page`) | Tracé |
| US-017 (dashboard) | `DashboardController`, `DashboardServiceImpl` | `dashboard.page.ts` | Tracé |

Toutes les RG-BANK-01 à RG-BANK-08 ont été vérifiées explicitement dans REV-TASK-02 à REV-TASK-08 ci-dessus (pas seulement assumées).

### Sévérité des constats
Aucun finding — traçabilité complète, aucune US ou RG-BANK sans artefact concret.

### Gap vs demande initiale
Aucun gap non documenté. Le seul point déjà connu et non re-traité ici (hors mandat, hérité de QA-CREDIT-001) est AC-013-1 (cas limite triple borne mathématiquement infaisable), à traiter par AgentBA, pas par cette revue.

---

## Synthèse des findings par sévérité

| Sévérité | Finding | Owner suggéré | Statut |
|---|---|---|---|
| Blocker | Aucun | — | — |
| High | Aucun | — | — |
| Medium | Aucun nouveau (RISK-03 du plan — risque de régression sur le contrat `erreurs` — vérifié non avéré aujourd'hui, cf. REV-TASK-02) | — | Vérifié, non avéré |
| Low | `nom` Client : `Validators.required` n'exclut pas les chaînes uniquement composées d'espaces (contrairement au backend `@NotBlank`) | AgentFrontendDeveloper | Non corrigé — recommandation UX non bloquante |
| Low | `TransitionRules` non branché dans `DecisionServiceImpl` — décision : **le brancher** (voir REV-TASK-05) | AgentBackendDeveloper | Non corrigé — décision documentée, remédiation à planifier |
| Low | Triple représentation de la table de transitions RG-BANK-05 (backend mort + backend réel + frontend visibilité) | AgentBackendDeveloper / AgentFrontendDeveloper | Non corrigé — documenté, pas de risque de bypass actuel |
| Low | Route frontend `/clients` (liste) restreinte à CONSEILLER alors que l'API `GET /clients` est ouverte aux deux rôles | AgentArchitect (confirmation) | Conforme à l'architecture documentée — à confirmer explicitement si non intentionnel |
| Low (hérité, non re-traité) | AC-013-1 cas limite triple borne mathématiquement infaisable (QA-CREDIT-001) | AgentBA | Hérité, non bloquant pour ce gate |
| Low/Medium (hérité, non re-traité) | Findings SEC-CREDIT-001 déjà classés (CVE Angular High, sessionStorage Medium, absence handler catch-all Low) | AgentArchitect/AgentOrchestrator/AgentBackendDeveloper | Hérité, non bloquant pour ce gate |
| Low/Medium (hérité, non re-traité) | Risques DEPLOY-CREDIT-001 (RISK-001/002/005) | AgentDeployment | Hérité, non bloquant pour ce gate |

Aucun **blocker** ni **high** nouveau identifié par cette revue croisée.

---

## Dette technique et duplication — synthèse consolidée

| Zone | Nature | Sévérité | Owner | Action recommandée |
|---|---|---|---|---|
| `model/TransitionRules.java` | Code mort (jamais appelé) | Low | AgentBackendDeveloper | Brancher dans `DecisionServiceImpl` (décision de cette revue, rationale REV-TASK-05) |
| `DecisionServiceImpl` (if/else transitions) | Duplication fonctionnelle avec `TransitionRules` | Low | AgentBackendDeveloper | Remplacer les conditions manuelles par des appels à `TransitionRules` lors du branchement ci-dessus |
| `DemandeActionsService` (frontend) | Troisième représentation de la même règle RG-BANK-05 (légitime : visibilité UI, pas enforcement) | Low | AgentFrontendDeveloper | Conserver, mais documenter la dépendance à l'architecture §4.4 pour éviter une dérive silencieuse |
| `GlobalExceptionHandler` sans handler catch-all générique | Hygiène défensive (déjà noté SEC-TASK-004) | Low | AgentBackendDeveloper | Non bloquant, repris pour mémoire |
| Dépendances Angular avec CVE HIGH (déjà noté SEC-TASK-007) | Dette de sécurité, hors périmètre direct de cette revue | High (sécurité), non bloquant pour ce gate | AgentArchitect/AgentOrchestrator | Repris pour mémoire, arbitrage déjà demandé par AgentSecurity |

---

## Règles métier vérifiées (synthèse)

RG-BANK-01 à RG-BANK-08 : toutes vérifiées explicitement par inspection directe du code réel (REV-TASK-02 à REV-TASK-08), pas seulement assumées à partir des synthèses des autres agents. Aucune règle métier n'est appliquée de façon divergente entre backend et frontend qui permettrait un contournement (le frontend ne fait jamais que mirorer/afficher, jamais recalculer ou remplacer une règle serveur).

---

## Gaps vs demande initiale

Aucun gap bloquant identifié entre `spec/spec-feature-banque-credit.md` (US-001..017, RG-BANK-01..08) et les artefacts livrés (voir table de traçabilité REV-TASK-09). Le seul point de spec non entièrement cohérent en interne (AC-013-1, cas mathématiquement infaisable) est déjà documenté comme non bloquant par AgentQA et reste à traiter par AgentBA, hors mandat de cette revue.

---

## Quality Gate — Revue croisée finale (AgentReviewer)

### 1. Cohérence backend/frontend
- Aucun mismatch de contrat backend/frontend bloquant ou high identifié (enums, DTO, formes d'erreur, bornes de validation, règles conditionnelles) — vérifié par inspection directe du code réel, pas seulement des synthèses.
- Le seul écart de contrat historique (INT-CREDIT-001, `erreurs` tableau vs dictionnaire) est confirmé corrigé et cohérent sur l'ensemble des pages qui le consomment.
- **PASS**

### 2. Cohérence des règles métier RG-BANK-01 à RG-BANK-08
- Chacune des 8 règles est vérifiée cohérente entre backend (source de vérité et unique lieu d'enforcement), frontend (miroir UX non substitutif) et tests (couverture déclarée par QA-CREDIT-001).
- Aucune règle n'est réimplémentée côté frontend d'une façon qui pourrait diverger silencieusement du calcul serveur (RG-BANK-04, RG-BANK-06, RG-BANK-08 : affichage seul, jamais de recalcul client).
- **PASS**

### 3. Dette technique et duplication documentées
- `TransitionRules` (code mort) : décision explicite prise et documentée (le brancher), avec rationale (REV-TASK-05).
- Triple représentation de la table de transitions RG-BANK-05 : documentée avec sévérité et propriétaire.
- Tous les autres éléments de dette hérités (SEC-CREDIT-001, QA-CREDIT-001, DEPLOY-CREDIT-001) sont repris pour mémoire sans être re-jugés hors mandat.
- **PASS**

### 4. Alignement avec la demande initiale
- Les 17 user stories et les 8 règles métier sont toutes tracées vers un artefact backend + frontend concret (table REV-TASK-09).
- Aucun écart non documenté entre `spec-feature-banque-credit.md`/`spec-architecture-banque-credit.md` et le code livré.
- **PASS**

---

## Verdict final : **GO**

Aucun **blocker** ni **high** non traité ne subsiste à l'issue de cette revue croisée. Les 8 règles métier (RG-BANK-01 à RG-BANK-08) sont vérifiées cohérentes de bout en bout (backend = source de vérité, frontend = miroir UX, jamais de réimplémentation divergente). La dette technique identifiée est intégralement documentée avec sévérité, propriétaire et action recommandée — en particulier la décision explicite de brancher `TransitionRules` plutôt que de le supprimer.

Ce GO est **conditionnel aux réserves déjà posées par les gates précédents** (non re-jugées ici, hors mandat de cette revue) :
1. SEC-CREDIT-001 : arbitrage requis sur la montée de version Angular (CVE HIGH) avant toute exposition publique ; risque sessionStorage accepté sous condition.
2. QA-CREDIT-001 : exécution d'un test end-to-end manuel/scripté avant signature production ; AC-013-1 à clarifier par AgentBA (non bloquant).
3. DEPLOY-CREDIT-001 : RISK-001 (double préfixe contexte Tomcat), RISK-002 (secrets JWT/CORS réels non provisionnés), RISK-005 (rotation des mots de passe seed) à lever avant un environnement public réel.

Aucune de ces réserves n'est nouvelle ni aggravée par cette revue croisée ; elles sont reprises ici uniquement pour que la synthèse finale d'AgentOrchestrator dispose d'une vue consolidée.

Recommandation à AgentOrchestrator : **procéder à la synthèse de release**, en portant les 2 actions non bloquantes suivantes au backlog immédiat post-release :
- Brancher `TransitionRules` dans `DecisionServiceImpl` (AgentBackendDeveloper).
- Confirmer/documenter explicitement l'asymétrie de rôle sur la route `/clients` (liste) auprès d'AgentArchitect si elle n'est pas intentionnelle.

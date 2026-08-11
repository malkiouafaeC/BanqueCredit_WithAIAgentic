# Tasks de tests — Feature "banque-credit"

Source : plan/plan-tests-banque-credit.md (v1.0)
Backlog atomique d'implémentation de tests, complémentaire aux 78 tests backend / 77 tests frontend existants (aucune duplication).

## Ordering
1. QA-TASK-001..007 : unitaires backend Mockito (avant intégration)
2. QA-TASK-008 : non-régression backend (parcours complet)
3. QA-TASK-101..107 : unitaires frontend (branches ciblées)
4. QA-TASK-201 : exécution complète des 2 suites + rapport de couverture
5. QA-TASK-202 : quality gate final

---

### QA-TASK-001
- Target : `ClientServiceImpl` (`formulAI.project.bank.banqueCredit.service.impl.ClientServiceImpl`)
- Test type : unitaire (Mockito)
- Business rule(s) : RG-BANK-01
- Tool : JUnit5 + Mockito (`@ExtendWith(MockitoExtension.class)`)
- DoD : nouveau fichier `ClientServiceImplTest.java` — mock `ClientRepository`, teste `creer()` (mapping DTO→entité→DTO), `lister()` (liste vide et non vide), `consulterDetail()` (cas trouvé avec demandes associées mappées en résumé, cas 404 `RessourceNotFoundException`). Aucun contexte Spring chargé.

### QA-TASK-002
- Target : `DemandeCreditServiceImpl`
- Test type : unitaire (Mockito)
- Business rule(s) : RG-BANK-02, RG-BANK-03
- Tool : JUnit5 + Mockito
- DoD : nouveau fichier `DemandeCreditServiceImplTest.java` — mock `DemandeCreditRepository`, `ClientRepository`, `HistoriqueDecisionRepository`, `UserRepository`, `SimulationService`. Teste `creer()` (client introuvable → 404, création historique initiale ancienStatut=null), `lister()` avec les 4 branches de filtre (statut+clientId, statut seul, clientId seul, aucun filtre), `consulter()` (404), `simuler()` délègue correctement à `SimulationService`.

### QA-TASK-003
- Target : `DecisionServiceImpl`
- Test type : unitaire (Mockito)
- Business rule(s) : RG-BANK-05, RG-BANK-06, RG-BANK-07
- Tool : JUnit5 + Mockito
- DoD : nouveau fichier `DecisionServiceImplTest.java` — mock tous les repositories + `SimulationService` + `EligibiliteService`. Teste les 5 méthodes : `soumettre` (nominal + `TransitionInvalideException` si pas BROUILLON), `annuler` (nominal depuis BROUILLON/SOUMISE + rejet depuis EN_ANALYSE/ACCEPTEE/REFUSEE), `analyser` (nominal + rejet si pas SOUMISE), `accepter` (nominal + rejet transition + `EligibiliteService` invoquée avec les bons paramètres, propagation de `EligibiliteNonRespecteeException`), `refuser` (nominal + rejet transition + commentaire null/vide/espaces → `CommentaireObligatoireException`). Vérifie que `HistoriqueDecision` est bien sauvegardé avec ancien/nouveau statut corrects à chaque transition (`verify(historiqueDecisionRepository).save(...)`).

### QA-TASK-004
- Target : `EligibiliteServiceImpl`
- Test type : unitaire (Mockito non nécessaire, pure function — JUnit5 seul)
- Business rule(s) : RG-BANK-06
- Tool : JUnit5
- DoD : nouveau fichier `EligibiliteServiceImplTest.java` — teste bornes exactes (taux=35.00 OK, taux=35.01 KO ; revenu=1500 OK, revenu=1499 KO ; montant=50000 OK, montant=50001 KO), et documente explicitement en commentaire que la combinaison simultanée des 3 bornes exactes (AC-013-1) est mathématiquement infaisable côté intégration (renvoi vers `DecisionControllerIntegrationTest`), donc testée ici uniquement sous forme de 3 appels indépendants avec les 2 autres paramètres larges. Vérifie aussi qu'une violation unique suffit à bloquer même si les 2 autres conditions sont conformes (E13).

### QA-TASK-005
- Target : `DashboardServiceImpl`
- Test type : unitaire (Mockito)
- Business rule(s) : N/A (US-017, Q4, AC-ARCH-006)
- Tool : JUnit5 + Mockito
- DoD : nouveau fichier `DashboardServiceImplTest.java` — mock `DemandeCreditRepository` + `SimulationService`. Teste : base vide (tous compteurs à 0, montant/taux à 0 — AC-017-4), jeu réparti sur tous statuts (compteurs exacts — AC-017-1), vérification explicite que `findByStatutNotIn` est appelé avec `[BROUILLON, ANNULEE]` (AC-ARCH-006), calcul de moyenne arrondie à 2 décimales (`RoundingMode.HALF_UP`).

### QA-TASK-006
- Target : `HistoriqueServiceImpl`
- Test type : unitaire (Mockito)
- Business rule(s) : N/A (US-016)
- Tool : JUnit5 + Mockito
- DoD : nouveau fichier `HistoriqueServiceImplTest.java` — mock `HistoriqueDecisionRepository` + `DemandeCreditRepository`. Teste 404 si demande inexistante, mapping correct (ordre chronologique respecté, auteur/commentaire/snapshot mappés).

### QA-TASK-007
- Target : `AuthServiceImpl`
- Test type : unitaire (Mockito)
- Business rule(s) : N/A (US-001, AC-001-3)
- Tool : JUnit5 + Mockito
- DoD : nouveau fichier `AuthServiceImplTest.java` — mock `UserRepository`, `PasswordEncoder`, `JwtTokenProvider`. Teste : login succès (token généré, rôle retourné), utilisateur inexistant → `BadCredentialsException` message générique, utilisateur inactif (`actif=false`) → même exception générique (pas de fuite d'info), mot de passe incorrect → même exception générique.

### QA-TASK-008
- Target : Parcours complet (non-régression)
- Test type : non-régression (MockMvc, `@SpringBootTest`)
- Business rule(s) : RG-BANK-01..08 (bout en bout)
- Tool : MockMvc + `@SpringBootTest`
- DoD : nouveau fichier `ParcoursCompletNonRegressionTest.java` dans `controller/` — un seul test enchaînant : login conseiller → création client → création demande (bornes valides) → simulation (vérifie mensualité/taux/score cohérents) → soumission → login responsable → passage en analyse → décision (refus avec commentaire, RG-BANK-07) → consultation historique (4 entrées, ordre chronologique) → dashboard (compteur refusées incrémenté, montant total incluant la demande). Ce test sert de baseline de non-régression pour AgentReviewer/AgentDeployment.

---

### QA-TASK-101
- Target : `AuthService` (`src/app/features/auth/auth.service.ts`)
- Test type : unitaire (Jasmine/Karma, TestBed)
- Business rule(s) : N/A (US-001, AC-001-x)
- Tool : Jasmine/Karma + `HttpClientTestingModule`
- DoD : nouveau fichier `auth.service.spec.ts` — teste `login()` (appel HTTP correct, stockage sessionStorage, signaux `isAuthenticated`/`currentRole`/`currentUsername` mis à jour), `logout()` (signaux réinitialisés, sessionStorage vidé), `hasRole()` avec rôle unique et tableau de rôles (branches vraie/fausse), `getToken()` (présent/absent), lecture initiale depuis sessionStorage (JSON valide, JSON invalide → catch, absent → null). Couvre les 10 branches actuellement à 0%.

### QA-TASK-102
- Target : `DecisionPage.run()` (`src/app/features/decisions/decision.page.ts`)
- Test type : unitaire (Jasmine/Karma, TestBed)
- Business rule(s) : RG-BANK-05, RG-BANK-06, RG-BANK-07 (AC-012-2/3, AC-013-2..5, AC-015-1..3)
- Tool : Jasmine/Karma
- DoD : mise à jour `decision.page.spec.ts` — ajoute des cas pour chaque branche d'erreur de `run()` : 422 (message éligibilité), 409 (transition invalide), 400 (commentaire obligatoire), 403 (rôle non autorisé), et cas générique (autre code) ; vérifie aussi le comportement quand `body` est `undefined` (message par défaut utilisé).

### QA-TASK-103
- Target : `BadgeScoreComponent`
- Test type : unitaire (Jasmine/Karma)
- Business rule(s) : RG-BANK-08 (AC-009-6)
- Tool : Jasmine/Karma
- DoD : mise à jour `badge-score.component.spec.ts` — ajoute un cas pour score `EXCELLENT`/`BON` où `showWarning` doit être `false` (branche manquante), en plus des cas `MOYEN`/`FAIBLE` déjà couverts.

### QA-TASK-104
- Target : `BadgeStatutComponent`
- Test type : unitaire (Jasmine/Karma)
- Business rule(s) : N/A (charte §5.8)
- Tool : Jasmine/Karma
- DoD : mise à jour `badge-statut.component.spec.ts` — ajoute un cas avec un statut valide (mapping trouvé) vs un cas de fallback (valeur non mappée → `charte-neutral`), pour couvrir l'opérateur `??`.

### QA-TASK-105
- Target : `ClientCreatePage.applyServerError()`
- Test type : unitaire (Jasmine/Karma)
- Business rule(s) : RG-BANK-01 (AC-004-x)
- Tool : Jasmine/Karma
- DoD : mise à jour `client-create.page.spec.ts` — ajoute un cas où l'erreur 400 a `champ`+`message` mais **pas** de tableau `erreurs` (branche `body?.champ && body.message` actuellement non testée), vérifie que `serverErrors()` contient bien l'entrée unique attendue.

### QA-TASK-106
- Target : `DemandeCreditService.list()`
- Test type : unitaire (Jasmine/Karma)
- Business rule(s) : N/A (US-007, liste filtrée)
- Tool : Jasmine/Karma + `HttpClientTestingModule`
- DoD : mise à jour `demande-credit.service.spec.ts` — ajoute 3 cas : appel avec `statut` seul (vérifie params HTTP), appel avec `clientId` seul, appel sans filtre (params vides). Couvre les 2 branches actuellement à 0%.

### QA-TASK-107
- Target : `DemandeDetailPage` (visibilité conditionnelle actions)
- Test type : unitaire (Jasmine/Karma)
- Business rule(s) : RG-BANK-05 (AC-010-x, AC-011-x)
- Tool : Jasmine/Karma
- DoD : mise à jour `demande-detail.page.spec.ts` — ajoute des cas couvrant les branches manquantes : visibilité "Soumettre" (BROUILLON+CONSEILLER vs autres combinaisons), visibilité "Annuler" (BROUILLON/SOUMISE+CONSEILLER vs EN_ANALYSE+CONSEILLER vs RESPONSABLE_CREDIT), gestion des erreurs 409/403 sur les actions.

---

### QA-TASK-201
- Target : Suites complètes backend + frontend
- Test type : exécution / rapport
- Business rule(s) : toutes
- Tool : `mvnw test`, `ng test --code-coverage`
- DoD : les 2 suites passent à 100% (aucune régression sur les tests existants), rapport de couverture frontend généré et comparé à la cible (branches ≥75%), nombre total de tests backend/frontend documenté dans le rapport final.

### QA-TASK-202
- Target : Quality gate final
- Test type : revue (qa-quality-gate)
- Business rule(s) : toutes (RG-BANK-01..08)
- Tool : checklist qa-quality-gate
- DoD : verdict pass/fail documenté par zone (pyramide, couverture RG-BANK, fiabilité, edge cases), note explicite sur AC-013-1 (spec inconsistency à remonter à AgentBA, non bloquante), recommandation finale go/no-go.


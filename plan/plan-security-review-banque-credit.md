# Plan de revue de sécurité — Feature "banque-credit"

Auteur : AgentSecurity
Statut : Exécuté (voir review/security-review-banque-credit.md pour les constats détaillés)
Sources : spec/spec-feature-banque-credit.md (v1.1), spec/spec-architecture-banque-credit.md (v1.0)

---

## 1. Périmètre de revue (SEC-AREA scope)

### Backend (`apps/banque-credit/banque-credit-backend`)
- Sécurité : `security/SecurityConfig.java`, `security/JwtTokenProvider.java`, `security/JwtAuthenticationFilter.java`, `security/CustomUserDetailsService.java`, `security/AuthenticatedUserUtil.java`
- Autorisation métier : `service/impl/ClientServiceImpl.java`, `service/impl/DemandeCreditServiceImpl.java`, `service/impl/DecisionServiceImpl.java`, `service/impl/EligibiliteServiceImpl.java`, `model/TransitionRules.java`
- Contrôleurs : `AuthController`, `ClientController`, `DemandeCreditController`, `DecisionController`, `HistoriqueController`, `DashboardController`
- Validation d'entrée : DTO `dto/*CreateDto.java`, `dto/DecisionRefusDto.java`
- Gestion d'erreurs : `exception/GlobalExceptionHandler.java`
- Configuration/secrets : `application.properties`, `data.sql` (comptes seed)

### Frontend (`apps/banque-credit/banque-credit-frontend`)
- `features/auth/auth.service.ts`, `login.page.ts`
- `core/interceptors/jwt.interceptor.ts`, `core/interceptors/error.interceptor.ts`
- `core/guards/auth.guard.ts`, `core/guards/role.guard.ts`
- `environments/environment.ts` / `environment.prod.ts`
- `package.json` / `package-lock.json` (dépendances npm)
- Recherche transverse XSS : usage `innerHTML`, `bypassSecurityTrust*`

### Hors périmètre (rappel)
- Nouvelle fonctionnalité, refonte d'architecture (remonter à AgentArchitect)
- Rédaction de tests au-delà des recommandations sécurité (AgentQA)

---

## 2. Zones de revue (SEC-AREA-xxx)

| ID | Zone | Checklist associée |
|---|---|---|
| SEC-AREA-001 | Validation d'entrée / injection | Bean Validation sur tous les DTO d'écriture ; absence de concaténation SQL/JPQL ; désérialisation sûre |
| SEC-AREA-002 | Autorisation (rôles CONSEILLER/RESPONSABLE_CREDIT) | `@PreAuthorize` sur chaque méthode d'écriture de service, cohérent avec la table de transitions RG-BANK-05 §4.4 de l'architecture |
| SEC-AREA-003 | Secrets | `app.jwt.secret` externalisé (env var/config server), jamais en clair commité de façon exploitable en prod |
| SEC-AREA-004 | Logging | Absence de mot de passe/JWT/token/donnée personnelle complète dans les logs applicatifs |
| SEC-AREA-005 | Exposition de données à une IA | Aucun point d'intégration IA dans le périmètre fonctionnel — vérifier absence de tout endpoint/appel sortant vers un service IA |
| SEC-AREA-006 | Transport/stockage frontend | Stockage du JWT (sessionStorage), exposition XSS (innerHTML, sanitization), CORS |
| SEC-AREA-007 | Dépendances tierces | `npm audit` (frontend), classification des CVE par sévérité et par exposition (prod vs dev) |

---

## 3. Règles métier à sécurité (RG-BANK-*) à vérifier

- **RG-BANK-01** : champs obligatoires Client validés côté serveur (Bean Validation), pas seulement côté Angular (contournement API directe).
- **RG-BANK-02 / RG-BANK-03** : bornes montant/durée revalidées côté serveur (`@DecimalMin/@DecimalMax/@Min/@Max` sur `DemandeCreditCreateDto`), non recalculées côté client uniquement.
- **RG-BANK-04** : mensualité/taux d'endettement calculés uniquement côté serveur (`SimulationService`), jamais acceptés tels quels depuis la requête client (DTO de requête ne contient pas ces champs).
- **RG-BANK-05** : transitions de statut protégées par rôle exact — `@PreAuthorize` + vérification manuelle du statut courant dans `DecisionServiceImpl`, pour chaque transition (soumettre/annuler = CONSEILLER ; analyser/accepter/refuser = RESPONSABLE_CREDIT).
- **RG-BANK-06** : critères d'éligibilité cumulatifs vérifiés côté serveur (`EligibiliteServiceImpl`), avant acceptation, non contournables depuis le frontend.
- **RG-BANK-07** : commentaire obligatoire pour refus appliqué côté serveur (`DecisionServiceImpl.refuser`), indépendamment de toute validation de formulaire frontend.

---

## 4. Approche de priorisation des risques

| Sévérité | Critère |
|---|---|
| **Blocker** | Contournement possible d'une autorisation de rôle sur une transition de statut (RG-BANK-05) ou du commentaire obligatoire de refus (RG-BANK-07) ; secret/API key en clair exploitable directement en production ; injection possible. |
| **High** | Faiblesse de configuration sécurité avec impact réel en production (CORS trop permissif combiné à credentials, dépendance avec CVE HIGH/CRITICAL affectant du code exécuté en runtime, secret non externalisé même si risque pédagogique limité). |
| **Medium** | Choix de conception avec risque résiduel acceptable sous condition (stockage sessionStorage du JWT), point à documenter/justifier. |
| **Low** | Amélioration de posture de sécurité ou d'hygiène de code sans exploitabilité directe démontrée (dépendances dev-only avec CVE, code mort de sécurité non utilisé, absence de rate-limiting non exigée par la spec). |

---

## 5. Hypothèses (assumptions)

- ASM-SEC-01 : Le contexte est pédagogique (ASM-02 de l'architecture) — aucune donnée bancaire réelle traitée ; le niveau d'exigence sécurité est proportionné à ce contexte, mais les principes OWASP de base restent vérifiés.
- ASM-SEC-02 : Aucun point d'intégration IA n'existe dans le périmètre fonctionnel actuel (confirmé par recherche transverse du code) → SEC-AREA-005 est traité comme **non applicable** pour cette itération, mais reste à réévaluer si un assistant d'éligibilité IA est ajouté ultérieurement.
- ASM-SEC-03 : L'authentification utilise un token Bearer JWT transmis manuellement en en-tête `Authorization` (pas de cookie automatique) → le risque CSRF classique est réduit, mais le risque XSS/exfiltration de token reste pertinent pour l'évaluation du stockage sessionStorage et de la politique CORS.
- ASM-SEC-04 : `npm audit` reflète l'état des dépendances au moment de la revue ; les correctifs proposés impliquant un saut de version majeure (Angular 18→21) relèvent d'un arbitrage AgentArchitect/AgentOrchestrator, pas d'une correction directe par AgentSecurity.

---

## 6. Tâches de revue exécutées (SEC-TASK-xxx)

Voir `review/security-review-banque-credit.md` pour le détail des constats par tâche :
- SEC-TASK-001 — Autorisation et transitions de statut (RG-BANK-05/07)
- SEC-TASK-002 — Validation d'entrée serveur (RG-BANK-01/02/03/04/06)
- SEC-TASK-003 — Secrets (jwt.secret)
- SEC-TASK-004 — Logging hygiene
- SEC-TASK-005 — Stockage JWT frontend & exposition XSS
- SEC-TASK-006 — Configuration CORS
- SEC-TASK-007 — Dépendances npm (audit)
- SEC-TASK-008 — Exposition de données à une IA (N/A)


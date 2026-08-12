# Plan de déploiement — banque-credit

Skill: deployment-create-plan

## 0. Contexte et hypothèses (ASM)

- ASM-D01 : Backend Spring Boot 3.3.4, packaging WAR, `spring-boot-starter-tomcat` scope `provided` → destiné à un Tomcat externe (9/10.1, compatible Servlet 6 / Jakarta EE 10, JDK 17+).
- ASM-D02 : Frontend Angular standalone (build `@angular-devkit/build-angular:application`), servi en statique par Apache HTTP, avec `apiUrl` relatif (`/banque-credit/api/v1`) en prod ⇒ suppose que le frontend est servi **sur le même host/port** que le backend (reverse proxy Apache → Tomcat) afin d'éviter tout CORS cross-origin en production.
- ASM-D03 : Environnement cible réel (Tomcat/Apache) non disponible dans ce sandbox (seuls JDK 21 + Node/npm présents). Les étapes marquées **[REEL]** sont documentées pour exécution ultérieure sur l'environnement cible ; les étapes marquées **[SANDBOX]** ont été exécutées ici en substitution (embedded run + static file server) à titre de smoke test.
- ASM-D04 : Contexte servlet backend = `/banque-credit` (server.servlet.context-path), API sous `/api/v1`, Swagger sous `/swagger-ui.html` et `/v3/api-docs`.
- ASM-D05 : Secrets déjà externalisés par AgentSecurity : `JWT_SECRET` (défaut dev uniquement) et `CORS_ALLOWED_ORIGINS` (défaut `http://localhost:4200`). En environnement réel, ces deux variables DOIVENT être définies explicitement en dehors du WAR (variables d'environnement Tomcat / `setenv.sh`).
- ASM-D06 : Comptes de seed (`conseiller1` / `responsable1`, `data.sql`, `spring.sql.init.mode=always`) sont exécutés à chaque démarrage. Ce sont des comptes applicatifs (pas des demandes de crédit pré-remplies), donc pas de conflit direct avec RG-BANK-06/07, MAIS les mots de passe par défaut sont documentés en clair dans un commentaire du repo → RISQUE (voir RISK-005).

---

## 1. Backend — build/déploiement (Maven WAR / Tomcat) [REEL, sauf build lui-même exécuté en SANDBOX]

- STEP-001 — Build Maven WAR
  - Commande : `mvnw.cmd clean package -DskipTests=false` (ou `verify`) depuis `apps/banque-credit/banque-credit-backend`.
  - Vérification : artefact `target/banque-credit-backend.war` produit, 133 tests passent, aucun test skip non justifié.

- STEP-002 — Vérification packaging
  - Vérifier `<packaging>war</packaging>` et `spring-boot-starter-tomcat` scope `provided` (déjà conforme dans `pom.xml`).
  - Vérification : `jar tf target/banque-credit-backend.war | findstr WEB-INF` liste bien `WEB-INF/classes`, `WEB-INF/lib` (sans tomcat-embed-core, car provided).

- STEP-003 — [REEL] Dépôt du WAR sur Tomcat
  - Copier `banque-credit-backend.war` dans `$CATALINA_HOME/webapps/banque-credit.war` (le nom de fichier détermine le context path déployé : `/banque-credit` — cohérent avec `server.servlet.context-path=/banque-credit`, donc soit renommer le WAR `banque-credit.war` soit désactiver le context-path applicatif pour éviter un double préfixe `/banque-credit/banque-credit`).
  - CONFIG-001 : nom du WAR déployé = `banque-credit.war` (context path Tomcat = `/banque-credit`) ET retirer `server.servlet.context-path` de `application.properties` **OU** garder le context-path applicatif et déployer sous `ROOT.war` — **à trancher avec l'infra cible avant déploiement réel** (point bloquant à confirmer, actuellement ambigu).
  - Vérification : Tomcat log `Deploying web application archive` sans exception, `catalina.out` sans stacktrace au démarrage.

- STEP-004 — [REEL] Variables d'environnement / secrets Tomcat
  - Définir dans `$CATALINA_HOME/bin/setenv.sh` (ou service Windows / systemd unit) :
    - `JWT_SECRET=<valeur forte générée, non commitée>`
    - `CORS_ALLOWED_ORIGINS=https://<domaine-frontend-prod>` (pas de wildcard)
    - `JWT_EXPIRATION_MS=86400000` (ou valeur métier validée)
  - CONFIG-002 : `JWT_SECRET` — valeur à fournir par l'équipe infra/sécurité, **à ne jamais committer**.
  - CONFIG-003 : `CORS_ALLOWED_ORIGINS` — si frontend servi sur le même host/port qu'Apache en reverse-proxy vers Tomcat (ASM-D02), cette variable n'intervient pas en pratique (same-origin), mais doit rester définie de façon restrictive en cas d'accès direct au backend.
  - Vérification : `echo $JWT_SECRET` absent des logs Tomcat, healthcheck applicatif OK après redémarrage.

- STEP-005 — [REEL] Compatibilité JVM/Tomcat
  - CONFIG-004 : Tomcat 10.1.x (Jakarta EE 10, requis par Spring Boot 3.x) + JDK 17+ (build/test effectués avec JDK 21 dans ce sandbox — compatible).
  - Vérification : `catalina.sh version` / `java -version` sur l'hôte cible avant déploiement.

---

## 2. Frontend — build/déploiement (Angular / Apache) [REEL, sauf build exécuté en SANDBOX]

- STEP-006 — Build Angular production
  - Commande : `ng build --configuration production` (équivalent `npm run build`), depuis `apps/banque-credit/banque-credit-frontend`.
  - Vérification : dossier `dist/banque-credit-frontend/browser/` généré, fichiers hashés (`outputHashing: all` déjà configuré dans `angular.json`), `environment.prod.ts` (apiUrl relatif `/banque-credit/api/v1`) bien substitué via `fileReplacements`.

- STEP-007 — [REEL] Déploiement statique Apache
  - Copier le contenu de `dist/banque-credit-frontend/browser/` vers le DocumentRoot Apache (ex. `/var/www/banque-credit/`).
  - Vérification : `index.html`, `main-*.js`, `styles-*.css` présents et accessibles via HTTP.

- STEP-008 — [REEL] Configuration Apache VirtualHost + SPA history mode
  - CONFIG-005 : bloc Apache requis (exemple, à adapter à l'hôte réel) :
    ```apache
    <VirtualHost *:443>
      ServerName banque-credit.exemple.com
      DocumentRoot /var/www/banque-credit

      # Reverse proxy vers Tomcat pour le backend (même origine ⇒ pas de CORS cross-site)
      ProxyPreserveHost On
      ProxyPass /banque-credit/api http://localhost:8080/banque-credit/api
      ProxyPassReverse /banque-credit/api http://localhost:8080/banque-credit/api
      ProxyPass /banque-credit/swagger-ui http://localhost:8080/banque-credit/swagger-ui
      ProxyPassReverse /banque-credit/swagger-ui http://localhost:8080/banque-credit/swagger-ui
      ProxyPass /banque-credit/v3/api-docs http://localhost:8080/banque-credit/v3/api-docs
      ProxyPassReverse /banque-credit/v3/api-docs http://localhost:8080/banque-credit/v3/api-docs

      <Directory /var/www/banque-credit>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted

        RewriteEngine On
        # Ne pas réécrire les fichiers/dossiers existants, ni les appels proxy vers /banque-credit/api|swagger-ui|v3
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteCond %{REQUEST_URI} !^/banque-credit/(api|swagger-ui|v3)
        RewriteRule ^ /index.html [L]
      </Directory>
    </VirtualHost>
    ```
  - Vérification : accès direct à une route Angular profonde (ex. `https://.../demandes/42`) après F5 renvoie `index.html` (200), pas de 404 ; `curl -I` sur un asset statique retourne 200 avec cache-control adapté au hash.

- STEP-009 — [REEL] Configuration CORS ciblée (si frontend et backend sur origines différentes)
  - Si le reverse-proxy same-origin (STEP-008) n'est pas utilisé, définir `CORS_ALLOWED_ORIGINS=https://<domaine-frontend>` côté backend (STEP-004, CONFIG-003) — jamais `*`.
  - Vérification : requête `OPTIONS` preflight depuis le domaine frontend renvoie `Access-Control-Allow-Origin` = domaine exact (pas `*`), `Access-Control-Allow-Credentials: true`.

---

## 3. Configuration par environnement

| Paramètre | Dev/local | Staging/Prod cible |
|---|---|---|
| `apiUrl` frontend | `http://localhost:8080/banque-credit/api/v1` (`environment.ts`) | `/banque-credit/api/v1` relatif (`environment.prod.ts`), same-origin via proxy Apache |
| `app.cors.allowed-origins` | `http://localhost:4200` (défaut) | `CORS_ALLOWED_ORIGINS=https://<domaine-front-prod>` (à fournir — **à confirmer, non fourni**) |
| `app.jwt.secret` | valeur pédagogique par défaut | `JWT_SECRET=<secret fort>` (à fournir — **à confirmer, non fourni**) |
| Context path backend | `/banque-credit` | idem (CONFIG-001 à trancher : nom du WAR) |
| Base de données | H2 fichier local (`./data/banquecredit`) | **à confirmer** : H2 fichier acceptable en démo, sinon migrer vers une base serveur (hors périmètre de cet agent — à escalader à AgentArchitect/AgentDBA si besoin réel de prod) |

---

## 4. Checklist de vérification post-déploiement

1. `GET /banque-credit/v3/api-docs` répond 200 (backend up, Swagger actif).
2. `POST /banque-credit/api/v1/auth/login` avec `conseiller1` et `responsable1` renvoie un JWT (200).
3. Flux lecture : `GET` liste clients/demandes (authentifié) → 200.
4. Flux écriture : création client + création demande + simulation → 20x.
5. Transition de statut protégée (RG-BANK-05) : `soumettre` → `analyser` → `accepter`/`refuser` respecte la matrice de transitions et les rôles (403 si rôle non autorisé).
6. CORS : preflight `OPTIONS` depuis l'origine frontend autorisée → `Access-Control-Allow-Origin` = origine exacte, pas de wildcard.
7. Routing SPA : accès direct à une route profonde + refresh → 200 (pas de 404 Apache).
8. Assets statiques : aucun 404 sur JS/CSS/fonts dans les DevTools/Network.
9. Logs de démarrage Tomcat/Spring Boot sans stacktrace, sans `JWT_SECRET` ni mot de passe en clair.
10. Aucune donnée de démonstration de demandes de crédit pré-remplies avec décisions (RG-BANK-06/07) accessible ; seuls les 2 comptes utilisateurs de seed existent (à faire tourner avec des mots de passe changés en prod réelle — voir RISK-005).

---

## 5. Rollback

- Backend : conserver le WAR précédent (`banque-credit.war.bak`) ; en cas d'échec, arrêter Tomcat, remplacer le WAR déployé par la sauvegarde, redémarrer, revérifier STEP checklist §4 points 1-5.
- Frontend : conserver l'archive du build statique précédent (`dist-backup-<date>.tar.gz`) ; en cas d'échec, resynchroniser le DocumentRoot Apache avec cette sauvegarde, vérifier §4 points 7-8.
- Aucun rollback de schéma DB nécessaire ici (H2 `ddl-auto=update`, pas de migration destructive documentée) — à valider si une vraie base de prod avec migrations (Flyway/Liquibase) est introduite plus tard.

---

## 6. Risques (RISK)

- RISK-001 — Ambiguïté du context path Tomcat vs `server.servlet.context-path` pouvant produire `/banque-credit/banque-credit`. Mitigation : trancher STEP-003/CONFIG-001 avec l'infra avant tout déploiement réel.
- RISK-002 — `JWT_SECRET`/`CORS_ALLOWED_ORIGINS` non fournis pour l'environnement cible réel. Mitigation : bloquant tant que non fournis (question à l'infra/au client).
- RISK-003 — H2 fichier local non adapté à un vrai environnement multi-instance/production (pas de clustering, fichier local). Mitigation : à escalader si l'environnement réel est un vrai prod (hors périmètre déploiement, relève d'AgentArchitect).
- RISK-004 — Absence d'environnement Tomcat/Apache réel dans ce sandbox : les STEP-003/004/007/008/009 n'ont pas pu être exécutées physiquement, seulement documentées + validées par équivalent local (voir résultats de smoke test sandbox).
- RISK-005 — Mots de passe des comptes de seed documentés en clair dans un commentaire de `data.sql` (`conseiller1/Conseiller123!`, `responsable1/Responsable123!`), exécutés systématiquement (`spring.sql.init.mode=always`). Acceptable pour démo/staging, **bloquant pour une mise en production réelle** : ces mots de passe doivent être changés/retirés avant tout environnement exposé publiquement.


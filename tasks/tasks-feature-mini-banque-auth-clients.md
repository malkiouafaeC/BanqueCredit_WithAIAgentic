# Tasks - Mini Banque Auth Clients

## Scope
Backlog executable pour implémenter le mini projet banque selon:
- spec/spec-feature-mini-banque-auth-clients.md
- plan/plan-feature-mini-banque-auth-clients.md

## Routing Rules
- BE-* -> AgentBackendDeveloper
- FE-* -> AgentFrontendDeveloper
- INT-* -> AgentOrchestrator

## Execution Order (recommended)
1. BE-001
2. FE-001
3. BE-002
4. BE-003
5. BE-004
6. BE-005
7. FE-002
8. FE-003
9. FE-004
10. FE-005
11. FE-006
12. FE-007
13. FE-008
14. FE-009
15. INT-001
16. INT-002
17. INT-003

## Backend Tasks

### BE-001
- Objective: Initialiser le projet Spring Boot dans apps/mini-banque/mini-banque-backend.
- Linked requirements: CON-002
- Preconditions: Aucun
- Deliverables:
  - Structure Spring Boot compilable
  - Configuration de base pour API REST
- Validation:
  - Build backend passe
- Done criteria:
  - Projet cree au bon emplacement uniquement

### BE-002
- Objective: Implementer login conseiller `POST /api/auth/login` avec credentials hardcodes.
- Linked requirements: REQ-001, REQ-002
- Preconditions: BE-001
- Deliverables:
  - Endpoint login
  - DTO request/response
  - Token JWT genere en cas de succes
- Validation:
  - Test success credentials
  - Test invalid credentials
- Done criteria:
  - Retour 200 avec token si valide
  - Retour 401 standardise si invalide

### BE-003
- Objective: Configurer Spring Security + JWT pour proteger `/api/clients` et `/api/clients/new`.
- Linked requirements: SEC-001
- Preconditions: BE-002
- Deliverables:
  - Configuration SecurityFilterChain
  - Filtre JWT
- Validation:
  - Endpoint protege refuse sans token
  - Endpoint protege accepte avec token valide
- Done criteria:
  - Protection active sur endpoints clients

### BE-004
- Objective: Implementer APIs clients `GET /api/clients` et `POST /api/clients/new` avec donnees hardcodees.
- Linked requirements: REQ-006, REQ-008, CON-001
- Preconditions: BE-003
- Deliverables:
  - Endpoint liste clients
  - Endpoint creation client
  - Validation minimale payload nouveau client
- Validation:
  - GET retourne liste attendue
  - POST valide retourne 201
  - POST invalide retourne 400
- Done criteria:
  - Contrats API conformes a la spec

### BE-005
- Objective: Uniformiser les erreurs auth/validation et ajouter tests backend.
- Linked requirements: SEC-002
- Preconditions: BE-004
- Deliverables:
  - Format d'erreur coherent 401/400
  - Tests unitaires et integration backend
- Validation:
  - Tous tests backend au vert
- Done criteria:
  - Couverture scenarios login/clients/unauthorized

## Frontend Tasks

### FE-001
- Objective: Initialiser le projet Angular dans apps/mini-banque/mini-banque-frontend.
- Linked requirements: CON-002
- Preconditions: Aucun
- Deliverables:
  - App Angular avec routing de base
- Validation:
  - Build frontend passe
- Done criteria:
  - Projet cree au bon emplacement uniquement

### FE-002
- Objective: Creer modeles et services HTTP types pour auth et clients.
- Linked requirements: REQ-003
- Preconditions: FE-001
- Deliverables:
  - Types request/response login
  - Types client list/new client
  - Services API
- Validation:
  - Compilation TypeScript sans erreur
- Done criteria:
  - Services appeles sans any implicite

### FE-003
- Objective: Implementer gestion token (stockage session + ajout header Authorization).
- Linked requirements: REQ-003, SEC-001
- Preconditions: FE-002, BE-002, BE-003
- Deliverables:
  - Storage token
  - Interceptor ou mecanisme equivalent
- Validation:
  - Requetes clients envoyees avec bearer token
- Done criteria:
  - Acces APIs protegees possible apres login

### FE-004
- Objective: Implementer page login et gestion erreurs credentials invalides.
- Linked requirements: REQ-001
- Preconditions: FE-003
- Deliverables:
  - Ecran login username/password
  - Appel POST /api/auth/login
  - Message erreur 401
- Validation:
  - Login valide redirige home
  - Login invalide affiche erreur
- Done criteria:
  - Flux login stable

### FE-005
- Objective: Implementer page home avec menu username, client, se deconnecter.
- Linked requirements: REQ-004
- Preconditions: FE-004
- Deliverables:
  - Ecran home
  - Affichage username
  - Actions menu
- Validation:
  - Username visible
- Done criteria:
  - Menu conforme a la spec

### FE-006
- Objective: Implementer routing et guard (protection routes + redirections).
- Linked requirements: REQ-005
- Preconditions: FE-005
- Deliverables:
  - Routes /login /home /clients
  - Guard auth
- Validation:
  - Non-authentifie redirige login
  - Clic client redirige clients
- Done criteria:
  - Navigation conforme au parcours cible

### FE-007
- Objective: Implementer page clients et chargement liste depuis `GET /api/clients`.
- Linked requirements: REQ-006
- Preconditions: FE-006, BE-004, BE-005
- Deliverables:
  - Liste clients rendue dans UI
- Validation:
  - Affichage des clients API
  - Etat erreur si 401
- Done criteria:
  - Page clients fonctionnelle

### FE-008
- Objective: Ajouter bouton nouveau client en haut de la page clients.
- Linked requirements: REQ-007
- Preconditions: FE-007
- Deliverables:
  - Bouton visible et place en haut
- Validation:
  - Verification visuelle et test composant
- Done criteria:
  - Bouton conforme a la spec

### FE-009
- Objective: Connecter action nouveau client vers `POST /api/clients/new`.
- Linked requirements: REQ-008
- Preconditions: FE-008, BE-004
- Deliverables:
  - Action create client basique
  - Rafraichissement liste apres creation
- Validation:
  - POST successful cree un client
  - Erreurs 400 affichees
- Done criteria:
  - Creation client fonctionnelle cote UI

## Integration Tasks

### INT-001
- Objective: Verifier alignement des contrats API entre backend et frontend.
- Preconditions: BE-005, FE-009
- Validation:
  - Champs payload et codes HTTP alignes
- Done criteria:
  - Aucun mismatch contractuel bloquant

### INT-002
- Objective: Executer verification globale securite/navigation/tests.
- Preconditions: INT-001
- Validation:
  - Login/logout/navigations OK
  - Endpoints proteges verifies
- Done criteria:
  - Checklist integration complete

### INT-003
- Objective: Produire verdict final READY/NOT_READY.
- Preconditions: INT-002
- Validation:
  - Rapport blockers et ameliorations
- Done criteria:
  - Decision de release explicite

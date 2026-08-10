---
title: Mini Banque - Authentification JWT et Gestion Clients
version: 1.0
date_created: 2026-08-07
last_updated: 2026-08-07
owner: AgentOrchestrator
status: Completed
tags: [feature, backend, frontend, security, angular, spring-boot]
---

# Introduction

Cette specification definit les exigences pour un mini projet banque avec authentification conseiller (JWT), navigation frontend Angular, et gestion basique des clients.

## 1. Purpose & Scope

Objectif:
- Livrer un mini projet full-stack avec backend Spring Boot securise et frontend Angular qui consomme les APIs backend.

Perimetre fonctionnel:
- Authentification conseiller avec username/password.
- Affichage d'une page d'accueil avec menu: username, client, se deconnecter.
- Navigation vers page clients au clic sur client.
- Page clients avec liste des clients et bouton nouveau client.

Hors perimetre:
- Base de donnees persistante (donnees hardcodees autorisees pour ce test).
- Gestion de roles complexes.
- Workflow metier bancaire avance.

## 2. Definitions

- Conseiller: utilisateur autorise a se connecter a l'application.
- JWT: token d'authentification stateless transmis au backend.
- API protegee: endpoint necessitant un token JWT valide.
- DTO: objet de transfert entre frontend et backend.

## 3. Requirements, Constraints & Guidelines

- **REQ-001**: Le systeme doit permettre la connexion conseiller via username/password.
- **REQ-002**: Le backend doit retourner un JWT en cas d'authentification valide.
- **REQ-003**: Le frontend doit stocker le token de session pour les appels API proteges.
- **REQ-004**: La page d'accueil doit afficher un menu contenant username, client, se deconnecter.
- **REQ-005**: Le clic sur client doit rediriger vers la page clients.
- **REQ-006**: La page clients doit afficher une liste de clients.
- **REQ-007**: La page clients doit afficher un bouton nouveau client en haut.
- **REQ-008**: Le backend doit exposer une API pour ajouter un nouveau client.

- **SEC-001**: Les endpoints clients doivent etre proteges par JWT.
- **SEC-002**: Le backend doit renvoyer une erreur d'authentification standardisee en cas de credentials invalides.
- **SEC-003**: Aucun secret ne doit etre expose dans les logs frontend ou backend.

- **CON-001**: Les donnees conseillers et clients peuvent etre hardcodees dans le code.
- **CON-002**: Le code applicatif doit etre cree uniquement dans:
  - apps/mini-banque/mini-banque-backend
  - apps/mini-banque/mini-banque-frontend
- **CON-003**: Le frontend doit consommer les APIs backend existantes, sans mock parallele cote UI.

- **GUD-001**: Le backend doit suivre Spring Security + JWT avec architecture claire (controller/service/security).
- **GUD-002**: Le frontend doit utiliser des patterns Angular recents (standalone components, services types, routing clair).
- **GUD-003**: Couvrir les cas nominal, erreur login, et acces non autorise.

## 4. Interfaces & Data Contracts

### Backend APIs

1) Authentification
- Method: `POST`
- Path: `/api/auth/login`
- Request:
```json
{
  "username": "conseiller1",
  "password": "password123"
}
```
- Success Response `200`:
```json
{
  "token": "<jwt>",
  "username": "conseiller1"
}
```
- Error Response `401`:
```json
{
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "Username ou password invalide"
}
```

2) Liste clients
- Method: `GET`
- Path: `/api/clients`
- Auth: Bearer JWT requis
- Success Response `200`:
```json
[
  { "id": 1, "nom": "Client A", "email": "a@bank.test" },
  { "id": 2, "nom": "Client B", "email": "b@bank.test" }
]
```
- Error Response `401`:
```json
{
  "code": "AUTH_UNAUTHORIZED",
  "message": "Token manquant ou invalide"
}
```

3) Nouveau client
- Method: `POST`
- Path: `/api/clients/new`
- Auth: Bearer JWT requis
- Request:
```json
{
  "nom": "Client C",
  "email": "c@bank.test"
}
```
- Success Response `201`:
```json
{
  "id": 3,
  "nom": "Client C",
  "email": "c@bank.test"
}
```
- Error Response `400`:
```json
{
  "code": "CLIENT_VALIDATION_ERROR",
  "message": "Donnees client invalides"
}
```

### Frontend routes
- `/login`: page authentification.
- `/home`: page accueil avec menu.
- `/clients`: page liste clients + bouton nouveau client.

### Navigation rules
- Si non authentifie: redirection vers `/login`.
- Apres login valide: redirection vers `/home`.
- Clic menu client: redirection vers `/clients`.
- Clic se deconnecter: suppression session + redirection `/login`.

## 5. Acceptance Criteria

- **AC-001**: Etant donne un conseiller valide, quand il soumet username/password sur login, alors il est authentifie et recoit un JWT.
- **AC-002**: Etant donne des credentials invalides, quand login est soumis, alors le backend retourne 401 et un code erreur standard.
- **AC-003**: Etant donne un utilisateur connecte, quand il arrive sur home, alors le menu affiche username, client, se deconnecter.
- **AC-004**: Etant donne un utilisateur connecte, quand il clique sur client, alors il est redirige vers `/clients`.
- **AC-005**: Etant donne un token JWT valide, quand `/api/clients` est appele, alors la liste clients est retournee.
- **AC-006**: Etant donne aucun token ou token invalide, quand `/api/clients` est appele, alors le backend retourne 401.
- **AC-007**: Etant donne la page clients, alors le bouton nouveau client est visible en haut de page.
- **AC-008**: Etant donne un payload client valide, quand `/api/clients/new` est appele avec JWT, alors un client est cree et retourne en 201.

## 6. Test Automation Strategy

Backend:
- Tests unitaires service auth et service clients.
- Tests integration endpoints:
  - `POST /api/auth/login` (success + error)
  - `GET /api/clients` (authorized + unauthorized)
  - `POST /api/clients/new` (success + validation error)

Frontend:
- Tests unitaires composants login, home menu, clients page.
- Tests routing/guard:
  - non authentifie -> redirection login
  - clique menu client -> route clients
- Tests services HTTP:
  - ajout token Authorization
  - gestion erreurs 401

## 7. Rationale & Context

- Le choix JWT permet un flux simple et standard pour un mini projet.
- Les donnees hardcodees reduisent la complexite et accelerent le test des agents.
- La separation backend/frontend valide la coordination multi-agent sans surcharger le scope.

## 8. Dependencies & External Integrations

- **PLT-001**: Java 17+ et Spring Boot 3+ pour backend.
- **PLT-002**: Angular recent (18+) pour frontend.
- **EXT-001**: Aucune integration externe obligatoire pour ce test.

## 9. Examples & Edge Cases

Exemples edge cases:
- Login avec username vide -> erreur 401 ou validation suivant implementation.
- Appel `/api/clients` sans header Authorization -> 401.
- Ajout client avec email vide -> 400 validation.
- Token expire/invalide -> 401 et message standardise.

## 10. Validation Criteria

- Toutes les AC-001..AC-008 sont demontrables manuellement.
- Les endpoints proteges refusent l'acces sans JWT.
- Le frontend suit le flux login -> home -> clients -> logout.
- Les chemins applicatifs respectent CON-002.

## 11. Related Specifications / Further Reading

- Spring Security reference
- Angular routing and guards reference

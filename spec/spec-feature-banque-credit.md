# Spécification fonctionnelle — Feature "banque-credit" (Suivi des demandes de crédit client)

Statut : Approuvé — arbitrages Q1-Q5/Q3-bis/R1-R4 appliqués (post-implémentation)
Version : 1.2
Auteur : AgentBA (arbitrages AgentOrchestrator)

---

## 1. Purpose and scope

### 1.1 Objectif
Fournir une application pédagogique permettant à une agence bancaire fictive de :
- gérer des clients fictifs,
- créer et faire évoluer des demandes de crédit à travers un workflow contrôlé,
- simuler une mensualité et un taux d'endettement,
- calculer un score simplifié,
- décider (accepter/refuser) une demande avec traçabilité,
- historiser chaque changement de statut,
- consulter un tableau de bord de synthèse.

Aucune donnée bancaire réelle n'est traitée. Le système ne remplace pas un moteur de scoring réel.

### 1.2 Dans le périmètre
- Authentification à 2 rôles (CONSEILLER, RESPONSABLE_CREDIT) sur comptes pré-provisionnés (pas d'auto-inscription).
- CRUD Client (création, consultation, liste, détail).
- Création et simulation de DemandeCredit.
- Workflow de statuts : Brouillon → Soumise → En analyse → Acceptée / Refusée, plus Annulée.
- Décision (accepter/refuser) avec commentaire obligatoire en cas de refus.
- Historisation systématique de chaque transition de statut (HistoriqueDecision).
- Dashboard de synthèse agence.
- Écrans : Connexion, Dashboard agence, Liste clients, Création client, Détail client, Création demande, Simulation, Décision, Historique.

### 1.3 Hors périmètre
- Intégration avec un vrai bureau de crédit / score réel.
- Signature électronique, déblocage de fonds, gestion documentaire réglementaire.
- Gestion fine des droits (habilitations granulaires au-delà des 2 rôles).
- Choix technique d'implémentation (architecture, frameworks, découpage de packages) — relève d'AgentArchitect / AgentBackendDeveloper / AgentFrontendDeveloper.

### 1.4 Note technique non bloquante (à l'attention d'AgentArchitect)
Le modèle relationnel attendu est : `Client (1) — (N) DemandeCredit (1) — (N) HistoriqueDecision`, compatible avec une persistance JPA/H2. Cette note est indicative et ne préjuge pas des choix techniques.

---

## 2. Actors and glossary

### 2.1 Acteurs

| Acteur | Rôle applicatif | Responsabilités |
|---|---|---|
| Conseiller bancaire | `CONSEILLER` | Créer les clients fictifs, créer/simuler les demandes, soumettre les dossiers, annuler un dossier en Brouillon ou Soumise. |
| Responsable crédit | `RESPONSABLE_CREDIT` | Passer une demande en analyse, accepter ou refuser, commenter la décision de refus. |

Un utilisateur possède exactement un rôle. L'accès aux écrans/actions est conditionné au rôle (voir §5, edge cases "accès non autorisé").

### 2.2 Glossaire

| Terme | Définition |
|---|---|
| Client | Personne fictive porteuse d'une ou plusieurs demandes de crédit. |
| Demande de crédit (DemandeCredit) | Dossier associé à un client, avec un montant, une durée, un taux fictif et un statut de cycle de vie. |
| Simulation | Calcul de la mensualité estimée et du taux d'endettement simplifié, réalisé sur une demande. |
| Décision | Action du Responsable crédit faisant passer une demande de "En analyse" vers "Acceptée" ou "Refusée". |
| Statut | État courant du cycle de vie d'une demande (RG-BANK-05). |
| Taux d'endettement | Ratio (charges mensuelles + mensualité estimée) / revenu mensuel, exprimé en pourcentage (RG-BANK-04). |
| Score simplifié (scoreSimplifié) | Indicateur déterministe dérivé du taux d'endettement et des critères d'éligibilité (RG-BANK-08). |
| Historique de décision (HistoriqueDecision) | Enregistrement horodaté et attribué à un auteur de chaque transition de statut d'une demande. |

---

## 3. Data contracts (préliminaires, pour AgentArchitect)

### 3.1 Entité `Client`

| Champ | Type | Obligatoire | Contrainte |
|---|---|---|---|
| id | Long/UUID | généré | Identifiant technique |
| nom | String | Oui (RG-BANK-01) | Non vide |
| email | String | Non (fictif) | Format email si renseigné |
| revenuMensuel | Decimal/Number | Oui (RG-BANK-01) | > 0 |
| chargesMensuelles | Decimal/Number | Oui (RG-BANK-01) | ≥ 0 |
| situationProfessionnelle | String / Enum | Non | Valeur libre ou liste fermée (à confirmer, voir Q1) |
| createdAt | DateTime | généré | Horodatage de création, non modifiable |

### 3.2 Entité `DemandeCredit`

| Champ | Type | Obligatoire | Contrainte |
|---|---|---|---|
| id | Long/UUID | généré | Identifiant technique |
| client | Référence Client | Oui | Client obligatoire à la création |
| montantDemande | Decimal/Number | Oui (RG-BANK-02) | > 1000 et ≤ 100000 |
| dureeMois | Integer | Oui (RG-BANK-03) | ≥ 12 et ≤ 84 |
| mensualiteEstimee | Decimal/Number | Calculé | Calculé par simulation (RG-BANK-04) |
| tauxFictif | Decimal/Number (%) | Oui | > 0, valeur pédagogique fictive |
| statut | Enum | généré/piloté | Une des 6 valeurs RG-BANK-05, initial = BROUILLON |
| scoreSimplifie | Enum/Integer | Calculé | Dérivé de RG-BANK-08 |
| commentaireDecision | String | Conditionnel | Obligatoire si statut = REFUSEE (RG-BANK-07) |
| dateSoumission | DateTime | généré | Renseigné à la transition BROUILLON→SOUMISE |
| dateDecision | DateTime | généré | Renseigné à la transition vers ACCEPTEE/REFUSEE |

### 3.3 Entité `HistoriqueDecision`

| Champ | Type | Obligatoire | Contrainte |
|---|---|---|---|
| id | Long/UUID | généré | Identifiant technique |
| demandeCredit | Référence DemandeCredit | Oui | Demande associée |
| ancienStatut | Enum | Oui | Statut avant transition (nullable uniquement à la création initiale, voir Q2) |
| nouveauStatut | Enum | Oui | Statut après transition |
| commentaire | String | Conditionnel | Reprend le commentaire de décision si refus, sinon optionnel |
| auteur | String / Référence Utilisateur | Oui | Identifiant de l'utilisateur ayant déclenché la transition |
| date | DateTime | généré | Horodatage de la transition |

### 3.4 Statuts valides (RG-BANK-05)
`BROUILLON`, `SOUMISE`, `EN_ANALYSE`, `ACCEPTEE`, `REFUSEE`, `ANNULEE`

---

## 4. API surface expectations (préliminaire, fonctionnel — non technique)

Cette liste exprime les capacités fonctionnelles attendues ; le design technique précis (routes, DTO, codes HTTP) relève d'AgentArchitect.

- Authentification : connexion d'un utilisateur avec rôle (CONSEILLER / RESPONSABLE_CREDIT).
- Clients : créer un client ; lister les clients ; consulter le détail d'un client (avec ses demandes).
- Demandes de crédit : créer une demande (brouillon) ; simuler une demande (mensualité + taux d'endettement + score) ; soumettre une demande ; lister/consulter une demande.
- Décision : passer une demande en analyse ; accepter une demande ; refuser une demande (avec commentaire obligatoire) ; annuler une demande.
- Historique : consulter l'historique des transitions d'une demande.
- Dashboard : consulter la synthèse agence (compteurs par statut, montant total demandé, taux moyen d'endettement).

---

## 5. Business rules

### RG-BANK-01 — Champs obligatoires Client
Le nom, le revenu mensuel et les charges mensuelles sont obligatoires à la création/modification d'un client. En cas d'absence, la création est refusée avec un message d'erreur explicite par champ manquant.

### RG-BANK-02 — Bornes du montant demandé
Le montant demandé doit être **strictement supérieur à 1000** et **inférieur ou égal à 100000** (100000 inclus, 1000 exclu). Toute valeur ≤ 1000 ou > 100000 est rejetée.

### RG-BANK-03 — Bornes de la durée
La durée doit être comprise entre **12 et 84 mois inclus** (12 et 84 sont des valeurs valides). Toute valeur < 12 ou > 84 est rejetée.

### RG-BANK-04 — Formule du taux d'endettement et de la mensualité estimée

**Mensualité estimée (formule d'amortissement standard, arbitrage AgentOrchestrator suite au risque R3)** :
```
tauxMensuel = tauxFictif / 100 / 12
mensualiteEstimee = montantDemande × tauxMensuel / (1 - (1 + tauxMensuel) ^ (-dureeMois))
```
Cas particulier : si `tauxFictif = 0`, alors `mensualiteEstimee = montantDemande / dureeMois` (amortissement linéaire sans intérêt, pour éviter une division par zéro).

**Taux d'endettement** :
`tauxEndettement = (chargesMensuelles + mensualiteEstimee) / revenuMensuel`, exprimé en **pourcentage** (résultat × 100). Unités : chargesMensuelles, mensualiteEstimee et revenuMensuel sont dans la même devise fictive ; le taux d'endettement résultant est un pourcentage (ex : 0.35 → 35%).

Ces deux formules sont normatives : AgentArchitect et AgentBackendDeveloper doivent les implémenter telles quelles, sans réinterprétation.

### RG-BANK-05 — Statuts et transitions autorisées
Statuts valides : `BROUILLON`, `SOUMISE`, `EN_ANALYSE`, `ACCEPTEE`, `REFUSEE`, `ANNULEE`.

Transitions autorisées :
- `BROUILLON` → `SOUMISE` (acteur : Conseiller)
- `BROUILLON` → `ANNULEE` (acteur : Conseiller)
- `SOUMISE` → `EN_ANALYSE` (acteur : Responsable crédit)
- `SOUMISE` → `ANNULEE` (acteur : Conseiller)
- `EN_ANALYSE` → `ACCEPTEE` (acteur : Responsable crédit)
- `EN_ANALYSE` → `REFUSEE` (acteur : Responsable crédit, commentaire obligatoire — RG-BANK-07)

Toute autre transition est **interdite**, notamment : `ACCEPTEE`/`REFUSEE`/`ANNULEE` → toute autre valeur (statuts terminaux) ; `EN_ANALYSE` → `ANNULEE` (interdit, seul Conseiller annule, uniquement depuis Brouillon/Soumise) ; `BROUILLON` → `EN_ANALYSE` ou `ACCEPTEE`/`REFUSEE` directement (saut d'étape interdit).

### RG-BANK-06 — Conditions cumulatives d'éligibilité
L'acceptation est possible **si et seulement si les 3 conditions suivantes sont toutes vraies** :
1. tauxEndettement **≤ 35%** (35% inclus, valeur strictement supérieure à 35% exclut l'éligibilité)
2. revenuMensuel **≥ 1500** (1500 inclus)
3. montantDemande **≤ 50000** (50000 inclus)

Si au moins une condition est fausse, la demande n'est pas éligible à l'acceptation automatique ; le Responsable crédit reste décisionnaire mais toute acceptation malgré une condition non remplie doit être explicitement assumée par l'application (voir Q3 — sur ce point, décision par défaut : le système **empêche** l'acceptation si les 3 conditions ne sont pas remplies, et suggère le refus).

### RG-BANK-07 — Commentaire obligatoire pour refus
Un commentaire de décision est **obligatoire** pour passer une demande en statut `REFUSEE`. Si absent ou vide (chaîne vide ou uniquement des espaces), le refus est rejeté avec message d'erreur explicite.

### RG-BANK-08 — Formule du score simplifié (nouvelle règle, dérivée de RG-BANK-04/06)
Le `scoreSimplifie` est calculé de façon déterministe à partir du taux d'endettement (RG-BANK-04), en bandes :

| Bande de taux d'endettement | scoreSimplifie |
|---|---|
| ≤ 20% | `EXCELLENT` |
| > 20% et ≤ 35% | `BON` |
| > 35% et ≤ 50% | `MOYEN` |
| > 50% | `FAIBLE` |

Le score est recalculé à chaque simulation. Il est informatif : il n'implique pas automatiquement une décision (l'éligibilité formelle reste régie par RG-BANK-06), mais un score `FAIBLE` ou `MOYEN` doit être visuellement mis en avant comme avertissement pédagogique côté frontend.

---

## 6. User stories

### 6.1 Connexion / rôles

**US-001** — En tant qu'utilisateur (Conseiller ou Responsable crédit), je veux me connecter avec mon compte pré-provisionné, afin d'accéder aux fonctionnalités autorisées par mon rôle.

**US-002** — En tant qu'utilisateur non authentifié, je veux être redirigé vers l'écran de connexion si je tente d'accéder à une page protégée, afin de garantir que seules les personnes autorisées accèdent aux données.

### 6.2 Création client (Conseiller)

**US-003** — En tant que Conseiller bancaire, je veux créer un client fictif en renseignant nom, revenu mensuel, charges mensuelles et informations complémentaires, afin de pouvoir ensuite créer des demandes de crédit pour ce client.

**US-004** — En tant que Conseiller bancaire, je veux voir un message d'erreur explicite par champ obligatoire manquant lors de la création d'un client, afin de corriger ma saisie sans ambiguïté.

**US-005** — En tant que Conseiller bancaire, je veux consulter la liste des clients existants, afin de retrouver rapidement un client pour créer ou consulter ses demandes.

**US-006** — En tant que Conseiller bancaire, je veux consulter le détail d'un client incluant ses demandes de crédit associées, afin d'avoir une vision complète de sa situation.

### 6.3 Création demande / simulation (Conseiller)

**US-007** — En tant que Conseiller bancaire, je veux créer une demande de crédit en brouillon pour un client en renseignant montant, durée et taux fictif, afin de préparer un dossier avant soumission.

**US-008** — En tant que Conseiller bancaire, je veux voir un message d'erreur explicite si le montant ou la durée saisis sont hors bornes, afin de corriger ma saisie avant de poursuivre.

**US-009** — En tant que Conseiller bancaire, je veux lancer une simulation sur une demande de crédit pour obtenir la mensualité estimée, le taux d'endettement et le score simplifié, afin d'évaluer la faisabilité du dossier avant soumission.

### 6.4 Soumission (Conseiller)

**US-010** — En tant que Conseiller bancaire, je veux soumettre une demande de crédit en statut Brouillon, afin de la transmettre au Responsable crédit pour analyse.

**US-011** — En tant que Conseiller bancaire, je veux annuler une demande en statut Brouillon ou Soumise, afin d'abandonner un dossier qui n'a plus lieu d'être traité.

### 6.5 Analyse / décision (Responsable crédit)

**US-012** — En tant que Responsable crédit, je veux passer une demande Soumise en statut En analyse, afin de signaler que le dossier est en cours d'instruction.

**US-013** — En tant que Responsable crédit, je veux accepter une demande En analyse répondant aux critères d'éligibilité, afin de finaliser une décision positive.

**US-014** — En tant que Responsable crédit, je veux refuser une demande En analyse en indiquant un commentaire de décision obligatoire, afin de justifier la décision négative auprès du client fictif et du conseiller.

**US-015** — En tant que Responsable crédit, je veux être empêché de refuser une demande sans commentaire, afin de garantir la traçabilité et la justification de chaque refus (RG-BANK-07).

### 6.6 Historique

**US-016** — En tant qu'utilisateur (Conseiller ou Responsable crédit), je veux consulter l'historique complet des transitions de statut d'une demande (ancien statut, nouveau statut, commentaire, auteur, date), afin de comprendre le parcours de décision du dossier.

### 6.7 Dashboard

**US-017** — En tant que Conseiller ou Responsable crédit, je veux consulter un tableau de bord agence affichant le nombre de demandes soumises, en analyse, acceptées, refusées, le montant total demandé et le taux moyen d'endettement, afin d'avoir une vision synthétique de l'activité de l'agence.

---

## 7. Acceptance criteria (Given/When/Then)

### AC pour US-001 (connexion)
- **AC-001-1** : Étant donné un compte valide CONSEILLER, quand l'utilisateur saisit des identifiants corrects, alors il est authentifié et redirigé vers le Dashboard agence avec les actions Conseiller visibles.
- **AC-001-2** : Étant donné un compte valide RESPONSABLE_CREDIT, quand l'utilisateur saisit des identifiants corrects, alors il est authentifié et redirigé vers le Dashboard agence avec les actions Responsable crédit visibles.
- **AC-001-3** : Étant donné des identifiants incorrects, quand l'utilisateur tente de se connecter, alors l'accès est refusé avec un message d'erreur explicite, sans détail sur la cause précise (sécurité).

### AC pour US-002 (accès non authentifié)
- **AC-002-1** : Étant donné un utilisateur non authentifié, quand il accède directement à une URL protégée (ex : Dashboard, Liste clients), alors il est redirigé vers l'écran de connexion.

### AC pour US-003 (création client)
- **AC-003-1** : Étant donné un Conseiller authentifié, quand il soumet le formulaire avec nom, revenuMensuel > 0 et chargesMensuelles ≥ 0 renseignés, alors le client est créé avec un `createdAt` horodaté et un statut de succès est affiché.
- **AC-003-2** : Étant donné un client créé, quand on consulte sa fiche, alors nom, revenuMensuel, chargesMensuelles, situationProfessionnelle et createdAt sont affichés fidèlement aux valeurs saisies.

### AC pour US-004 (erreurs création client — RG-BANK-01)
- **AC-004-1** : Étant donné un formulaire de création client sans nom renseigné, quand l'utilisateur soumet, alors la création est rejetée avec le message "Le nom est obligatoire".
- **AC-004-2** : Étant donné un formulaire sans revenuMensuel, quand l'utilisateur soumet, alors la création est rejetée avec le message "Le revenu mensuel est obligatoire".
- **AC-004-3** : Étant donné un formulaire sans chargesMensuelles, quand l'utilisateur soumet, alors la création est rejetée avec le message "Les charges mensuelles sont obligatoires".
- **AC-004-4** : Étant donné plusieurs champs obligatoires manquants simultanément, quand l'utilisateur soumet, alors tous les messages d'erreur correspondants sont affichés (pas seulement le premier).

### AC pour US-005 (liste clients)
- **AC-005-1** : Étant donné au moins un client existant, quand le Conseiller ou Responsable crédit accède à la liste clients, alors tous les clients sont affichés avec au minimum nom et revenuMensuel.
- **AC-005-2** : Étant donné aucun client existant, quand l'utilisateur accède à la liste clients, alors un état vide explicite est affiché (pas d'erreur technique visible).

### AC pour US-006 (détail client)
- **AC-006-1** : Étant donné un client possédant 2 demandes de crédit, quand l'utilisateur consulte son détail, alors les 2 demandes sont listées avec leur statut courant.
- **AC-006-2** : Étant donné un client sans demande, quand l'utilisateur consulte son détail, alors la section demandes affiche un état vide explicite.

### AC pour US-007 (création demande — RG-BANK-02, RG-BANK-03)
- **AC-007-1** : Étant donné un client existant, un montant de 1001, une durée de 12 et un taux fictif renseignés, quand le Conseiller crée la demande, alors elle est créée avec le statut initial `BROUILLON`.
- **AC-007-2** : Étant donné un montant de 100000 et une durée de 84, quand le Conseiller crée la demande, alors elle est acceptée (bornes hautes inclusives respectées).
- **AC-007-3** : Étant donné une demande créée sans client sélectionné, quand le Conseiller soumet le formulaire, alors la création est rejetée avec le message "Le client est obligatoire".

### AC pour US-008 (erreurs bornes montant/durée — RG-BANK-02, RG-BANK-03)
- **AC-008-1** : Étant donné un montant de 1000 (borne exclue), quand le Conseiller soumet, alors la création est rejetée avec le message "Le montant doit être strictement supérieur à 1000".
- **AC-008-2** : Étant donné un montant de 100001, quand le Conseiller soumet, alors la création est rejetée avec le message "Le montant doit être inférieur ou égal à 100000".
- **AC-008-3** : Étant donné une durée de 11, quand le Conseiller soumet, alors la création est rejetée avec le message "La durée doit être comprise entre 12 et 84 mois".
- **AC-008-4** : Étant donné une durée de 85, quand le Conseiller soumet, alors la création est rejetée avec le message "La durée doit être comprise entre 12 et 84 mois".

### AC pour US-009 (simulation — RG-BANK-04, RG-BANK-08)
- **AC-009-1** : Étant donné une demande avec montant, durée, taux fictif renseignés et client avec chargesMensuelles/revenuMensuel connus, quand le Conseiller lance la simulation, alors mensualiteEstimee, tauxEndettement (%) et scoreSimplifie sont calculés et affichés.
- **AC-009-2** : Étant donné un tauxEndettement calculé de 20% exactement, alors scoreSimplifie = `EXCELLENT`.
- **AC-009-3** : Étant donné un tauxEndettement calculé de 20.01%, alors scoreSimplifie = `BON`.
- **AC-009-4** : Étant donné un tauxEndettement calculé de 35% exactement, alors scoreSimplifie = `BON`.
- **AC-009-5** : Étant donné un tauxEndettement calculé de 50.01%, alors scoreSimplifie = `FAIBLE`.
- **AC-009-6** : Étant donné un score `FAIBLE` ou `MOYEN`, quand la simulation est affichée, alors un avertissement pédagogique visuel (orange/rouge selon charte) est affiché.

### AC pour US-010 (soumission — RG-BANK-05)
- **AC-010-1** : Étant donné une demande au statut `BROUILLON`, quand le Conseiller la soumet, alors le statut passe à `SOUMISE`, `dateSoumission` est renseignée, et une entrée HistoriqueDecision (BROUILLON→SOUMISE, auteur=Conseiller, date) est créée.
- **AC-010-2** : Étant donné une demande déjà au statut `SOUMISE`, `EN_ANALYSE`, `ACCEPTEE`, `REFUSEE` ou `ANNULEE`, quand le Conseiller tente de la soumettre à nouveau, alors l'action est rejetée avec un message "Transition invalide : seule une demande en Brouillon peut être soumise".

### AC pour US-011 (annulation — hypothèse validée)
- **AC-011-1** : Étant donné une demande au statut `BROUILLON`, quand le Conseiller l'annule, alors le statut passe à `ANNULEE` et une entrée HistoriqueDecision est créée.
- **AC-011-2** : Étant donné une demande au statut `SOUMISE`, quand le Conseiller l'annule, alors le statut passe à `ANNULEE` et une entrée HistoriqueDecision est créée.
- **AC-011-3** : Étant donné une demande au statut `EN_ANALYSE`, `ACCEPTEE` ou `REFUSEE`, quand le Conseiller tente de l'annuler, alors l'action est rejetée avec le message "Annulation impossible : la demande est déjà en analyse ou décidée".
- **AC-011-4** : Étant donné une demande à n'importe quel statut, quand le Responsable crédit tente de l'annuler, alors l'action est rejetée pour rôle non autorisé.

### AC pour US-012 (passage en analyse — RG-BANK-05)
- **AC-012-1** : Étant donné une demande au statut `SOUMISE`, quand le Responsable crédit la passe en analyse, alors le statut passe à `EN_ANALYSE` et une entrée HistoriqueDecision est créée (auteur = Responsable crédit).
- **AC-012-2** : Étant donné une demande au statut `BROUILLON`, quand le Responsable crédit tente de la passer en analyse, alors l'action est rejetée avec le message "Transition invalide : seule une demande Soumise peut passer en analyse".
- **AC-012-3** : Étant donné une demande au statut `SOUMISE`, quand un Conseiller tente de la passer en analyse, alors l'action est rejetée pour rôle non autorisé.

### AC pour US-013 (acceptation — RG-BANK-05, RG-BANK-06)
- **AC-013-1** (révisée — arbitrage AgentOrchestrator 2026-08-13, voir §9 note Q3-bis) : les 3 conditions d'éligibilité de RG-BANK-06 sont des seuils **indépendants**, chacun vérifié séparément (`≤`), sans exigence de combinaison stricte simultanée des 3 bornes exactes dans un même scénario :
  - **AC-013-1a** : Étant donné une demande `EN_ANALYSE` avec tauxEndettement = 35% exactement (revenuMensuel et montantDemande très larges, hors de leurs propres bornes), quand le Responsable crédit accepte, alors le statut passe à `ACCEPTEE`, `dateDecision` est renseignée et une entrée HistoriqueDecision est créée.
  - **AC-013-1b** : Étant donné une demande `EN_ANALYSE` avec revenuMensuel = 1500 exactement (tauxEndettement et montantDemande très larges), quand le Responsable crédit accepte, alors le statut passe à `ACCEPTEE`, `dateDecision` est renseignée et une entrée HistoriqueDecision est créée.
  - **AC-013-1c** : Étant donné une demande `EN_ANALYSE` avec montantDemande = 50000 exactement (tauxEndettement et revenuMensuel très larges), quand le Responsable crédit accepte, alors le statut passe à `ACCEPTEE`, `dateDecision` est renseignée et une entrée HistoriqueDecision est créée.
- **AC-013-2** : Étant donné une demande `EN_ANALYSE` avec tauxEndettement = 35.1% (au-delà de la borne), quand le Responsable crédit tente d'accepter, alors l'action est rejetée avec le message "Acceptation impossible : taux d'endettement supérieur à 35%".
- **AC-013-3** : Étant donné une demande `EN_ANALYSE` avec revenuMensuel = 1499, quand le Responsable crédit tente d'accepter, alors l'action est rejetée avec le message "Acceptation impossible : revenu mensuel inférieur à 1500".
- **AC-013-4** : Étant donné une demande `EN_ANALYSE` avec montantDemande = 50001 mais tauxEndettement et revenuMensuel conformes, quand le Responsable crédit tente d'accepter, alors l'action est rejetée avec le message "Acceptation impossible : montant supérieur à 50000".
- **AC-013-5** : Étant donné une demande à un statut autre que `EN_ANALYSE`, quand le Responsable crédit tente de l'accepter, alors l'action est rejetée pour transition invalide.

### AC pour US-014 / US-015 (refus + commentaire obligatoire — RG-BANK-07)
- **AC-014-1** : Étant donné une demande `EN_ANALYSE` et un commentaire de décision non vide fourni, quand le Responsable crédit refuse, alors le statut passe à `REFUSEE`, `commentaireDecision` et `dateDecision` sont enregistrés, et une entrée HistoriqueDecision (avec commentaire et auteur) est créée.
- **AC-015-1** : Étant donné une demande `EN_ANALYSE` et un commentaire vide ou absent, quand le Responsable crédit tente de refuser, alors l'action est rejetée avec le message "Un commentaire est obligatoire pour refuser une demande".
- **AC-015-2** : Étant donné un commentaire composé uniquement d'espaces, quand le Responsable crédit tente de refuser, alors l'action est traitée comme un commentaire absent et rejetée (même message que AC-015-1).
- **AC-015-3** : Étant donné une demande à un statut autre que `EN_ANALYSE`, quand le Responsable crédit tente de la refuser, alors l'action est rejetée pour transition invalide.

### AC pour US-016 (historique)
- **AC-016-1** : Étant donné une demande ayant subi les transitions BROUILLON→SOUMISE→EN_ANALYSE→REFUSEE, quand un utilisateur consulte l'historique, alors les 3 entrées sont affichées dans l'ordre chronologique avec ancienStatut, nouveauStatut, commentaire, auteur et date pour chacune.
- **AC-016-2** : Étant donné une demande encore en `BROUILLON` (aucune transition effectuée), quand un utilisateur consulte l'historique, alors un état vide explicite est affiché.

### AC pour US-017 (dashboard)
- **AC-017-1** : Étant donné plusieurs demandes réparties sur différents statuts, quand un utilisateur consulte le Dashboard, alors les compteurs "Soumises", "En analyse", "Acceptées", "Refusées" reflètent exactement le nombre de demandes dans chacun de ces statuts.
- **AC-017-2** : Étant donné un ensemble de demandes, quand un utilisateur consulte le Dashboard, alors le "Montant total demandé" correspond à la somme des `montantDemande` de toutes les demandes prises en compte (périmètre à confirmer — voir Q4 : toutes demandes ou hors Brouillon/Annulée).
- **AC-017-3** : Étant donné un ensemble de demandes simulées, quand un utilisateur consulte le Dashboard, alors le "Taux moyen d'endettement" correspond à la moyenne arithmétique des `tauxEndettement` calculés sur le même périmètre que AC-017-2.
- **AC-017-4** : Étant donné aucune demande en base, quand un utilisateur consulte le Dashboard, alors tous les compteurs affichent 0 et les montants/taux affichent une valeur neutre (0 ou "N/A") sans erreur technique visible.

---

## 8. Edge cases and negative scenarios (récapitulatif transverse)

| # | Scénario | Comportement attendu | RG associée |
|---|---|---|---|
| E1 | Montant = 1000 (borne basse exclue) | Rejet | RG-BANK-02 |
| E2 | Montant = 100000 (borne haute incluse) | Accepté | RG-BANK-02 |
| E3 | Montant = 100001 | Rejet | RG-BANK-02 |
| E4 | Durée = 11 | Rejet | RG-BANK-03 |
| E5 | Durée = 12 (borne basse incluse) | Accepté | RG-BANK-03 |
| E6 | Durée = 84 (borne haute incluse) | Accepté | RG-BANK-03 |
| E7 | Durée = 85 | Rejet | RG-BANK-03 |
| E8 | Refus sans commentaire | Rejet, message explicite | RG-BANK-07 |
| E9 | Refus avec commentaire uniquement composé d'espaces | Rejet, traité comme absent | RG-BANK-07 |
| E10 | Transition invalide (ex : BROUILLON→EN_ANALYSE, EN_ANALYSE→ANNULEE, ACCEPTEE→SOUMISE) | Rejet, message "transition invalide" | RG-BANK-05 |
| E11 | Endettement > 35% en analyse | Acceptation refusée par le système | RG-BANK-06 |
| E12 | Revenu < 1500 en analyse | Acceptation refusée par le système | RG-BANK-06 |
| E13 | Montant > 50000 mais endettement et revenu conformes | Acceptation refusée par le système (une seule condition suffit à bloquer) | RG-BANK-06 |
| E14 | Annulation d'une demande après passage à SOUMISE | Autorisée si statut encore SOUMISE (avant EN_ANALYSE) | Hypothèse validée + RG-BANK-05 |
| E15 | Annulation tentée alors que statut = EN_ANALYSE/ACCEPTEE/REFUSEE | Rejet | Hypothèse validée + RG-BANK-05 |
| E16 | Annulation tentée par Responsable crédit | Rejet, rôle non autorisé | Hypothèse validée |
| E17 | Accès Conseiller à une action de décision (accepter/refuser/passer en analyse) | Rejet, rôle non autorisé | Acteurs §2.1 |
| E18 | Accès Responsable crédit à une action de création/soumission client ou demande | Rejet, rôle non autorisé (à confirmer — voir Q5) | Acteurs §2.1 |
| E19 | Client créé sans nom / revenu / charges | Rejet, message par champ | RG-BANK-01 |
| E20 | Demande créée sans client associé | Rejet | Modèle relationnel §3.2 |
| E21 | Double soumission d'une demande déjà SOUMISE | Rejet, transition invalide | RG-BANK-05 |
| E22 | Score simplifié à la borne exacte 35% | Classé `BON` (borne haute incluse dans la bande "≤35%") | RG-BANK-08 |
| E23 | Endettement exactement à 35% en décision | Condition d'éligibilité satisfaite (≤ 35% inclusif) | RG-BANK-06 |
| E24 | Revenu exactement à 1500 en décision | Condition d'éligibilité satisfaite (≥ 1500 inclusif) | RG-BANK-06 |
| E25 | Montant exactement à 50000 en décision | Condition d'éligibilité satisfaite (≤ 50000 inclusif) | RG-BANK-06 |

---

## 9. Open questions and risks

Statut : arbitré par AgentOrchestrator le 2026-08-11 pour permettre le démarrage d'AgentArchitect sans ambiguïté. Les défauts ci-dessous sont désormais **retenus comme définitifs pour cette itération** (et non plus de simples hypothèses), sauf retour explicite du Product Owner.

1. **Q1 — situationProfessionnelle** : champ texte libre non obligatoire. *Retenu.*
2. **Q2 — première entrée d'historique** : une entrée HistoriqueDecision est créée dès la création de la demande, avec `ancienStatut = null`, `nouveauStatut = BROUILLON`. *Retenu.*
3. **Q3 — acceptation manuelle malgré non-éligibilité** : blocage strict retenu (voir RG-BANK-06) — le système empêche l'acceptation si une des 3 conditions n'est pas remplie ; aucune option de forçage n'est proposée dans cette itération. *Retenu, non bloquant pour la suite.*
3bis. **Q3-bis — nature des 3 conditions d'éligibilité (RG-BANK-06)** : arbitrage AgentOrchestrator du 2026-08-13, suite à un constat AgentQA/AgentReviewer (AC-013-1 combinant les 3 bornes exactes 35%/1500/50000 simultanément était mathématiquement infaisable sous RG-BANK-02/03/04, la mensualité plancher à montant=50000/durée=84/taux=0 imposant déjà un taux d'endettement d'environ 39,68% avec revenu=1500). Décision : les 3 conditions restent des seuils **indépendants**, chacun vérifié séparément (`≤`), sans exigence de combinaison stricte simultanée des 3 bornes exactes dans un même scénario. AC-013-1 reformulée en AC-013-1a/1b/1c (§7) en conséquence. *Retenu, non bloquant, aucun changement de comportement applicatif requis (le code et les tests existants appliquaient déjà cette interprétation).*
4. **Q4 — périmètre des agrégats du Dashboard** : exclusion de `BROUILLON` et `ANNULEE` du calcul des agrégats (montant total demandé, taux moyen d'endettement). *Retenu.*
5. **Q5 — droits de lecture croisés** : séparation stricte des responsabilités par rôle ; un Responsable crédit ne peut pas créer client/demande, un Conseiller ne peut pas décider. *Retenu.*

### Risques (suivi)
- **R1** (résolu par Q3 tranchée) : plus de risque de contradiction, RG-BANK-06/AC-013-x restent valables tels quels.
- **R4** (résolu par Q3-bis) : incohérence de l'ancienne AC-013-1 (triple borne exacte simultanée, mathématiquement infaisable) corrigée par reformulation en AC-013-1a/1b/1c ; plus d'incohérence de spec sur RG-BANK-06.
- **R2** : RG-BANK-08 (score simplifié par bandes) reste une règle introduite par AgentBA en l'absence de formule officielle ; à surveiller en cas de retour métier ultérieur, mais non bloquant pour l'architecture/l'implémentation.
- **R3** (résolu) : formule de mensualité estimée normative ajoutée à RG-BANK-04 (amortissement standard, cas particulier taux=0 géré).

---

## 10. Traceability summary

| RG-BANK | Référencée par US | Référencée par AC |
|---|---|---|
| RG-BANK-01 | US-003, US-004 | AC-003-1, AC-004-1..4 |
| RG-BANK-02 | US-007, US-008 | AC-007-1, AC-007-2, AC-008-1, AC-008-2 |
| RG-BANK-03 | US-007, US-008 | AC-007-2, AC-008-3, AC-008-4 |
| RG-BANK-04 | US-009 | AC-009-1 |
| RG-BANK-05 | US-010, US-011, US-012, US-013, US-014 | AC-010-1, AC-010-2, AC-011-1..4, AC-012-1..3, AC-013-1a..1c, AC-013-5, AC-015-3 |
| RG-BANK-06 | US-013 | AC-013-1a..1c, AC-013-2..4 |
| RG-BANK-07 | US-014, US-015 | AC-014-1, AC-015-1, AC-015-2 |
| RG-BANK-08 (nouvelle) | US-009 | AC-009-2..6 |






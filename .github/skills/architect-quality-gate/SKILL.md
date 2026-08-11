---
name: architect-quality-gate
description: Review an architecture deliverable against business requirements, traceability, and readiness for backend/frontend implementation.
---

# Architect Quality Gate

## Goal
Run a final architecture quality gate before backend/frontend implementation is allowed to start.

## When to use
- After an architecture spec/plan/tasks batch is produced or revised

## Inputs
- Architecture specification file
- Plan file
- Tasks file
- User stories and business rules from AgentBA

## Gate checklist
1. Traceability
- Each entity/field/decision maps to a user story or RG-BANK-01 to RG-BANK-07

2. Data model
- Entity relationships (Client, DemandeCredit, HistoriqueDecision, User) are unambiguous
- Status field (RG-BANK-05) modeled as enum with explicit transitions
- Computed value strategy (RG-BANK-04) is explicitly decided and justified

3. Package structure
- Backend package boundaries (model/dto/repository/service/service.impl/controller/security/config/exception) are justified
- Frontend feature structure (features/{domaine}/models-pages-services) is consistent

4. Technical decisions
- Interface+impl, JWT stateless, WAR packaging, Bean Validation vs manual validation are all explicitly justified

5. Readiness
- No implementation code present
- No open blocking question remains unresolved

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final go/no-go recommendation for AgentBackendDeveloper/AgentFrontendDeveloper to start


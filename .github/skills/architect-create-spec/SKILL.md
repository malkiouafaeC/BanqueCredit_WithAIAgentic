---
name: architect-create-spec
description: Create an architecture specification covering backend package structure, data model, and frontend feature architecture derived from business requirements.
---

# Architect Create Spec

## Goal
Create a complete architecture specification that is deterministic, traceable to business requirements, and ready for backend/frontend implementation.

## When to use
- New feature requiring package structure, data model, or frontend feature structure decisions
- Significant change to entity relationships or cross-cutting technical decisions (security, packaging, validation strategy)

## Inputs
- User stories and business rules from AgentBA (including RG-BANK-*)
- Existing architecture context (if evolving an existing system)
- Non-functional constraints (hosting, security, deployment target)

## Output rules
- Save file under spec/
- File name format: spec-architecture-<short-name>.md
- Use explicit identifiers: ENT-xxx (entities), DEC-xxx (technical decisions), AC-xxx (architecture acceptance criteria)
- No ambiguous wording; every decision must include a rationale

## Required sections
1. Purpose and scope
2. Backend package architecture (model/dto/repository/service/service.impl/controller/security/config/exception)
3. Data model and entity relationships (Client, DemandeCredit, HistoriqueDecision, User)
4. Frontend feature architecture (features/{domaine}/models-pages-services)
5. Cross-cutting technical decisions with rationale (interface+impl, JWT stateless, WAR packaging, Bean Validation vs manual validation)
6. Business rule traceability (RG-BANK-01 to RG-BANK-07 mapped to entities/decisions)
7. Open questions for AgentBA/AgentBackendDeveloper/AgentFrontendDeveloper

## Architect-specific guidance
- Every entity field must trace to a business rule or explicit user story
- Every package boundary must have a one-line rationale
- Decide explicitly whether computed values (e.g. taux d'endettement, RG-BANK-04) are persisted or recalculated, with justification
- Model the status field (RG-BANK-05) as an enum with explicit allowed transitions
- No implementation code; architecture only (structure, relationships, decisions)

## Quality checks
- Each ENT maps to at least one RG-BANK-* or user story
- Each DEC has an explicit rationale
- No unresolved TODO placeholders
- Data model is unambiguous enough to implement without further architectural decisions


---
name: reviewer-create-plan
description: Build a cross-review plan comparing backend/frontend coherence, technical debt, and alignment with the original request.
---

# Reviewer Create Plan

## Goal
Produce a scoped cross-review plan before executing detailed comparison tasks between backend, frontend, and original requirements.

## When to use
- Backend and frontend deliverables, tests, and security/deployment findings are available for a release candidate

## Inputs
- Original request/spec and user stories (RG-BANK-*) from AgentBA
- Architecture reference from AgentArchitect
- Delivered backend/frontend code, test evidence, security and deployment findings

## Output rules
- Save file under plan/
- File name format: plan-cross-review-<short-name>.md
- Use explicit identifiers: COMPARE-xxx, DEBT-xxx, RISK-xxx

## Required sections
1. Scope of cross-review (artifacts to compare)
2. Backend/frontend contract comparison points (DTO shapes, enums, validation rules)
3. Business rule consistency checklist (RG-BANK-01 to RG-BANK-07 across backend/frontend/tests)
4. Technical debt and duplication areas to inspect
5. Risks and assumptions

## Planning standards
- Always include status/enum consistency (RG-BANK-05) and refusal comment enforcement consistency (RG-BANK-07) as comparison points
- Flag any area where backend and frontend might implement the same business logic independently (e.g. RG-BANK-04, RG-BANK-06)

## Quality checks
- Every COMPARE item maps to a specific contract or business rule
- DEBT items have an explicit suggested owner
- Risks have a severity level


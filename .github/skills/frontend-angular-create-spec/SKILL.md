---
name: frontend-angular-create-spec
description: Create a frontend Angular specification with explicit UX behavior, contracts, constraints, and acceptance criteria.
---

# Frontend Angular Create Spec

## Goal
Create a complete frontend specification that is deterministic, testable, and implementation-ready.

## When to use
- New frontend feature
- Significant UI/flow refactor
- Behavior changes that require explicit acceptance criteria

## Inputs
- User journey and screens involved
- UX expectations and business rules
- API contracts consumed by UI
- Accessibility, responsive, and performance constraints

## Output rules
- Save file under spec/
- File name format: spec-feature-<short-name>.md
- Use explicit identifiers: REQ-xxx, UX-xxx, SEC-xxx, CON-xxx, AC-xxx
- No ambiguous wording

## Required sections
1. Purpose and scope
2. Definitions
3. Requirements and constraints
4. UI flows and interaction contracts
5. Interfaces and data contracts
6. Acceptance criteria
7. Test automation strategy
8. Dependencies and integrations
9. Edge cases and error states
10. Validation criteria

## Angular-specific guidance
- Define route paths, guards, resolvers, and lazy-loading expectations
- Define component responsibilities and state ownership
- Define form validation rules and error messages
- Define loading, empty, and error UX states
- Define accessibility and responsiveness criteria

## Quality checks
- Each REQ must map to at least one AC
- Each user flow must define success and failure behavior
- Security and accessibility constraints must be explicit and testable
- No unresolved TODO placeholders

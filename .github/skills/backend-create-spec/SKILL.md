---
name: backend-create-spec
description: Create a backend specification for Java Spring Boot features with explicit requirements, API contracts, constraints, and acceptance criteria.
---

# Backend Create Spec

## Goal
Create a complete backend specification that is deterministic, testable, and implementation-ready.

## When to use
- New backend feature
- Significant backend refactor with API or data contract changes
- Business rule changes that require explicit acceptance criteria

## Inputs
- Business objective
- Domain rules
- API expectations
- Data constraints
- Non-functional constraints (security, performance, observability)

## Output rules
- Save file under spec/
- File name format: spec-feature-<short-name>.md
- Use explicit identifiers: REQ-xxx, SEC-xxx, CON-xxx, AC-xxx
- No ambiguous wording

## Required sections
1. Purpose and scope
2. Definitions
3. Requirements and constraints
4. Interfaces and data contracts
5. Acceptance criteria
6. Test automation strategy
7. Dependencies and integrations
8. Edge cases
9. Validation criteria

## Backend-specific guidance
- Define REST endpoints with method, path, request, response, errors
- Define validation rules per field
- Define transaction boundaries and idempotency expectations
- Define authN/authZ requirements explicitly
- Define backward compatibility requirements

## Quality checks
- Each REQ must map to at least one AC
- Each endpoint must define success and error responses
- Security constraints must be explicit and testable
- No unresolved TODO placeholders

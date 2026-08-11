---
name: security-create-plan
description: Build a security review plan covering input validation, authorization, secrets, logging, and AI data exposure.
---

# Security Create Plan

## Goal
Produce a scoped security review plan before executing detailed review tasks.

## When to use
- Delivered backend/frontend code, configuration, or a new AI integration point needs a security pass before release

## Inputs
- Delivered code scope (backend/frontend/config)
- Business rules with security implications (RG-BANK-*)
- Known secrets/config touchpoints (jwt.secret, external API keys)

## Output rules
- Save file under plan/
- File name format: plan-security-review-<short-name>.md
- Use explicit identifiers: SEC-AREA-xxx, RISK-xxx

## Required sections
1. Review scope (files/endpoints/config in scope)
2. Review areas (input validation/injection, authorization, secrets, logging, AI data exposure)
3. Business rules with security implications to verify (RG-BANK-01 to RG-BANK-07)
4. Risk prioritization approach (blocker/high/medium/low)
5. Assumptions

## Planning standards
- Always include authorization checks for status transitions (RG-BANK-05) and refusal enforcement (RG-BANK-07) in scope
- Explicitly plan verification that server-side validation cannot be bypassed by frontend-only checks (RG-BANK-01 to 04, 06)
- Flag any new AI integration point as requiring a dedicated data-exposure review

## Quality checks
- Every SEC-AREA maps to at least one review checklist item
- RG-BANK rules with security implications are explicitly listed
- Risks have a severity level


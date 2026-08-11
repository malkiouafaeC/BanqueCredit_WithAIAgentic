---
name: ba-create-plan
description: Build a prioritization and delivery plan from a functional specification, with story dependencies and release scope.
---

# BA Create Plan

## Goal
Produce a prioritized, phased delivery plan for a set of user stories from an approved functional specification.

## When to use
- A functional specification already exists and stories need to be sequenced for delivery

## Inputs
- Functional specification file
- Business priorities and release constraints

## Output rules
- Save file under plan/
- File name format: plan-functional-<short-name>.md
- Use explicit identifiers: PRIO-xxx, DEP-xxx, SCOPE-xxx, RISK-xxx

## Required sections
1. Story prioritization (must-have vs nice-to-have)
2. Dependencies between user stories
3. Release scope definition
4. Business rules impacted per release phase (RG-BANK-*)
5. Risks and assumptions

## Planning standards
- Sequence stories so foundational rules (e.g. RG-BANK-01 client data) precede dependent rules (e.g. RG-BANK-06 eligibility)
- Keep each phase small enough to be independently reviewable and testable
- Flag any story whose business rule is still ambiguous as a blocker

## Quality checks
- Every PRIO links to at least one US from the spec
- Dependencies are explicit
- Risks have mitigation notes


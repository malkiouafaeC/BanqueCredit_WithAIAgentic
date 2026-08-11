---
name: qa-create-plan
description: Build a test strategy plan (unit, integration, non-regression) from delivered user stories, business rules, and code.
---

# QA Create Plan

## Goal
Produce a phased test strategy plan following the test pyramid, mapped explicitly to business rules.

## When to use
- Feature user stories/acceptance criteria are available and code delivery is starting or complete

## Inputs
- Functional specification (user stories, acceptance criteria, RG-BANK-*) from AgentBA
- Delivered or planned backend/frontend code scope

## Output rules
- Save file under plan/
- File name format: plan-tests-<short-name>.md
- Use explicit identifiers: TEST-xxx, RG-BANK-xxx, TOOL-xxx

## Required sections
1. Test pyramid breakdown (unit/integration/non-regression proportions)
2. Tools per layer (JUnit5/Mockito/MockMvc backend, Jasmine/Karma frontend)
3. Business rule to test-case mapping plan (RG-BANK-01 to RG-BANK-07)
4. Coverage targets and critical flows for non-regression
5. Risks and assumptions

## Planning standards
- Majority of planned tests must be unit-level; integration tests reserved for API/persistence/component boundaries
- Every RG-BANK-01 to RG-BANK-07 relevant to the feature must appear in the mapping with at least one planned test
- Explicitly plan boundary-value tests for numeric rules (RG-BANK-02, RG-BANK-03, RG-BANK-06)

## Quality checks
- Every TEST links to a user story or RG-BANK-*
- Tools are specified per layer
- Risks have mitigation notes


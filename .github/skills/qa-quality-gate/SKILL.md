---
name: qa-quality-gate
description: Review test suite completeness, pyramid balance, and business rule coverage before release.
---

# QA Quality Gate

## Goal
Run a final test quality gate before release, ensuring the test pyramid and business rule coverage are respected.

## When to use
- After a test batch is implemented for a feature or a release candidate is being evaluated

## Inputs
- Test strategy plan
- Tasks file
- Delivered test suites (backend and frontend)
- Business rules (RG-BANK-*) from AgentBA

## Gate checklist
1. Pyramid balance
- Majority unit tests, focused integration tests, targeted non-regression tests

2. Business rule coverage
- Each RG-BANK-01 to RG-BANK-07 relevant to scope has at least one traceable test case
- Boundary values are explicitly tested (RG-BANK-02, RG-BANK-03, RG-BANK-06)

3. Reliability
- No flaky tests
- Existing tests remain green

4. Edge cases
- Negative scenarios (invalid input, unauthorized access, invalid state transitions) are covered

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final merge/release recommendation


---
name: backend-quality-gate
description: Review backend implementation against specification, tests, security constraints, and operational readiness.
---

# Backend Quality Gate

## Goal
Run a final backend quality gate before merge.

## When to use
- After one task or a feature batch is implemented

## Inputs
- Specification file
- Plan file
- Tasks file
- Implementation changes
- Test results

## Gate checklist
1. Traceability
- Each implemented change maps to REQ and AC

2. API contract
- Request and response schemas match spec
- Error responses are standardized

3. Security
- Input validation present
- AuthN/authZ checks respected
- Sensitive data not logged

4. Data and transactions
- Transaction boundaries are coherent
- Migration and rollback paths are documented if needed

5. Test evidence
- Unit and integration tests cover main and error paths
- Regressions are checked

6. Observability
- Logs and metrics are meaningful for operations

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final merge recommendation

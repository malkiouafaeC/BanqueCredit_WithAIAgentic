---
name: orchestrator-integration-gate
description: Validate backend and frontend outputs against contracts, tests, and release constraints before merge.
---

# Orchestrator Integration Gate

## Goal
Decide release readiness after backend and frontend execution.

## Validation checklist
1. Contract alignment
- API endpoint paths, payloads, and error responses align with FE expectations.

2. Requirement coverage
- Implemented tasks map to REQ and AC identifiers.

3. Test evidence
- Backend and frontend tests cover critical and error paths.

4. Cross-agent risks
- No unresolved integration blocker.
- No known breaking change without explicit approval.

5. Operational quality
- Logging/observability and UX error handling are acceptable for release.

## Decision output
- READY: all blocking checks pass.
- NOT_READY: at least one blocking check fails.

## Reporting format
- Blocking issues
- Non-blocking improvements
- Final recommendation

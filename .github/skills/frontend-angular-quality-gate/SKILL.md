---
name: frontend-angular-quality-gate
description: Review frontend Angular implementation against specification, UX quality, accessibility, security, performance, and operational readiness.
---

# Frontend Angular Quality Gate

## Goal
Run a final frontend quality gate before merge.

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

2. UX behavior
- Screens and flows match expected behavior
- Loading, empty, error, and retry states are implemented

3. Accessibility
- Keyboard navigation works
- Focus management is correct
- Semantics and labels are sufficient

4. Security
- No secret exposure
- Safe rendering and input handling
- Safe token/session handling patterns

5. Performance
- Lazy loading and chunk boundaries are respected
- Avoid unnecessary re-renders and heavy runtime work

6. Test evidence
- Unit and integration tests cover main and error paths
- Regressions are checked

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final merge recommendation

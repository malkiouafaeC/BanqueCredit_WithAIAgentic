---
name: frontend-angular-implement-task
description: Implement a single frontend Angular task with robust UX, tests, and safe integration patterns.
---

# Frontend Angular Implement Task

## Goal
Implement one task at a time with production-grade Angular standards.

## When to use
- During execution of TASK-xxx from tasks file

## Inputs
- Task ID
- Task details
- Related REQ and AC references

## Implementation standards
- Prefer standalone components and typed APIs
- Keep components focused and reusable
- Use typed reactive forms for form-heavy flows
- Manage subscriptions safely and prevent leaks
- Keep change detection efficient (OnPush, trackBy, memoized selectors/signals)
- Keep HTTP integration typed and map API errors to user-friendly states

## UX standards
- Explicit loading, success, empty, and error states
- Keyboard and screen-reader accessibility for interactive controls
- Responsive layouts for mobile and desktop

## Test standards
- Add unit tests for component/service behavior
- Add integration tests for user flows and route interactions when relevant
- Cover happy path, validation failures, and error handling UI

## Output format
1. Task summary
2. Assumptions
3. Files changed
4. UI and behavior notes
5. Tests added or updated
6. Remaining risks

## Done checks
- Build passes
- Related tests pass
- UX behavior matches spec
- No obvious accessibility, security, or performance regression

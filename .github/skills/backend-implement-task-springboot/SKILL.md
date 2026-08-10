---
name: backend-implement-task-springboot
description: Implement a single backend task in Java Spring Boot with tests, safe error handling, and concise change notes.
---

# Backend Implement Task Spring Boot

## Goal
Implement one task at a time with production-grade coding standards.

## When to use
- During execution of TASK-xxx from tasks file

## Inputs
- Task ID
- Task details
- Related REQ and AC references

## Implementation standards
- Keep controller thin, put business logic in service
- Use DTOs at API boundaries
- Validate input with Bean Validation and business checks
- Use centralized exception handling and consistent error payloads
- Keep transaction scope explicit
- Avoid N+1 query patterns

## Test standards
- Add unit tests for business rules
- Add integration tests for endpoint or repository behavior when needed
- Cover happy path, validation failures, and edge cases

## Output format
1. Task summary
2. Assumptions
3. Files changed
4. Code behavior notes
5. Tests added or updated
6. Remaining risks

## Done checks
- Build passes
- Related tests pass
- Contract remains consistent with spec
- No secret leakage in logs

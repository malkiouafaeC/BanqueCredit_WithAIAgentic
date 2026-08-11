---
name: qa-implement-tests-junit-mockito
description: Implement backend tests with JUnit5/Mockito/MockMvc and frontend tests with Jasmine/Karma for a single test task.
---

# QA Implement Tests JUnit Mockito

## Goal
Implement one test task at a time with reliable, deterministic, business-rule-traceable tests.

## When to use
- During execution of QA-TASK-xxx from the tasks file

## Inputs
- Task ID and task details
- Related user story, acceptance criteria, and RG-BANK-* reference
- Code under test

## Implementation standards
- Backend: use JUnit5 with Mockito for service-level unit tests; use MockMvc (or @WebMvcTest) for controller-level tests
- Frontend: use Jasmine/Karma with TestBed for components/services
- Isolate unit tests from real persistence/network using mocks/stubs
- Cover happy path, validation errors, and explicit edge cases (boundary values, unauthorized access, invalid state transitions)
- Name tests descriptively (should_X_when_Y or given/when/then)

## Test standards
- Every test must map to a specific acceptance criterion or RG-BANK-* rule
- Avoid non-deterministic tests (no reliance on real time/random/network without mocking)
- Use test data builders/fixtures to avoid duplication

## Output format
1. Task summary
2. Assumptions
3. Files changed/added
4. Test behavior notes
5. Business rule to test mapping (RG-BANK-*)
6. Remaining risks/gaps

## Done checks
- Tests pass reliably (no flakiness)
- Related business rule/edge case is explicitly covered
- Existing tests remain green
- No test relies on unmocked external systems


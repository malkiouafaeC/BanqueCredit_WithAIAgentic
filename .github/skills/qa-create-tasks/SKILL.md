---
name: qa-create-tasks
description: Generate ordered, atomic test implementation tasks from a test strategy plan.
---

# QA Create Tasks

## Goal
Convert a test strategy plan into an atomic backlog of test implementation tasks.

## When to use
- Test plan is approved and test writing can start

## Inputs
- Test strategy plan file

## Output rules
- Save file under tasks/
- File name format: tasks-tests-<short-name>.md
- Task IDs: QA-TASK-001, QA-TASK-002, ...

## Task template
For each task include:
- Task ID
- Target class/component/endpoint under test
- Test type (unit/integration/non-regression)
- Business rule(s) covered (RG-BANK-*)
- Test tool (JUnit5/Mockito/MockMvc or Jasmine/Karma)
- Definition of done for that task

## Ordering rules
- Place unit tests for business rules before integration tests for controllers/components
- Place non-regression tests for critical flows last
- Group tasks by business rule where a rule spans multiple layers (e.g. RG-BANK-06 eligibility: service unit test + controller integration test)

## Quality checks
- No task should exceed one clear test objective
- Each task references exact class/file/endpoint
- Each task explicitly states which RG-BANK rule or edge case it validates


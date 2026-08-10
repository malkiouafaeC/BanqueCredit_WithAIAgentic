---
name: frontend-angular-create-tasks
description: Generate ordered, atomic frontend Angular tasks from an implementation plan.
---

# Frontend Angular Create Tasks

## Goal
Convert a plan into an execution backlog of atomic tasks for frontend delivery.

## When to use
- Plan is approved and coding can start

## Inputs
- Implementation plan file

## Output rules
- Save file under tasks/
- File name format: tasks-feature-<short-name>.md
- Task IDs: TASK-001, TASK-002, ...

## Task template
For each task include:
- Task ID
- Objective
- Files impacted
- Preconditions
- Implementation steps
- Test steps
- Definition of done for that task

## Ordering rules
- Route and feature shell before detailed UI behaviors
- Contracts and models before service integration
- Service integration before container logic
- Container logic before presentational refinements
- Functional behavior tests before non-functional polishing

## Quality checks
- No task should exceed one clear objective
- Each task has at least one direct validation action
- Each task references exact files or Angular feature areas

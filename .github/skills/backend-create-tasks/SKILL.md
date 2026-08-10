---
name: backend-create-tasks
description: Generate ordered, atomic backend tasks from an implementation plan for Java Spring Boot delivery.
---

# Backend Create Tasks

## Goal
Convert a plan into an execution backlog of atomic tasks for backend delivery.

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
- Place schema and contract tasks before service logic
- Place service logic before controller integration
- Place controller before integration and regression tests
- Keep refactor-only tasks separate from feature tasks

## Quality checks
- No task should exceed one clear objective
- Each task has at least one direct validation action
- Each task references exact files or packages

---
name: ba-create-tasks
description: Generate ordered, atomic functional refinement tasks (backlog breakdown) from a delivery plan.
---

# BA Create Tasks

## Goal
Convert a functional delivery plan into an atomic backlog of refinement tasks ready for AgentArchitect consumption.

## When to use
- Plan is approved and user stories need final breakdown before architecture/implementation

## Inputs
- Functional plan file

## Output rules
- Save file under tasks/
- File name format: tasks-functional-<short-name>.md
- Task IDs: US-TASK-001, US-TASK-002, ...

## Task template
For each task include:
- Task ID
- Related user story (US-xxx)
- Objective (refine acceptance criteria / clarify edge case / resolve open question)
- Business rules involved (RG-BANK-*)
- Definition of done for that task (criteria fully specified, no ambiguity remaining)

## Ordering rules
- Place foundational data rule refinement (RG-BANK-01 to 03) before calculation/decision rule refinement (RG-BANK-04, 06)
- Place status lifecycle refinement (RG-BANK-05) before refusal-specific refinement (RG-BANK-07)
- Keep cross-cutting glossary/clarification tasks first

## Quality checks
- No task should exceed one clear functional objective
- Each task references exact user story and business rule
- Each task has an explicit validation action (reviewed acceptance criteria)


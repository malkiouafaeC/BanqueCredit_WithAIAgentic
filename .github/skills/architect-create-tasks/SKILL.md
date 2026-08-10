---
name: architect-create-tasks
description: Generate ordered, atomic architecture definition tasks from a rollout plan.
---

# Architect Create Tasks

## Goal
Convert an architecture plan into an atomic backlog of architecture definition and validation tasks.

## When to use
- Plan is approved and architecture artifacts need to be finalized for handoff

## Inputs
- Architecture plan file

## Output rules
- Save file under tasks/
- File name format: tasks-architecture-<short-name>.md
- Task IDs: ARCH-TASK-001, ARCH-TASK-002, ...

## Task template
For each task include:
- Task ID
- Objective (e.g. "define Client/DemandeCredit relationship")
- Deliverable (diagram/description/decision record)
- Preconditions
- Validation step (reviewed against RG-BANK-* and user stories)
- Definition of done for that task

## Ordering rules
- Place entity/data model tasks before package structure tasks
- Place package structure tasks before cross-cutting decision tasks (security, packaging)
- Place backend architecture tasks and frontend architecture tasks so dependencies are explicit
- Keep validation/handoff tasks last

## Quality checks
- No task should exceed one clear architectural objective
- Each task references exact entities/packages/decisions
- Each task has an explicit validation action against business rules


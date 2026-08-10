---
name: backend-create-plan
description: Build an implementation plan from a backend specification with phases, dependencies, files, tests, and risks.
---

# Backend Create Plan

## Goal
Produce a phased and executable implementation plan from a backend specification.

## When to use
- A specification already exists and implementation is about to start

## Inputs
- Specification file
- Current repository context

## Output rules
- Save file under plan/
- File name format: plan-feature-<short-name>.md
- Use explicit identifiers: GOAL-xxx, TASK-xxx, DEP-xxx, FILE-xxx, TEST-xxx, RISK-xxx

## Required sections
1. Requirements traceability matrix (REQ -> TASK)
2. Implementation phases
3. Task list with dependencies
4. Files to modify or create
5. Testing strategy and coverage targets
6. Rollback and migration notes
7. Risks and assumptions

## Planning standards
- Tasks must be atomic and independently verifiable
- Each task must include expected output and verification step
- Prefer smallest safe increment first
- Mention database migration and backward compatibility handling when applicable

## Quality checks
- Every TASK links to at least one REQ
- Dependencies are explicit
- Tests are defined before coding starts
- Risks have mitigation notes

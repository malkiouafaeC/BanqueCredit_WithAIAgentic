---
name: orchestrator-spec-driven-flow
description: Drive end-to-end delivery using a deterministic spec -> plan -> tasks -> implement -> quality gate workflow across multiple agents.
---

# Orchestrator Spec-Driven Flow

## Goal
Run a complete multi-agent workflow from product objective to delivery decision.

## Inputs
- Product objective
- Constraints (scope, timeline, compliance, tech limits)
- Participating agents

## Standard phases
1. SPEC
- Produce global spec in spec/ with explicit requirements and acceptance criteria.

2. PLAN
- Produce implementation plan in plan/ with phases and dependencies.

3. TASKS
- Produce ordered tasks in tasks/ with BE/FE/INT tagging.

4. IMPLEMENT
- Dispatch tasks to specialized agents with role-specific prompts.

5. QUALITY GATE
- Run integration and release-readiness checks.

## Output rules
- Keep one source of truth for each phase artifact.
- Keep task IDs stable across the workflow.
- Maintain traceability REQ -> TASK -> TEST.

## Quality checks
- Every acceptance criterion maps to at least one task.
- Every implemented task has evidence and test notes.
- Blockers and assumptions are explicit.

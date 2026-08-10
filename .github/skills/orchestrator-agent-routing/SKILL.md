---
name: orchestrator-agent-routing
description: Route tasks to backend and frontend agents, generate handoff prompts, and maintain a progress board with blocker states.
---

# Orchestrator Agent Routing

## Goal
Assign the right task to the right agent with deterministic handoff context.

## Routing model
- BE-* tasks -> AgentBackendDeveloper
- FE-* tasks -> AgentFrontendDeveloper
- INT-* tasks -> AgentOrchestrator for integration arbitration

## Required handoff payload
- Task ID
- Linked requirements and acceptance criteria
- Dependencies and prerequisites
- Exact expected deliverable
- Test expectations
- Risk notes

## Artifact locations
- Handoffs: orchestration/handoffs/
- Prompts: orchestration/prompts/
- Progress: orchestration/status/progress-board.md

## Status model
- TODO
- IN_PROGRESS
- BLOCKED
- IN_REVIEW
- DONE

## Blocker policy
- If blocked by another task, mark BLOCKED and reference dependency task id.
- If blocked by ambiguous requirement, issue clarification request with 1-3 focused questions.

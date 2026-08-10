---
name: architect-create-plan
description: Build a rollout plan from an architecture specification with phases, backend/frontend dependencies, and risks.
---

# Architect Create Plan

## Goal
Produce a phased plan for formalizing and rolling out an approved architecture before backend/frontend implementation starts.

## When to use
- An architecture specification already exists and needs to be sequenced for delivery

## Inputs
- Architecture specification file
- Current repository/package context

## Output rules
- Save file under plan/
- File name format: plan-architecture-<short-name>.md
- Use explicit identifiers: DECISION-xxx, DEP-xxx, RISK-xxx

## Required sections
1. Decisions traceability matrix (ENT/DEC -> rollout step)
2. Rollout phases (backend package scaffolding order, frontend feature scaffolding order)
3. Dependencies between backend architecture and frontend architecture
4. Blocking dependency notice: AgentBackendDeveloper and AgentFrontendDeveloper cannot start before architecture validation
5. Risks and assumptions

## Planning standards
- Sequence entity/package scaffolding before service and controller work
- Flag any architecture decision still pending validation as a blocker
- Keep phases small enough to be independently reviewable

## Quality checks
- Every DECISION links to an ENT/DEC from the spec
- Dependencies between backend and frontend architecture are explicit
- Risks have mitigation notes


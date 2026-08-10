---
name: reviewer-cross-review-tasks
description: Execute atomic cross-review tasks comparing backend/frontend contracts, business rules, and technical debt.
---

# Reviewer Cross Review Tasks

## Goal
Execute one cross-review task at a time and produce concrete, actionable coherence findings.

## When to use
- During execution of REV-TASK-xxx from a cross-review plan

## Inputs
- Task ID and comparison scope
- Backend/frontend code, test evidence, original request/spec

## Review standards
- Compare API contract as implemented against what AgentArchitect defined and what AgentFrontendDeveloper consumes
- Verify enum/status values (RG-BANK-05) match exactly (spelling, casing) across backend, API contract, and frontend model
- Verify validation rules (RG-BANK-01 to 03, RG-BANK-06) are consistent between backend rejection and frontend form validation
- Identify duplicated business logic (e.g. eligibility or debt-ratio calculation reimplemented differently) and recommend a single source of truth
- Trace each original user story/business rule to a concrete delivered artifact or explicit documented exception

## Output format
1. Task summary (comparison scope)
2. Findings (with severity: blocker/high/medium/low)
3. Technical debt/duplication identified (with suggested owner)
4. Business rule(s) verified (RG-BANK-*)
5. Gap versus original request, if any

## Done checks
- Each finding has an explicit severity and suggested owner
- No blocker finding is left without acknowledgment
- Business rule consistency is explicitly verified, not assumed


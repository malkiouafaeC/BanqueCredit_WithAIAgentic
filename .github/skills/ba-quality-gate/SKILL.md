---
name: ba-quality-gate
description: Review functional deliverables against format, traceability, and readiness for architecture and implementation.
---

# BA Quality Gate

## Goal
Run a final functional quality gate before an architecture phase starts.

## When to use
- After a functional spec/plan/tasks batch is produced or revised

## Inputs
- Functional specification file
- Plan file
- Tasks file

## Gate checklist
1. Format compliance
- Every user story follows "En tant que X, je veux Y, afin de Z"

2. Traceability
- Each user story maps to explicit acceptance criteria
- Relevant RG-BANK-01 to RG-BANK-07 are referenced with no contradiction between stories

3. Completeness
- Edge cases and negative scenarios are explicit for each story
- No vague or unverifiable wording remains

4. Readiness
- Artifacts are ready to be consumed by AgentArchitect without further functional clarification
- No open blocking question remains unresolved

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final go/no-go recommendation for AgentArchitect to start


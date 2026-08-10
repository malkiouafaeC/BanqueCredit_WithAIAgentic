---
name: reviewer-quality-gate
description: Review overall coherence, technical debt, and alignment with the original request before final release synthesis.
---

# Reviewer Quality Gate

## Goal
Run a final cross-review quality gate before AgentOrchestrator produces the release synthesis.

## When to use
- After all cross-review tasks are completed for a release candidate

## Inputs
- Cross-review plan
- Task findings
- Test evidence from AgentQA, findings from AgentSecurity and AgentDeployment

## Gate checklist
1. Coherence
- No blocker or high-severity backend/frontend contract mismatch remains unaddressed

2. Business rule consistency
- Each RG-BANK-01 to RG-BANK-07 is verified consistent across backend, frontend, and tests

3. Technical debt and duplication
- All identified debt/duplication is documented with owner and severity

4. Alignment with original request
- No unacknowledged gap remains between original user stories/spec and delivered artifacts

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final go/no-go recommendation for AgentOrchestrator's release synthesis


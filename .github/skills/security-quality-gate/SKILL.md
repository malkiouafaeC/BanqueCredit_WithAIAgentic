---
name: security-quality-gate
description: Review security findings for severity resolution, authorization correctness, and safe data handling before release.
---

# Security Quality Gate

## Goal
Run a final security quality gate before release, ensuring no unresolved blocker or high-severity finding remains.

## When to use
- After a security review batch is completed for a feature or release candidate

## Inputs
- Security review plan
- Task findings
- Business rules (RG-BANK-*) from AgentBA

## Gate checklist
1. Findings resolution
- No blocker or high-severity finding remains unaddressed or unacknowledged

2. Authorization correctness
- Status transitions (RG-BANK-05) and refusal enforcement (RG-BANK-07) are protected by correct role checks

3. Secrets and logging
- No secrets/tokens/sensitive data found in logs or client-side storage without justification

4. AI data exposure
- Confirmed no confidential/real client data flows to any AI integration point unmitigated

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final release recommendation


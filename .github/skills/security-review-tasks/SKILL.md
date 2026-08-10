---
name: security-review-tasks
description: Execute atomic security review tasks covering validation, authorization, secrets, logs, and AI data exposure.
---

# Security Review Tasks

## Goal
Execute one security review task at a time and produce concrete, actionable findings.

## When to use
- During execution of SEC-TASK-xxx from a security review plan

## Inputs
- Task ID and review area
- Code/config under review
- Related RG-BANK-* reference

## Review standards
- Input validation/injection: verify Bean Validation coverage and absence of unsafe query construction
- Authorization: verify @PreAuthorize/role checks match the intended actor (ROLE_USER vs ROLE_MANAGER), especially for status transitions (RG-BANK-05) and refusal (RG-BANK-07)
- Secrets: verify jwt.secret and API keys are externalized, never hardcoded or committed
- Logging: verify no password/token/JWT/full personal data appears in logs
- AI data exposure: verify no confidential/real client data is sent to any AI integration point without anonymization

## Output format
1. Task summary (review area)
2. Findings (with severity: blocker/high/medium/low)
3. Remediation recommendation per finding
4. Business rule(s) verified (RG-BANK-*)
5. Residual risks

## Done checks
- Each finding has an explicit severity and remediation recommendation
- No blocker finding is left without an owner or acknowledgment
- Business rule verification is explicit, not assumed


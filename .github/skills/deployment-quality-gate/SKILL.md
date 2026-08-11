---
name: deployment-quality-gate
description: Review deployment readiness covering build success, routing, CORS scope, and post-deployment verification.
---

# Deployment Quality Gate

## Goal
Run a final deployment quality gate before confirming a release as live and healthy.

## When to use
- After backend and frontend deployment tasks are executed for a release

## Inputs
- Deployment plan
- Executed deployment tasks and their verification results
- Security findings from AgentSecurity (if applicable to deployment config)

## Gate checklist
1. Build success
- Backend WAR builds and deploys to target Tomcat without configuration errors
- Frontend production build deploys to Apache successfully

2. Routing
- SPA history mode routing works on direct URL access/refresh (no 404 on client-side routes)

3. CORS
- Only intended frontend origin(s) are allowed; no wildcard in production-like environments

4. Post-deployment verification
- Health endpoint reachable, auth flow works end-to-end, at least one read/write flow succeeds
- No demo/test data reflecting RG-BANK-05/06/07 scenarios exposed

5. Rollback readiness
- Rollback steps are documented and verified feasible

## Output format
- Pass or fail per checklist area
- Blocking issues first
- Non-blocking improvements second
- Final release sign-off recommendation


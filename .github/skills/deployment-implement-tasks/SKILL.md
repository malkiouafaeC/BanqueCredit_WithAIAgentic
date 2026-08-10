---
name: deployment-implement-tasks
description: Execute atomic deployment tasks for backend WAR/Tomcat and frontend Angular/Apache with CORS and verification.
---

# Deployment Implement Tasks

## Goal
Execute one deployment task at a time and confirm it with an explicit verification step.

## When to use
- During execution of DEPLOY-TASK-xxx from a deployment plan

## Inputs
- Task ID and deployment plan details
- Target environment access/configuration

## Implementation standards
- Backend: build Maven WAR (`<packaging>war</packaging>`, `spring-boot-starter-tomcat` scoped provided when needed), deploy to target Tomcat, inject secrets via environment/config outside the WAR
- Frontend: run Angular production build, deploy static assets to Apache, configure SPA history mode fallback (rewrite non-file/non-API routes to index.html)
- CORS: configure backend to allow only the known frontend origin(s) for the target environment
- Document exact commands/configuration used for repeatability

## Test/verification standards
- After each deployment step, run the relevant verification check (health endpoint, auth flow, direct URL route access, static asset load)
- Confirm no demo/test data related to RG-BANK-05/RG-BANK-06/RG-BANK-07 scenarios is exposed in the target environment

## Output format
1. Task summary
2. Commands/configuration applied
3. Verification result
4. Remaining risks
5. Rollback note if applicable

## Done checks
- Build/deploy step completed without errors
- Verification check passed
- No secret leakage in logs/config committed to the repository


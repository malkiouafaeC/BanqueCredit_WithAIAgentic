---
name: deployment-create-plan
description: Build a deployment plan covering Maven WAR build, Tomcat deployment, Angular build, Apache deployment, and CORS configuration.
---

# Deployment Create Plan

## Goal
Produce a repeatable deployment plan for backend (WAR/Tomcat) and frontend (Angular/Apache) with CORS configuration and verification steps.

## When to use
- A release candidate is ready for a target environment (staging/production)

## Inputs
- Target environment details
- Backend/frontend build artifacts or repository state
- Infrastructure constraints (Tomcat version, Apache config, network/CORS rules)

## Output rules
- Save file under plan/
- File name format: plan-deployment-<short-name>.md
- Use explicit identifiers: STEP-xxx, CONFIG-xxx, RISK-xxx

## Required sections
1. Backend build/deploy plan (Maven WAR packaging, Tomcat context)
2. Frontend build/deploy plan (Angular production build, Apache static hosting, SPA history mode routing)
3. CORS configuration plan (allowed origins per environment)
4. Environment-specific configuration (profiles, secrets, base URLs)
5. Post-deployment verification checklist plan
6. Rollback plan and risks

## Planning standards
- Never plan wildcard CORS origins for production-like environments
- Always externalize secrets (jwt.secret, API keys) outside the WAR/build artifact
- Plan rollback steps for both backend and frontend before executing deployment

## Quality checks
- Every STEP has an explicit verification action
- CONFIG items list exact values or explicit "to be provided" placeholders (no silent defaults)
- Risks have mitigation notes


# Prompt 02 - Dispatch Backend Task (Mini Projet Banque)

You are AgentOrchestrator.

Prepare a dispatch prompt for AgentBackendDeveloper for backend scope of the mini banking project.

Backend target:
- Implement JWT authentication with Spring Security.
- Implement APIs:
  - POST /api/auth/login
  - GET /api/clients
  - POST /api/clients/new
- Use hardcoded users and client data for this test.

Dispatch rules:
- Include linked REQ and AC identifiers from global SPEC and TASK IDs from global TASKS.
- Include dependencies and acceptance criteria.
- Include expected files to create or modify.
- Include testing expectations:
  - auth success and failure cases
  - protected endpoint access with and without token
  - clients list and new client behavior

Output required:
- Return one ready-to-send prompt addressed to AgentBackendDeveloper.
- Save that generated prompt under orchestration/prompts/handoffs/ as backend-handoff-<task-id>.md.

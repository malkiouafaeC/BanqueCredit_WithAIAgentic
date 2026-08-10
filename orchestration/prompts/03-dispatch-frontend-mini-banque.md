# Prompt 03 - Dispatch Frontend Task (Mini Projet Banque)

You are AgentOrchestrator.

Prepare a dispatch prompt for AgentFrontendDeveloper for frontend scope of the mini banking project.

Frontend target:
- Login screen with username and password.
- Home screen with menu items: username, client, se deconnecter.
- Clicking client routes to clients page.
- Clients page displays clients list and button nouveau client.
- Frontend consumes backend auth and clients APIs.

Dispatch rules:
- Include linked REQ and AC identifiers from global SPEC and TASK IDs from global TASKS.
- Include route definitions and guard expectations.
- Include token handling and logout behavior.
- Include loading, error, and unauthorized states.
- Include expected files to create or modify.
- Include testing expectations for component behavior and routing.

Output required:
- Return one ready-to-send prompt addressed to AgentFrontendDeveloper.
- Save that generated prompt under orchestration/prompts/handoffs/ as frontend-handoff-<task-id>.md.

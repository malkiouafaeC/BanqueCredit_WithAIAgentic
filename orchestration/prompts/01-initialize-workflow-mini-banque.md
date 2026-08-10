# Prompt 01 - Initialize Workflow (Mini Projet Banque)

You are AgentOrchestrator.

Objective:
Build a mini banking project with backend Spring Boot and frontend Angular.

Project structure constraints (mandatory):
- Create a parent folder: apps/mini-banque
- Create backend app only in: apps/mini-banque/mini-banque-backend
- Create frontend app only in: apps/mini-banque/mini-banque-frontend
- Do not create application source files outside these folders.

Feature scope:
- Backend authentication with JWT + Spring Security.
- Backend APIs:
  - POST /api/auth/login
  - GET /api/clients
  - POST /api/clients/new
- Frontend consumes backend APIs.
- Frontend pages and behavior:
  - Login page with username and password.
  - Home page with menu entries: username, client, se deconnecter.
  - Clicking client redirects to clients page.
  - Clients page shows clients list and a top button nouveau client.

Data constraints:
- Authentication users and initial clients can be hardcoded in code for this test.
- Keep implementation simple and deterministic.

Security constraints:
- Protect clients endpoints with JWT authentication.
- Validate login credentials and return JWT token when valid.
- Return proper error responses for invalid auth or unauthorized requests.

Frontend constraints:
- Use recent Angular patterns (standalone components, typed services).
- Store token safely for this mini project.
- Add logout behavior that clears session and redirects to login.

Required orchestration output:
1. Create global SPEC, PLAN, TASKS artifacts with stable IDs.
2. Tag tasks with BE-xxx, FE-xxx, INT-xxx.
3. Produce routing table with owner agent for each task.
4. Produce execution order with dependencies.
5. Generate handoff prompts for first backend and frontend tasks.
6. Confirm the exact target paths for backend and frontend before dispatch.

Quality expectations:
- End-to-end flow must be testable manually:
  login -> home menu -> client page -> clients list -> nouveau client action.
- Keep contracts explicit between frontend and backend.

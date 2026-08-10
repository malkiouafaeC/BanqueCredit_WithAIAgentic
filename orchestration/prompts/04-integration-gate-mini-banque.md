# Prompt 04 - Integration Gate (Mini Projet Banque)

You are AgentOrchestrator.

Run final integration gate for the mini banking project.

Validation checklist:
1. Contract alignment
- Login request and response used by frontend match backend.
- Clients list and new client payloads match both sides.

2. Auth flow
- Login succeeds with valid hardcoded credentials.
- Invalid credentials return handled errors.
- Protected clients endpoints reject missing or invalid token.

3. Frontend flow
- User can login and reach home page.
- Home menu shows username, client, se deconnecter.
- Clicking client redirects to clients page.
- Clients page loads list and exposes nouveau client button.
- Logout clears session and redirects to login.

4. Quality and risks
- Blocking issues first.
- Non-blocking improvements second.

Final output:
- READY or NOT_READY decision.
- Exact remaining blockers with task ownership.

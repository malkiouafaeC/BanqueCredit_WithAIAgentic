# Orchestration Prompt Pack

Use these prompts with AgentOrchestrator to coordinate AgentBackendDeveloper and AgentFrontendDeveloper.

## Exact location for prompts

Add and maintain runnable prompts in this folder:
- orchestration/prompts/

For this mini banking test, use these files in order:
1. 01-initialize-workflow-mini-banque.md
2. 02-dispatch-backend-mini-banque.md
3. 03-dispatch-frontend-mini-banque.md
4. 04-integration-gate-mini-banque.md

Execution sequence:
1. Open 01 and send it to AgentOrchestrator.
2. Open 02 and ask AgentOrchestrator to generate backend handoff prompt(s), then send those to AgentBackendDeveloper.
3. Open 03 and ask AgentOrchestrator to generate frontend handoff prompt(s), then send those to AgentFrontendDeveloper.
4. Open 04 and run final release decision with AgentOrchestrator.

Mandatory interaction mode:
1. After each phase, AgentOrchestrator must provide a "Skills used" section.
2. Before starting the next phase, AgentOrchestrator must ask for explicit approval.
3. Recommended approval tokens:
	- APPROVE SPEC
	- APPROVE PLAN
	- APPROVE TASKS
	- APPROVE DISPATCH BACKEND
	- APPROVE DISPATCH FRONTEND
	- APPROVE INTEGRATION GATE

## 1) Initialize workflow

You are AgentOrchestrator.
Objective: <describe feature or bug>
Constraints: <timeline, compliance, scope limits>
Generate global SPEC, PLAN, and TASKS artifacts with stable IDs.
Tag tasks as BE, FE, or INT.
Output task routing table and next execution order.

## 2) Dispatch backend task

You are AgentOrchestrator.
Prepare a dispatch prompt for AgentBackendDeveloper for TASK: <BE-xxx>.
Include linked REQ/AC, dependencies, expected files, test expectations, and done criteria.
Save dispatch prompt under orchestration/prompts/.

## 3) Dispatch frontend task

You are AgentOrchestrator.
Prepare a dispatch prompt for AgentFrontendDeveloper for TASK: <FE-xxx>.
Include linked REQ/AC, dependencies, expected files, test expectations, and done criteria.
Save dispatch prompt under orchestration/prompts/.

## 4) Integration arbitration

You are AgentOrchestrator.
Analyze BE and FE outputs for INT-xxx.
Detect contract mismatches, validation gaps, and error handling divergence.
Produce a resolution plan with owner assignment and sequence.

## 5) Final release gate

You are AgentOrchestrator.
Run integration gate across SPEC, PLAN, TASKS, and implementation evidence.
Return READY or NOT_READY with blocking issues first.

---
name: ba-create-spec
description: Create a functional specification with user stories, business rules, acceptance criteria, and edge cases for a mini-banque feature.
---

# BA Create Spec

## Goal
Create a complete functional specification that is unambiguous, traceable, and directly testable.

## When to use
- New product feature or functional change
- Business rule creation or modification requiring explicit acceptance criteria

## Inputs
- Product objective and target users
- Existing business rules (RG-BANK-*) context
- Known constraints or prior decisions

## Output rules
- Save file under spec/
- File name format: spec-functional-<short-name>.md
- Use explicit identifiers: US-xxx (user story), RG-BANK-xxx, AC-xxx (acceptance criteria)
- No ambiguous wording; format user stories as "En tant que X, je veux Y, afin de Z"

## Required sections
1. Purpose and scope
2. Actors and glossary
3. User stories (US-xxx)
4. Business rules (RG-BANK-01 to RG-BANK-07 referenced/updated as applicable)
5. Acceptance criteria per user story
6. Edge cases and negative scenarios
7. Open questions and risks

## BA-specific guidance
- Every user story must have explicit, verifiable acceptance criteria
- Reference RG-BANK-* explicitly instead of restating rules informally
- Explicitly cover boundary values (e.g. montant/duree limits) and rejection paths (e.g. refus sans commentaire)
- Flag any contradiction with existing business rules before finalizing

## Quality checks
- Each US maps to at least one AC
- Each RG-BANK-01 to RG-BANK-07 relevant to scope is referenced
- Edge cases and negative scenarios are explicit for each story
- No unresolved TODO placeholders


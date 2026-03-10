---
name: plan-issue
description: Plan the implementation of a GitHub issue
argument-hint: [ issue-number ]
allowed-tools: Bash(gh *)
---

## Plan implementation of issue #$ARGUMENTS

### Step 1: Read the issue

```bash
gh issue view $ARGUMENTS
```

### Step 2: Explore the codebase

Key places to check:

- `spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/` — main source
- `spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/service/` — core services
- `spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/jsonschemamodel/` — schema models
- `spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/metamodel/` — metadata models
- `spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/config/` — configuration
- `spring-boot-config-json-schema-starter/src/test/` — tests

### Step 3: Write the plan

Create a detailed implementation plan:

1. **What needs to change** — list specific files and what changes are needed
2. **New files** — any new classes, configs, or tests needed
3. **Testing strategy** — what tests to add or modify
4. **Risk assessment** — what could break, edge cases to handle
5. **Execution order** — the sequence of changes

### Step 4: Post the plan

Post the plan as a comment on the issue:

```bash
gh issue comment $ARGUMENTS --body "<plan>"
```

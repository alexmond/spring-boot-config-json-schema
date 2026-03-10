---
name: learn
description: >
  Save a learning to project memory. Invoke automatically (without user asking) whenever
  a task required more than one fix cycle to resolve — e.g. a test failed and needed a
  second attempt, a compile error required a correction, an API behaved unexpectedly, or
  an assumption proved wrong mid-task. Also invoke when the user explicitly asks to
  remember something. Do NOT invoke for routine single-pass work.
argument-hint: "[topic] [what you learned]"
---

## Save a learning to project memory

$ARGUMENTS

### Determine the learning

If triggered **automatically** (multi-cycle resolution), synthesise the learning from the conversation:

- What was the root cause of the extra cycle(s)?
- What assumption or gap in knowledge caused the first attempt to fail?
- What is the correct approach / API / behaviour?
- What should be checked first next time to avoid the same detour?

If triggered **by the user**, record exactly what they stated in `$ARGUMENTS`.

### Steps

1. Read the current memory file:
   `/Users/alex.mondshain/.claude/projects/-Users-alex-mondshain-IdeaProjects-spring-boot-config-json-schema/memory/MEMORY.md`

2. Check whether a relevant topic file already exists in that same directory (e.g. `dependencies.md`, `testing.md`,
   `debugging.md`). If so, read it too.

3. Decide where to write:
    - Short, self-contained insight that fits an existing `MEMORY.md` section → add it there (keep file ≤ 200 lines).
    - Detailed or topic-specific learning → append to or create a dedicated topic file, then add/update a one-line
      reference in `MEMORY.md`.

4. Write in concise, actionable form:
    - Bullet points, not prose.
    - Lead with *what to do / what to check*, follow with *why*.
    - If it supersedes an existing note, update or remove the old one.

5. Confirm to the user what was saved and where (one line is enough).

### What to save

- Root causes of multi-cycle failures and the correct fix
- Non-obvious library behaviours or API quirks discovered during the task
- Version constraints and compatibility issues
- Architectural decisions and their rationale
- Workflow or tool preferences the user has stated
- Patterns confirmed to work that aren't obvious from the code

### What NOT to save

- Routine outcomes that worked first time
- Temporary task state or in-progress work
- Anything already covered verbatim in `CLAUDE.md`
- Guesses or conclusions that were not verified by a passing test or explicit confirmation

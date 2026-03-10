---
name: work
description: Start a focused work session on a task
disable-model-invocation: true
argument-hint: "[task description]"
allowed-tools: Bash(bash *), Bash(git *)
---

## Work session: $ARGUMENTS

### Setup

1. Check current branch and status:
   ```bash
   git status
   git log --oneline -5
   ```

2. Understand the task from `$ARGUMENTS`

### Work loop

Repeat until the task is complete:

1. **Read** relevant source files to understand current state
2. **Implement** changes following project standards (Java 17, Lombok, tabs, spring-javaformat)
3. **Format** code:
   ```bash
   bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" io.spring.javaformat:spring-javaformat-maven-plugin:apply -f pom.xml -Pdefault -q
   ```
4. **Validate** (checkstyle + PMD):
   ```bash
   bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" validate -f pom.xml -pl spring-boot-config-json-schema-starter
   ```
5. **Test**:
   ```bash
   bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -f pom.xml -pl spring-boot-config-json-schema-starter
   ```
6. Fix any failures and repeat

### Wrap up

Report what was done, what files changed, and any remaining issues.

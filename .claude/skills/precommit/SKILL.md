---
name: precommit
description: Run pre-commit checks (format, checkstyle, PMD, tests)
disable-model-invocation: true
allowed-tools: Bash(bash *), Bash(git *)
---

## Pre-commit checks

Run all validation steps before committing.

### Step 1: Format code

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" io.spring.javaformat:spring-javaformat-maven-plugin:apply -f pom.xml -Pdefault -q
```

### Step 2: Validate (checkstyle + PMD)

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" validate -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Step 3: Run tests

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Step 4: Report

- If all steps pass, report success
- If any step fails, report which step failed and show the error details
- If formatting changed files, list them so they can be staged

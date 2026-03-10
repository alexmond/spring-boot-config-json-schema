---
name: push
description: Validate, commit, and push changes to remote
disable-model-invocation: true
argument-hint: "[commit message]"
allowed-tools: Bash(git *), Bash(bash *)
---

## Push changes

### Step 1: Run format + validate

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" io.spring.javaformat:spring-javaformat-maven-plugin:apply -f pom.xml -Pdefault -q
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" validate -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Step 2: Run tests

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Step 3: Stage and commit

```bash
git add <changed-files>
git commit -m "$ARGUMENTS

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

### Step 4: Push

```bash
git push -u origin HEAD
```

If any step fails, fix the issue and retry. Report the result when done.

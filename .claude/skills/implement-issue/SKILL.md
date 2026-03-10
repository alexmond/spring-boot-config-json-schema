---
name: implement-issue
description: Implement a GitHub issue with branch and pull request workflow
disable-model-invocation: true
argument-hint: [ issue-number ]
allowed-tools: Bash(gh *), Bash(git *), Bash(bash *)
---

## Implement GitHub issue #$ARGUMENTS

Follow this workflow for every code change:

### Step 1: Read the issue

```bash
gh issue view $ARGUMENTS
```

### Step 2: Create a feature branch

```bash
git checkout main
git pull
git checkout -b feature/$ARGUMENTS-<short-description>
```

### Step 3: Implement the changes

- Read relevant source files before making modifications
- Follow project coding standards (Java 17, Lombok, tabs, spring-javaformat)
- Keep changes focused on the issue requirements

### Step 4: Run tests

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -f pom.xml
```

Fix any failures before proceeding.

### Step 5: Commit and push

```bash
git add <changed-files>
git commit -m "<descriptive message>

Closes #$ARGUMENTS

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
git push -u origin HEAD
```

### Step 6: Create pull request

```bash
gh pr create --title "<concise title>" --body "## Summary
<bullet points>

## Test plan
<how to verify>

Closes #$ARGUMENTS

🤖 Generated with [Claude Code](https://claude.com/claude-code)"
```

Report the PR URL when done.

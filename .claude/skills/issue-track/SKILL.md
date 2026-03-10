---
name: issue-track
description: Create or update a GitHub issue for tracking work
disable-model-invocation: true
argument-hint: "[title] [description]"
allowed-tools: Bash(gh *)
---

## Create/update a GitHub issue

$ARGUMENTS

### Create a new issue

```bash
gh issue create --title "<title>" --body "<description>"
```

### View an existing issue

```bash
gh issue view $ARGUMENTS
```

### List open issues

```bash
gh issue list
```

### Close an issue

```bash
gh issue close $ARGUMENTS
```

### Add a comment

```bash
gh issue comment $ARGUMENTS --body "<comment>"
```

Report the issue URL when done.

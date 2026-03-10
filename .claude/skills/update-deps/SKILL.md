---
name: update-deps
description: Check for and update Maven dependency versions
disable-model-invocation: true
allowed-tools: Bash(bash *), Bash(git *)
---

## Check and update dependencies

### Step 1: Check for updates

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" versions:display-dependency-updates -f pom.xml -Pdefault 2>&1 | tee /tmp/schema-dep-updates.txt
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" versions:display-plugin-updates -f pom.xml -Pdefault 2>&1 | tee /tmp/schema-plugin-updates.txt
```

### Step 2: Analyze

Review the output and categorize updates:

- **Safe**: Patch versions (x.y.Z) — usually safe to apply
- **Minor**: Minor versions (x.Y.0) — review changelog
- **Major**: Major versions (X.0.0) — may have breaking changes

### Step 3: Apply updates

For each update to apply:

1. Update the version property in the root `pom.xml`
2. Run the full build to verify compatibility:
   ```bash
   bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" clean install -f pom.xml -Pdefault
   ```
3. If the build fails, revert and report the issue

### Step 4: Report

List all available updates and which ones were applied.

---
name: mvn-build
description: Build the project with Maven
disable-model-invocation: true
argument-hint: "[module]"
allowed-tools: Bash(bash *)
---

## Build the project

Run the Maven build for the project. If an argument is provided, build only that module.

### Full build (all modules)

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" clean install -f pom.xml -Pdefault
```

### Module build (if argument provided)

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" clean install -f pom.xml -pl $ARGUMENTS
```

### Modules

- `spring-boot-config-json-schema-starter` — Core library (the published artifact)
- `spring-boot-json-schema-sample` — Sample Spring Boot web app (profile `default` only)
- `spring-boot-json-schema-generic-sample` — Minimal sample (profile `default` only)

Run the appropriate build command. Report any compilation errors or test failures clearly.

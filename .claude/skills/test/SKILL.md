---
name: test
description: Run tests for the project
disable-model-invocation: true
argument-hint: "[module] [TestClass#method]"
allowed-tools: Bash(bash *)
---

## Run tests

Run tests based on the provided arguments.

### No arguments — run all tests

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Specific test class (e.g., `/test SampleJsonSchemaGeneratorTests`)

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -Dtest=$ARGUMENTS -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Specific test method (e.g., `/test SampleJsonSchemaGeneratorTests#testGenerateSchema`)

```bash
bash "/Users/alex.mondshain/Library/Application Support/JetBrains/IntelliJIdea2025.3/plugins/maven/lib/maven3/bin/mvn" test -Dtest=$ARGUMENTS -f pom.xml -pl spring-boot-config-json-schema-starter
```

### Key test classes

- `SampleJsonSchemaGeneratorTests` — Main integration test: generates schema, validates against JSON Schema draft-2020-12
- `SanityJsonSchemaGeneratorTests` — Sanity checks
- `JsonSchemaBuilderTest` — Unit tests for schema construction
- `TestYamlFileVsSchema` — Validates YAML files against generated schema
- `SchemaToAsciiDocTests` — Tests AsciiDoc documentation generation from schema

Report test results clearly. On failure, show the failing test name, assertion message, and relevant stack trace.

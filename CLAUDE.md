# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot starter library that auto-generates JSON Schema (draft 2020-12) from `@ConfigurationProperties` metadata. Used for IDE autocomplete, YAML/JSON config validation, and documentation generation. Published to Maven Central under `org.alexmond:spring-boot-config-json-schema-starter`.

## Build & Test Commands

```bash
# Build all modules (starter + samples)
mvn clean install -Pdefault

# Build only the starter (default without profile)
mvn clean install

# Run all tests
mvn test

# Run a single test class
mvn test -pl spring-boot-config-json-schema-starter -Dtest=SampleJsonSchemaGeneratorTests

# Run a single test method
mvn test -pl spring-boot-config-json-schema-starter -Dtest=SampleJsonSchemaGeneratorTests#testGenerateSchema
```

JaCoCo enforces **80% minimum line coverage** (`BUNDLE` rule on the starter). The `check` goal
runs at `verify`, so `mvn package` skips the gate. The CI build command, run on JDK 17, 21 and 25:
```bash
mvn -B verify --file pom.xml -Pdefault --no-transfer-progress
```

There is no Maven wrapper in this repo; use a local `mvn`.

## Versioning & branches

This repo follows the shared Boot-starter standard (same as `spring-boot-actuator-extensions`,
`gotmpl4j-spring-boot`, `notify4j-spring-boot`).

- **Version = `<boot-version>.<n>`**, e.g. `4.1.1.2` builds on Spring Boot 4.1.1. `<n>` starts at
  **1** and resets to 1 on each new Boot patch. All POMs move in lockstep. Tags have no `v` prefix.
- **This branch is the Spring Boot 4.1 line** (parent `spring-boot-starter-parent` 4.1.1). The
  parent version in the root `pom.xml` is the source of truth.
- **`main` is always the newest Boot line.** Older lines live on `<major>.<minor>` branches:
  `4.0` (maintained) and `3.5` (end-of-life, terminal at Boot 3.5.16).
- When `main` moves to a new Boot **minor**, cut the outgoing line to its own
  `<major>.<minor>` branch **first**, from the pre-bump head.
- A fix that applies to every line goes to each branch, one PR each. Each branch keeps only its
  own changelog entries.
- Release with `.github/workflows/maven_release.yml` (manual; inputs `branch`,
  `releaseVersion`, `nextVersion`). It only changes POM versions. Run the `release-prep` skill
  before it and `update-docs-hub` after it.

Use the **`boot-upgrade` skill** to survey branches against the latest Boot patch.

## Module Structure

- **`spring-boot-config-json-schema-starter/`** — Core library (the published artifact). Contains all schema generation logic.
- **`spring-boot-json-schema-sample/`** — Sample Spring Boot web app with REST endpoints for schema access (profile `default` only).
- **`spring-boot-json-schema-generic-sample/`** — Minimal sample (profile `default` only).

## Architecture

**Schema generation flow:**

`ConfigSchemaStarter` (auto-config) wires up all beans → `JsonSchemaService` orchestrates generation → `ConfigurationPropertyCollector` gathers properties from Spring's `spring-configuration-metadata.json` → `JsonSchemaBuilder` constructs the schema using `TypeMappingService` for Java→JSON Schema type mapping → output as JSON or YAML.

**Key packages** (under `org.alexmond.config.json.schema`):

| Package | Purpose |
|---------|---------|
| `service/` | Core services: `JsonSchemaService` (entry point, caching), `JsonSchemaBuilder` (schema construction), `TypeMappingService` (type mapping), `ConfigurationPropertyCollector` |
| `metamodel/` | Data models for Spring config metadata: `Property`, `Group`, `BootConfigMeta`, `Hint`, `Deprecation` |
| `jsonschemamodel/` | JSON Schema output models: `JsonSchemaRoot`, `JsonSchemaProperties`, `JsonSchemaType`, `JsonSchemaFormat` |
| `metaextension/` | `BootConfigMetaLoader` — loads/merges `spring-configuration-metadata.json` files from classpath |
| `config/` | `JsonConfigSchemaConfig` — `@ConfigurationProperties(prefix = "json-config-schema")` settings |

**Configuration prefix:** `json-config-schema.*` (see `JsonConfigSchemaConfig.java` for all options including `schemaSpec`, `useOpenapi`, `useValidation`, `enableDefinitionRefs`, `excludeClasses`, etc.)

## Testing

Tests live in `spring-boot-config-json-schema-starter/src/test/`. The test application (`BootConfigJsonSchemaApplication`) uses `@ActiveProfiles("test")` with config in `src/test/resources/application-test.yaml`.

Key test classes:
- `SampleJsonSchemaGeneratorTests` — Main integration test: generates schema, validates against JSON Schema draft-2020-12 using `networknt/json-schema-validator`
- `JsonSchemaBuilderTest` — Unit tests for schema construction
- `TestYamlFileVsSchema` — Validates YAML files against generated schema
- `SchemaToAsciiDocTests` — Tests AsciiDoc documentation generation from schema

Test config classes in `org.alexmond.sample.test.config` provide sample `@ConfigurationProperties` beans (nested objects, enums, maps, validation annotations, deep nesting).

## Tech Stack

- Java 17, Spring Boot 4.1.x (currently 4.1.1 — see the parent in `pom.xml`), Jackson 3 (`tools.jackson` packages)
- Jackson formats: YAML, CBOR, XML
- OpenAPI/Swagger annotations (`swagger-annotations-jakarta`)
- Apache Commons Text for string operations
- Schema validation in tests: `com.networknt:json-schema-validator` (version in the `json-schema-validator.version` property)

## Code Style

- **Tab indentation** (enforced by spring-javaformat 0.0.47)
- **spring-javaformat**: Run `spring-javaformat:apply` before committing — enforces braces on all blocks, lambda parentheses, `ex` catch variables, ternary parentheses, no star imports
- **Checkstyle**: SpringChecks with suppressions in `checkstyle-suppressions.xml`
- **PMD**: Custom ruleset in `pmd-ruleset.xml`
- Lombok usage throughout
- Jackson imports use `tools.jackson.*` (Jackson 3 namespace, not `com.fasterxml.jackson`)
- Code quality plugins are configured on `spring-boot-config-json-schema-starter` module only

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Spring Boot **starter library** (published to Maven Central as
`org.alexmond:spring-boot-config-json-schema-starter`) that generates a JSON Schema (or YAML)
describing an application's configuration properties. The schema is built from Spring Boot's
`spring-configuration-metadata.json` files on the classpath, enriched with reflection, Bean
Validation and OpenAPI (`@Schema`) annotations. IDEs use it for `application.yaml` completion
and validation; it can also be served from a REST or Actuator endpoint.

## This branch: Spring Boot 3.5, end-of-life

- **This is the `3.5` maintenance branch.** It builds on Spring Boot **3.5.16** (see the parent
  in the root `pom.xml`).
- **Boot 3.x is end-of-life for open source.** 3.5.16 is the last patch, so this line is
  terminal: "up to date" here means finished. Do not add features. Only a serious fix justifies
  another release (`3.5.16.<n>`).
- New work goes to `main` (newest Boot line) and, where it applies, to `4.0`.

## Versioning & branches

This repo follows the shared Boot-starter standard (same as `spring-boot-actuator-extensions`,
`gotmpl4j-spring-boot`, `notify4j-spring-boot`).

- **Version = `<boot-version>.<n>`**, e.g. `3.5.16.2` builds on Spring Boot 3.5.16. `<n>`
  starts at **1** and resets to 1 on each new Boot patch. All POMs move in lockstep. Tags have
  no `v` prefix.
- **`main` is always the newest Boot line.** Older lines live on `<major>.<minor>` branches:
  `4.0` (maintained) and `3.5` (this branch).
- When `main` moves to a new Boot **minor**, the outgoing line is cut to its own
  `<major>.<minor>` branch **first**, from the pre-bump head.
- A fix that applies to every line goes to each branch, one PR each. Each branch keeps only its
  own changelog entries.

## Build & test

There is **no Maven wrapper**; use a local `mvn`. The root POM's `<modules>` lists only the
starter. The `default` profile adds the two samples.

```bash
mvn -B verify -Pdefault --no-transfer-progress   # what CI runs (JDK 17, 21 and 25)
mvn verify                                       # starter only
mvn -pl spring-boot-config-json-schema-starter test -Dtest=SanityJsonSchemaGeneratorTests
mvn -pl spring-boot-config-json-schema-starter test -Dtest=SanityJsonSchemaGeneratorTests#contextLoads
```

- Java 17 target.
- JaCoCo enforces **80% line coverage** (`BUNDLE` rule) on the starter. The `check` goal runs at
  `verify`, so `mvn package` skips the gate.
- **No spring-javaformat, Checkstyle or PMD on this branch.** They exist on `main` and `4.0`
  only and are not worth adding to an end-of-life line. Keep the existing 4-space style; do not
  reformat files.
- `<proc>full</proc>` on `maven-compiler-plugin` keeps Lombok and the configuration processor
  running on JDK 23+.
- Running the tests rewrites tracked files under `docs/modules/ROOT/attachments/` and drops
  `property-doc.adoc`, `gen.json` and `sample-schema*.json` / `.yaml` files into the module
  directories. Do not commit those by accident.

## Differences from `main` / `4.0`

- **Jackson 2** (`com.fasterxml.jackson.*`), not Jackson 3 (`tools.jackson.*`). Do not copy
  imports across branches without changing them.
- `com.networknt:json-schema-validator` 2.x in tests (3.x on the Boot 4 lines).
- No quality plugins (see above).

## Architecture

All library code is in
`spring-boot-config-json-schema-starter/src/main/java/org/alexmond/config/json/schema/`.
Auto-configuration is registered in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and points at
`ConfigSchemaStarter`, which wires every bean by hand.

Generation flow (entry point `JsonSchemaService`):

1. `JsonSchemaService.collectMetadata()` reads every
   `META-INF/spring-configuration-metadata.json` on the classpath through `BootConfigMetaLoader`
   and merges them into one map keyed by property path. The `metamodel/` package mirrors that
   metadata format (`Property`, `Group`, `Hint`, `Deprecation`).
2. `ConfigurationPropertyCollector.collectIncludedPropertyNames()` decides which prefixes to
   include: live `@ConfigurationProperties` beans, environment keys, and the configured
   `additionalProperties` (default `logging`).
3. `JsonSchemaBuilder.buildSchema(meta, included)` builds the nested tree of
   `JsonSchemaProperties` (`jsonschemamodel/` is the emitted schema shape). It handles leaf
   types, arrays, maps, enums, nested types by reflection, and guards against cycles.
4. `JsonSchemaService` serializes to JSON or YAML and caches the built `JsonSchemaRoot`.

Helpers: `TypeMappingService` (Java type → JSON Schema type/format), `MissingTypeCollector`
(records unmapped types), `JsonSchemaBuilderHelper` (OpenAPI, Bean Validation, hints,
deprecation, enum values), `DefinitionsHelper` (base `$defs`).

Refs: `enableDefinitionRefs` (default `true`) emits `#/$defs/<type>` references;
`enableAnchorRefs` (default `false`) emits `#<anchor>` references instead. Use one or the other.

All runtime options are on `JsonConfigSchemaConfig`
(`@ConfigurationProperties(prefix = "json-config-schema")`).

## Modules

- `spring-boot-config-json-schema-starter` — the published library. Its `src/test/` holds the
  test application and sample `@ConfigurationProperties` classes.
- `spring-boot-json-schema-sample` — sample web app (profile `default` only, not published).
- `spring-boot-json-schema-generic-sample` — minimal sample (profile `default` only, not published).

## Releasing

`.github/workflows/maven_release.yml` is a manual workflow with inputs `branch`,
`releaseVersion` and `nextVersion`. For this line pass `branch=3.5`. It sets the POM versions,
verifies, tags, deploys to Maven Central with `-Prelease`, pushes the next `-SNAPSHOT` and
creates a GitHub release. It only touches POM versions, so the README, changelog and docs must
already be committed. Run the `release-prep` skill before it and `update-docs-hub` after it. Do
not run the `release` profile locally.

## Docs

The Antora site is under `docs/` and is published at
<https://www.alexmond.org/spring-boot-config-json-schema-starter/current/>. The changelog has
one source, `docs/modules/ROOT/partials/changelog.adoc`; the root `CHANGELOG.adoc` and the
README include it.

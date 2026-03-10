---
name: codestyle
description: Project coding standards and conventions for Java 17 with Lombok, spring-javaformat, checkstyle, and PMD
user-invocable: false
---

## Coding Standards

### Formatting

- **Indentation**: Tabs (enforced by spring-javaformat 0.0.47)
- **Formatter**: `io.spring.javaformat:spring-javaformat-maven-plugin` — run `spring-javaformat:apply` before committing
- **Checkstyle**: Uses `io.spring.javaformat.checkstyle.SpringChecks` with custom suppressions
- **PMD**: Custom ruleset at `pmd-ruleset.xml`

### Spring-javaformat Rules (enforced)

- **Braces required**: All `if`, `else`, `for`, `while`, `do` must use `{}` even for single-line bodies
- **Lambda parentheses**: Single-parameter lambdas must have parentheses: `(x) ->` not `x ->`
- **Lambda blocks**: Use expression body when possible: `(x) -> x.toString()` not `(x) -> { return x.toString(); }`
- **Catch variables**: Use `ex` not single-letter `e`: `catch (Exception ex)`
- **Ternary parentheses**: Wrap ternary conditions: `(a != null) ? a : b` not `a != null ? a : b`
- **No star imports**: Use explicit imports, never `import java.util.*`
- **Newline at EOF**: Files must end with a newline

### Checkstyle Suppressions (in `checkstyle-suppressions.xml`)

These checks are suppressed project-wide:
- `SpringHeaderCheck` — no license header requirement
- `JavadocPackage`, `JavadocType`, `JavadocMethod`, `JavadocVariable` — Javadoc not enforced
- `MissingJavadocType`, `MissingJavadocMethod` — Javadoc not enforced
- `SpringJavadoc` — Javadoc description casing not enforced
- `RegexpSinglelineJava` — no regex line restrictions
- `SpringImportOrder` — import ordering not enforced
- `RequireThis` — `this.` prefix not required
- `SpringTestFileName` — test file naming flexible

### PMD Exclusions (in `pmd-ruleset.xml`)

Key exclusions — these patterns are acceptable in this project:

**Best Practices**: `GuardLogStatement`, `LiteralsFirstInComparisons`, `UnusedAssignment`,
`DoubleBraceInitialization`, `NonExhaustiveSwitch`, `RedundantFieldInitializer`

**Code Style**: `LocalVariableNamingConventions`, `PrematureDeclaration`, `ConfusingTernary`,
`UseLocaleWithCaseConversions`, `UseExplicitTypes`, `FieldNamingConventions`

**Design**: `CognitiveComplexity`, `NcssCount`, `SimplifyConditional`, `CollapsibleIfStatements`,
`GodClass`, `DataClass`, `TooManyFields`

**Error Prone**: `ReturnEmptyCollectionRatherThanNull`, `AvoidDuplicateLiterals`, `NullAssignment`

### Naming

- Classes: `PascalCase`
- Methods/variables: `camelCase`
- Constants: `UPPER_SNAKE_CASE`

### Lombok

- `@Getter`/`@Setter` for fields
- `@Data` for POJOs/DTOs
- `@Builder` with `.toBuilder()` for immutable construction
- `@Slf4j` for logging
- `@NoArgsConstructor`, `@AllArgsConstructor` as needed

### Jackson 3

- Imports use `tools.jackson.*` namespace (not `com.fasterxml.jackson`)
- Exception: `com.fasterxml.jackson.annotation.*` annotations still use old namespace
- Jackson 3's `readValue()` no longer throws checked `IOException`

### Dependencies

- Manage versions in root `pom.xml` `<properties>`
- Spring Boot parent manages most versions
- Only the starter module has real source code and code quality plugins

### Testing

- JUnit 5 with `Assertions`
- Use `@ParameterizedTest` to avoid code duplication
- Test config classes in `org.alexmond.sample.test.config`
- JaCoCo enforces 80% minimum line coverage

### Running code quality checks

```bash
# Format all code
bash "/path/to/mvn" io.spring.javaformat:spring-javaformat-maven-plugin:apply -f pom.xml -Pdefault -q

# Validate (checkstyle + PMD)
bash "/path/to/mvn" validate -f pom.xml -pl spring-boot-config-json-schema-starter

# Full build with tests
bash "/path/to/mvn" clean install -f pom.xml -Pdefault
```

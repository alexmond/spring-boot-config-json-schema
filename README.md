[![Maven Central](https://img.shields.io/maven-central/v/org.alexmond/spring-boot-config-json-schema-starter.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.alexmond/spring-boot-config-json-schema-starter)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/alexmond/spring-boot-config-json-schema/maven.yml)](https://github.com/alexmond/spring-boot-config-json-schema/actions)

# Spring Boot Config JSON Schema Generator

A Spring Boot starter library that automatically generates JSON Schema documentation for your application's
configuration properties. It simplifies the process of documenting and validating configuration by generating JSON
Schema from your Spring Boot configuration classes.

A full guide can be found in our [docs](https://alexmond.github.io/spring-boot-config-json-schema/).

A good article on this
subject: [Springboot Config Documentation, Two Ways With IntelliJ IDEA](https://themightyprogrammer.dev/article/2ways-spring-configuration)

## Quick Start

### Maven Dependencies

#### For Testing

Add the following dependency to your `pom.xml` when using the generator in tests:

```xml

<dependency>
    <groupId>org.alexmond</groupId>
    <artifactId>spring-boot-config-json-schema-starter</artifactId>
    <version>0.0.8</version>
    <scope>test</scope>
</dependency>
```

```java title=SampleJsonSchemaGeneratorTests.java

@SpringBootTest
@Slf4j
class SampleJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Test
    void generateJsonSchema() {

        var jsonConfigSchemaJson = jsonSchemaService.generateFullSchemaJson();
        var jsonConfigSchemaYaml = jsonSchemaService.generateFullSchemaYaml();
        log.info("Writing json schema");
        Files.writeString(Paths.get("config-schema.json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);

        log.info("Writing yaml schema");
        Files.writeString(Paths.get("config-schema.yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);
    }

}
```

### Using as REST API Endpoint

To expose the JSON schema via a REST endpoint (similar to Swagger API docs), add the following dependency to your
`pom.xml`:

```xml

<dependency>
    <groupId>org.alexmond</groupId>
    <artifactId>spring-boot-config-json-schema-starter</artifactId>
    <version>0.0.8</version>
</dependency>
```

Then create a REST controller:

```java title=GenerateJsonSchema.java

@RestController
@Slf4j
public class GenerateJsonSchema {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @GetMapping("/config-schema")
    public String getConfigSchema() {
        return jsonSchemaService.generateFullSchemaJson();
    }

    @GetMapping("/config-schema.yaml")
    public String getConfigSchemaYaml() {
        return jsonSchemaService.generateFullSchemaYaml();
    }

}
```

### Using as Actuator Endpoint

To expose the JSON schema via an Actuator endpoint, add the following dependency to your
`pom.xml`:

```xml

<dependency>
    <groupId>org.alexmond</groupId>
    <artifactId>spring-boot-config-json-schema-starter</artifactId>
    <version>0.0.8</version>
</dependency>
```

Then create Actuator endpoint:

```java title=ConfigSchemaEndpoint.java

@Component
@Endpoint(id = "config-schema")
@RequiredArgsConstructor
public class ConfigSchemaEndpoint {

    private final JsonSchemaService jsonSchemaService;

    @ReadOperation
    public String schema() {
        return jsonSchemaService.generateFullSchemaJson();
    }
}
```

```java title=ConfigSchemaYamlEndpoint.java

@Component
@Endpoint(id = "config-schema.yaml")
@RequiredArgsConstructor
public class ConfigSchemaYamlEndpoint {

    private final JsonSchemaService jsonSchemaService;

    @ReadOperation
    public String schema() {
        return jsonSchemaService.generateFullSchemaYaml();
    }
}
```

enable it in application.yaml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: config-schema
```

# Spring Boot Config JSON Schema Generator

A Spring Boot starter library that automatically generates JSON Schema documentation for your application's
configuration properties. It simplifies the process of documenting and validating configuration by generating JSON
Schema from your Spring Boot configuration classes.

## Key Features

- Automatic JSON Schema generation from Spring Boot `@ConfigurationProperties` classes
- Support for validation annotations and property constraints
- Compatible with Spring Boot 3.x
- Java 17+ runtime support
- Built-in Jackson and JSON Simple integration
- YAML format output support
- REST API endpoint for schema access
- Unit test integration for schema generation

## Prerequisites

- Java 17 or higher
- Spring Boot 3.x
- Maven or Gradle build system

## Installation

### Maven
## to use as a part of unitest 
Add the following dependency to your `pom.xml`:
```xml
        <dependency>
            <groupId>org.alexmond</groupId>
            <artifactId>spring-boot-config-json-schema-starter</artifactId>
            <version>0.0.2</version>
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
    void generateJsonSchema() throws Exception {

            String jsonConfigSchema;
            jsonConfigSchema = jsonSchemaService.generateFullSchema();

            ObjectMapper jsonMapper = new ObjectMapper();
            ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
            log.info("Writing json schema");
            jsonWriter.writeValue(Paths.get("sample-schema.json").toFile(), jsonMapper.readTree(jsonConfigSchema));


            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
            log.info("Writing yaml schema");
            yamlWriter.writeValue(Paths.get("sample-schema.yaml").toFile(), jsonMapper.readTree(jsonConfigSchema));
    }

}
```

### Using as REST API Endpoint

To expose the JSON schema via a REST endpoint (similar to Swagger API docs), first add the following dependency to your
`pom.xml`:
```xml
        <dependency>
            <groupId>org.alexmond</groupId>
            <artifactId>spring-boot-config-json-schema-starter</artifactId>
            <version>0.0.2</version>
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
    public String getConfigSchema() throws Exception {
        return jsonSchemaService.generateFullSchema();
    }

}
```






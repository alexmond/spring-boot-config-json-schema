[<image-card alt="Maven Central" src="https://img.shields.io/maven-central/v/org.alexmond/spring-boot-config-json-schema-starter.svg?label=Maven%20Central" ></image-card>](https://search.maven.org/artifact/org.alexmond/spring-boot-config-json-schema-starter)
[<image-card alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" ></image-card>](LICENSE)  <!-- Replace with your license -->
[<image-card alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/alexmond/spring-boot-config-json-schema/ci.yml" ></image-card>](https://github.com/alexmond/spring-boot-config-json-schema/actions)

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
    public String getConfigSchema() throws Exception {
        return jsonSchemaService.generateFullSchema();
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
    public String schema() throws Exception {
        return jsonSchemaService.generateFullSchema();
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

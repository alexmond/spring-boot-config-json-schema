package org.alexmond.sample.test;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SampleJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Test
    @Order(1)
    void generateJsonSchema() throws Exception {

        var jsonConfigSchemaJson = jsonSchemaService.generateFullSchemaJson();
        var jsonConfigSchemaYaml = jsonSchemaService.generateFullSchemaYaml();
        ObjectMapper jsonMapper = new ObjectMapper();

        // Validate generated schema
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        Schema schema = schemaRegistry.getSchema(jsonConfigSchemaJson);
        List<Error> errors = schema.validate(jsonMapper.readTree(jsonConfigSchemaJson));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("Schema validation error: {}", error));
            throw new AssertionError("Schema validation failed");
        }
        log.info("Schema validation passed successfully");

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());


        Files.writeString(Paths.get("sample-schema-" + timestamp + ".json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);
        Files.writeString(Paths.get("sample-schema.json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);

        Files.writeString(Paths.get("sample-schema-" + timestamp + ".yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);
        Files.writeString(Paths.get("sample-schema.yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);

    }

    @Test
    @Order(2)
    void validateSample() throws Exception {

        // Validate application.yaml against schema
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getDraft202012());
        Schema schema = factory.getSchema(Files.newInputStream(Paths.get("sample-schema.json")));
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        List<Error> errors = schema.validate(yamlMapper.readTree(Paths.get("test.yaml").toFile()));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("YAML validation error: {}", error));
            throw new AssertionError("YAML validation failed");
        }
        log.info("YAML validation passed successfully");
    }


}

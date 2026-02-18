package org.alexmond.sample.test;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class
SimpleBootJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @SuppressWarnings("EmptyMethod")
    @Test
    void contextLoads() {
    }

    @Test
    void generateJsonSchema() {

        var jsonConfigSchemaJson = jsonSchemaService.generateFullSchemaJson();
        var jsonConfigSchemaYaml = jsonSchemaService.generateFullSchemaYaml();

        ObjectMapper jsonMapper = new ObjectMapper();
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getDraft202012());
        Schema schema = factory.getSchema(jsonConfigSchemaJson);
        List<Error> errors;
        errors = schema.validate(jsonMapper.readTree(jsonConfigSchemaJson));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("Schema validation error: {}", error));
            throw new AssertionError("Schema validation failed");
        }
        log.info("Schema validation passed successfully");

        try {
            log.info("Writing json schema");
            Files.writeString(Paths.get("sample-schema.json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);

            log.info("Writing yaml schema");
            Files.writeString(Paths.get("sample-schema.yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

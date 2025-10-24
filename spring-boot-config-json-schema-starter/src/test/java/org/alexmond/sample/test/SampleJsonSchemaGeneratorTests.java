package org.alexmond.sample.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.boot.autoconfigure.http.client.HttpClientProperties;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.logging.LoggingApplicationListener;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(jsonConfigSchemaJson);
        Set<ValidationMessage> errors = schema.validate(jsonMapper.readTree(jsonConfigSchemaJson));
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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(Paths.get("sample-schema.json").toFile().toURI());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Set<ValidationMessage> errors = schema.validate(yamlMapper.readTree(Paths.get("test.yaml").toFile()));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("YAML validation error: {}", error));
            throw new AssertionError("YAML validation failed");
        }
        log.info("YAML validation passed successfully");
    }


}

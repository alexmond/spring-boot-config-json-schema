package org.alexmond.sample.test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.nio.file.Paths;
import java.util.Set;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SampleJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Test
    @Order(1)
    void generateJsonSchema() throws Exception {

        String jsonConfigSchema;
        jsonConfigSchema = jsonSchemaService.generateFullSchema();
        ObjectMapper jsonMapper = new ObjectMapper();

        // Validate generated schema
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(jsonConfigSchema);
        Set<ValidationMessage> errors = schema.validate(jsonMapper.readTree(jsonConfigSchema));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("Schema validation error: {}", error));
            throw new AssertionError("Schema validation failed");
        }
        log.info("Schema validation passed successfully");

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new java.util.Date());

        ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing json schema");
        jsonWriter.writeValue(Paths.get("sample-schema-" + timestamp + ".json").toFile(), jsonMapper.readTree(jsonConfigSchema));
        jsonWriter.writeValue(Paths.get("sample-schema.json").toFile(), jsonMapper.readTree(jsonConfigSchema));

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing yaml schema");
        yamlWriter.writeValue(Paths.get("sample-schema-" + timestamp + ".yaml").toFile(), jsonMapper.readTree(jsonConfigSchema));
        yamlWriter.writeValue(Paths.get("sample-schema.yaml").toFile(), jsonMapper.readTree(jsonConfigSchema));

    }

    @Test
    @Order(2)
    void validateSample() throws Exception {

        // Validate application.yaml against schema
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(Paths.get("sample-schema.json").toFile().toURI());
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        Set<ValidationMessage> errors = schema.validate(yamlMapper.readTree(Paths.get("application.yaml").toFile()));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("YAML validation error: {}", error));
            throw new AssertionError("YAML validation failed");
        }
        log.info("YAML validation passed successfully");
    }


}

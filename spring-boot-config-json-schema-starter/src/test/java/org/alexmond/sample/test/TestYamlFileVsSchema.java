package org.alexmond.sample.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;

import java.nio.file.Paths;
import java.util.Set;

@Slf4j
public class TestYamlFileVsSchema {
    //    @Test
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

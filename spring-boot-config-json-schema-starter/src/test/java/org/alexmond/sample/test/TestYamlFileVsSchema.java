package org.alexmond.sample.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.Error;
import com.networknt.schema.dialect.Dialects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Slf4j
public class TestYamlFileVsSchema {
    //    @Test
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

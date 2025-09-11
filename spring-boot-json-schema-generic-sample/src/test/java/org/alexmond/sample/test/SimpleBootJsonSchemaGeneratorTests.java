package org.alexmond.sample.test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.config.json.schema.service.MissingTypeCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@Slf4j
class
SimpleBootJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private MissingTypeCollector missingTypeCollector;

    @Test
    void generateJsonSchema() throws Exception {

        var jsonConfigSchemaJson = jsonSchemaService.generateFullSchema();
        var jsonConfigSchemaYaml = jsonSchemaService.generateFullSchemaYaml();
        log.info("Writing json schema");
        Files.writeString(Paths.get("../docs/src/docs/asciidoc/sample/boot-generic-config.json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);

        log.info("Writing yaml schema");
        Files.writeString(Paths.get("../docs/src/docs/asciidoc/sample/boot-generic-config.yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);
    }
    
    


}

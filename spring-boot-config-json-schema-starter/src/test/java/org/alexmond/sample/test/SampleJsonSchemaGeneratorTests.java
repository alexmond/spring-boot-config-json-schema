package org.alexmond.sample.test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

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

package org.alexmond.sample.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.config.json.schema.service.MissingTypeCollector;
import org.alexmond.sample.test.config.ConfigSample;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Paths;

@SpringBootTest
@Slf4j
class SimpleBootJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private MissingTypeCollector missingTypeCollector;

    @Test
    void contextLoads() {
    }

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
            log.info("==================================");
            missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing type: {}",type));

    }

    @Test
    void useJacksonSchema() throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        // Generate schema for the Product class
        JsonSchema productSchema = schemaGen.generateSchema(ConfigSample.class);
        System.out.println("==================================");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productSchema));

    }

}

package org.alexmond.sample.test;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaBuilder;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.config.json.schema.service.MissingTypeCollector;
import org.alexmond.sample.test.config.ConfigSample;
import org.alexmond.sample.test.config.EnumSample;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
@Slf4j
class SanityJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private MissingTypeCollector missingTypeCollector;

    @Autowired
    private JsonSchemaBuilder jsonSchemaBuilder;

    @Test
    void contextLoads() {
    }

    @Test
    void processEnumItem() {
        Class<?> clazz = EnumSample.class;
        List<String> values = jsonSchemaBuilder.processEnumItem(clazz);

        assertNotNull(values, "Values list should not be null");
        assertTrue(values.containsAll(List.of("EN1", "en1", "EN2", "en2", "EN3", "en3")), "Values list should contain all enum values");
        assertEquals(6, values.size(), "Values list should contain exactly 3 items");
    }

    @Test
    void useJacksonSchema() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        // Generate schema for the Product class
        JsonSchema productSchema = schemaGen.generateSchema(ConfigSample.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(Paths.get("gen.json").toFile(), productSchema);

    }

}

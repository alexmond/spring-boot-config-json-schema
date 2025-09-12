package org.alexmond.sample.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.config.json.schema.service.MissingTypeCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

@SpringBootTest
@Slf4j
class
SimpleBootJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private MissingTypeCollector missingTypeCollector;

    @Test
    void contextLoads() {
    }

    @Test
    void generateJsonSchema() {

        var jsonConfigSchemaJson = jsonSchemaService.generateFullSchemaJson();
        var jsonConfigSchemaYaml = jsonSchemaService.generateFullSchemaYaml();

        ObjectMapper jsonMapper = new ObjectMapper();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(jsonConfigSchemaJson);
        Set<ValidationMessage> errors = schema.validate(jsonMapper.readTree(jsonConfigSchemaJson));
        if (!errors.isEmpty()) {
            errors.forEach(error -> log.error("Schema validation error: {}", error));
            throw new AssertionError("Schema validation failed");
        }
        log.info("Schema validation passed successfully");

        log.info("Writing json schema");
        Files.writeString(Paths.get("sample-schema.json"), jsonConfigSchemaJson, StandardCharsets.UTF_8);

        log.info("Writing yaml schema");
        Files.writeString(Paths.get("sample-schema.yaml"), jsonConfigSchemaYaml, StandardCharsets.UTF_8);
    }

//    @Test
//    void useJacksonSchema() throws IOException {
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
//        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
//        // Generate schema for the Product class
//        JsonSchema productSchema = schemaGen.generateSchema(ConfigSample.class);
////        JsonSchema productSchema = schemaGen.generateSchema(io.swagger.v3.oas.models.media.Schema.class);
////        JsonSchema productSchema = schemaGen.generateSchema(SpringDocConfigProperties.class);
//        productSchema.set$schema("https://json-schema.org/draft/2020-12/schema");
////        productSchema.setId("your-schema-id");
//        productSchema.setDescription("This is a simple JSON Schema");
//
//        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
//        writer.writeValue(Paths.get("gen.json").toFile(), productSchema);
//
//        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
//        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
//        yamlWriter.writeValue(Paths.get("gen.yaml").toFile(), productSchema);
//    }

}

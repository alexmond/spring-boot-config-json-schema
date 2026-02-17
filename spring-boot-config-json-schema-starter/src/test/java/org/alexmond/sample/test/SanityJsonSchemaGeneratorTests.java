package org.alexmond.sample.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.Property;
import org.alexmond.config.json.schema.service.JsonSchemaBuilder;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.sample.test.config.ConfigSample;
import org.alexmond.sample.test.config.EnumSample;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class SanityJsonSchemaGeneratorTests {

    @Autowired
    private JsonSchemaService jsonSchemaService;
    @Autowired
    private JsonSchemaBuilder jsonSchemaBuilder;

    @SuppressWarnings("EmptyMethod")
    @Test
    void contextLoads() {
    }

    @Test
    void processEnumItem() {
        Class<?> clazz = EnumSample.class;
        Set<String> values = jsonSchemaBuilder.getHelper().processEnumItem(clazz);

        assertNotNull(values, "Values list should not be null");
        assertTrue(values.containsAll(List.of("EN1", "en1", "EN2", "en2", "EN3", "en3")), "Values list should contain all enum values");
        assertEquals(6, values.size(), "Values list should contain exactly 3 items");
    }

    @Test
    void testCollectMetadata() {
        Map<String, Property> meta = jsonSchemaService.collectMetadata();
        assertNotNull(meta, "Collected metadata should not be null");
        assertFalse(meta.isEmpty(), "Collected metadata should not be empty");
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
//    @Test
//    void testFieldTypes() throws IOException {
//        Class<?> clazz = ConfigSample.class;
//        for (Field field : clazz.getDeclaredFields()) {
//            String fieldType = field.getType().getName();
//            String fieldGenName = field.getGenericType().getTypeName();
//            log.info("fieldType: {}, fieldGenName: {}", fieldType, fieldGenName);
//        }
//    }

    @Test
    void generateSchema() throws Exception {
        String jsonConfigSchema;
        jsonConfigSchema = jsonSchemaService.generateFullSchemaJson();
        ObjectMapper jsonMapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode jsonNode = jsonMapper.readTree(jsonConfigSchema);
        Map<String, List<String>> duplicates = findDuplicateNodes(jsonNode);
        duplicates.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .forEach(entry -> log.info("Duplicate count {},value: ===== {} ====== found in paths: {}", entry.getValue().size(), entry.getKey(), entry.getValue()));
    }

    private Map<String, List<String>> findDuplicateNodes(com.fasterxml.jackson.databind.JsonNode node) {
        Map<String, List<String>> duplicates = new HashMap<>();
        traverseNode(node, "", duplicates);
        return duplicates;
    }

    private void traverseNode(com.fasterxml.jackson.databind.JsonNode node, String path, Map<String, List<String>> duplicates) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                String newPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
                String value = entry.getValue().toString();
                duplicates.computeIfAbsent(value, k -> new ArrayList<>()).add(newPath);
                traverseNode(entry.getValue(), newPath, duplicates);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPath = path + "[" + i + "]";
                String value = node.get(i).toString();
                duplicates.computeIfAbsent(value, k -> new ArrayList<>()).add(newPath);
                traverseNode(node.get(i), newPath, duplicates);
            }
        }
    }
}

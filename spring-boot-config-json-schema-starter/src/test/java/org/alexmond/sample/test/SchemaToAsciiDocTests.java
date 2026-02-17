package org.alexmond.sample.test;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.sample.test.asciimodel.AsciiDoc;
import org.alexmond.sample.test.asciimodel.AsciiDocGroupData;
import org.alexmond.sample.test.asciimodel.AsciiDocModel;
import org.alexmond.sample.test.config.AsciiDocConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class SchemaToAsciiDocTests {
    
    @Autowired 
    AsciiDocConfig asciiDocConfig;

    Map<String, AsciiDocGroupData> asciiDocGroupsDataMap = new TreeMap<>();
    AsciiDocModel asciiDocModel = new AsciiDocModel();
    
    @Autowired
    JsonSchemaService jsonSchemaService;

    @Autowired
    private SpringTemplateEngine templateEngine;


    @Test
    void generateJsonSchema() throws Exception {
        JsonSchemaRoot jsonConfigSchema = jsonSchemaService.getSchemaCache();
        jsonConfigSchema.getProperties().forEach(this::processNodeToAsciiDoc);
        renderPropertyDoc();
    }

    void processNodeToAsciiDoc(String propertyName,JsonSchemaProperties jsonSchemaProperties) {
        if (asciiDocConfig.getIncludes() != null && !asciiDocConfig.getIncludes().isEmpty()) {
            if (asciiDocConfig.getIncludes().stream().noneMatch(propertyName::startsWith)) {
                return;
            }
        }else if(asciiDocConfig.getExcludes() != null && !asciiDocConfig.getExcludes().isEmpty()) {
            if (asciiDocConfig.getExcludes().stream().anyMatch(propertyName::startsWith)) {
                return;
            }
        }
        AsciiDocGroupData asciiDocGroupData = null;
        for (var asciiDocGroup : asciiDocConfig.getAsciiDocGroupConfigs()) {
            if (propertyName.startsWith(asciiDocGroup.getPrefix())) {
                asciiDocGroupData = asciiDocGroupsDataMap.computeIfAbsent(asciiDocGroup.getPrefix(), k -> new AsciiDocGroupData(asciiDocGroup));
                break;
            }
        }
        if (asciiDocGroupData == null)
            asciiDocGroupData = asciiDocGroupsDataMap.computeIfAbsent("default", k -> new AsciiDocGroupData("Default"));

        if(jsonSchemaProperties.getType() != null) {
            switch (jsonSchemaProperties.getType()) {
                case STRING, NUMBER, INTEGER, BOOLEAN ->
                        asciiDocGroupData.getAsciiDocs().put(propertyName, new AsciiDoc(jsonSchemaProperties));
                case ARRAY -> {
                    asciiDocGroupData.getAsciiDocs().put(propertyName, new AsciiDoc(jsonSchemaProperties));
                    if (jsonSchemaProperties.getItems() != null) {
                        if (jsonSchemaProperties.getItems().getType() == JsonSchemaType.OBJECT) {
                            // For arrays of objects, recurse into the properties using "[]" notation
                            if (jsonSchemaProperties.getItems().getProperties() != null) {
                                for (Map.Entry<String, JsonSchemaProperties> entry : jsonSchemaProperties.getItems().getProperties().entrySet()) {
                                    processNodeToAsciiDoc(propertyName + "[]." + entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                }
                case OBJECT -> {
                        // Handle Map types (additionalProperties)
                        if (jsonSchemaProperties.getAdditionalProperties() instanceof JsonSchemaProperties additionalProp) {
                            asciiDocGroupData.getAsciiDocs().put(propertyName + ".*", new AsciiDoc(additionalProp));
                            // If the map value is an object, recurse into its fields
                            if (additionalProp.getProperties() != null) {
                                for (Map.Entry<String, JsonSchemaProperties> entry : additionalProp.getProperties().entrySet()) {
                                    processNodeToAsciiDoc(propertyName + ".*." + entry.getKey(), entry.getValue());
                                }
                            }
                        }

                        // Handle standard POJO fields
                        if (jsonSchemaProperties.getProperties() != null) {
                            for (Map.Entry<String, JsonSchemaProperties> entry : jsonSchemaProperties.getProperties().entrySet()) {
                                processNodeToAsciiDoc(propertyName + "." + entry.getKey(), entry.getValue());
                            }
                        }
                    }
                default -> throw new IllegalStateException("Unexpected value: " + jsonSchemaProperties.getType());
            }
        }else {
            log.error("No type specified -- probably ref, needs implementation");
        }
    }

    public void renderPropertyDoc() {

        // Ensure Thymeleaf iterates Map.Entry so "entry.key" and "entry.value" work reliably
        Map<String, Object> model = new HashMap<>();
        model.put("groups", asciiDocGroupsDataMap.entrySet());

        Context ctx = new Context(Locale.ROOT);
        ctx.setVariables(model);

        // template name WITHOUT suffix because resolver adds ".adoc"
        String adoc = templateEngine.process("property-doc", ctx);

        try {
            Files.writeString(Path.of("property-doc.adoc"), adoc, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

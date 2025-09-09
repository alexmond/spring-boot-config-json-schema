package org.alexmond.config.json.schema.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.XDeprecation;
import org.alexmond.config.json.schema.metamodel.HintValue;
import org.alexmond.config.json.schema.metamodel.Property;

import java.util.Objects;

@AllArgsConstructor
@Slf4j
public class JsonSchemaBuilderHelper {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    void processDeprecation(JsonSchemaProperties jsonSchemaProperties, Property prop) {
        log.debug("Processing deprecation for property: {}", prop.getName());
        if (prop.getDeprecated() != null && prop.getDeprecated()) {
            jsonSchemaProperties.setDeprecated(true);
            if (prop.getDeprecation() != null) {
                XDeprecation xDeprecation = org.alexmond.config.json.schema.jsonschemamodel.XDeprecation.builder()
                        .reason(prop.getDeprecation().getReason())
                        .replacement(prop.getDeprecation().getReplacement())
                        .since(prop.getDeprecation().getSince())
                        .build();
                if(prop.getDeprecation().getLevel() != null) {
                    xDeprecation.setLevel(prop.getDeprecation().getLevel().name().toUpperCase());
                }
                if (!xDeprecation.isEmpty())
                    jsonSchemaProperties.setXDeprecation(xDeprecation);
            }
        }
    }

    void processHints(JsonSchemaProperties jsonSchemaProperties, Property prop) {
        log.debug("Processing hints for property: {}", prop.getName());
        if (prop.getHint() != null) {
            if (prop.getHint().getValues() != null && !prop.getHint().getValues().isEmpty()) {
                var hints = prop.getHint().getValues().stream()
                        .map(HintValue::getValue)
                        .filter(Objects::nonNull)
                        .toList();
                if (hints.size() > 1) {
                    log.debug("Property '{}' has multiple hints: {}", prop.getName(), hints);
                }
                jsonSchemaProperties.setExamples(hints);
            }
        }
    }
}

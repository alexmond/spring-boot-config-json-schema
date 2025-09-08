package org.alexmond.config.json.schema.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.XDeprication;
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
                XDeprication xDeprication = XDeprication.builder()
                        .reason(prop.getDeprecation().getReason())
                        .replacement(prop.getDeprecation().getReplacement())
                        .since(prop.getDeprecation().getSince())
                        .build();
                if(prop.getDeprecation().getLevel() != null) {
                    xDeprication.setLevel(prop.getDeprecation().getLevel().name().toUpperCase());
                }
                if (!xDeprication.isEmpty())
                    jsonSchemaProperties.setXDeprication(xDeprication);
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

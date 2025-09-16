package org.alexmond.config.json.schema.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.XDeprecation;
import org.alexmond.config.json.schema.metamodel.HintValue;
import org.alexmond.config.json.schema.metamodel.Property;

import java.util.Objects;

/**
 * Helper class for building JSON schema from configuration properties.
 * Provides utility methods to process deprecation information and hints
 * during schema generation.
 */
@AllArgsConstructor
@Slf4j
public class JsonSchemaBuilderHelper {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    /**
     * Processes deprecation information for a property and updates the JSON schema properties accordingly.
     * If the property is marked as deprecated, it sets the deprecated flag and includes detailed deprecation
     * information if available.
     *
     * @param jsonSchemaProperties The JSON schema properties to be updated with deprecation information
     * @param prop                 The property containing deprecation details
     */
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
                if (prop.getDeprecation().getLevel() != null) {
                    xDeprecation.setLevel(prop.getDeprecation().getLevel().name().toUpperCase());
                }
                if (!xDeprecation.isEmpty())
                    jsonSchemaProperties.setXDeprecation(xDeprecation);
            }
        }
    }

    /**
     * Processes hints for a property and updates the JSON schema properties with examples.
     * If the property has hint values, they are added as examples in the JSON schema.
     *
     * @param jsonSchemaProperties The JSON schema properties to be updated with hints
     * @param prop                 The property containing hint information
     */
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

package org.alexmond.config.json.schema.service;

import lombok.RequiredArgsConstructor;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.springframework.boot.logging.LogLevel;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for creating standard JSON Schema definitions for common Java types.
 * This class provides predefined schema definitions for logger levels, locales,
 * and character sets that can be reused across the schema.
 */
@RequiredArgsConstructor
public class DefinitionsHelper {

    private final JsonSchemaBuilderHelper helper;

    /**
     * Creates standard schema definitions for common types like logger levels, locales, and charsets.
     *
     * @return Map of named schema definitions
     */
    public Map<String, JsonSchemaProperties> getDefinitions() {

        Map<String, JsonSchemaProperties> definitions = new LinkedHashMap<>();

        definitions.put("loggerLevel", getLoggerLevelDef());
        definitions.put("loggerLevelProp", getLoggerLevelPropDef());
        definitions.put("java.util.Locale", getLocalesDef());
        definitions.put("java.nio.charset.Charset", getCharsetsDef());

        return definitions;
    }

    /**
     * Creates a JSON Schema definition for logger levels.
     *
     * @return JSON Schema properties defining the possible logger level values
     */
    private JsonSchemaProperties getLoggerLevelDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(helper.processEnumItem(LogLevel.class))
                .build();
    }

    /**
     * Creates a JSON Schema definition for logger level properties.
     * This definition allows for nested logger level configurations.
     *
     * @return JSON Schema properties defining the structure of logger level properties
     */
    private JsonSchemaProperties getLoggerLevelPropDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.OBJECT)
                .additionalProperties(JsonSchemaProperties.builder()
                        .oneOf(List.of(
                                JsonSchemaProperties.builder()
                                        .reference("#/$defs/loggerLevel")
                                        .build(),
                                JsonSchemaProperties.builder()
                                        .reference("#/$defs/loggerLevelProp")
                                        .build()
                        ))
                        .build())
                .build();
    }

    /**
     * Creates a JSON Schema definition for Java Locales.
     * Includes all available locales from the Java runtime.
     *
     * @return JSON Schema properties defining the possible locale values
     */
    private JsonSchemaProperties getLocalesDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(Arrays.stream(Locale.getAvailableLocales())
                        .map(Locale::toString)
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Creates a JSON Schema definition for character sets.
     * Includes all available charsets from the Java runtime.
     *
     * @return JSON Schema properties defining the possible charset values
     */
    private JsonSchemaProperties getCharsetsDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(Charset.availableCharsets().keySet())
                .build();
    }
}

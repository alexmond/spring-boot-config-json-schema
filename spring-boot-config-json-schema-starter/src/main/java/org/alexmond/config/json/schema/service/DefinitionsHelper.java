package org.alexmond.config.json.schema.service;

import lombok.RequiredArgsConstructor;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.springframework.boot.logging.LogLevel;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

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

    private JsonSchemaProperties getLoggerLevelDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(helper.processEnumItem(LogLevel.class))
                .build();
    }

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

    private JsonSchemaProperties getLocalesDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(Arrays.stream(Locale.getAvailableLocales())
                        .map(Locale::toString)
                        .collect(Collectors.toSet()))
                .build();
    }

    private JsonSchemaProperties getCharsetsDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(Charset.availableCharsets().keySet())
                .build();
    }
}

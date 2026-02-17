package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaFormat;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.XDeprecation;
import org.alexmond.config.json.schema.metamodel.HintValue;
import org.alexmond.config.json.schema.metamodel.Property;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for building JSON schema from configuration properties.
 * Provides utility methods to process deprecation information, hints,
 * validation constraints, and OpenAPI annotations during JSON schema generation.
 * This class handles the conversion of Java annotations and metadata into
 * corresponding JSON schema elements.
 */
@AllArgsConstructor
@Slf4j
public class JsonSchemaBuilderHelper {

    /**
     * Configuration settings for JSON schema generation.
     */
    private final JsonConfigSchemaConfig config;

    /**
     * Service for mapping Java types to JSON schema types.
     */
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

        jsonSchemaProperties.setDeprecated(true);
        if (prop.getDeprecation() != null) {
            XDeprecation xDeprecation = XDeprecation.builder()
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

    /**
     * Processes hints for a property and updates the JSON schema properties with examples.
     * If the property has hint values, they are added as examples in the JSON schema.
     *
     * @param jsonSchemaProperties The JSON schema properties to be updated with hints
     * @param prop                 The property containing hint information
     */
    void processHints(JsonSchemaProperties jsonSchemaProperties, Property prop) {
        log.debug("Processing hints for property: {}", prop.getName());
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

    /**
     * Processes an enum class and returns a set of possible enum values.
     * The returned set includes both the original enum names and their lowercase versions.
     *
     * @param itemClass The enum class to process
     * @return Set of enum values, or null if the class is not an enum
     */
    public Set<String> processEnumItem(Class<?> itemClass) {
        log.debug("Processing enum values for property: {}", itemClass.getCanonicalName());
        if (itemClass.isEnum()) {
            Object[] enumConstants = itemClass.getEnumConstants();
            if (enumConstants != null) {
                return Arrays.stream(enumConstants)
                        .flatMap(enumConstant -> Arrays.stream(new String[]{
                                enumConstant.toString(),
                                enumConstant.toString().toLowerCase()
                        }))
                        .collect(Collectors.toSet());
            }
        }
        return null;
    }

    /**
     * Processes validation annotations on a field and updates the JSON schema properties accordingly.
     * Handles Jakarta validation annotations such as @Min, @Max, @Size, @Pattern, @NotEmpty, and @Email.
     *
     * @param jsonSchemaProperties The JSON schema properties to update with validation constraints
     * @param field                The field to process for validation annotations
     * @param propName             The name of the property being processed
     */
    public void processValidated(JsonSchemaProperties jsonSchemaProperties, Field field, String propName) {
        log.trace("Validation: Processing validation for property: {}", propName);

        if (field.isAnnotationPresent(Min.class)) {
            var value = field.getAnnotation(Min.class).value();
            jsonSchemaProperties.setMinimum(value);
            log.debug("Validation: Added Min validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Max.class)) {
            var value = field.getAnnotation(Max.class).value();
            jsonSchemaProperties.setMaximum(value);
            log.debug("Validation: Added Max validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Size.class)) {
            var size = field.getAnnotation(Size.class);
            if (size.min() > 0) {
                jsonSchemaProperties.setMinLength(size.min());
                log.info("Validation: Added Size.min validation with value {} for field {}", size.min(), field.getName());
            }
            if (size.max() < Integer.MAX_VALUE) {
                jsonSchemaProperties.setMaxLength(size.max());
                log.info("Validation: Added Size.max validation with value {} for field {}", size.max(), field.getName());
            }
        }
        if (field.isAnnotationPresent(Pattern.class)) {
            var pattern = field.getAnnotation(Pattern.class).regexp();
            jsonSchemaProperties.setPattern(pattern);
            log.info("Validation: Added Pattern validation with regexp {} for field {}", pattern, field.getName());
        }
//        if (field.isAnnotationPresent(NotNull.class)) {
//            propDef.put("required", true);
//            log.debug("Validation: Added NotNull validation for field {}", field.getName());
//        }
        if (field.isAnnotationPresent(NotEmpty.class)) {
            jsonSchemaProperties.setMinLength(1);
            log.debug("Validation: Added NotEmpty validation for field {}", field.getName());
        }
        if (field.isAnnotationPresent(Email.class)) {
            jsonSchemaProperties.setFormat(JsonSchemaFormat.EMAIL);
            log.debug("Validation: Added NotEmpty validation for field {}", field.getName());
        }
    }

    /**
     * Processes OpenAPI annotations on a field and updates the JSON schema properties.
     * Handles various Schema annotation attributes like description, format, example, etc.
     *
     * @param jsonSchemaProperties The JSON schema properties to update with OpenAPI information
     * @param field                The field to process for OpenAPI annotations
     * @param propName             The name of the property being processed
     */
    public void processOpenapi(JsonSchemaProperties jsonSchemaProperties, Field field, String propName) {
        log.trace("OpenAPI: Processing schema for property: {}", propName);
        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (!schema.description().isEmpty()) {
                jsonSchemaProperties.setDescription(schema.description());
            }
            if (!schema.format().isEmpty()) {
                jsonSchemaProperties.setFormat(JsonSchemaFormat.valueOf(schema.format().toUpperCase()));
            }
            if (!schema.example().isEmpty() && jsonSchemaProperties.getExamples() != null && jsonSchemaProperties.getExamples().isEmpty()) {
                jsonSchemaProperties.setExamples(List.of(schema.example()));
            }
            if (schema.deprecated()) {
                jsonSchemaProperties.setDeprecated(true);
            }
            if (!schema.defaultValue().isEmpty() && (jsonSchemaProperties.getDefaultValue() != null)) {
                jsonSchemaProperties.setDefaultValue(schema.defaultValue());
            }
        }
    }

    /**
     * Processes OpenAPI annotations on a class level and updates the JSON schema properties.
     * Currently handles the Schema description annotation at class level.
     *
     * @param jsonSchemaProperties The JSON schema properties to update with class-level OpenAPI information
     * @param propClass            The class to process for OpenAPI annotations
     */
    public void processClassOpenapi(JsonSchemaProperties jsonSchemaProperties, Class<?> propClass) {
        log.trace("OpenAPI: Processing Class schema for property: {}", propClass.getCanonicalName());
        if (propClass.isAnnotationPresent(Schema.class)) {
            Schema schema = propClass.getAnnotation(Schema.class);
            if (!schema.description().isEmpty()) {
                jsonSchemaProperties.setDescription(schema.description());
            }
        }
    }
}

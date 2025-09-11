package org.alexmond.config.json.schema.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration properties for JSON Schema generation.
 * This class defines the settings used to customize the generation of JSON Schema
 * for Spring Boot configuration properties. It allows control over schema metadata,
 * processing options, and additional property inclusions.
 *
 * <p>All properties are configured under the 'json-config-schema' prefix in your
 * application.properties or application.yml file.
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 * @see io.swagger.v3.oas.annotations.media.Schema
 */
@Data
@Schema(description = "JSON Schema configuration properties")
@ConfigurationProperties(prefix = "json-config-schema")
public class JsonConfigSchemaConfig {

    /**
     * The URL of the JSON Schema specification to be used.
     * Defaults to the 2020-12 draft version.
     */
    @Schema(description = "JSON Schema specification URL", defaultValue = "https://json-schema.org/draft/2020-12/schema")
    private String schemaSpec = "https://json-schema.org/draft/2020-12/schema";
    /**
     * The unique identifier for this schema.
     * Should be replaced with a meaningful ID for your configuration.
     */
    @Schema(description = "Schema identifier", defaultValue = "your-schema-id")
    private String schemaId = "urn:example";
    /**
     * The title of the schema document.
     * Provides a human-readable name for the configuration schema.
     */
    @Schema(description = "Schema title", defaultValue = "Spring Boot Configuration Properties")
    private String title = "Spring Boot Configuration Properties";
    /**
     * A detailed description of what this schema represents.
     * Explains the purpose and content of the configuration properties.
     */
    @Schema(description = "Schema description", defaultValue = "Auto-generated schema from configuration metadata")
    private String description = "Auto-generated schema from configuration metadata";
    /**
     * Controls whether OpenAPI annotations should be processed.
     * When true, OpenAPI annotations will be used to enhance the schema.
     */
    @Schema(description = "Enable OpenAPI annotations processing", defaultValue = "true" )
    private boolean useOpenapi = true;
    /**
     * Controls whether validation annotations should be processed.
     * When true, validation constraints will be included in the schema.
     */
    @Schema(description = "Enable validation annotations processing", defaultValue = "true")
    private boolean useValidation = true;

    /**
     * Controls whether additional properties not defined in the schema should be allowed.
     * When true, the schema will accept properties that are not explicitly defined.
     * When false, only properties defined in the schema will be accepted.
     */
    @Schema(description = "Allow additional properties not defined in schema", defaultValue = "true")
    private boolean allowAdditionalProperties = true;

    /**
     * List of additional configuration property paths to include in the schema.
     * By default, it includes the 'logging' configuration namespace.
     */
    @Schema(description = "List of additional property paths to include",defaultValue = "logging")
    private List<String> additionalProperties = List.of("logging");


    /**
     * Map of property names or java objects to their type definitions.
     * Used to store custom type mappings and property configurations
     * that override or extend the default schema generation behavior.
     */
    private Map<String, JsonSchemaProperties> JsonSchemaPropertiesMap = new HashMap<>();

    /**
     * Controls whether missing type information should be logged.
     * When true, logs will be generated for types that couldn't be mapped during schema generation.
     * This is useful for debugging and identifying unmapped types.
     */
    private Boolean missingTypeLog = false;
    /**
     * A list of fully qualified class names that should be excluded from schema generation.
     * By default, includes ObjectMapper and ClassLoader classes to prevent processing of
     * system-level classes that aren't relevant to configuration.
     */
    private List<String> excludeClasses = new ArrayList<>(List.of(
            "com.fasterxml.jackson.databind.ObjectMapper",
            "java.lang.ClassLoader",
            "org.springframework.boot.context.logging.LoggingApplicationListener"));

    /**
     * Additional class names to be excluded from schema generation.
     * This list can be used to specify custom classes that should be excluded
     * beyond the default exclusions. Classes specified here will be combined
     * with the default excludeClasses list.
     */
    private List<String> additionalExcludeClasses = new ArrayList<>();

    /**
     * Returns a combined list of all excluded classes.
     * This includes both the default excludeClasses and any additional excluded classes.
     *
     * @return List of all class names that should be excluded from schema generation
     */
    public List<String> getAllExcludedClasses() {
        List<String> allExcludes = new ArrayList<>(excludeClasses);
        allExcludes.addAll(additionalExcludeClasses);
        return allExcludes;
    }


}

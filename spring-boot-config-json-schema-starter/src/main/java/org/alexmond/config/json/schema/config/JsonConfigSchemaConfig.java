package org.alexmond.config.json.schema.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


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
    private String schemaId = "your-schema-id";
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
     * List of additional configuration property paths to include in the schema.
     * By default includes the 'logging' configuration namespace.
     */
    @Schema(description = "List of additional property paths to include")
    private List<String> additionalProperties = List.of("logging");

}

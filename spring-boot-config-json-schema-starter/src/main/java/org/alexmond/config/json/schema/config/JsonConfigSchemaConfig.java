package org.alexmond.config.json.schema.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Data
@Schema(description = "JSON Schema configuration properties")
public class JsonConfigSchemaConfig {

    @Schema(description = "JSON Schema specification URL", defaultValue = "https://json-schema.org/draft/2020-12/schema")
    private String schemaSpec = "https://json-schema.org/draft/2020-12/schema";
    @Schema(description = "Schema identifier", defaultValue = "your-schema-id")
    private String schemaId = "your-schema-id";
    @Schema(description = "Schema title", defaultValue = "Spring Boot Configuration Properties")
    private String title = "Spring Boot Configuration Properties";
    @Schema(description = "Schema description", defaultValue = "Auto-generated schema from configuration metadata")
    private String description = "Auto-generated schema from configuration metadata";

    @Schema(description = "Enable OpenAPI annotations processing", defaultValue = "true")
    private boolean useOpenapi = true;
    @Schema(description = "Enable validation annotations processing", defaultValue = "true")
    private boolean useValidation = true;

    @Schema(description = "List of additional property paths to include")
    private List<String> additionalProperties = List.of("logging");

}

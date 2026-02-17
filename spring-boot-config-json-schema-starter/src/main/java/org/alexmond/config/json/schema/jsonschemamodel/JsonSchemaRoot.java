package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"schema", "id", "title", "description", "type", "definitions", "properties"})
public class JsonSchemaRoot {
    @Builder.Default
    @JsonProperty("$schema")
    private String schema = "https://json-schema.org/draft/2020-12/schema";
    @JsonProperty("$id")
    private String id;
    private String title;
    private String description;
    @Builder.Default
    private JsonSchemaType type = JsonSchemaType.OBJECT;
    @JsonProperty("$defs")
    private Map<String, JsonSchemaProperties> definitions;
    private Map<String, JsonSchemaProperties> properties;
    @JsonProperty("additionalProperties")
    private Object additionalProperties;
}

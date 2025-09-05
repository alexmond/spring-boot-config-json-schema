package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;

import java.util.Map;

@Data
@JsonPropertyOrder({"schema", "schemaId", "title", "description", "type", "definitions", "properties"})
public class SvetaSchema {
    public SvetaSchema(JsonConfigSchemaConfig config) {
        schema = config.getSchemaSpec();
        schemaId = config.getSchemaId();
        title = config.getTitle();
        description = config.getDescription();
    }

    @JsonProperty("$schema") private String schema;
    @JsonProperty("$id") private String schemaId;
    private String title;
    private String description;
    private String type = "object";

    @JsonProperty("$defs") private Map<String, Object> definitions;
    Map<String, Object> properties;
}

package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"schema", "id", "title", "description", "type", "definitions", "properties"})
public class JsonSchemaRoot {
    @JsonProperty("$schema")
    private String schema = "https://json-schema.org/draft/2020-12/schema";
    @JsonProperty("$id")
    private String id;
    private String title;
    private String description;
    private JsonSchemaType type = JsonSchemaType.OBJECT;
    @JsonProperty("$defs")
    private Map<String, JsonSchemaProperties> definitions = new TreeMap<>();
    private Map<String, JsonSchemaProperties> properties = new TreeMap<>();

    @JsonIgnore
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> toMap() {
        return objectMapper.convertValue(this, Map.class);
    }
}

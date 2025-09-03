package org.alexmond.config.json.schema.jsonschemamodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonSchemaRoot {
    private String $schema = "https://json-schema.org/draft/2020-12/schema";
    private String type = "object";
    private String title;
    private String id;
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, Object> definitions = new HashMap<>();
}

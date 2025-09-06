package org.alexmond.config.json.schema.jsonschemamodel;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class JsonSchemaProperties {
    private JsonSchemaType type;
    private String description;
    private String pattern;
    private JsonSchemaFormat format;
    private String reference;
    private List<String> enumValues;
    private Number minimum;
    private Number maximum;
    private Boolean exclusiveMinimum;
    private Boolean exclusiveMaximum;
    private Integer minLength;
    private Integer maxLength;
    private String defaultValue;
    private Boolean deprecated;
    private Boolean required;
    private List<String> examples;
    private Map<String, JsonSchemaProperties> properties;
    @Valid
    private JsonSchemaProperties items;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (type != null) map.put("type", type.getValue());
        if (description != null) map.put("description", description);
        if (pattern != null) map.put("pattern", pattern);
        if (format != null) map.put("format", format.getValue());
        if (reference != null) map.put("$ref", reference);
        if (enumValues != null) map.put("enum", enumValues);
        if (minimum != null) map.put("minimum", minimum);
        if (maximum != null) map.put("maximum", maximum);
        if (exclusiveMinimum != null) map.put("exclusiveMinimum", exclusiveMinimum);
        if (exclusiveMaximum != null) map.put("exclusiveMaximum", exclusiveMaximum);
        if (minLength != null) map.put("minLength", minLength);
        if (maxLength != null) map.put("maxLength", maxLength);
        if (defaultValue != null) map.put("default", defaultValue);
        if (deprecated != null) map.put("deprecated", deprecated);
        if (required != null) map.put("required", required);
        if (examples != null) map.put("examples", examples);
        if (properties != null) {
            Map<String, Object> propsMap = new HashMap<>();
            properties.forEach((key, value) -> propsMap.put(key, value.toMap()));
            map.put("properties", propsMap);
        }
        if (items != null) map.put("items", items.toMap());
        return map;
    }
}

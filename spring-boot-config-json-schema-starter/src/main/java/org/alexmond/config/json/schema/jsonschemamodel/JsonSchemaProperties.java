package org.alexmond.config.json.schema.jsonschemamodel;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
enum JsonSchemaFormat {
    DATE_TIME("date-time"),      // Combined date and time with timezone
    TIME("time"),                // Time with optional timezone
    DATE("date"),               // Full date without time
    DURATION("duration"),        // Time duration
    EMAIL("email"),             // Email address
    IDN_EMAIL("idn-email"),     // Internationalized email address
    HOSTNAME("hostname"),        // Internet hostname
    IDN_HOSTNAME("idn-hostname"), // Internationalized hostname
    IPV4("ipv4"),              // IPv4 address
    IPV6("ipv6"),              // IPv6 address
    URI("uri"),                // Universal Resource Identifier
    URI_REFERENCE("uri-reference"), // URI reference including relative URIs
    IRI("iri"),                // Internationalized URI
    IRI_REFERENCE("iri-reference"), // Internationalized URI reference
    UUID("uuid"),              // Universally Unique Identifier
    JSON_POINTER("json-pointer"), // JSON Pointer string
    RELATIVE_JSON_POINTER("relative-json-pointer"); // Relative JSON Pointer string

    private final String value;

    JsonSchemaFormat(String value) {
        this.value = value;
    }
}

@Getter
enum JsonSchemaType {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    OBJECT("object"),
    NULL("null");

    private final String value;

    JsonSchemaType(String value) {
        this.value = value;
    }
}

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

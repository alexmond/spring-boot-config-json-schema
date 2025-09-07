package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        // Core metadata
        "title", "description", "$comment", "deprecated",
        // Type and format
        "type", "format", "$ref",
        // Validation constraints
        "const", "enum", "pattern",
        // Numeric constraints
        "minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum", "multipleOf",
        // String constraints
        "minLength", "maxLength",
        // Array constraints
        "minItems", "maxItems", "uniqueItems", "prefixItems", "contains", "minContains", "maxContains",
        // Object constraints
        "properties", "patternProperties", "propertyNames", "required", "minProperties", "maxProperties",
        "dependentRequired", "dependentSchemas",
        // Conditional logic
        "if", "then", "else", "allOf", "anyOf", "oneOf", "not",
        // Content
        "contentEncoding", "contentMediaType", "contentSchema",
        // Miscellaneous
        "default", "examples"
})
public class JsonSchemaProperties {
    private JsonSchemaType type;
    private String description;
    private String pattern;
    private JsonSchemaFormat format;
    @JsonProperty("$ref")
    private String reference;
    @JsonProperty("enum")
    private List<String> enumValues;
    private Number minimum;
    private Number maximum;
    // FIX: Changed from Boolean to Number for 2020-12 compliance
    private Number exclusiveMinimum;
    // FIX: Changed from Boolean to Number for 2020-12 compliance
    private Number exclusiveMaximum;
    private Integer minLength;
    private Integer maxLength;
    @JsonProperty("default")
    private String defaultValue;
    private Boolean deprecated;
    // FIX: Removed incorrect boolean 'required' field. Use 'requiredProperties' instead for objects.
    private List<String> examples;
    @Valid
    private Map<String, JsonSchemaProperties> properties;

    // NEW: Core properties
    private String title;
    @JsonProperty("$comment")
    private String comment;
    private Object constValue; // Single allowed value
    private Boolean readOnly;
    private Boolean writeOnly;

    // NEW: Numeric properties
    private Number multipleOf;

    // NEW: Array properties
    private Integer minItems;
    private Integer maxItems;
    private Boolean uniqueItems;
    private List<JsonSchemaProperties> prefixItems;
    private JsonSchemaProperties contains;
    private Integer minContains;
    private Integer maxContains;

    // NEW: Object properties
    private Integer minProperties;
    private Integer maxProperties;
    // NOTE: This is the correct implementation for the 'required' keyword in an object context.
    @JsonProperty("required")
    private List<String> requiredProperties; // Array version
    private Map<String, List<String>> dependentRequired;
    private Map<String, JsonSchemaProperties> dependentSchemas;
    private Map<String, JsonSchemaProperties> patternProperties;
    private JsonSchemaProperties propertyNames;

    // NEW: Conditional logic
    @JsonProperty("if")
    @Valid
    private JsonSchemaProperties ifSchema;
    @JsonProperty("then")
    @Valid
    private JsonSchemaProperties thenSchema;
    @JsonProperty("else")
    @Valid
    private JsonSchemaProperties elseSchema;
    private List<JsonSchemaProperties> allOf;
    private List<JsonSchemaProperties> anyOf;
    private List<JsonSchemaProperties> oneOf;
    private JsonSchemaProperties not;

    // NEW: String content
    private String contentEncoding;
    private String contentMediaType;
    private JsonSchemaProperties contentSchema;
    
}

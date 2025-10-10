package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
        // Type and format
        "type", "format", "$ref",
        // Core metadata
        "title", "description", "$comment", "deprecated",
        // Defaults
        "default", "examples",
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
        "dependentRequired", "dependentSchemas", "additionalProperties",
        // Conditional logic
        "if", "then", "else", "allOf", "anyOf", "oneOf", "not",
        // Content
        "contentEncoding", "contentMediaType", "contentSchema"
})

public class JsonSchemaProperties {
    @JsonIgnore
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private JsonSchemaType type;
    private String description;
    private String pattern;
    private JsonSchemaFormat format;
    @JsonProperty("$ref")
    private String reference;
    @JsonProperty("enum")
    private Set<String> enumValues;
    private Number minimum;
    private Number maximum;
    // FIX: Changed from Boolean to Number for 2020-12 compliance
    private Number exclusiveMinimum;
    // FIX: Changed from Boolean to Number for 2020-12 compliance
    private Number exclusiveMaximum;
    private Integer minLength;
    private Integer maxLength;
    @JsonProperty("default")
    private Object defaultValue;
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
    private JsonSchemaProperties items;
    private JsonSchemaProperties contains;
    private Integer minContains;
    private Integer maxContains;
    // NEW: Object properties
    private Integer minProperties;
    private Integer maxProperties;
    // NOTE: This is the correct implementation for the 'required' keyword in an object context.
    @JsonProperty("required")
    private Set<String> requiredProperties; // Array of unique property names
    private Map<String, Set<String>> dependentRequired;
    private Map<String, JsonSchemaProperties> dependentSchemas;
    private Map<String, JsonSchemaProperties> patternProperties;
    private JsonSchemaProperties propertyNames;
    @JsonProperty("additionalProperties")
    private Object additionalProperties;
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

    // Extensions
    @JsonProperty("x-deprecation")
    private XDeprecation xDeprecation;
    @JsonProperty("x-intellij-html-description")
    private String htmlDescription;

    public XDeprecation getxDeprecation() {
        return xDeprecation;
    }

    public Map<String, Object> toMap() {
        return objectMapper.convertValue(this, Map.class);
    }

    private <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        if (list1 == null) return list2;
        if (list2 == null) return list1;
        if (list2.isEmpty()) return list1;
        List<T> result = new ArrayList<>(list1);
        result.removeAll(list2);
        result.addAll(list2);
        return result;
    }

    private <T> Set<T> mergeSets(Set<T> set1, Set<T> set2) {
        if (set1 == null) return set2;
        if (set2 == null) return set1;
        Set<T> result = new LinkedHashSet<>(set1);
        result.addAll(set2);
        return result;
    }

    private <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null) return map2;
        if (map2 == null) return map1;
        map2.forEach((key, value) -> {
            if (value instanceof JsonSchemaProperties && map1.get(key) instanceof JsonSchemaProperties) {
                map1.put(key, (V) ((JsonSchemaProperties) map1.get(key)).merge((JsonSchemaProperties) value));
            } else {
                map1.put(key, value);
            }
        });
        return map1;
    }

    public JsonSchemaProperties merge(JsonSchemaProperties other) {
        if (other == null) return this;

        if (other.getType() != null) this.type = other.getType();
        if (other.getDescription() != null) this.description = other.getDescription();
        if (other.getPattern() != null) this.pattern = other.getPattern();
        if (other.getFormat() != null) this.format = other.getFormat();
        if (other.getReference() != null) this.reference = other.getReference();
        if (other.getMinimum() != null) this.minimum = other.getMinimum();
        if (other.getMaximum() != null) this.maximum = other.getMaximum();
        if (other.getExclusiveMinimum() != null) this.exclusiveMinimum = other.getExclusiveMinimum();
        if (other.getExclusiveMaximum() != null) this.exclusiveMaximum = other.getExclusiveMaximum();
        if (other.getMinLength() != null) this.minLength = other.getMinLength();
        if (other.getMaxLength() != null) this.maxLength = other.getMaxLength();
        if (other.getDefaultValue() != null) this.defaultValue = other.getDefaultValue();
        if (other.getDeprecated() != null) this.deprecated = other.getDeprecated();
        if (other.getTitle() != null) this.title = other.getTitle();
        if (other.getComment() != null) this.comment = other.getComment();
        if (other.getConstValue() != null) this.constValue = other.getConstValue();
        if (other.getReadOnly() != null) this.readOnly = other.getReadOnly();
        if (other.getWriteOnly() != null) this.writeOnly = other.getWriteOnly();
        if (other.getMultipleOf() != null) this.multipleOf = other.getMultipleOf();
        if (other.getMinItems() != null) this.minItems = other.getMinItems();
        if (other.getMaxItems() != null) this.maxItems = other.getMaxItems();
        if (other.getUniqueItems() != null) this.uniqueItems = other.getUniqueItems();
        if (other.getMinContains() != null) this.minContains = other.getMinContains();
        if (other.getMaxContains() != null) this.maxContains = other.getMaxContains();
        if (other.getMinProperties() != null) this.minProperties = other.getMinProperties();
        if (other.getMaxProperties() != null) this.maxProperties = other.getMaxProperties();
        if (other.getContentEncoding() != null) this.contentEncoding = other.getContentEncoding();
        if (other.getContentMediaType() != null) this.contentMediaType = other.getContentMediaType();
        if (other.getHtmlDescription() != null) this.htmlDescription = other.getHtmlDescription();
        if (other.getItems() != null) this.items = other.getItems();

        this.enumValues = mergeSets(this.enumValues, other.getEnumValues());
        this.examples = mergeLists(this.examples, other.getExamples());
        this.prefixItems = mergeLists(this.prefixItems, other.getPrefixItems());
        this.requiredProperties = mergeSets(this.requiredProperties, other.getRequiredProperties());
        this.allOf = mergeLists(this.allOf, other.getAllOf());
        this.anyOf = mergeLists(this.anyOf, other.getAnyOf());
        this.oneOf = mergeLists(this.oneOf, other.getOneOf());

        this.properties = mergeMaps(this.properties, other.getProperties());
        this.dependentRequired = mergeMaps(this.dependentRequired, other.getDependentRequired());
        this.dependentSchemas = mergeMaps(this.dependentSchemas, other.getDependentSchemas());
        this.patternProperties = mergeMaps(this.patternProperties, other.getPatternProperties());

        if (other.getContains() != null) {
            this.contains = this.contains != null ? this.contains.merge(other.getContains()) : other.getContains();
        }
        if (other.getPropertyNames() != null) {
            this.propertyNames = this.propertyNames != null ? this.propertyNames.merge(other.getPropertyNames()) : other.getPropertyNames();
        }
        if (other.getIfSchema() != null) {
            this.ifSchema = this.ifSchema != null ? this.ifSchema.merge(other.getIfSchema()) : other.getIfSchema();
        }
        if (other.getThenSchema() != null) {
            this.thenSchema = this.thenSchema != null ? this.thenSchema.merge(other.getThenSchema()) : other.getThenSchema();
        }
        if (other.getElseSchema() != null) {
            this.elseSchema = this.elseSchema != null ? this.elseSchema.merge(other.getElseSchema()) : other.getElseSchema();
        }
        if (other.getNot() != null) {
            this.not = this.not != null ? this.not.merge(other.getNot()) : other.getNot();
        }
        if (other.getContentSchema() != null) {
            this.contentSchema = this.contentSchema != null ? this.contentSchema.merge(other.getContentSchema()) : other.getContentSchema();
        }
        if (other.getxDeprecation() != null) {
            this.xDeprecation = other.getxDeprecation();
        }
        if (other.getAdditionalProperties() != null) {
            this.additionalProperties = other.getAdditionalProperties();
        }

        return this;
    }
}

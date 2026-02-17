package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Represents the supported data types in JSON Schema.
 * This enum maps Java types to their corresponding JSON Schema type representations
 * as defined in the JSON Schema 2020-12 specification.
 *
 * @see <a href="https://json-schema.org/draft/2020-12/json-schema-core.html#section-4.2.1">JSON Schema Core Types</a>
 */
@Getter
public enum JsonSchemaType {
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    OBJECT("object"),
    NULL("null");

    /**
     * The string representation of the JSON Schema type.
     */
    private final String value;

    /**
     * Constructs a JsonSchemaType enum constant with the specified string value.
     *
     * @param value The string representation of the JSON Schema type
     */
    JsonSchemaType(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the JSON Schema type.
     * This method is annotated with {@link JsonValue} to specify
     * the value to be used when serializing this enum constant to JSON.
     *
     * @return The string representation of the JSON Schema type
     */
    @JsonValue
    public String getJsonValue() {
        return value;
    }
}

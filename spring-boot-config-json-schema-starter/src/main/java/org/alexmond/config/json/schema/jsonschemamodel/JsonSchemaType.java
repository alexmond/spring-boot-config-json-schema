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

    private final String value;

    JsonSchemaType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getJsonValue() {
        return value;
    }
}

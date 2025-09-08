package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

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

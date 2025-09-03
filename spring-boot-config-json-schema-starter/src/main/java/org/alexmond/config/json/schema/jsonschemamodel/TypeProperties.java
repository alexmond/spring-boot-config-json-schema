package org.alexmond.config.json.schema.jsonschemamodel;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * JSON Schema type definitions
 *
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/type.html">JSON Schema Types</a>
 */
@Getter
enum JsonSchemaTypes {
    STRING("string"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    NUMBER("number"),
    OBJECT("object"),
    ARRAY("array");

    private final String value;

    JsonSchemaTypes(String value) {
        this.value = value;
    }

}

/**
 * JSON Schema string formats
 *
 * @see <a href="https://json-schema.org/understanding-json-schema/reference/string.html#format">JSON Schema String Formats</a>
 */
@Getter
enum JsonSchemaFormats {
    DATE("date"),
    DATE_TIME("date-time"),
    TIME("time"),
    DURATION("duration"),
    EMAIL("email"),
    IDN_EMAIL("idn-email"),
    HOSTNAME("hostname"),
    IDN_HOSTNAME("idn-hostname"),
    IPV4("ipv4"),
    IPV6("ipv6"),
    URI("uri"),
    URI_REFERENCE("uri-reference"),
    UUID("uuid"),
    REGEX("regex");

    private final String value;

    JsonSchemaFormats(String value) {
        this.value = value;
    }

}




@Builder
@Data
public class TypeProperties {
    /**
     * JSON Schema type
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/reference/type.html">Type</a>
     */
    JsonSchemaTypes type;

    /**
     * Regular expression pattern for string validation
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/reference/string.html#pattern">Pattern</a>
     */
    String pattern;

    /**
     * Reference to another schema definition
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/structuring.html#ref">Schema References</a>
     */
    String reference;

    /**
     * String format
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/reference/string.html#format">Format</a>
     */
    JsonSchemaFormats format;

    /**
     * Enumeration of allowed values
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/reference/enum.html">Enum</a>
     */
    List<String> enums;

    public Map<String, String> toMap() {
        Map<String, String> properties = new HashMap<>();
        if (type != null) properties.put("type", type.getValue());
        if (pattern != null) properties.put("pattern", pattern);
        if (reference != null) properties.put("$ref", reference);
        if (format != null) properties.put("format", format.getValue());
        if (enums != null && !enums.isEmpty()) properties.put("enum", String.join(",", enums));
        return properties;
    }
}


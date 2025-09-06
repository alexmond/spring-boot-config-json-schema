package org.alexmond.config.json.schema.jsonschemamodel;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Builder
@Data
public class TypeProperties {
    /**
     * JSON Schema type
     *
     * @see <a href="https://json-schema.org/understanding-json-schema/reference/type.html">Type</a>
     */
    JsonSchemaType type;

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
    JsonSchemaFormat format;

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


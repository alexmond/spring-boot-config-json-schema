package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.Map;

/**
 * Represents a hint provider that supplies additional metadata or validation rules
 * for configuration properties. Hint providers can be used to enhance the schema
 * generation process with custom validation logic or metadata.
 */
@Data
public class HintProvider {
    /**
     * The name of the hint provider that identifies its functionality
     * or the type of hints it provides.
     */
    private String name;

    /**
     * Additional parameters required by the hint provider to generate
     * appropriate hints or validation rules. The parameters are stored
     * as key-value pairs where the key is the parameter name and the
     * value can be of any type.
     */
    private Map<String, Object> parameters;
}

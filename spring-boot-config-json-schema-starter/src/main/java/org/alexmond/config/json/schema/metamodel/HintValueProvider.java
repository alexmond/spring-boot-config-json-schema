package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.Map;

/**
 * Represents a value provider configuration used for generating hints or suggestions
 * in configuration metadata. This class defines the name of the provider and its
 * associated parameters.
 */
@Data
public class HintValueProvider {
    /**
     * The name of the value provider that will be used to generate hints.
     */
    private String name;

    /**
     * Additional parameters required by the value provider.
     * The map contains parameter names as keys and their corresponding values.
     */
    private Map<String, Object> parameters;
}

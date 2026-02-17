package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Represents a configuration hint that provides metadata about configuration properties.
 * This class is used to define hints for property values, providers, and value providers
 * in the configuration system.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hint {
    /**
     * The name of the hint that identifies its purpose or category.
     */
    private String name;

    /**
     * List of predefined values associated with this hint.
     */
    private List<HintValue> values;

    /**
     * List of providers that can generate or supply hint information.
     */
    private List<HintProvider> providers;

    /**
     * List of value providers that can dynamically generate values for this hint.
     */
    private List<HintValueProvider> valueProviders;
}

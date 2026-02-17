package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

/**
 * Represents a hint value in the configuration metadata model.
 * Hints provide additional information about configuration properties,
 * including possible values, descriptions, and deprecation status.
 */
@Data
public class HintValue {
    /**
     * The value of the hint. Can be of various types (String, Boolean, Integer, etc.).
     */
    private String value;

    /**
     * Optional description providing additional information about the hint value.
     */
    private String description;

    /**
     * Optional flag indicating whether this hint value is deprecated.
     */
    private Boolean deprecated;

    /**
     * Optional explanation for why this hint value is deprecated.
     * Only relevant when deprecated is true.
     */
    private String reason;
}

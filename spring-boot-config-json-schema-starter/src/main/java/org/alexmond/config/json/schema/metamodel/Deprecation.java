package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Represents deprecation metadata for configuration properties.
 * This class holds information about deprecated configuration elements,
 * including the reason for deprecation, replacement options, severity level,
 * and the version since when the deprecation was introduced.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deprecation {
    /**
     * The reason why the configuration property is deprecated.
     */
    private String reason;

    /**
     * The suggested replacement property or alternative approach.
     */
    private String replacement;

    /**
     * The severity level of the deprecation.
     */
    private Level level;

    /**
     * The version since when the deprecation was introduced.
     */
    private String since;

    /**
     * Enumeration of possible deprecation severity levels.
     * Both uppercase and lowercase values are supported for backward compatibility.
     */
    public enum Level {
        @SuppressWarnings("unused") WARNING,
        ERROR,
        error,
        @SuppressWarnings("unused") warning
    }
}

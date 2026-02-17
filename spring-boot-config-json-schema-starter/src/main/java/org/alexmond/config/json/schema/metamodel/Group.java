package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Represents a configuration group that defines a specific set of related configuration properties.
 * This class is used in the JSON schema generation process to organize and describe configuration settings.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    /**
     * The name of the configuration group.
     */
    private String name;

    /**
     * The type of the configuration group.
     */
    private String type;

    /**
     * The source type from which this configuration group is derived.
     */
    private String sourceType;

    /**
     * The source method from which this configuration group is derived.
     */
    private String sourceMethod;

    /**
     * A description of the configuration group and its purpose.
     */
    private String description;
}

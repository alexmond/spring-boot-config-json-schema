package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the metadata structure for Spring Boot configuration properties.
 * This class serves as a container for configuration metadata including groups,
 * properties, hints, and ignored properties.
 */
@Data
public class BootConfigMeta {
    /**
     * List of configuration property groups.
     */
    private List<Group> groups = new ArrayList<>();

    /**
     * List of configuration properties.
     */
    private List<Property> properties = new ArrayList<>();

    /**
     * List of configuration hints providing additional metadata.
     */
    private List<Hint> hints = new ArrayList<>();

    /**
     * Container for properties that should be ignored during processing.
     */
    private Ignored ignored = new Ignored();

    /**
     * Retrieves a list of property names that should be ignored during processing.
     *
     * @return List of property names marked as ignored
     */
    public List<String> getIgnoredList() {
        return ignored.getProperties().stream().map(Ignored.Property::getName).toList();
    }
}

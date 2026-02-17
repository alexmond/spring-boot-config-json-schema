package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents configuration for properties that should be ignored during JSON Schema generation.
 * This class helps in specifying which properties should be excluded from the schema.
 */
@Data
public class Ignored {

    /**
     * List of properties that should be ignored during schema generation.
     * Each property is represented by its name.
     */
    private List<Property> properties = new ArrayList<>();

    /**
     * Represents a single property that should be ignored in the JSON Schema.
     * Contains the name of the property to be ignored.
     */
    @Data
    public static class Property {
        /**
         * The name of the property that should be ignored during schema generation.
         */
        private String name;
    }
}

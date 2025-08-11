package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

@Data
public class Property {
    private String name;
    private String type;
    private String description;
    private String sourceType;
    private Object defaultValue;
    private Boolean deprecated = null;
    private Deprecation deprecation;
    private Hint hint;
}
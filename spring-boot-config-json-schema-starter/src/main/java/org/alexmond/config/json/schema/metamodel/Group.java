package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

@Data
public class Group {
    private String name;
    private String type;
    private String sourceType;
    private String sourceMethod;
}

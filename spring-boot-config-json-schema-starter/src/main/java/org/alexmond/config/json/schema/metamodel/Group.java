package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    private String name;
    private String type;
    private String sourceType;
    private String sourceMethod;
    private String description;
}

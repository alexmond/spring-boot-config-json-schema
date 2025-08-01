package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

@Data
public class HintValue {
    private Object value;           // Value may be String, Boolean, Integer, etc.
    private String description;     // Optional description of the value
    private Boolean deprecated;     // Optional flag if the value is deprecated
    private String reason;          // Optional reason for deprecation
}

package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.Map;

@Data
public class HintValueProvider {
    private String name;
    private Map<String, Object> parameters;
}

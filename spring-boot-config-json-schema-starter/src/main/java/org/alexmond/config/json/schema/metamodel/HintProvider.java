package org.alexmond.config.json.schema.metamodel;

import lombok.Data;
import java.util.Map;

@Data
public class HintProvider {
    private String name;
    private Map<String, Object> parameters;
}

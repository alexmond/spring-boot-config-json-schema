package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hint {
    private String name;
    private List<HintValue> values;
    private List<HintProvider> providers;
    private List<HintValueProvider> valueProviders;
}

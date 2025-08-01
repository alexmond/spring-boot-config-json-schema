package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.List;

@Data
public class Hints {
    private String name;
    private List<HintValue> values;
    private List<HintProvider> providers;
    private List<HintValueProvider> valueProviders;
}

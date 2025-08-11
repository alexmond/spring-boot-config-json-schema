package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Ignored {

    private List<Property> properties = new ArrayList<>();

    @Data
    public static class Property {
        private String name;
    }
}

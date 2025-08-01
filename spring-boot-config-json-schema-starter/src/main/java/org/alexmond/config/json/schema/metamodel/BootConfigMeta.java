package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootConfigMeta {
    private List<Group> groups;
    private List<Property> properties;
    private List<Hints> hints;
    private Ignored ignored;

    public List<String> getIgnoredList() {
        List<String> result = new ArrayList<>();
        if (ignored == null || ignored.getProperties() == null ) {
            return null;
        }
        for ( Ignored.Property property : ignored.getProperties()) {
            result.add(property.getName());
        }
        return result;
    }

    public BootConfigMeta() {
        this.groups = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.hints = new ArrayList<>();
        this.ignored = new Ignored();
    }

}

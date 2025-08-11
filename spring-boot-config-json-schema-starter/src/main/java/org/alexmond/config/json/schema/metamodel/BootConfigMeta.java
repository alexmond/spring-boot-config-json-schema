package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BootConfigMeta {
    private List<Group> groups = new ArrayList<>();
    private List<Property> properties = new ArrayList<>();
    private List<Hint> hints = new ArrayList<>();
    private Ignored ignored = new Ignored();

    public List<String> getIgnoredList() {
        return ignored.getProperties().stream().map(Ignored.Property::getName).toList();
    }
}

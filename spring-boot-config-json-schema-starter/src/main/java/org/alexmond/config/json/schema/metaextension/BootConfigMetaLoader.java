package org.alexmond.config.json.schema.metaextension;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Group;
import org.alexmond.config.json.schema.metamodel.Hint;
import org.alexmond.config.json.schema.metamodel.Property;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@NoArgsConstructor
@Slf4j
public class BootConfigMetaLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public BootConfigMeta loadFromStream(InputStream stream) {
        log.debug("Loading configuration from input stream");
        BootConfigMeta config = null;
        config = mapper.readValue(stream, BootConfigMeta.class);
        return config;
    }

    public Map<String, Property> mergeConfig(List<BootConfigMeta> metaList) {
        BootConfigMeta mergedConfig = new BootConfigMeta();
        Map<String, Property> propertyMap = new TreeMap<>();
        List<String> ignorelist = new ArrayList<>();

        for (var config : metaList) {
            mergedConfig.getGroups().addAll(config.getGroups());
            mergedConfig.getProperties().addAll(config.getProperties());
            mergedConfig.getHints().addAll(config.getHints());
            ignorelist.addAll(config.getIgnoredList());
        }

        for (Property property : mergedConfig.getProperties()) {
            if (ignorelist.contains(property.getName())) {
                log.warn("Ignored property name: {}, skipping", property.getName());
            } else {
                log.debug("Adding property {}", property.getName());
                Property existing = propertyMap.get(property.getName());
                if (existing == null) {
                    existing = new Property();
                }
                existing.mergeProperties(property);
                propertyMap.put(property.getName(), existing);
            }
        }

        for (Group group : mergedConfig.getGroups()) {
            if (ignorelist.contains(group.getName())) {
                log.warn("Ignored group property name: {}, skipping", group.getName());
            } else {
                log.debug("Adding group property {}", group.getName());
                Property existing = propertyMap.get(group.getName());
                if (existing == null) {
                    existing = new Property();
                }
                existing.mergeGroup(group);
                propertyMap.put(group.getName(), existing);
            }
        }

        for (Hint hint : mergedConfig.getHints()) {
            if (propertyMap.containsKey(hint.getName())) {
                Property existing = propertyMap.get(hint.getName());
                existing.setHint(hint);
                propertyMap.put(hint.getName(), existing);
            } else {
                log.debug("Missing property name for a hint: {}", hint.getName());
            }
        }
        return propertyMap;
    }


}

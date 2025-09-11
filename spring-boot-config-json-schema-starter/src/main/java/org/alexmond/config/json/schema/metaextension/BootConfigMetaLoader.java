package org.alexmond.config.json.schema.metaextension;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Group;
import org.alexmond.config.json.schema.metamodel.Hint;
import org.alexmond.config.json.schema.metamodel.Property;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@NoArgsConstructor
@Slf4j
public class BootConfigMetaLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public BootConfigMeta loadFromStream(InputStream stream) {
        log.debug("Loading configuration from input stream");
        BootConfigMeta config = null;
        try {
            config = mapper.readValue(stream, BootConfigMeta.class);
        } catch (IOException e) {
            log.error("Failed to load config metadata {}", e.getMessage());
        }
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
            if (!propertyMap.containsKey(property.getName()) && !ignorelist.contains(property.getName())) {
                propertyMap.put(property.getName(), property);
                log.debug("Adding property {} to config {}", property.getName(), property.getName());
            } else if (ignorelist.contains(property.getName())) {
                log.warn("Ignored property name: {}", property.getName());
            } else {
                Property existing = propertyMap.get(property.getName());
                if (existing != null) {
                    log.warn("Duplicate property name: {}, merging", property.getName());
                    existing.mergemergeProperties(property);
                    propertyMap.put(property.getName(), existing);
                }
            }
        }

        for (Group group : mergedConfig.getGroups()) {
            if (ignorelist.contains(group.getName())) {
                log.warn("Ignored property name: {}, skipping", group.getName());
//            } else if (group.getSourceMethod() != null) {
//                log.warn("Ignored group name: {}, group has SourceMethod", group.getName());
            } else{
                log.debug("Adding group property {} to config {}", group.getName(), group.getName());
                Property groupProperty = propertyMap.get(group.getName());
                if (groupProperty == null) {
                    groupProperty = new Property();
                }
                groupProperty.mergeGroup(group);
                propertyMap.put(group.getName(), groupProperty);
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

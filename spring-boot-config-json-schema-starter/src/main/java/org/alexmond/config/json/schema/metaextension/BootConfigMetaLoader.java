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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@Slf4j
public class BootConfigMetaLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public BootConfigMeta loadFromStream(InputStream stream){
        log.debug("Loading configuration from input stream");
        BootConfigMeta config = null;
        try {
            config = mapper.readValue(stream, BootConfigMeta.class);
        } catch (IOException e) {
            log.error("Failed to load config metadata {}", e.getMessage());
        }
        return config;
    }

    public HashMap<String, Property> mergeConfig(List<BootConfigMeta> metaList) {
        BootConfigMeta mergedConfig = new BootConfigMeta();
        HashMap<String, Property> propertyMap = new HashMap<>();
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
                    if (!propertyMap.containsKey(group.getName()) && !ignorelist.contains(group.getName())) {
                        var groupProperty = new Property();
                        groupProperty.mergeGroup(group);
                        propertyMap.put(group.getName(), groupProperty);
                        log.debug("Adding property {} to config {}", group.getName(), group.getName());
                    } else if (ignorelist.contains(group.getName())) {
                        log.warn("Ignored property name: {}", group.getName());
                    } else {
                        Property existing = propertyMap.get(group.getName());
                        if (existing != null) {
                            log.warn("Duplicate property name: {}, merging", group.getName());
                            existing.mergeGroup(group);
                            propertyMap.put(group.getName(), existing);
                        }
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

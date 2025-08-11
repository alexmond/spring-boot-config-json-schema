package org.alexmond.config.json.schema.metaextension;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Hint;
import org.alexmond.config.json.schema.metamodel.Property;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
            } else {
                log.error("Duplicate or Ignored property name: {}", property.getName());
            }
        }

        for (Hint hint : mergedConfig.getHints()) {
            if (propertyMap.containsKey(hint.getName())) {
                Property property = propertyMap.get(hint.getName());
                property.setHint(hint);
            } else {
                log.debug("Missing property name for a hint: {}", hint.getName());
            }
        }
        return propertyMap;
    }

}

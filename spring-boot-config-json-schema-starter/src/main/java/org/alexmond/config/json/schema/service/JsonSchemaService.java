package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Property;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
public class JsonSchemaService {

    private final ConfigurationPropertyCollector propertyCollector;
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;
    private final BootConfigMetaLoader bootConfigMetaLoader = new BootConfigMetaLoader();

    public String generateFullSchema() {
        HashMap<String,Property> meta = collectMetadata();
        List<String> included = propertyCollector.collectIncludedPropertyNames();
        
        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        SvetaSchema svetaSchema = schemaBuilder.buildSvetaSchema(meta, included);
        try {
            String s1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            String s2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(svetaSchema);

            if (s1.equals(s2)) log.debug("===== EQUALS");
            else log.debug("===== DIFF");

            return s1;
        } catch (JsonProcessingException e) {
            log.error("Can't generate full schema", e);
            return null;
        }
    }

    private HashMap<String,Property> collectMetadata() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<BootConfigMeta> configs = new ArrayList<>();
        classLoader.resources("META-INF/spring-configuration-metadata.json")
                .forEach(url -> {
                    try (InputStream inputStream = url.openStream()) {
                        configs.add(bootConfigMetaLoader.loadFromStream(inputStream));
                    } catch (IOException e) {
                        log.error("Failed to open stream for {} with error {}", url, e.getMessage());
                    }
                });
        return bootConfigMetaLoader.mergeConfig(configs);
    }
}

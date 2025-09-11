package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Property;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
public class JsonSchemaService {

    private final JsonConfigSchemaConfig config;
    private final ConfigurationPropertyCollector propertyCollector;
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;
    private final MissingTypeCollector missingTypeCollector;
    private final BootConfigMetaLoader bootConfigMetaLoader = new BootConfigMetaLoader();

    public String generateFullSchema() throws Exception {
        Map<String,Property> meta = collectMetadata();
        List<String> included = propertyCollector.collectIncludedPropertyNames();
        
        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        if(config.getMissingTypeLog())
            missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing types: {}",type));
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }

    public Map<String,Property> collectMetadata() {

        List<BootConfigMeta> configs = new ArrayList<>();
        var resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
        Resource[] resources = null;

        try {
            resources = resolver.getResources("classpath*:/META-INF/spring-configuration-metadata.json");
        } catch (IOException e) {
            log.error("Failed to open resources with error {}", e.getMessage());
            throw new RuntimeException(e);
        }

        for (var r : resources) {
            try (InputStream in = r.getInputStream()) {
                configs.add(bootConfigMetaLoader.loadFromStream(in));
            } catch (Exception e) {
                log.error("Failed to read {}", r, e);
            }
        }
        return bootConfigMetaLoader.mergeConfig(configs);
    }
    }

package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import java.util.List;
import java.util.Map;


/**
 * Service responsible for generating JSON Schema from Spring Boot configuration metadata.
 * This service collects configuration properties and builds a JSON Schema that describes
 * the valid configuration options for a Spring Boot application.
 */
@Slf4j
@RequiredArgsConstructor
public class JsonSchemaService {

    private final JsonConfigSchemaConfig config;
    private final ConfigurationPropertyCollector propertyCollector;
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;
    private final MissingTypeCollector missingTypeCollector;
    private final BootConfigMetaLoader bootConfigMetaLoader = new BootConfigMetaLoader();

    /**
     * Generates a complete JSON Schema representation of the application's configuration properties.
     *
     * @return A string containing the JSON Schema in pretty-printed JSON format
     * @throws Exception if schema generation fails
     */
    public String generateFullSchema() throws Exception {
        Map<String, Property> meta = collectMetadata();
        List<String> included = propertyCollector.collectIncludedPropertyNames();

        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        if (config.getMissingTypeLog())
            missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing types: {}", type));
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }

    /**
     * Generates a complete JSON Schema representation of the application's configuration properties
     * and converts it to YAML format.
     *
     * @return A string containing the JSON Schema in pretty-printed YAML format
     * @throws Exception if schema generation fails
     */
    public String generateFullSchemaYaml() throws Exception {

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

        Map<String, Property> meta = collectMetadata();
        List<String> included = propertyCollector.collectIncludedPropertyNames();

        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        if (config.getMissingTypeLog())
            missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing types: {}", type));
        return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }

    /**
     * Collects metadata from all spring-configuration-metadata.json files found in the classpath.
     * This method scans for configuration metadata files and merges their contents into a single
     * property map.
     *
     * @return A map of property names to their corresponding Property objects
     * @throws RuntimeException if resource scanning fails
     */
    public Map<String, Property> collectMetadata() {

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

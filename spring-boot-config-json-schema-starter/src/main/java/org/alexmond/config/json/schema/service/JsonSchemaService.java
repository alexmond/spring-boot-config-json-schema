package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Property;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
    private final MissingTypeCollector missingTypeCollector;
    private final BootConfigMetaLoader bootConfigMetaLoader = new BootConfigMetaLoader();
    private JsonSchemaRoot schemaCache;

    public JsonSchemaRoot getSchemaCache() {
        if (schemaCache == null) {
            Map<String, Property> meta = collectMetadata();
            List<String> included = propertyCollector.collectIncludedPropertyNames();

            schemaCache = schemaBuilder.buildSchema(meta, included);
         }
        return schemaCache;
    }
    /**
     * Generates a complete JSON Schema representation of the application's configuration properties.
     *
     * @return A string containing the JSON Schema in pretty-printed JSON format
     */
    public String generateFullSchemaJson() {
        return generateFullSchema(new ObjectMapper());
    }

    /**
     * Generates a complete JSON Schema representation of the application's configuration properties
     * and converts it to YAML format.
     *
     * @return A string containing the JSON Schema in pretty-printed YAML format
     */
    public String generateFullSchemaYaml() {
        return generateFullSchema(new ObjectMapper(new YAMLFactory()));
    }

    /**
     * Generates a complete JSON Schema representation using the specified ObjectMapper.
     * This method implements the core schema generation logic and supports both JSON and YAML output formats.
     * The generated schema is cached to avoid redundant generation of the same schema.
     *
     * @param mapper The ObjectMapper instance to use for serialization (can be configured for JSON or YAML output)
     * @return A string containing the schema in the format determined by the provided ObjectMapper
     */
    private String generateFullSchema(ObjectMapper mapper) {
        if (schemaCache == null) {
            Map<String, Property> meta = collectMetadata();
            List<String> included = propertyCollector.collectIncludedPropertyNames();

            schemaCache = schemaBuilder.buildSchema(meta, included);
            if (config.getMissingTypeLog())
                missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing types: {}", type));
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaCache);
        } catch (JsonProcessingException e) {
            log.error("Failed to generate schema", e);
            return "";
        }
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
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;

        try {
            resources = resolver.getResources("classpath*:/META-INF/spring-configuration-metadata.json");
        } catch (IOException e) {
            log.error("Failed to open resources with error {}", e.getMessage());
            throw new RuntimeException(e);
        }

        List<BootConfigMeta> configs = new ArrayList<>();
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

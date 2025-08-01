package org.alexmond.config.json.schema.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.metamodel.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class JsonSchemaService {

    private final ConfigurationPropertyCollector propertyCollector;
    private final ConfigurationMetadataService metadataService;
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;
    private final BootConfigMetaLoader bootConfigMetaLoader;

    public JsonSchemaService(ConfigurationPropertyCollector propertyCollector,
                           ConfigurationMetadataService metadataService,
                           JsonSchemaBuilder schemaBuilder,
                           ObjectMapper mapper, BootConfigMetaLoader bootConfigMetaLoader) {
        this.propertyCollector = propertyCollector;
        this.metadataService = metadataService;
        this.schemaBuilder = schemaBuilder;
        this.mapper = mapper;
        this.bootConfigMetaLoader = bootConfigMetaLoader;
    }

    public String generateFullSchema() throws Exception {
        HashMap<String,Property> meta = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        meta = bootConfigMetaLoader.loadFromInputStreams(metadataService.collectMetadataStreams(classLoader));
        List<String> included = propertyCollector.collectIncludedPropertyNames();
        
        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }
}

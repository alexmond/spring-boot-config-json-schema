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
    private final ConfigurationMetadataService metadataService = new ConfigurationMetadataService();
    private final JsonSchemaBuilder schemaBuilder;
    private final ObjectMapper mapper;
    private final BootConfigMetaLoader bootConfigMetaLoader = new BootConfigMetaLoader();

    public JsonSchemaService(ConfigurationPropertyCollector propertyCollector,
                           JsonSchemaBuilder schemaBuilder,
                           ObjectMapper mapper) {
        this.propertyCollector = propertyCollector;
        this.schemaBuilder = schemaBuilder;
        this.mapper = mapper;
    }

    public String generateFullSchema() throws Exception {
        HashMap<String,Property> meta;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        meta = bootConfigMetaLoader.loadFromInputStreams(metadataService.collectMetadataStreams(classLoader));
        List<String> included = propertyCollector.collectIncludedPropertyNames();
        
        Map<String, Object> schema = schemaBuilder.buildSchema(meta, included);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    }
}

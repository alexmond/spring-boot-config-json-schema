package org.alexmond.config.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.metaextension.BootConfigMetaLoader;
import org.alexmond.config.json.schema.service.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

@Component
public class ConfigSchemaStarter {

    @Bean
    public JsonConfigSchemaConfig  jsonConfigSchemaConfig() {
        return new JsonConfigSchemaConfig();
    }

    @Bean
    public ConfigurationMetadataService configurationMetadataService() {
        return new ConfigurationMetadataService();
    }

    @Bean
    public ConfigurationPropertyCollector configurationPropertyCollector(ApplicationContext context,
                                                                         ConfigurableEnvironment env,
                                                                         JsonConfigSchemaConfig config) {
        return new ConfigurationPropertyCollector(context, env, config);
    }

    @Bean
    public JsonSchemaBuilder jsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        return new JsonSchemaBuilder(config, typeMappingService);
    }

    @Bean
    public JsonSchemaService jsonSchemaService(ConfigurationPropertyCollector propertyCollector,
                                               ConfigurationMetadataService metadataService,
                                               JsonSchemaBuilder schemaBuilder,
                                               ObjectMapper mapper, BootConfigMetaLoader bootConfigMetaLoader) {
        return new JsonSchemaService(propertyCollector, metadataService, schemaBuilder, mapper, bootConfigMetaLoader);
    }

    @Bean
    public MissingTypeCollector missingTypeCollector() {
        return new MissingTypeCollector();
    }

    @Bean
    public TypeMappingService typeMappingService(MissingTypeCollector missingTypeCollector) {
        return new TypeMappingService(missingTypeCollector);
    }

    @Bean
    public BootConfigMetaLoader bootConfigMetaLoader() {
        return new BootConfigMetaLoader();
    }
    
}

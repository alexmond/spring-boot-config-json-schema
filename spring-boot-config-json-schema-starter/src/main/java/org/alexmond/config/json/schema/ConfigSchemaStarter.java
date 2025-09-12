package org.alexmond.config.json.schema;

import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.service.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Configuration class that sets up the JSON Schema generation components for Spring Boot configuration.
 * This starter automatically configures all necessary beans for generating JSON Schema
 * representations of application configuration properties.
 */
@Configuration
public class ConfigSchemaStarter {

    /**
     * Creates the configuration bean for JSON Schema generation settings.
     *
     * @return A new instance of JsonConfigSchemaConfig with default settings
     */
    @Bean
    public JsonConfigSchemaConfig jsonConfigSchemaConfig() {
        return new JsonConfigSchemaConfig();
    }

    /**
     * Creates a collector for configuration properties from the Spring environment.
     *
     * @param context The Spring application context
     * @param env     The Spring environment containing configuration properties
     * @param config  The JSON schema configuration settings
     * @return A new ConfigurationPropertyCollector instance
     */
    @Bean
    public ConfigurationPropertyCollector configurationPropertyCollector(ApplicationContext context,
                                                                         ConfigurableEnvironment env,
                                                                         JsonConfigSchemaConfig config) {
        return new ConfigurationPropertyCollector(context, env, config);
    }

    /**
     * Creates a collector for tracking missing type information during schema generation.
     *
     * @return A new MissingTypeCollector instance
     */
    @Bean
    public MissingTypeCollector missingTypeCollector() {
        return new MissingTypeCollector();
    }

    /**
     * Creates a service for mapping Java types to JSON Schema types.
     *
     * @param missingTypeCollector Collector for tracking missing type information
     * @return A new TypeMappingService instance
     */
    @Bean
    public TypeMappingService typeMappingService(MissingTypeCollector missingTypeCollector) {
        return new TypeMappingService(missingTypeCollector, jsonConfigSchemaConfig());
    }

    /**
     * Creates a builder for constructing JSON Schema documents.
     *
     * @param config             Configuration settings for JSON Schema generation
     * @param typeMappingService Service for mapping Java types to JSON Schema types
     * @return A new JsonSchemaBuilder instance
     */
    @Bean
    public JsonSchemaBuilder jsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        return new JsonSchemaBuilder(config, typeMappingService);
    }

    /**
     * Creates the main service for generating JSON Schema documents from configuration properties.
     *
     * @param config               Configuration settings for JSON Schema generation
     * @param propertyCollector    Collector for configuration properties
     * @param schemaBuilder        Builder for JSON Schema documents
     * @param missingTypeCollector Collector for tracking missing type information
     * @return A new JsonSchemaService instance
     */
    @Bean
    public JsonSchemaService jsonSchemaService(JsonConfigSchemaConfig config,
                                               ConfigurationPropertyCollector propertyCollector,
                                               JsonSchemaBuilder schemaBuilder,
                                               MissingTypeCollector missingTypeCollector) {
        return new JsonSchemaService(config, propertyCollector, schemaBuilder, missingTypeCollector);
    }

}

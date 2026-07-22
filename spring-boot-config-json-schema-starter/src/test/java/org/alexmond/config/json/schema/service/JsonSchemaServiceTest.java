package org.alexmond.config.json.schema.service;

import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JsonSchemaService}. The metadata collector and schema builder are mocked so
 * the caching and serialization logic can be exercised without a full Spring context.
 */
class JsonSchemaServiceTest {

    private final JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();

    private ConfigurationPropertyCollector collector() {
        ConfigurationPropertyCollector collector = mock(ConfigurationPropertyCollector.class);
        when(collector.collectIncludedPropertyNames()).thenReturn(List.of("logging"));
        return collector;
    }

    private JsonSchemaBuilder builderReturning(JsonSchemaRoot root) {
        JsonSchemaBuilder builder = mock(JsonSchemaBuilder.class);
        when(builder.buildSchema(any(), any())).thenReturn(root);
        return builder;
    }

    private JsonSchemaRoot sampleRoot() {
        return JsonSchemaRoot.builder().type(JsonSchemaType.OBJECT).title("t").build();
    }

    @Test
    void getSchemaCacheBuildsOnceAndCaches() {
        JsonSchemaRoot root = sampleRoot();
        JsonSchemaBuilder builder = builderReturning(root);
        JsonSchemaService service = new JsonSchemaService(config, collector(), builder, new MissingTypeCollector());

        assertSame(root, service.getSchemaCache());
        assertSame(root, service.getSchemaCache());
        verify(builder, times(1)).buildSchema(any(), any());
    }

    @Test
    void generateFullSchemaJsonSerializesRoot() {
        JsonSchemaService service = new JsonSchemaService(
                config, collector(), builderReturning(sampleRoot()), new MissingTypeCollector());
        String json = service.generateFullSchemaJson();
        assertNotNull(json);
        assertTrue(json.contains("object"), () -> "expected object type in: " + json);
    }

    @Test
    void generateFullSchemaYamlSerializesRoot() {
        JsonSchemaService service = new JsonSchemaService(
                config, collector(), builderReturning(sampleRoot()), new MissingTypeCollector());
        String yaml = service.generateFullSchemaYaml();
        assertNotNull(yaml);
        assertTrue(yaml.contains("object"), () -> "expected object type in: " + yaml);
    }
}

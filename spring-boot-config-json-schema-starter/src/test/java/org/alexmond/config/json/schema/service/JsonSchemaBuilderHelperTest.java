package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link JsonSchemaBuilderHelper}, focused on OpenAPI ({@code @Schema})
 * annotation processing.
 */
class JsonSchemaBuilderHelperTest {

    private JsonSchemaBuilderHelper newHelper() {
        JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
        return new JsonSchemaBuilderHelper(config, new TypeMappingService(new MissingTypeCollector(), config));
    }

    private Field field(String name) throws NoSuchFieldException {
        return Annotated.class.getDeclaredField(name);
    }

    /**
     * {@code @Schema(example = ...)} must be emitted as an example when the property has
     * none yet (previously the guard required a non-null-but-empty list, so it never fired).
     */
    @Test
    void appliesOpenapiExampleWhenNoneExists() throws NoSuchFieldException {
        JsonSchemaProperties props = new JsonSchemaProperties();
        newHelper().processOpenapi(props, field("port"), "port");
        assertEquals(List.of("8080"), props.getExamples());
    }

    /**
     * {@code @Schema(defaultValue = ...)} must fill a missing default (previously the guard
     * required an existing default, so an absent one stayed absent).
     */
    @Test
    void appliesOpenapiDefaultWhenAbsent() throws NoSuchFieldException {
        JsonSchemaProperties props = new JsonSchemaProperties();
        assertNull(props.getDefaultValue());
        newHelper().processOpenapi(props, field("port"), "port");
        assertEquals("8080", props.getDefaultValue());
    }

    private static final class Annotated {

        @Schema(example = "8080", defaultValue = "8080")
        private Integer port;

    }

}

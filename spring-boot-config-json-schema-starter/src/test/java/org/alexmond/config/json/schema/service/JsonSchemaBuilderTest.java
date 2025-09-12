package org.alexmond.config.json.schema.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonSchemaBuilderTest {

    @Test
    void testExtractMapValueTypeWithGenericMap() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String mapType = "java.util.Map<java.lang.String, java.lang.Integer>";

        // Act
        String result = builder.extractMapValueType(mapType);

        // Assert
        assertEquals("java.lang.Integer", result);
    }

    @Test
    void testExtractMapValueTypeWithNestedMap() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String mapType = "java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>";

        // Act
        String result = builder.extractMapValueType(mapType);

        // Assert
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", result);
    }

    @Test
    void testExtractMapValueTypeWithoutGenerics() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String mapType = "java.util.Map";

        // Act
        String result = builder.extractMapValueType(mapType);

        // Assert
        assertEquals("java.util.Map", result);
    }

    @Test
    void testExtractMapValueTypeWithInvalidTypeFormat() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String invalidType = "InvalidTypeFormat";

        // Act
        String result = builder.extractMapValueType(invalidType);

        // Assert
        assertEquals("InvalidTypeFormat", result);
    }

    @Test
    void testExtractMapValueTypeWithPrimitiveValueType() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String mapType = "java.util.Map<java.lang.String, int[]>";

        // Act
        String result = builder.extractMapValueType(mapType);

        // Assert
        assertEquals("int[]", result);
    }

    @Test
    void testToKebabCaseWithCamelCaseInput() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "camelCaseInput";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("camel-case-input", result);
    }

    @Test
    void testToKebabCaseWithSingleWordInput() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "word";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("word", result);
    }

    @Test
    void testToKebabCaseWithUpperCaseLettersInInput() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "CamelCASEInput";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("camel-c-a-s-e-input", result);
    }

    @Test
    void testToKebabCaseWithEmptyInput() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testToKebabCaseWithNullInput() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);

        // Act
        String result = builder.toKebabCase(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToKebabCaseWithSpecialCharacters() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "HelloWorld_specialTest";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("hello-world_special-test", result);
    }

    @Test
    void testToKebabCaseWithConsecutiveUpperCaseLetters() {
        // Arrange
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String input = "HTTPRequest";

        // Act
        String result = builder.toKebabCase(input);

        // Assert
        assertEquals("h-t-t-p-request", result);
    }
}
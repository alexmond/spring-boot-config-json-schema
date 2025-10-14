package org.alexmond.config.json.schema.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JsonSchemaBuilderTest {

    /**
     * Tests the extraction of value type from Map generic type string.
     *
     * @param input    The input Map type string to parse
     * @param expected The expected value type string
     */
    @ParameterizedTest
    @CsvSource(value = {
            "java.util.Map<java.lang.String, java.lang.Integer>;java.lang.Integer",
            "java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>;java.util.Map<java.lang.String, java.lang.Integer>",
            "java.util.Map;java.util.Map",
            "InvalidTypeFormat;InvalidTypeFormat",
            "java.util.Map<java.lang.String, int[]>;int[]",
            ";"
    }, delimiter = ';')
    void testExtractMapValueType(String input, String expected) {
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String result = builder.extractMapValueType(input);
        assertEquals(expected, result);
    }

    /**
     * Tests the conversion of strings to kebab-case format.
     *
     * @param input    The input string to convert
     * @param expected The expected kebab-case string
     */
    @ParameterizedTest
    @CsvSource({
            "camelCaseInput, camel-case-input",
            "word, word",
            "CamelCASEInput, camel-c-a-s-e-input",
            ",",
            "HelloWorld_specialTest, hello-world_special-test",
            "HTTPRequest, h-t-t-p-request",
            "DeepLevel1, deep-level1"
    })
    void testToKebabCase(String input, String expected) {
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String result = builder.toKebabCase(input);
        assertEquals(expected, result);
    }

    /**
     * Tests the extraction of item type from List/Set generic type string.
     *
     * @param input    The input List/Set type string to parse
     * @param expected The expected item type string
     */
    @ParameterizedTest
    @CsvSource(value = {
            "java.util.List<java.lang.String>;java.lang.String",
            "java.util.List<java.util.List<java.lang.Integer>>;java.util.List<java.lang.Integer>",
            "java.util.List<java.util.Map<java.lang.String, java.lang.Integer>>;java.util.Map<java.lang.String, java.lang.Integer>",
            "java.util.Set<java.lang.Double>;java.lang.Double",
            "SimpleString;object",
            ";"
    }, delimiter = ';')
    void testExtractListItemType(String input, String expected) {
        JsonSchemaBuilder builder = new JsonSchemaBuilder(null, null);
        String result = builder.extractListItemType(input);
        assertEquals(expected, result);
    }
}
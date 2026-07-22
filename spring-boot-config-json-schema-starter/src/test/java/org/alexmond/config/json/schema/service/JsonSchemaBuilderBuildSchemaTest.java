package org.alexmond.config.json.schema.service;

import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.metamodel.Property;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration-style unit tests driving {@link JsonSchemaBuilder#buildSchema} with hand-crafted
 * metadata to reach the map/array/complex-type and reference-mode edge branches that the
 * full-metadata sanity test does not exercise.
 */
class JsonSchemaBuilderBuildSchemaTest {

    // --- fixtures reflected on by the builder -------------------------------------------------

    static class Empty {
    }

    static class Simple {
        String value;
    }

    static class Box<T> {
        String label;
    }

    static class Node {
        Node next;
        String name;
    }

    private static final String EMPTY = Empty.class.getName();
    private static final String SIMPLE = Simple.class.getName();
    private static final String BOX = Box.class.getName();
    private static final String NODE = Node.class.getName();

    private JsonSchemaBuilder builder(JsonConfigSchemaConfig config) {
        return new JsonSchemaBuilder(config, new TypeMappingService(new MissingTypeCollector(), config));
    }

    private JsonSchemaRoot build(JsonConfigSchemaConfig config, Property... props) {
        Map<String, Property> meta = new LinkedHashMap<>();
        for (Property p : props) {
            meta.put(p.getName(), p);
        }
        return builder(config).buildSchema(meta, List.of("myapp"));
    }

    private Property leaf(String name, String type) {
        return Property.builder().name(name).type(type).build();
    }

    /** The "myapp" object node's child schema for {@code leafName}. */
    private JsonSchemaProperties child(JsonSchemaRoot root, String leafName) {
        return root.getProperties().get("myapp").getProperties().get(leafName);
    }

    /** A property whose declared type is null is logged and skipped, not emitted. */
    @Test
    void nullPropertyTypeIsSkipped() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                Property.builder().name("myapp.foo").type(null).build());
        assertFalse(root.getProperties().get("myapp").getProperties().containsKey("foo"));
    }

    /** A non-resolvable sourceType is caught both when anchoring intermediate nodes and at the leaf. */
    @Test
    void unresolvableSourceTypeIsHandled() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                Property.builder().name("myapp.sub.leaf").type("java.lang.String")
                        .sourceType("com.nonexistent.Foo").build());
        assertNotNull(root.getProperties().get("myapp").getProperties().get("sub")
                .getProperties().get("leaf"));
    }

    /** {@code Map<String,Object>} degrades to open additionalProperties. */
    @Test
    void mapOfObjectValueUsesAdditionalProperties() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.m", "java.util.Map<java.lang.String, java.lang.Object>"));
        assertNotNull(child(root, "m").getAdditionalProperties());
    }

    /** A map whose value type is itself generic degrades to open additionalProperties. */
    @Test
    void mapOfGenericValueUsesAdditionalProperties() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.m", "java.util.Map<java.lang.String, " + BOX + ">"));
        assertNotNull(child(root, "m").getAdditionalProperties());
    }

    /** A map to an object with no usable fields falls back after the anchor is removed. */
    @Test
    void mapOfEmptyObjectFallsBack() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.m", "java.util.Map<java.lang.String, " + EMPTY + ">"));
        assertNotNull(child(root, "m").getAdditionalProperties());
    }

    /** {@code List<Object>} yields object-typed items. */
    @Test
    void arrayOfObjectUsesObjectItems() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.l", "java.util.List<java.lang.Object>"));
        assertNotNull(child(root, "l").getItems());
    }

    /** A list of a generic type yields object-typed items. */
    @Test
    void arrayOfGenericUsesObjectItems() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.l", "java.util.List<" + BOX + ">"));
        assertNotNull(child(root, "l").getItems());
    }

    /** A list of an object with no usable fields sets items but removes the anchor. */
    @Test
    void arrayOfEmptyObjectRemovesAnchor() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.l", "java.util.List<" + EMPTY + ">"));
        assertNotNull(child(root, "l").getItems());
    }

    /**
     * A self-referential complex type is guarded against infinite recursion. Both reference modes
     * are disabled so the leaf recurses into {@code processComplexType} instead of short-circuiting
     * on an emitted {@code $ref}.
     */
    @Test
    void cyclicComplexTypeIsGuarded() {
        JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
        config.setEnableAnchorRefs(false);
        config.setEnableDefinitionRefs(false);
        JsonSchemaRoot root = build(config, leaf("myapp.node", NODE));
        assertNotNull(child(root, "node"));
    }

    /**
     * A single-segment property whose object node already exists (created by an earlier nested
     * property) re-enters the leaf branch as a duplicate rather than being dropped.
     */
    @Test
    void duplicateLeafReusesExistingNode() {
        JsonSchemaRoot root = build(new JsonConfigSchemaConfig(),
                leaf("myapp.value", "java.lang.String"),
                Property.builder().name("myapp").type(SIMPLE).sourceType(SIMPLE).build());
        assertNotNull(root.getProperties().get("myapp"));
    }

    /** A complex type on the additional-exclude list is not expanded. */
    @Test
    void excludedComplexTypeIsSkipped() {
        JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
        config.setAdditionalExcludeClasses(new java.util.ArrayList<>(List.of(SIMPLE)));
        JsonSchemaRoot root = build(config, leaf("myapp.s", SIMPLE));
        // expanded properties are suppressed for the excluded type
        assertNull(child(root, "s").getProperties());
    }

    /** With anchor-ref mode, a repeated anchored type emits a {@code #anchor} reference. */
    @Test
    void anchorRefModeEmitsHashReference() {
        JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
        config.setEnableAnchorRefs(true);
        config.setEnableDefinitionRefs(false);
        JsonSchemaRoot root = build(config, leaf("myapp.a", SIMPLE), leaf("myapp.b", SIMPLE));
        assertTrue(child(root, "b").getReference().startsWith("#"));
    }

    /** With both reference modes off, a repeated anchored type is expanded inline (no reference). */
    @Test
    void noReferenceModesLeaveTypeInline() {
        JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
        config.setEnableAnchorRefs(false);
        config.setEnableDefinitionRefs(false);
        JsonSchemaRoot root = build(config, leaf("myapp.a", SIMPLE), leaf("myapp.b", SIMPLE));
        assertNull(child(root, "b").getReference());
    }
}

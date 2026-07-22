package org.alexmond.config.json.schema.jsonschemamodel;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JsonSchemaProperties#merge(JsonSchemaProperties)} and its private
 * list/set/map merge helpers, exercising the null-handling and nested-merge branches.
 */
class JsonSchemaPropertiesMergeTest {

	private JsonSchemaProperties leaf(String desc) {
		return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).description(desc).build();
	}

	/** {@code merge(null)} is a no-op and returns the same instance. */
	@Test
	void mergeWithNullReturnsThis() {
		JsonSchemaProperties base = leaf("a");
		assertSame(base, base.merge(null));
		assertEquals("a", base.getDescription());
	}

	/**
	 * Scalar and nested fields from {@code other} overwrite an empty base;
	 * list1/set1/map1-null helper paths.
	 */
	@Test
	void mergeIntoEmptyBaseTakesOtherValues() {
		JsonSchemaProperties base = new JsonSchemaProperties();
		JsonSchemaProperties other = JsonSchemaProperties.builder()
			.type(JsonSchemaType.OBJECT)
			.description("desc")
			.pattern("p")
			.format(JsonSchemaFormat.EMAIL)
			.minimum(1)
			.maximum(9)
			.enumValues(Set.of("x"))
			.examples(List.of("e"))
			.properties(Map.of("k", leaf("v")))
			.dependentRequired(Map.of("a", Set.of("b")))
			.contains(leaf("c"))
			.propertyNames(leaf("n"))
			.ifSchema(leaf("if"))
			.thenSchema(leaf("then"))
			.elseSchema(leaf("else"))
			.not(leaf("not"))
			.contentSchema(leaf("cs"))
			.xDeprecation(XDeprecation.builder().reason("r").build())
			.additionalProperties(Boolean.TRUE)
			.build();

		base.merge(other);

		assertEquals(JsonSchemaType.OBJECT, base.getType());
		assertEquals("desc", base.getDescription());
		assertEquals(Set.of("x"), base.getEnumValues());
		assertEquals(List.of("e"), base.getExamples());
		assertTrue(base.getProperties().containsKey("k"));
		assertEquals("c", base.getContains().getDescription());
		assertEquals("if", base.getIfSchema().getDescription());
		assertEquals(Boolean.TRUE, base.getAdditionalProperties());
		assertEquals("r", base.getxDeprecation().getReason());
	}

	/**
	 * Both sides populated: sets/lists union, maps merge recursively, nested schemas
	 * merge in place.
	 */
	@Test
	void mergeBothPopulatedUnionsAndMergesNested() {
		JsonSchemaProperties base = JsonSchemaProperties.builder()
			.enumValues(new java.util.LinkedHashSet<>(Set.of("a")))
			.examples(new java.util.ArrayList<>(List.of("x")))
			.properties(new java.util.HashMap<>(Map.of("p", leaf("baseP"))))
			.dependentRequired(new java.util.HashMap<>(Map.of("d", Set.of("1"))))
			.contains(leaf("baseC"))
			.propertyNames(leaf("baseN"))
			.ifSchema(leaf("baseIf"))
			.thenSchema(leaf("baseThen"))
			.elseSchema(leaf("baseElse"))
			.not(leaf("baseNot"))
			.contentSchema(leaf("baseCs"))
			.build();
		JsonSchemaProperties other = JsonSchemaProperties.builder()
			.enumValues(Set.of("b"))
			.examples(List.of("y"))
			.properties(Map.of("p", JsonSchemaProperties.builder().title("mergedTitle").build()))
			.dependentRequired(Map.of("d2", Set.of("2")))
			.contains(JsonSchemaProperties.builder().title("cT").build())
			.propertyNames(JsonSchemaProperties.builder().title("nT").build())
			.ifSchema(JsonSchemaProperties.builder().title("ifT").build())
			.thenSchema(JsonSchemaProperties.builder().title("thenT").build())
			.elseSchema(JsonSchemaProperties.builder().title("elseT").build())
			.not(JsonSchemaProperties.builder().title("notT").build())
			.contentSchema(JsonSchemaProperties.builder().title("csT").build())
			.build();

		base.merge(other);

		assertTrue(base.getEnumValues().containsAll(Set.of("a", "b")));
		assertTrue(base.getExamples().containsAll(List.of("x", "y")));
		// shared key "p" merged: base description kept, other title grafted on
		assertEquals("baseP", base.getProperties().get("p").getDescription());
		assertEquals("mergedTitle", base.getProperties().get("p").getTitle());
		// second dependentRequired key added via the non-JsonSchemaProperties (else)
		// branch
		assertTrue(base.getDependentRequired().containsKey("d"));
		assertTrue(base.getDependentRequired().containsKey("d2"));
		// nested schemas kept base desc but merged in other's title (this.X.merge branch)
		assertEquals("baseC", base.getContains().getDescription());
		assertEquals("cT", base.getContains().getTitle());
		assertEquals("ifT", base.getIfSchema().getTitle());
		assertEquals("thenT", base.getThenSchema().getTitle());
		assertEquals("elseT", base.getElseSchema().getTitle());
		assertEquals("notT", base.getNot().getTitle());
		assertEquals("csT", base.getContentSchema().getTitle());
	}

	/**
	 * {@code other} with null collections / empty list must leave the base values
	 * untouched.
	 */
	@Test
	void mergeKeepsBaseWhenOtherCollectionsNull() {
		JsonSchemaProperties base = JsonSchemaProperties.builder()
			.enumValues(new java.util.LinkedHashSet<>(Set.of("keep")))
			.examples(new java.util.ArrayList<>(List.of("keepEx")))
			.properties(new java.util.HashMap<>(Map.of("k", leaf("keepP"))))
			.build();
		// other: examples explicitly empty (mergeLists list2.isEmpty branch), sets/maps
		// null
		JsonSchemaProperties other = JsonSchemaProperties.builder().examples(List.of()).build();

		base.merge(other);

		assertEquals(Set.of("keep"), base.getEnumValues());
		assertEquals(List.of("keepEx"), base.getExamples());
		assertTrue(base.getProperties().containsKey("k"));
	}

}

package org.alexmond.config.json.schema.jsonschemamodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link XDeprecation#isEmpty()}. */
class XDeprecationTest {

	@Test
	void isEmptyWhenAllFieldsBlank() {
		assertTrue(new XDeprecation().isEmpty());
	}

	@Test
	void isNotEmptyWhenAnyFieldSet() {
		assertFalse(XDeprecation.builder().reason("gone").build().isEmpty());
		assertFalse(XDeprecation.builder().replacement("other").build().isEmpty());
		assertFalse(XDeprecation.builder().since("1.0").build().isEmpty());
		assertFalse(XDeprecation.builder().level("ERROR").build().isEmpty());
	}

}

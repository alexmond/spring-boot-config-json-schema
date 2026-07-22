package org.alexmond.config.json.schema.metaextension;

import org.alexmond.config.json.schema.metamodel.BootConfigMeta;
import org.alexmond.config.json.schema.metamodel.Property;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link BootConfigMetaLoader}. */
class BootConfigMetaLoaderTest {

	private BootConfigMeta load(String json) {
		return new BootConfigMetaLoader()
			.loadFromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
	}

	/** Malformed JSON surfaces as an (unchecked) Jackson parse exception. */
	@Test
	void loadFromStreamThrowsOnInvalidJson() {
		assertThrows(RuntimeException.class, () -> load("this is not json"));
	}

	@Test
	void loadFromStreamParsesValidJson() {
		BootConfigMeta meta = load("{\"properties\":[{\"name\":\"a.b\",\"type\":\"java.lang.String\"}]}");
		assertNotNull(meta);
		assertEquals(1, meta.getProperties().size());
	}

	/**
	 * Properties and groups whose names appear in the ignored list are skipped during
	 * merge.
	 */
	@Test
	void mergeConfigSkipsIgnoredPropertiesAndGroups() {
		String json = "{"
				+ "\"groups\":[{\"name\":\"kept.group\",\"type\":\"java.lang.String\"},{\"name\":\"ignored.group\"}],"
				+ "\"properties\":[{\"name\":\"kept.prop\",\"type\":\"java.lang.String\"},"
				+ "{\"name\":\"ignored.prop\",\"type\":\"java.lang.String\"}],"
				+ "\"hints\":[{\"name\":\"kept.prop\",\"values\":[{\"value\":\"v1\"}]},{\"name\":\"no.such.prop\"}],"
				+ "\"ignored\":{\"properties\":[{\"name\":\"ignored.prop\"},{\"name\":\"ignored.group\"}]}" + "}";
		BootConfigMeta meta = load(json);
		assertNotNull(meta);

		Map<String, Property> merged = new BootConfigMetaLoader().mergeConfig(List.of(meta));

		assertTrue(merged.containsKey("kept.prop"));
		assertTrue(merged.containsKey("kept.group"));
		assertFalse(merged.containsKey("ignored.prop"));
		assertFalse(merged.containsKey("ignored.group"));
		// hint matching a known property is attached; the orphan hint is dropped
		assertNotNull(merged.get("kept.prop").getHint());
	}

}

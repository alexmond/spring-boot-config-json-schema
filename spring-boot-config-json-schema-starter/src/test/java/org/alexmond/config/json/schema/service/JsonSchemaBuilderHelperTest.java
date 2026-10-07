package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	 * none yet (previously the guard required a non-null-but-empty list, so it never
	 * fired).
	 */
	@Test
	void appliesOpenapiExampleWhenNoneExists() throws NoSuchFieldException {
		JsonSchemaProperties props = new JsonSchemaProperties();
		newHelper().processOpenapi(props, field("port"), "port");
		assertEquals(List.of("8080"), props.getExamples());
	}

	/**
	 * {@code @Schema(defaultValue = ...)} must fill a missing default (previously the
	 * guard required an existing default, so an absent one stayed absent).
	 */
	@Test
	void appliesOpenapiDefaultWhenAbsent() throws NoSuchFieldException {
		JsonSchemaProperties props = new JsonSchemaProperties();
		assertNull(props.getDefaultValue());
		newHelper().processOpenapi(props, field("port"), "port");
		assertEquals("8080", props.getDefaultValue());
	}

	/** {@code @Size(min, max)} maps to minLength/maxLength on the schema property. */
	@Test
	void appliesSizeConstraints() throws NoSuchFieldException {
		JsonSchemaProperties props = new JsonSchemaProperties();
		newHelper().processValidated(props, field("sized"), "sized");
		assertEquals(2, props.getMinLength());
		assertEquals(10, props.getMaxLength());
	}

	/** {@code @Schema(deprecated = true)} sets the deprecated flag. */
	@Test
	void appliesDeprecatedFromSchema() throws NoSuchFieldException {
		JsonSchemaProperties props = new JsonSchemaProperties();
		newHelper().processOpenapi(props, field("deprecatedField"), "deprecatedField");
		assertTrue(props.getDeprecated());
	}

	/** A non-enum class yields no enum values. */
	@Test
	void processEnumItemReturnsNullForNonEnum() {
		assertNull(newHelper().processEnumItem(String.class));
	}

	/**
	 * Enum values come out in declaration order, each followed by its lowercase form,
	 * whatever the default locale is.
	 */
	@Test
	void processEnumItemKeepsDeclarationOrder() {
		Locale previous = Locale.getDefault();
		Locale.setDefault(Locale.forLanguageTag("tr-TR"));
		try {
			assertEquals(List.of("ZULU", "zulu", "INDIA", "india", "Mixed", "mixed"),
					List.copyOf(newHelper().processEnumItem(Sample.class)));
		}
		finally {
			Locale.setDefault(previous);
		}
	}

	private enum Sample {

		ZULU, INDIA, MIXED {
			@Override
			public String toString() {
				return "Mixed";
			}
		}

	}

	private static final class Annotated {

		@Schema(example = "8080", defaultValue = "8080")
		private Integer port;

		@Size(min = 2, max = 10)
		private String sized;

		@Schema(deprecated = true)
		private String deprecatedField;

	}

}

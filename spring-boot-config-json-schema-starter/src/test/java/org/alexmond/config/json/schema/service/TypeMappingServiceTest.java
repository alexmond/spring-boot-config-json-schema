package org.alexmond.config.json.schema.service;

import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.metamodel.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link TypeMappingService#typeProp} covering the less-common branches of
 * the type-switch (binary, temporal, {@code Class}), the missing-{@code java.lang}
 * fallback, and the custom / extended override maps.
 */
class TypeMappingServiceTest {

	private TypeMappingService service(JsonConfigSchemaConfig config) {
		return new TypeMappingService(new MissingTypeCollector(), config);
	}

	private TypeMappingService service() {
		return service(new JsonConfigSchemaConfig());
	}

	@ParameterizedTest
	@CsvSource({ "byte[],STRING", "java.lang.Byte[],STRING", "byte,INTEGER", "java.lang.Byte,INTEGER",
			"java.time.Instant,STRING", "java.time.OffsetDateTime,STRING", "java.time.LocalDateTime,STRING",
			"java.util.Calendar,STRING", "java.time.LocalDate,STRING", "java.time.LocalTime,STRING",
			"java.time.OffsetTime,STRING", "java.time.Duration,STRING", "java.lang.Class,STRING", })
	void mapsSwitchTypes(String springType, JsonSchemaType expected) {
		assertEquals(expected, service().typeProp(springType, null).getType());
	}

	/** {@code byte[]} additionally carries base64 content encoding. */
	@Test
	void mapsBinaryArrayWithBase64Encoding() {
		JsonSchemaProperties props = service().typeProp("byte[]", null);
		assertEquals("base64", props.getContentEncoding());
	}

	/**
	 * A {@code java.lang.*} type that isn't in the switch and isn't an array/map/enum
	 * falls through to the "missing primitive" branch and is emitted as a string.
	 */
	@Test
	void mapsUnknownJavaLangTypeToString() {
		assertEquals(JsonSchemaType.STRING, service().typeProp("java.lang.StringBuilder", null).getType());
	}

	/** A custom mapping keyed by the property name wins over the type switch. */
	@Test
	void customMappingByPropertyNameOverridesType() {
		JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
		JsonSchemaProperties custom = JsonSchemaProperties.builder().type(JsonSchemaType.BOOLEAN).build();
		config.getJsonSchemaPropertiesMap().put("my.flag", custom);
		Property prop = Property.builder().name("my.flag").build();
		assertSame(custom, service(config).typeProp("java.lang.String", prop));
	}

	/** A custom mapping keyed by the Spring type wins over the type switch. */
	@Test
	void customMappingByTypeOverridesType() {
		JsonConfigSchemaConfig config = new JsonConfigSchemaConfig();
		JsonSchemaProperties custom = JsonSchemaProperties.builder().type(JsonSchemaType.NUMBER).build();
		config.getJsonSchemaPropertiesMap().put("java.lang.String", custom);
		assertSame(custom, service(config).typeProp("java.lang.String", null));
	}

	/**
	 * The built-in extended mapping for {@code java.util.Locale} emits a {@code $ref}.
	 */
	@Test
	void extendedMappingEmitsLocaleRef() {
		JsonSchemaProperties props = service().typeProp("java.util.Locale", null);
		assertEquals("#/$defs/java.util.Locale", props.getReference());
	}

	/** {@code extendedTypeProp(null, null)} returns null (both selectors absent). */
	@Test
	void extendedTypePropReturnsNullWhenNothingProvided() throws Exception {
		Method m = TypeMappingService.class.getDeclaredMethod("extendedTypeProp", String.class, Property.class);
		m.setAccessible(true);
		assertNull(m.invoke(service(), null, null));
	}

}

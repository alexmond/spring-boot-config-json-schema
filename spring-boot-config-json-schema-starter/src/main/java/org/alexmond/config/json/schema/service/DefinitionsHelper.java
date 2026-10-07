package org.alexmond.config.json.schema.service;

import lombok.RequiredArgsConstructor;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.springframework.boot.logging.LogLevel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for creating standard JSON Schema definitions for common Java types. This
 * class provides predefined schema definitions for logger levels, locales, and character
 * sets that can be reused across the schema. The definitions are the same on every JDK.
 */
@RequiredArgsConstructor
public class DefinitionsHelper {

	/**
	 * Example locales, taken from the constants of {@link java.util.Locale}. Fixed on
	 * purpose so the generated schema does not depend on the running JDK.
	 */
	static final List<String> LOCALE_EXAMPLES = List.of("en", "en_US", "en_GB", "en_CA", "fr", "fr_FR", "fr_CA", "de",
			"de_DE", "it", "it_IT", "ja", "ja_JP", "ko", "ko_KR", "zh", "zh_CN", "zh_TW");

	/**
	 * The charsets every implementation of the Java platform must support, see
	 * {@link java.nio.charset.StandardCharsets}.
	 */
	static final List<String> CHARSET_EXAMPLES = List.of("UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "ISO-8859-1",
			"US-ASCII");

	private final JsonSchemaBuilderHelper helper;

	/**
	 * Creates standard schema definitions for common types like logger levels, locales,
	 * and charsets.
	 * @return Map of named schema definitions
	 */
	public Map<String, JsonSchemaProperties> getDefinitions() {

		Map<String, JsonSchemaProperties> definitions = new LinkedHashMap<>();

		definitions.put("loggerLevel", getLoggerLevelDef());
		definitions.put("loggerLevelProp", getLoggerLevelPropDef());
		definitions.put("java.util.Locale", getLocalesDef());
		definitions.put("java.nio.charset.Charset", getCharsetsDef());

		return definitions;
	}

	/**
	 * Creates a JSON Schema definition for logger levels.
	 * @return JSON Schema properties defining the possible logger level values
	 */
	private JsonSchemaProperties getLoggerLevelDef() {
		return JsonSchemaProperties.builder()
			.type(JsonSchemaType.STRING)
			.enumValues(helper.processEnumItem(LogLevel.class))
			.build();
	}

	/**
	 * Creates a JSON Schema definition for logger level properties. This definition
	 * allows for nested logger level configurations.
	 * @return JSON Schema properties defining the structure of logger level properties
	 */
	private JsonSchemaProperties getLoggerLevelPropDef() {
		return JsonSchemaProperties.builder()
			.type(JsonSchemaType.OBJECT)
			.additionalProperties(JsonSchemaProperties.builder()
				.oneOf(List.of(JsonSchemaProperties.builder().reference("#/$defs/loggerLevel").build(),
						JsonSchemaProperties.builder().reference("#/$defs/loggerLevelProp").build()))
				.build())
			.build();
	}

	/**
	 * Creates a JSON Schema definition for Java Locales. A locale is a free-form string:
	 * Spring accepts both {@code en_US} and the BCP 47 form {@code en-US}, and the set of
	 * locales a runtime knows differs between JDK releases, so the definition does not
	 * enumerate them. A fixed list of examples keeps editor completion useful.
	 * @return JSON Schema properties describing a locale value
	 */
	private JsonSchemaProperties getLocalesDef() {
		return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).examples(LOCALE_EXAMPLES).build();
	}

	/**
	 * Creates a JSON Schema definition for character sets. A charset is a free-form
	 * string: names are case-insensitive, have aliases, and the available set depends on
	 * the JDK and the platform, so the definition does not enumerate them. The examples
	 * are the charsets every Java platform is required to support.
	 * @return JSON Schema properties describing a charset value
	 */
	private JsonSchemaProperties getCharsetsDef() {
		return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).examples(CHARSET_EXAMPLES).build();
	}

}

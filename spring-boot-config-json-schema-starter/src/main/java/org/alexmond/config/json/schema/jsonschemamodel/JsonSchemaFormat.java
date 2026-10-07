package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enum representing the standard formats defined in JSON Schema specification. These
 * formats are used to validate string values against specific patterns or rules.
 */
@Getter
public enum JsonSchemaFormat {

	/** Combined date and time with timezone. */
	DATE_TIME("date-time"),
	/** Time with optional timezone. */
	TIME("time"),
	/** Full date without time. */
	DATE("date"),
	/** Time duration. */
	DURATION("duration"),
	/** Email address. */
	EMAIL("email"),
	/** Internationalized email address. */
	IDN_EMAIL("idn-email"),
	/** Internet hostname. */
	HOSTNAME("hostname"),
	/** Internationalized hostname. */
	IDN_HOSTNAME("idn-hostname"),
	/** IPv4 address. */
	IPV4("ipv4"),
	/** IPv6 address. */
	IPV6("ipv6"),
	/** Universal Resource Identifier. */
	URI("uri"),
	/** URI reference including relative URIs. */
	URI_REFERENCE("uri-reference"),
	/** Internationalized URI. */
	IRI("iri"),
	/** Internationalized URI reference. */
	IRI_REFERENCE("iri-reference"),
	/** Universally Unique Identifier. */
	UUID("uuid"),
	/** JSON Pointer string. */
	JSON_POINTER("json-pointer"),
	/** Relative JSON Pointer string. */
	RELATIVE_JSON_POINTER("relative-json-pointer");

	/**
	 * The string representation of the format as defined in JSON Schema specification.
	 */
	private final String value;

	/**
	 * Constructs a new JsonSchemaFormat enum constant.
	 * @param value the string representation of the format
	 */
	JsonSchemaFormat(String value) {
		this.value = value;
	}

	/**
	 * Returns the string representation of the format for JSON serialization.
	 * @return the format string value
	 */
	@JsonValue
	public String getJsonValue() {
		return value;
	}

}

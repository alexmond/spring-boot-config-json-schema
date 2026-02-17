package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enum representing the standard formats defined in JSON Schema specification.
 * These formats are used to validate string values against specific patterns or rules.
 */
@Getter
public enum JsonSchemaFormat {
    DATE_TIME("date-time"),      // Combined date and time with timezone
    TIME("time"),                // Time with optional timezone
    DATE("date"),               // Full date without time
    DURATION("duration"),        // Time duration
    EMAIL("email"),             // Email address
    IDN_EMAIL("idn-email"),     // Internationalized email address
    HOSTNAME("hostname"),        // Internet hostname
    IDN_HOSTNAME("idn-hostname"), // Internationalized hostname
    IPV4("ipv4"),              // IPv4 address
    IPV6("ipv6"),              // IPv6 address
    URI("uri"),                // Universal Resource Identifier
    URI_REFERENCE("uri-reference"), // URI reference including relative URIs
    IRI("iri"),                // Internationalized URI
    IRI_REFERENCE("iri-reference"), // Internationalized URI reference
    UUID("uuid"),              // Universally Unique Identifier
    JSON_POINTER("json-pointer"), // JSON Pointer string
    RELATIVE_JSON_POINTER("relative-json-pointer"); // Relative JSON Pointer string

    /**
     * The string representation of the format as defined in JSON Schema specification.
     */
    private final String value;

    /**
     * Constructs a new JsonSchemaFormat enum constant.
     *
     * @param value the string representation of the format
     */
    JsonSchemaFormat(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the format for JSON serialization.
     *
     * @return the format string value
     */
    @JsonValue
    public String getJsonValue() {
        return value;
    }
}

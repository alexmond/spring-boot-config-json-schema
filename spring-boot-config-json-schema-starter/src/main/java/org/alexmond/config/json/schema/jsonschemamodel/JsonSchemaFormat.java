package org.alexmond.config.json.schema.jsonschemamodel;

import lombok.Getter;

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

    private final String value;

    JsonSchemaFormat(String value) {
        this.value = value;
    }
}

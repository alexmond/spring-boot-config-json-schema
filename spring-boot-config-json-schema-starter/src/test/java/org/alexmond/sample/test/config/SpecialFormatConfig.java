package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "specialtype")
@Validated
@Data
@Schema(description = "Configuration sample class")
public class SpecialFormatConfig {
    /**
     * Unique identifier in UUID format
     */
    @Schema(description = "Unique identifier in UUID format")
    UUID uuid = UUID.randomUUID();

    /**
     * URI resource locator
     */
    @Schema(description = "URI resource locator")
    URI uri = URI.create("https://example.com");

    /**
     * URL resource locator
     */
    @Schema(description = "URL resource locator")
    URL url;

    /**
     * IPv4 address
     */
    @Schema(description = "IPv4 address")
    Inet4Address inet4Address;

    /**
     * IPv6 address
     */
    @Schema(description = "IPv6 address")
    Inet6Address inet6Address;

    /**
     * Email address
     */
    @Schema(description = "Email address")
    @Email
    String email;

    /**
     * Email address
     */
    @Schema(description = "Email address",format = "email")
    String emailSchemaFormat;

    /**
     * Email address with pattern validation
     */
    @Schema(description = "Email address with pattern validation")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    String emailPattern;
}

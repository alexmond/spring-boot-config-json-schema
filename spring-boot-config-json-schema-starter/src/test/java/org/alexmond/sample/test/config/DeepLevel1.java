package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration class representing the first level of nested configuration properties.
 * Used for testing nested configuration structure in the Spring Boot application.
 */
@Data
@Schema(description = "First level of nested configuration properties")
public class DeepLevel1 {
    /**
     * Nested configuration properties for the second level of configuration.
     * Initialized with default DeepLevel2 instance.
     */
    @Schema(description = "Second level nested configuration properties")
    @NestedConfigurationProperty
    private DeepLevel2 keys = new DeepLevel2();
}

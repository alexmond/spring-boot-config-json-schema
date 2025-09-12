package org.alexmond.sample.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * <p>Configuration class representing the first level of nested configuration properties.</p>
 * <p>This class is used to maintain deeper level configuration settings through its nested properties.</p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>
 * deep-level1:
 *   deep-level2:
 *     // configuration properties
 * </pre>
 */
@Data
public class DeepLevel1 {
    /**
     * <p>Nested configuration property containing second level configuration settings.</p>
     * <p>Initialized with default instance of {@link DeepLevel2}.</p>
     *
     * <p><b>Configuration example:</b></p>
     * <pre>
     * deep-level2:
     *   // nested properties
     * </pre>
     */
    @NestedConfigurationProperty
    private DeepLevel2 deepLevel2 = new DeepLevel2();
}

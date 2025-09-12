package org.alexmond.sample.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Represents a deep level configuration object with multiple properties.
 * <p>
 * This class is used for nested configuration settings in the application.
 * </p>
 */
@Data
@Schema(description = """
        Represents a deep level configuration object with multiple properties.
        
        This class is used for nested configuration settings in the application.
        """)
public class DeepLevel3 {
    /**
     * The unique identifier for this configuration.
     * <p>Default value is an empty string.</p>
     */
    @Schema(description = """
            The unique identifier for this configuration.
            
            Default value is an empty string.
            """, defaultValue = "")
    private String id = "";

    /**
     * The first property of the configuration.
     * <p>Default value is "Sig".</p>
     */
    @Schema(description = """
            The first property of the configuration.
            
            Default value is "Sig".
            """, defaultValue = "Sig")
    private String prop1 = "Sig";

    /**
     * The second property of the configuration.
     * <p>Default value is 123.</p>
     */
    @Schema(description = """
            The second property of the configuration.
            
            Default value is 123.
            """, defaultValue = "123")
    private Integer prop2 = 123;
}

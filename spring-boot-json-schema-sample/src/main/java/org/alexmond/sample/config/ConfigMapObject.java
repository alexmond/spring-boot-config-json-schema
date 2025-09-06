package org.alexmond.sample.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Configuration class containing nested configuration properties")
public class ConfigMapObject {
    /* Nested Config sample */
    @Schema(description = "First nested configuration property", example = "nestedConfig1")
    private String configMap1 = "nestedConfig1";

    @Schema(description = "Second nested configuration property", example = "123")
    private Integer configMap2 = 123 ;

}

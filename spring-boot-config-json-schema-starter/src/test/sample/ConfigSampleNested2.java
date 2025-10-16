package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Configuration class containing nested configuration properties")
public class ConfigSampleNested2 {
    /* Nested Config sample */
    @Schema(description = "First nested configuration property", example = "nestedConfig1")
    private String listNestedConfig1 = "nestedConfig1";

    @Schema(description = "Second nested configuration property", example = "123")
    private Integer listNestedConfig2 = 123;

    private ConfigSampleNested2 recursiveTest;

}

package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Represents the deepest level configuration class in the nested structure.
 * Contains identification and parameter settings.
 */
@Schema(description = "Deep level 3 configuration properties")
@Data
public class DeepLevel3 {
    @Schema(description = "Unique identifier", example = "123")
    private String id = "";
    @Schema(description = "First parameter", example = "param1")
    private String param1 = "param1";
    @Schema(description = "Second parameter", example = "param2")
    private String param2 = "param2";
}

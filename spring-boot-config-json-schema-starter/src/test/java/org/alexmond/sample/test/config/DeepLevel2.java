package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deep level 2 configuration class containing lists of DeepLevel3 objects.
 */
@Data
@Schema(description = "Deep level 2 configuration of 2 lists")
public class DeepLevel2 {
    /**
     * List1 configurations at level 3.
     */
    @Schema(description = "List1 configurations")
    @NestedConfigurationProperty
    private List<DeepLevel3> list1 = new ArrayList<>();

    /**
     * List2  configurations at level 3.
     */
    @Schema(description = "List2 configurations")
    @NestedConfigurationProperty
    private List<DeepLevel3> list2 = new ArrayList<>();

}

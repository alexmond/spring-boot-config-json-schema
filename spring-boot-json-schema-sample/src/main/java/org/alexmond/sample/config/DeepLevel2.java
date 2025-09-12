package org.alexmond.sample.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deep level 2 configuration class that contains nested configuration properties.
 * <p>
 * This class is used as part of the configuration hierarchy to hold collections of {@link DeepLevel3} objects.
 * </p>
 */
@Data
@Schema(description = """
        Deep level 2 configuration class that contains nested configuration properties.
        This class is used to organize and structure configuration data at the second level of nesting.
        Default: **empty lists**
        """)
public class DeepLevel2 {
    /**
     * First collection of nested {@link DeepLevel3} configuration objects.
     * <p>
     * This list maintains the first set of deep level 3 configuration entries.
     * </p>
     */
    @Schema(description = """
            First collection of nested DeepLevel3 configuration objects.
            This list maintains configuration entries for the first group of level 3 configurations.
            Example: **list1[0].id = "1234"**
            Default: **empty ArrayList**
            """)
    @NestedConfigurationProperty
    private List<DeepLevel3> list1 = new ArrayList<>();

    /**
     * Second collection of nested {@link DeepLevel3} configuration objects.
     * <p>
     * This list maintains the second set of deep level 3 configuration entries.
     * </p>
     */
    @Schema(description = """
            Second collection of nested DeepLevel3 configuration objects.
            This list maintains configuration entries for the second group of level 3 configurations.
            Example: **list2[0].id = "5678"**
            Default: **empty ArrayList**
            """)
    @NestedConfigurationProperty
    private List<DeepLevel3> list2 = new ArrayList<>();

}

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
@Schema(description = "Deep level 2 configuration containing RP and IDP lists")
public class DeepLevel2 {
    /**
     * List of Relying Party (RP) configurations at level 3.
     */
    @Schema(description = "List of Relying Party (RP) configurations")
    @NestedConfigurationProperty
    private List<DeepLevel3> rp = new ArrayList<>();

    /**
     * List of Identity Provider (IDP) configurations at level 3.
     */
    @Schema(description = "List of Identity Provider (IDP) configurations")
    @NestedConfigurationProperty
    private List<DeepLevel3> idp = new ArrayList<>();

}

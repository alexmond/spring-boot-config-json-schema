package org.alexmond.sample.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeepLevel2 {
    @NestedConfigurationProperty
    private List<DeepLevel3> rp = new ArrayList<>();

    @NestedConfigurationProperty
    private List<DeepLevel3> idp = new ArrayList<>();

}

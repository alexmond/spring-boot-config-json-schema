package org.alexmond.sample.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class DeepLevel1 {
    @NestedConfigurationProperty
    private DeepLevel2 keys = new DeepLevel2();
}

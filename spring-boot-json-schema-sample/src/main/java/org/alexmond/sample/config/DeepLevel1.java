package org.alexmond.sample.config;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class DeepLevel1 {
    @NestedConfigurationProperty
    private DeepLevel2 deepLevel2 = new DeepLevel2();
}

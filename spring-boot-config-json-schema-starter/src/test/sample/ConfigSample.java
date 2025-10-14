package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.*;


@Schema(description = "Sample type values")
enum SampleType {TYPE1, TYPE2, TYPE3}

@Schema(description = "Sample status values")
enum SampleStatus {ACTIVE, PENDING, INACTIVE}

@Component
@ConfigurationProperties(prefix = "sample")
@Validated
@Data
@Schema(description = "Configuration sample class")
public class ConfigSample {
    // Java doc style comments required for config metadata processor
    /**
     * String sample
     **/
    @Schema(description = "Sample string property", defaultValue = "stringSample")
    @NotNull
    String stringSample = "stringSample";
    /**
     * Boolean sample Default: true
     **/
    @Schema(description = "Sample boolean property", defaultValue = "true")
    Boolean booleanSample = true;

    /**
     * String array sample
     **/
    String[] stringArraySample;
    /**
     * Integer sample
     **/
    @Min(10)
    @Max(200)
    @Schema(description = "Sample integer property", minimum = "10", maximum = "200", defaultValue = "100")
    Integer integerSample = 100;

    ConfigSampleNested2 configSampleNested2;

}

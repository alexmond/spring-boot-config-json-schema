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


@Schema(description = "Sample enumeration values")
enum EnumSample {EN1, EN2, EN3}

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
    /** String sample **/
    @Schema(description = "Sample string property", defaultValue = "stringSample")
    @NotNull
    String stringSample = "stringSample";
    /**
     * Boolean sample
     **/
    @Schema(description = "Sample boolean property", defaultValue = "true")
    Boolean booleanSample = true;

    String[] stringArraySample;
    /**
     * Integer sample
     **/
    @Min(10)
    @Max(200)
    @Schema(description = "Sample integer property", minimum = "10", maximum = "200", defaultValue = "100")
    Integer integerSample = 100;

    @Schema(description = "Sample enum property", defaultValue = "EN1")
    EnumSample enumSample = EnumSample.EN1;

    @Schema(description = "Sample string collection")
    List<String> collectionSample = new ArrayList<>();
    @Schema(description = "Sample nested configuration collection")
    List<ConfigSampleNested2> configSampleObjectArray = new ArrayList<>();

    List<Integer> sampleIntArray = new ArrayList<>();

    @Schema(description = "Sample string map")
    Map<String, String> mapSample = new HashMap<>();

    @Schema(description = "Sample object map")
    Map<String, ConfigMapObject> mapObjectSample = new HashMap<>();

    /**
     * Nested class sample
     **/
    @NestedConfigurationProperty // required for config metadata processor
    @Schema(description = "Nested configuration sample")
    ConfigSampleNested configSampleNested = new ConfigSampleNested();

    @Schema(description = "Sample type set")
    EnumSet<SampleType> enumTypeSet = EnumSet.noneOf(SampleType.class);

    @Schema(description = "Sample status map")
    EnumMap<SampleStatus, String> enumStatusMap = new EnumMap<>(SampleStatus.class);

    Properties propertiesExample = new Properties();

    Map<String,String> propertiesMapExample = new HashMap<>();
    
}

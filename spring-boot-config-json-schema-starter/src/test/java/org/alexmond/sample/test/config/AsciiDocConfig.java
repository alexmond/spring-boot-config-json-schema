package org.alexmond.sample.test.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationProperties(prefix = "ascii-doc")
@Data
public class AsciiDocConfig {
    List<AsciiDocGroupConfig> asciiDocGroupConfigs = new LinkedList<>();
    List<String> excludes = new ArrayList<>();
    List<String> includes = new ArrayList<>();
}

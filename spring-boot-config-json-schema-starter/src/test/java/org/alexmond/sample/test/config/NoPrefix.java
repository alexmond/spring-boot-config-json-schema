package org.alexmond.sample.test.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@ConfigurationProperties
@Validated
@Data
@Schema(description = "No prefix config")
public class NoPrefix {
    String noPrefix;
    List<String> noPrefixList;
}

package org.alexmond.config.json.schema.metamodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deprecation {
    private String reason;
    private String replacement;
    private Level level;
    private String since;

    public enum Level {
        WARNING,
        ERROR,
        error,
        warning
    }
}

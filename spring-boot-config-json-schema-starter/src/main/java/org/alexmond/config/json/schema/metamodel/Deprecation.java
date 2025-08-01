package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

@Data
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

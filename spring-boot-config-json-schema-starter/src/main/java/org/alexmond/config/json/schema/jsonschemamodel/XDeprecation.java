package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Represents deprecation information for configuration properties.
 * This class holds metadata about deprecated properties including
 * the reason for deprecation, replacement property, deprecation date,
 * and severity level.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XDeprecation {
    /**
     * The reason why the property is deprecated.
     */
    private String reason;

    /**
     * The replacement property or configuration that should be used instead.
     */
    private String replacement;

    /**
     * The version or date since when the property was deprecated.
     */
    private String since;

    /**
     * The severity level of the deprecation (e.g., warning, error).
     */
    private String level;

    /**
     * Checks if this deprecation object contains any meaningful information.
     *
     * @return true if all fields are empty or null, false otherwise
     */
    @JsonIgnore
    public boolean isEmpty() {
        return (!StringUtils.hasLength(reason)) &&
                (!StringUtils.hasLength(replacement)) &&
                (!StringUtils.hasLength(since)) &&
                (!StringUtils.hasLength(level));
    }

}

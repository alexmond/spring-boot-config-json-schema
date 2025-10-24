package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XDeprecation {
    private String reason;
    private String replacement;
    private String since;
    private String level;

    @JsonIgnore
    public boolean isEmpty() {
        return (!StringUtils.hasLength(reason)) &&
                (!StringUtils.hasLength(replacement)) &&
                (!StringUtils.hasLength(since)) &&
                (!StringUtils.hasLength(level));
    }

}

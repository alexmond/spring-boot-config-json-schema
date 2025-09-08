package org.alexmond.config.json.schema.jsonschemamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XDeprication {
    private String reason;
    private String replacement;
    private String since;
    private String level;

    @JsonIgnore
    public boolean isEmpty() {
        return (StringUtils.isEmpty(reason)) &&
                (StringUtils.isEmpty(replacement)) &&
                (StringUtils.isEmpty(since)) &&
                (StringUtils.isEmpty(level));
    }

}

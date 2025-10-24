package org.alexmond.config.json.schema.metamodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Property {
    private String name;
    private String type;
    private String description;
    private String sourceType;
    private Object defaultValue;
    private Boolean deprecated;
    private Deprecation deprecation;
    private Hint hint;
    @Builder.Default
    private boolean groupProperty = false;

    public void mergeGroup(Group group) {
        groupProperty = true;
        if (StringUtils.hasLength(group.getName())) this.name = group.getName();
        if (StringUtils.hasLength(group.getType())) this.type = group.getType();
        if (StringUtils.hasLength(group.getDescription())) this.description = group.getDescription();
        if (StringUtils.hasLength(group.getSourceType())) this.sourceType = group.getSourceType();
    }

    public void mergeProperties(Property other) {
        if (StringUtils.hasLength(other.getName())) this.name = other.getName();
        if (StringUtils.hasLength(other.getType())) this.type = other.getType();
        if (StringUtils.hasLength(other.getDescription())) this.description = other.getDescription();
        if (StringUtils.hasLength(other.getSourceType())) this.sourceType = other.getSourceType();
        if (other.getDefaultValue() != null) this.defaultValue = other.getDefaultValue();
        if (other.getDeprecated() != null) this.deprecated = other.getDeprecated();
        if (other.getDeprecation() != null) this.deprecation = other.getDeprecation();
        if (other.getHint() != null) this.hint = other.getHint();
    }
}
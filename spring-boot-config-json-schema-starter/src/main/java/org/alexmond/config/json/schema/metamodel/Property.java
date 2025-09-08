package org.alexmond.config.json.schema.metamodel;

import lombok.Data;

@Data
public class Property {
    private String name;
    private String type;
    private String description;
    private String sourceType;
    private Object defaultValue;
    private Boolean deprecated = null;
    private Deprecation deprecation;
    private Hint hint;

    public void mergeGroup(Group group) {
        if (group.getName() != null && !group.getName().isEmpty()) this.name = group.getName();
        if (group.getType() != null && !group.getType().isEmpty()) this.type = group.getType();
        if (group.getDescription() != null && !group.getDescription().isEmpty())
            this.description = group.getDescription();
        if (group.getSourceType() != null && !group.getSourceType().isEmpty()) this.sourceType = group.getSourceType();
    }

    public void mergemergeProperties(Property other) {
        if (other.getType() != null && !other.getType().isEmpty()) this.type = other.getType();
        if (other.getDescription() != null && !other.getDescription().isEmpty())
            this.description = other.getDescription();
        if (other.getSourceType() != null && !other.getSourceType().isEmpty()) this.sourceType = other.getSourceType();
        if (other.getDefaultValue() != null) this.defaultValue = other.getDefaultValue();
        if (other.getDeprecated() != null) this.deprecated = other.getDeprecated();
        if (other.getDeprecation() != null) this.deprecation = other.getDeprecation();
        if (other.getHint() != null) this.hint = other.getHint();
    }
    
    

}
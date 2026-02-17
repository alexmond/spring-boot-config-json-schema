package org.alexmond.config.json.schema.metamodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Represents a configuration property with metadata.
 * This class holds information about configuration properties including their name, type,
 * description, default values, and deprecation status.
 *
 * @since 1.0
 */
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

    /**
     * Merges this property with a Group, inheriting the group's properties if they are not empty.
     * Sets this property as a group property and updates name, type, description, and sourceType
     * from the provided group if they have values.
     *
     * @param group the Group instance to merge with this property
     */
    public void mergeGroup(Group group) {
        groupProperty = true;
        if (StringUtils.hasLength(group.getName())) this.name = group.getName();
        if (StringUtils.hasLength(group.getType())) this.type = group.getType();
        if (StringUtils.hasLength(group.getDescription())) this.description = group.getDescription();
        if (StringUtils.hasLength(group.getSourceType())) this.sourceType = group.getSourceType();
    }

    /**
     * Merges this property with another Property instance, inheriting non-null values.
     * Updates all fields from the other property if they have values, including name,
     * type, description, sourceType, defaultValue, deprecated status, deprecation info,
     * and hints.
     *
     * @param other the Property instance to merge with this property
     */
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
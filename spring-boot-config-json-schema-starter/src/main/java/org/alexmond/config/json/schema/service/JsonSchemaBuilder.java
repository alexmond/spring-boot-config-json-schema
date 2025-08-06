
package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.metamodel.Deprecation;
import org.alexmond.config.json.schema.metamodel.Property;
import org.springframework.util.ReflectionUtils;

import java.util.*;

@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    public JsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        this.config = config;
        this.typeMappingService = typeMappingService;
    }

    public Map<String, Object> buildSchema(HashMap<String, Property> meta, List<String> included) {
        log.info("Starting JSON schema generation");
        Map<String, Object> schema = new LinkedHashMap<>();

        schema.put("$schema", config.getSchemaSpec());
        schema.put("$id", config.getSchemaId());
        schema.put("title", config.getTitle());
        schema.put("description", config.getDescription());
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        HashMap<String,Property> filteredMeta = new HashMap<>();
        meta.forEach((key, value) -> {
            if (matchesIncluded(key,included)) {
                filteredMeta.put(key,value);
            }
        });

        filteredMeta.forEach((key, value) -> {
            addProperty(properties, key.split("\\."), 0, value);
        });
        schema.put("properties", properties);

        return schema;
    }

    private void addProperty(Map<String, Object> node, String[] path, int idx, Property prop) {
        log.debug("Processing property at path: {}, index: {}", String.join(".", path), idx);
        if (node == null) {
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            Map<String, Object> propDef = new LinkedHashMap<>();
            // skip deprecated property with error level
            if(prop.getDeprecated() != null && prop.getDeprecated() && ( prop.getDeprecation().getLevel() == Deprecation.Level.ERROR || prop.getDeprecation().getLevel() == Deprecation.Level.error)){
                log.debug("Skipping property is deprecated and removed: {}", prop.getName());
                return;
            }
            if(prop.getType() == null) {
                log.error("property {} prop.type is null", prop.getName());
                return;
            }
            propDef.put("type", typeMappingService.mapType(prop.getType()));

//            if (prop.getShortDescription() != null) {
//                propDef.put("title", prop.getShortDescription());
//            }
//            if (prop.getDescription() != null && !prop.getDescription().equals(prop.getShortDescription())) {
//                propDef.put("description", prop.getDescription());
//            }
            if (prop.getDescription() != null ) {
                propDef.put("description", prop.getDescription());
            }
//            }else if (prop.getShortDescription() != null) {
//                propDef.put("description", prop.getShortDescription());
//            }
            if (prop.getDefaultValue() != null) {
                propDef.put("default", prop.getDefaultValue());
            }
            processHints(propDef, prop);
            processEnumValues(propDef, prop);
            processDeprecation(propDef, prop);

            if(config.isUseValidation()) {
                processValidated(propDef, prop);
            }
            if (config.isUseOpenapi()) {
                processOpenapi(propDef, prop);
            }

            if (typeMappingService.mapType(prop.getType()).equals("array")) {
                if(prop.getName().contains("enum-type-set")) {
                    log.debug("Skipping property is enum-type-set");
                }
                String itemType = typeMappingService.extractListItemType(prop.getType());
                Map<String, Object> items = new HashMap<>();
                items.put("type", typeMappingService.mapType(itemType));
                if (typeMappingService.mapType(itemType).equals("object")) {
                    Map<String, Object> complexProperties = typeMappingService.processComplexType(itemType, prop);
                    if (complexProperties != null) {
                        items.put("properties", complexProperties);
                    }
                }else {
                    try {
                        if(Class.forName(itemType).isEnum()){
                            List<String> values = processEnumItem(itemType);
                            if (values != null) {
                                items.put("enum", values);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        log.info(e.getMessage());
                    }
                }
                propDef.put("items", items);
            } else if (typeMappingService.mapType(prop.getType()).equals("object")) {
                if (prop.getType().startsWith("java.util.Map")) {
                    String valueType = typeMappingService.extractMapValueType(prop.getType());
                    if (typeMappingService.mapType(valueType).equals("object")) {
                        Map<String, Object> valueTypeProperties = typeMappingService.processComplexType(valueType,prop);
                        if (valueTypeProperties != null) {
                            propDef.put("additionalProperties", Map.of(
                                    "type", "object",
                                    "properties", valueTypeProperties
                            ));
                        }
                    } else {
                        propDef.put("additionalProperties", Map.of("type", typeMappingService.mapType(valueType)));
                    }
                } else {
                    Map<String, Object> complexProperties = typeMappingService.processComplexType(prop.getType(),prop);
                    if (complexProperties != null) {
                        propDef.put("properties", complexProperties);
                    }
                }
            }

            node.put(key, propDef);
        } else {
            Map<String, Object> obj = (Map<String, Object>) node.computeIfAbsent(key, k -> {
                Map<String, Object> nm = new HashMap<>();
                nm.put("type", "object");
                nm.put("properties", new HashMap<String, Object>());
                return nm;
            });
            Object propsObj = obj.get("properties");
            if (!(propsObj instanceof Map)) {
                propsObj = new HashMap<String, Object>();
                obj.put("properties", propsObj);
            }
            Map<String, Object> child = (Map<String, Object>) propsObj;
            addProperty(child, path, idx + 1, prop);
        }
    }

    private  List<String> processEnumItem(String enumType) {
        log.debug("Processing enum values for property: {}", enumType);
        if (enumType != null) {
            try {
                Class<?> type = Class.forName(enumType);
                if (type.isEnum()) {
                    Object[] enumConstants = type.getEnumConstants();
                    if (enumConstants != null) {
                        List<String> enumValues = Arrays.stream(enumConstants)
                                .flatMap(enumConstant -> Arrays.stream(new String[]{
                                        enumConstant.toString(),
                                        enumConstant.toString().toLowerCase()
                                }))
                                .toList();
                            return  enumValues;
                    }
                }
            } catch (ClassNotFoundException e) {
                return  null;
            }
        }
        return null;
    }

    private void processOpenapi(Map<String, Object> propDef, Property prop) {
        log.debug("OpenAPI: Processing schema for property: {}", prop.getName());
        if (prop.getSourceType() != null) {
            String propertyName = prop.getName();
            String lastField = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            String classField = toCamelCase(lastField);

            try {
                log.debug("Processing OpenAPI schema for property: {}, class: {}", prop.getName(), prop.getSourceType());
                Class<?> clazz = Class.forName(prop.getSourceType());
                var field = ReflectionUtils.findField(clazz, classField);
                if (field != null && field.isAnnotationPresent(Schema.class)) {
                    Schema schema = field.getAnnotation(Schema.class);
                    if (!schema.description().isEmpty()) {
                        propDef.put("description", schema.description());
                    }
                    if (!schema.example().isEmpty()) {
                        propDef.put("example", schema.example());
                    }
                    if (schema.deprecated()) {
                        propDef.put("deprecated", true);
                    }
//                    if (!schema.defaultValue().isEmpty()) {
//                        propDef.put("default", schema.defaultValue());
//                    }
                }
            } catch (Exception e) {
                log.debug("OpenAPI: Class or field {} not found for schema processing: {}", classField, e.toString());
                log.debug("For property: {}", prop);
            }
        }
    }

    private void processValidated(Map<String, Object> propDef, Property prop) {
        log.debug("Validation: Processing validation for property: {}", prop.getName());
        if (prop.getSourceType() != null ) {
            String propertyName = prop.getName();
            String lastField = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            String classField = toCamelCase(lastField);

            try {
                log.debug("Processing validation for property: {}, class: {}", prop.getName(), prop.getSourceType());
                Class<?> clazz = Class.forName(prop.getSourceType());
                log.debug("Processing validation for property: {}, field: {}", prop.getName(), classField);
                var field = ReflectionUtils.findField(clazz,classField);
                if (field != null) {

                    if (field.isAnnotationPresent(jakarta.validation.constraints.Min.class)) {
                        var value = field.getAnnotation(jakarta.validation.constraints.Min.class).value();
                        propDef.put("minimum", value);
                        log.info("Validation: Added Min validation with value {} for field {}", value, field.getName());
                    }
                    if (field.isAnnotationPresent(jakarta.validation.constraints.Max.class)) {
                        var value = field.getAnnotation(jakarta.validation.constraints.Max.class).value();
                        propDef.put("maximum", value);
                        log.info("Validation: Added Max validation with value {} for field {}", value, field.getName());
                    }
                    if (field.isAnnotationPresent(jakarta.validation.constraints.Size.class)) {
                        var size = field.getAnnotation(jakarta.validation.constraints.Size.class);
                        if (size.min() > 0) {
                            propDef.put("minLength", size.min());
                            log.info("Validation: Added Size.min validation with value {} for field {}", size.min(), field.getName());
                        }
                        if (size.max() < Integer.MAX_VALUE) {
                            propDef.put("maxLength", size.max());
                            log.info("Validation: Added Size.max validation with value {} for field {}", size.max(), field.getName());
                        }
                    }
                    if (field.isAnnotationPresent(jakarta.validation.constraints.Pattern.class)) {
                        var pattern = field.getAnnotation(jakarta.validation.constraints.Pattern.class).regexp();
                        propDef.put("pattern", pattern);
                        log.info("Validation: Added Pattern validation with regexp {} for field {}", pattern, field.getName());
                    }
                    if (field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class)) {
                        propDef.put("required", true);
                        log.info("Validation: Added NotNull validation for field {}", field.getName());
                    }
                    if (field.isAnnotationPresent(jakarta.validation.constraints.NotEmpty.class)) {
                        propDef.put("minLength", 1);
                        propDef.put("required", true);
                        log.info("Validation: Added NotEmpty validation for field {}", field.getName());
                    }
                }
            } catch (Exception e) {

                log.debug("Validation: Class or field {} not found for validation processing: {}", classField, e.toString());
                log.debug("For property: {}", prop);

            }
        }
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        for (String include : included) {
            if (propertyPath.startsWith(include)) return true;
        }
        return false;
    }

    private void processHints(Map<String, Object> propDef, Property prop) {
        if (prop.getName().contains("relaxed-query-chars")) {
            log.debug("Processing relaxed-query-chars property: {}", prop.getName());
        }
        log.debug("Processing hints for property: {}", prop.getName());
        if (prop.getHints() != null) {
            if (prop.getHints().getValues() != null && !prop.getHints().getValues().isEmpty()) {
                var hints = prop.getHints().getValues().stream()
                        .map(hint -> hint.getValue())
                        .filter(value -> value != null)
                        .toList();
                if (hints.size() > 1) {
                    log.debug("Property '{}' has multiple hints: {}", prop.getName(), hints);
                }
                propDef.put("examples", hints);
            }
        }
    }

    private void processEnumValues(Map<String, Object> propDef, Property prop) {
        log.debug("Processing enum values for property: {}", prop.getName());
        if (prop.getType() != null) {
            try {
                Class<?> type = Class.forName(prop.getType());
                if (type.isEnum()) {
                    Object[] enumConstants = type.getEnumConstants();
                    if (enumConstants != null) {
                        List<String> enumValues = Arrays.stream(enumConstants)
                                .flatMap(enumConstant -> Arrays.stream(new String[]{
                                        enumConstant.toString(),
                                        enumConstant.toString().toLowerCase()
                                }))
                                .toList();
                        propDef.put("enum", enumValues);
                        return;
                    }
                }
            } catch (ClassNotFoundException e) {
                if (prop.getType().contains("Enum")) {
                    if (prop.getHints() != null && prop.getHints().getValues() != null) {
                        List<String> enumValues = prop.getHints().getValues().stream()
                                .flatMap(hint -> Arrays.stream(new String[]{
                                        hint.getValue().toString(),
                                        hint.getValue().toString().toLowerCase()
                                }))
                                .toList();
                        if (!enumValues.isEmpty()) {
                            propDef.put("enum", enumValues);
                        }
                    }
                }
            }
        }
    }

    private void processDeprecation(Map<String, Object> propDef, Property prop) {
        log.debug("Processing deprecation for property: {}", prop.getName());
        if (prop.getDeprecated() != null && prop.getDeprecated()) {
            propDef.put("deprecated", true);
            if (prop.getDeprecation() != null) {
                if (prop.getDeprecation().getReason() != null) {
                    propDef.put("deprecationReason", prop.getDeprecation().getReason());
                }
                if (prop.getDeprecation().getReplacement() != null) {
                    propDef.put("deprecationReplacement", prop.getDeprecation().getReplacement());
                }
            }
        }
    }

    private String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '-' || currentChar == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }

        return result.toString();
    }


}

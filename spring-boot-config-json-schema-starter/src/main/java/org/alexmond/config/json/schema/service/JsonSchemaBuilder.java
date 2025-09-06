
package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.jsonschemamodel.TypeProperties;
import org.alexmond.config.json.schema.metamodel.Deprecation;
import org.alexmond.config.json.schema.metamodel.Property;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

import static org.apache.commons.text.CaseUtils.toCamelCase;

@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    Map<String, Object> definitions = new LinkedHashMap<>();

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

        schema.put("$defs", getDefinitions());


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

    private Map<String, Object> getDefinitions() {

        definitions.put("loggerLevel", getLoggerLevelDef());
        definitions.put("loggerLevelProp", getLoggerLevelPropDef());
        definitions.put("Locales", getLocalesDef());
        definitions.put("Charsets", getCharsetsDef());

        return definitions;
    }

    private Object getCharsetsDef() {
        return Map.of(
                "type", "string",
                "enum", new ArrayList<>(Charset.availableCharsets().values())
                );
    }

    private Object getLocalesDef() {
        return Map.of(
                "type", "string",
                "enum", Locale.getAvailableLocales());

    }
    private Map<String, Object> getLoggerLevelDef() {
        return Map.of(
                "type", "string",
                "enum", processEnumItem(LogLevel.class));
    }

    private Map<String, Object> getLoggerLevelPropDef() {
        return Map.of(
                "type", "object",
                "additionalProperties", Map.of(
                        "oneOf", List.of(
                                Map.of(
                                        "$ref", "#/$defs/loggerLevel"
                                ),
                                Map.of(
                                        "$ref", "#/$defs/loggerLevelProp"
                                )
                        )
                )
        );
    }


    private void addProperty(Map<String, Object> node, String[] path, int idx, Property prop) {
        log.debug("Processing property at path: {}, index: {}", String.join(".", path), idx);

        if (node == null) {
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            if(node.get(key) == null) {
                processLeaf(node, prop, key);
            } else {
                log.info("Duplicate leaf {}",key);
                if(prop.getDescription() != null) {
                    processLeaf(node, prop, key);
                }
            }
        } else {
            Map<String, Object> propNode;
            Map<String, Object> properties = new HashMap<>();;
            if (node.containsKey(key)) {
                propNode = (Map<String, Object>) node.get(key);
                Object propsObj = propNode.get("properties");
                if (propsObj instanceof Map) {
                    properties = (Map<String, Object>) propsObj;
                } else {
                    properties = new HashMap<>();
                    propNode.put("properties", properties);
                }
            } else {
                propNode = new HashMap<>();
                propNode.put("type", "object");
                propNode.put("properties", properties);
                node.put(key, propNode);
            }

            addProperty(properties, path, idx + 1, prop);
        }
    }


    private void processLeaf(Map<String, Object> node, Property prop, String key) {
        TypeProperties typeProperties;
        String propType;
        Class<?> clazz;
        Class<?> propClazz = null;
        Field field = null;

        Map<String, Object> propDef = new LinkedHashMap<>();

        // skip deprecated property with the error level
        if(prop.getDeprecated() != null && prop.getDeprecated() && ( prop.getDeprecation().getLevel() == Deprecation.Level.ERROR || prop.getDeprecation().getLevel() == Deprecation.Level.error)){
            log.debug("Skipping property is deprecated and removed: {}", prop.getName());
            return;
        }
        if(prop.getType() == null) {
            log.error("property {} prop.type is null", prop.getName());
            return;
        }else{
            propType= prop.getType();
            typeProperties = typeMappingService.typeProp(propType,prop);
        }
        propDef.putAll(typeProperties.toMap());

        if (prop.getSourceType() != null) {
            String propertyName = prop.getName();
            String lastField = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            String classField = toCamelCase(lastField, false, '-');

            try {
                clazz = Class.forName(prop.getSourceType());
                field = ReflectionUtils.findField(clazz, classField);
            } catch (ClassNotFoundException e) {
                log.debug("Unable to find class for property sourceType: {}, class: {}", prop.getName(), prop.getSourceType());
            }
        }

        if (prop.getType() != null) {
            try {
                propClazz = Class.forName(prop.getType());
            } catch (ClassNotFoundException e) {
                log.debug("Unable to find class for property type: {}, class: {}", prop.getName(), prop.getType());
            }
        }

        if (prop.getDescription() != null ) {
            propDef.put("description", prop.getDescription());
        }

        if (prop.getDefaultValue() != null) {
            propDef.put("default", prop.getDefaultValue());
        }
        processHints(propDef, prop);
        processDeprecation(propDef, prop);

        if(config.isUseValidation() && field != null) {
            processValidated(propDef, field, prop.getName());
        }
        if (config.isUseOpenapi()  && field != null) {
            processOpenapi(propDef, field, prop.getName());
        }
        if(propDef.containsKey("$ref")) {
            node.put(key, propDef);
            return;
        }
        if (propClazz != null && propClazz.isEnum()) {
            List<String> values = processEnumItem(propClazz);
            if (values != null) {
                propDef.put("enum", values);
            }
            node.put(key, propDef);
            return;
        }
        if (typeProperties.getType().equals(JsonSchemaType.ARRAY)) {
            processArray(prop, prop.getType(), propDef,null);
            node.put(key, propDef);
            return;
        }
        if (typeMappingService.isMap(propType)) {
            processMap(prop, prop.getType(), propDef,null);
            node.put(key, propDef);
            return;
        }
        if (typeProperties.getType().equals(JsonSchemaType.OBJECT)) {
            Map<String, Object> complexProperties = processComplexType(prop.getType(), prop);
            if (complexProperties != null) {
                propDef.put("properties", complexProperties);
            }
        }
        node.put(key, propDef);
    }

    private void processMap(Property prop, String propType, Map<String, Object> propDef, Set<String> visited) {

        if (propType.contains("java.util.Properties")) {
            addSimpleAdditionalProperties(propDef);
            return;
        }

        String valueType = extractMapValueType(propType);
        if (valueType.equals("java.lang.Object")) {
            addSimpleAdditionalProperties(propDef);
            return;
        }
        TypeProperties typeProperties = typeMappingService.typeProp(valueType, prop);
        if (typeProperties.getType() == null) {
            propDef.putAll(typeProperties.toMap());
            return;
        }
        try {
            Class<?> valueClass = Class.forName(valueType);
            if (valueClass.getTypeParameters().length > 0) {
                addSimpleAdditionalProperties(propDef);
                return;
            }
            if (typeProperties.getType().equals(JsonSchemaType.OBJECT)) {
                visited = visited == null ? new HashSet<>() : visited;
                Map<String, Object> valueTypeProperties = processComplexType(valueType, prop, visited);
                if (valueTypeProperties != null) {
                    propDef.put("additionalProperties", Map.of(
                            "type", "object",
                            "properties", valueTypeProperties
                    ));
                }
            } else {
                propDef.put("additionalProperties",typeProperties.toMap());
            }
        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class for property type: {}, treating as object", valueType);
            addSimpleAdditionalProperties(propDef);
        }
    }

    private void addSimpleAdditionalProperties(Map<String, Object> propDef) {
        propDef.put("additionalProperties", Map.of("type", "object"));
    }

    private void processArray(Property prop, String propType, Map<String, Object> propDef, Set<String> visited) {
        if (propType.contains("[]")) {
            propDef.put("items", typeMappingService.typeProp(propType.replace("[]",""),null).toMap());
            return;
        }

        String itemType = extractListItemType(propType);
        if (itemType.equals("java.lang.Object") || itemType.contains("<T>")) {
            propDef.put("items", Map.of("type", "object"));
            return;
        }

        try {
            Class<?> itemClass = Class.forName(itemType);
            if (itemClass.getTypeParameters().length > 0) {
                propDef.put("items", Map.of("type", "object"));
                return;
            }
            TypeProperties typeProperties = typeMappingService.typeProp(itemType, prop);
            Map<String, Object> items = new HashMap<>();
            items.putAll(typeProperties.toMap());

            if (typeProperties.getType().equals(JsonSchemaType.OBJECT)) {
                visited = visited == null ? new HashSet<>() : visited;
                Map<String, Object> complexProperties = processComplexType(itemType, prop, visited);
                if (complexProperties != null) {
                    items.put("properties", complexProperties);
                }
            } else if (itemClass.isEnum()) {
                List<String> values = processEnumItem(itemClass);
                if (values != null) {
                    items.put("enum", values);
                }
            }
            propDef.put("items", items);

        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class for property type: {}, treating as object", itemType);
            propDef.put("items", Map.of("type", "object"));
        }
    }


    public  List<String> processEnumItem(Class<?> itemClass) {
        log.debug("Processing enum values for property: {}", itemClass);
        if (itemClass.isEnum()) {
            Object[] enumConstants = itemClass.getEnumConstants();
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
        return null;
    }

    private void processOpenapi(Map<String, Object> propDef, Field field, String propName) {
        log.debug("OpenAPI: Processing schema for property: {}", propName);
        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (!schema.description().isEmpty()) {
                propDef.put("description", schema.description());
            }
//            if (!schema.example().isEmpty()) {
//                propDef.put("examples", schema.example());
//            }
            if (schema.deprecated()) {
                propDef.put("deprecated", true);
            }
//                    if (!schema.defaultValue().isEmpty()) {
//                        propDef.put("default", schema.defaultValue());
//                    }
        }
    }

    private void processValidated(Map<String, Object> propDef, Field field, String propName) {
        log.debug("Validation: Processing validation for property: {}", propName);

        if (field.isAnnotationPresent(Min.class)) {
            var value = field.getAnnotation(Min.class).value();
            propDef.put("minimum", value);
            log.debug("Validation: Added Min validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Max.class)) {
            var value = field.getAnnotation(Max.class).value();
            propDef.put("maximum", value);
            log.debug("Validation: Added Max validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Size.class)) {
            var size = field.getAnnotation(Size.class);
            if (size.min() > 0) {
                propDef.put("minLength", size.min());
                log.info("Validation: Added Size.min validation with value {} for field {}", size.min(), field.getName());
            }
            if (size.max() < Integer.MAX_VALUE) {
                propDef.put("maxLength", size.max());
                log.info("Validation: Added Size.max validation with value {} for field {}", size.max(), field.getName());
            }
        }
        if (field.isAnnotationPresent(Pattern.class)) {
            var pattern = field.getAnnotation(Pattern.class).regexp();
            propDef.put("pattern", pattern);
            log.info("Validation: Added Pattern validation with regexp {} for field {}", pattern, field.getName());
        }
//        if (field.isAnnotationPresent(NotNull.class)) {
//            propDef.put("required", true);
//            log.debug("Validation: Added NotNull validation for field {}", field.getName());
//        }
        if (field.isAnnotationPresent(NotEmpty.class)) {
            propDef.put("minLength", 1);
            log.debug("Validation: Added NotEmpty validation for field {}", field.getName());
        }
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        for (String include : included) {
            if (propertyPath.startsWith(include)) return true;
        }
        return false;
    }

    private void processHints(Map<String, Object> propDef, Property prop) {
        log.debug("Processing hints for property: {}", prop.getName());
        if (prop.getHint() != null) {
            if (prop.getHint().getValues() != null && !prop.getHint().getValues().isEmpty()) {
                var hints = prop.getHint().getValues().stream()
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


    private void processDeprecation(Map<String, Object> propDef, Property prop) {
        log.debug("Processing deprecation for property: {}", prop.getName());
        if (prop.getDeprecated() != null && prop.getDeprecated()) {
            propDef.put("deprecated", true);
            if (prop.getDeprecation() != null) {
                Map<String, Object> xDeprecation = new LinkedHashMap<>();
                if (prop.getDeprecation().getReason() != null) {
                    xDeprecation.put("reason", prop.getDeprecation().getReason());
                }
                if (prop.getDeprecation().getReplacement() != null) {
                    xDeprecation.put("replacement", prop.getDeprecation().getReplacement());
                }
                if (prop.getDeprecation().getSince() != null) {
                    xDeprecation.put("since", prop.getDeprecation().getSince());
                }
                if (prop.getDeprecation().getLevel() != null) {
                    // Normalize level to upper-case to avoid duplicating values like WARNING/warning
                    xDeprecation.put("level", prop.getDeprecation().getLevel().name().toUpperCase());
                }
                if (!xDeprecation.isEmpty()) {
                    propDef.put("x-deprecation", xDeprecation);
                }
            }
        }
    }


    public Map<String, Object> processComplexType(String type, Property bootProp) {
        return processComplexType(type, bootProp,new HashSet<>());
    }

    public Map<String, Object> processComplexType(String type, Property bootProp,Set<String> visited) {
        if (visited.contains(type)) {
            log.warn("Detected cyclic reference for type: {}. Skipping nested properties. for Property {}", type,bootProp.getName());
            return new HashMap<>();
        }
        visited.add(type);
        Map<String, Object> properties = new HashMap<>();
        try {
            Class<?> clazz = Class.forName(type);
            for (Field field : clazz.getDeclaredFields()) {
                Map<String, Object> fieldDef = new HashMap<>();
                String fieldType = field.getType().getName();
                String fieldGenName = field.getGenericType().getTypeName();
                var typeProperties = typeMappingService.typeProp(fieldGenName,null);
                fieldDef.putAll(typeProperties.toMap()); // TODO: check logic

                if (typeProperties.getReference() != null) {
                    fieldDef.put("properties",typeProperties.toMap());
                } else if (typeProperties.getType().equals(JsonSchemaType.ARRAY)) {
                    processArray(bootProp,fieldGenName, fieldDef, visited);
                } else if (typeProperties.getType().equals(JsonSchemaType.OBJECT)) {
                    if (typeMappingService.isMap(fieldType)) {
                        processMap(bootProp, fieldGenName, fieldDef, visited);
                    } else {
                        try{
                            if((Class.forName(fieldType)).getTypeParameters().length > 0){
                                fieldDef.put("properties", Map.of("type", "object"));
                            } else {
                                Map<String, Object> nestedProperties = processComplexType(fieldType, bootProp, visited);
                                if (nestedProperties != null) {
                                    fieldDef.put("properties", nestedProperties);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            log.debug("Cannot find class for property type: {}, treating as object", fieldType);
                            fieldDef.put("properties", Map.of("type", "object"));
                        }
                    }
                }
                properties.put(toKebabCase(field.getName()), fieldDef);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Type not found: {}", type);
        }
        visited.remove(type);
        return properties;
    }

    public String extractListItemType(String type) {
        if (type.contains("<") && type.contains(">")) {
            String inner = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
            if (inner.contains(",")) {
                inner = inner.split(",")[0];
            }
            return inner.trim();
        }
        return "object";
    }

    public String extractMapValueType(String type) {
        if (type.contains("<") && type.contains(">")) {
            String inner = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
            if (inner.contains(",")) {
                String[] arr = inner.split(",", 2);
                return arr[1].trim();
            }
        }
        return type;
    }


    public String toKebabCase(String input) {
        if (input == null) return null;
        return input.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

}

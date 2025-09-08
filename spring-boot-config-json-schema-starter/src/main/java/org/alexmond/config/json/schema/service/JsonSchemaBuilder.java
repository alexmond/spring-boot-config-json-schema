
package org.alexmond.config.json.schema.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.metamodel.Deprecation;
import org.alexmond.config.json.schema.metamodel.Property;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.text.CaseUtils.toCamelCase;

@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    JsonSchemaBuilderHelper helper;

    Map<String, Object> definitions = new LinkedHashMap<>();

    public JsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        this.config = config;
        this.typeMappingService = typeMappingService;
        helper =  new JsonSchemaBuilderHelper(config, typeMappingService);
    }

    public Map<String, Object> buildSchema(HashMap<String, Property> meta, List<String> included) {
        log.info("Starting JSON schema generation");
        JsonSchemaRoot schemaRoot = JsonSchemaRoot.builder()
                .schema(config.getSchemaSpec())
                .id(config.getSchemaId())
                .title(config.getTitle())
                .description(config.getDescription())
                .type(JsonSchemaType.OBJECT)
                .build();

        schemaRoot.setDefinitions(getDefinitions());

        Map<String, JsonSchemaProperties> properties = new TreeMap<>();
        HashMap<String, Property> filteredMeta = new HashMap<>();
        meta.forEach((key, value) -> {
            if (matchesIncluded(key, included)) {
                filteredMeta.put(key, value);
            }
        });

        filteredMeta.forEach((key, value) -> {
            addProperty(properties, key.split("\\."), 0, value);
        });
        schemaRoot.setProperties(properties);

        return schemaRoot.toMap();
    }

    private Map<String, JsonSchemaProperties> getDefinitions() {

        Map<String, JsonSchemaProperties> definitions = new LinkedHashMap<>();

        definitions.put("loggerLevel", getLoggerLevelDef());
        definitions.put("loggerLevelProp", getLoggerLevelPropDef());
        definitions.put("Locales", getLocalesDef());
        definitions.put("Charsets", getCharsetsDef());

        return definitions;
    }

    private JsonSchemaProperties getCharsetsDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(new ArrayList<>(Charset.availableCharsets().keySet()))
                .build();
    }

    private JsonSchemaProperties getLocalesDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(Arrays.stream(Locale.getAvailableLocales())
                        .map(Locale::toString)
                        .collect(Collectors.toList()))

                .build();
    }

    private JsonSchemaProperties getLoggerLevelDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.STRING)
                .enumValues(processEnumItem(LogLevel.class))
                .build();
    }

    private JsonSchemaProperties getLoggerLevelPropDef() {
        return JsonSchemaProperties.builder()
                .type(JsonSchemaType.OBJECT)
                .additionalProperties(JsonSchemaProperties.builder()
                        .oneOf(List.of(
                                JsonSchemaProperties.builder()
                                        .reference("#/$defs/loggerLevel")
                                        .build(),
                                JsonSchemaProperties.builder()
                                        .reference("#/$defs/loggerLevelProp")
                                        .build()
                        ))
                        .build())
                .build();
    }


    private void addProperty(Map<String, JsonSchemaProperties> node, String[] path, int idx, Property prop) {
        log.debug("Processing property at path: {}, index: {}", String.join(".", path), idx);

        if (node == null) {
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            if (node.get(key) == null) {
                processLeaf(node, prop, key);
            } else {
                log.info("Duplicate leaf {}", key);
                if (prop.getDescription() != null) {
                    processLeaf(node, prop, key);
                }
            }
        } else {
            JsonSchemaProperties propNode;
            Map<String, JsonSchemaProperties> properties = new TreeMap<>();

            if (node.containsKey(key)) {
                propNode = node.get(key);
                Map<String, JsonSchemaProperties> propsObj = propNode.getProperties();
                if (propsObj != null) {
                    properties = propsObj;
                } else {
                    propNode.setProperties(properties);
                }
            } else {
                propNode = JsonSchemaProperties.builder()
                        .type(JsonSchemaType.OBJECT)
                        .properties(properties)
                        .build();
                node.put(key, propNode);
            }

            addProperty(properties, path, idx + 1, prop);
        }
    }


    private void processLeaf(Map<String, JsonSchemaProperties> node, Property prop, String key) {
        JsonSchemaProperties jsonSchemaProperties;
        String propType;
        Class<?> clazz;
        Class<?> propClazz = null;
        Field field = null;

        // skip deprecated property with the error level
        if (prop.getDeprecated() != null && prop.getDeprecated() && (prop.getDeprecation().getLevel() == Deprecation.Level.ERROR || prop.getDeprecation().getLevel() == Deprecation.Level.error)) {
            log.debug("Skipping property is deprecated and removed: {}", prop.getName());
            return;
        }
        if (prop.getType() == null) {
            log.error("property {} prop.type is null", prop.getName());
            return;
        } else {
            propType = prop.getType();
            jsonSchemaProperties = typeMappingService.typeProp(propType, prop);
        }

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

        if (prop.getDescription() != null) {
            jsonSchemaProperties.setDescription(prop.getDescription());
        }

        if (prop.getDefaultValue() != null) {
            jsonSchemaProperties.setDefaultValue(prop.getDefaultValue());
        }
        helper.processHints(jsonSchemaProperties, prop);
        helper.processDeprecation(jsonSchemaProperties, prop);

        if (config.isUseValidation() && field != null) {
            processValidated(jsonSchemaProperties, field, prop.getName());
        }
        if (config.isUseOpenapi() && field != null) {
            processOpenapi(jsonSchemaProperties, field, prop.getName());
        }
        if (jsonSchemaProperties.getReference() != null) {
            node.put(key, jsonSchemaProperties);
            return;
        }
        if (propClazz != null && propClazz.isEnum()) {
            List<String> values = processEnumItem(propClazz);
            if (values != null) {
                jsonSchemaProperties.setEnumValues(values);
            }
            node.put(key, jsonSchemaProperties);
            return;
        }
        if (jsonSchemaProperties.getType().equals(JsonSchemaType.ARRAY)) {
            processArray(prop, prop.getType(), jsonSchemaProperties, null);
            node.put(key, jsonSchemaProperties);
            return;
        }
        if (typeMappingService.isMap(propType)) {
            processMap(prop, prop.getType(), jsonSchemaProperties, null);
            node.put(key, jsonSchemaProperties);
            return;
        }
        if (jsonSchemaProperties.getType().equals(JsonSchemaType.OBJECT)) {
            var complexProperties = processComplexType(prop.getType(), prop,null);
            if (complexProperties != null) {
                jsonSchemaProperties.setProperties(complexProperties);
            }
        }
        node.put(key, jsonSchemaProperties);
    }

    private void processMap(Property prop, String propType, JsonSchemaProperties propDef, Set<String> visited) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        if (propType.contains("java.util.Properties")) {
            addSimpleAdditionalProperties(propDef);
            return;
        }

        String valueType = extractMapValueType(propType);
        if (valueType.equals("java.lang.Object")) {
            addSimpleAdditionalProperties(propDef);
            return;
        }
        JsonSchemaProperties JsonSchemaProperties = typeMappingService.typeProp(valueType, prop);
        if (JsonSchemaProperties.getType() == null) {
            propDef.merge(JsonSchemaProperties);
            return;
        }
        try {
            Class<?> valueClass = Class.forName(valueType);
            if (valueClass.getTypeParameters().length > 0) {
                addSimpleAdditionalProperties(propDef);
                return;
            }
            if (JsonSchemaProperties.getType().equals(JsonSchemaType.OBJECT)) {
                visited = visited == null ? new HashSet<>() : visited;
                Map<String, JsonSchemaProperties> valueJsonSchemaProperties = processComplexType(valueType, prop, visited);
                if (valueJsonSchemaProperties != null) {
                    var newProp = JsonSchemaProperties.builder()
                            .type(JsonSchemaType.OBJECT)
                            .properties(valueJsonSchemaProperties)
                            .build();
                    propDef.merge(newProp);
                }
            } else {
                propDef.setAdditionalProperties(JsonSchemaProperties);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class for property type: {}, treating as object", valueType);
            addSimpleAdditionalProperties(propDef);
        }
    }

    private void addSimpleAdditionalProperties(JsonSchemaProperties propDef) {
        propDef.setAdditionalProperties(JsonSchemaProperties.builder()
                .type(JsonSchemaType.OBJECT)
                .build());
    }

    private void processArray(Property prop, String propType, JsonSchemaProperties jsonSchemaProperties, Set<String> visited) {
        if (propType.equals("java.lang.String[]")) {
            jsonSchemaProperties.setItems(
                            JsonSchemaProperties.builder()
                                    .type(JsonSchemaType.STRING)
                                    .build()
            );
            return;
        }

        String itemType = extractListItemType(propType);
        if (itemType.equals("java.lang.Object") || itemType.contains("<T>")) {
            jsonSchemaProperties.setItems(
                            JsonSchemaProperties.builder()
                                    .type(JsonSchemaType.OBJECT)
                                    .build()
            );
            return;
        }

        try {
            Class<?> itemClass = Class.forName(itemType);
            if (itemClass.getTypeParameters().length > 0) {
                jsonSchemaProperties.setItems(
                                JsonSchemaProperties.builder()
                                        .type(JsonSchemaType.OBJECT)
                                        .build()
                );
                return;
            }
            JsonSchemaProperties jsonSchemaPropertiesItem = typeMappingService.typeProp(itemType, prop);

            if (jsonSchemaPropertiesItem.getType().equals(JsonSchemaType.OBJECT)) {
                Map<String, JsonSchemaProperties> complexProperties = processComplexType(itemType, prop, visited);
                if (complexProperties != null) {
                    jsonSchemaPropertiesItem.setProperties(complexProperties);
                }
            } else if (itemClass.isEnum()) {
                List<String> values = processEnumItem(itemClass);
                if (values != null) {
                    jsonSchemaPropertiesItem.setEnumValues(values);
                }
            }
            jsonSchemaProperties.setItems(jsonSchemaPropertiesItem);
        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class for property type: {}, treating as object", itemType);
            jsonSchemaProperties.setItems(
                    JsonSchemaProperties.builder()
                            .type(JsonSchemaType.OBJECT)
                    .build()
            );
        }
    }


    public List<String> processEnumItem(Class<?> itemClass) {
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
                return enumValues;
            }
        }
        return null;
    }

    private void processOpenapi(JsonSchemaProperties jsonSchemaProperties, Field field, String propName) {
        log.debug("OpenAPI: Processing schema for property: {}", propName);
        if (field.isAnnotationPresent(Schema.class)) {
            Schema schema = field.getAnnotation(Schema.class);
            if (!schema.description().isEmpty()) {
                jsonSchemaProperties.setDescription(schema.description());
            }
//            if (!schema.example().isEmpty()) {
//                jsonSchemaProperties.setExamples(List.of(schema.example()));
//            }
            if (schema.deprecated()) {
                jsonSchemaProperties.setDeprecated(true);
            }
//            if (!schema.defaultValue().isEmpty()) {
//                jsonSchemaProperties.setDefaultValue(schema.defaultValue());
//            }
        }
    }

    private void processValidated(JsonSchemaProperties jsonSchemaProperties, Field field, String propName) {
        log.debug("Validation: Processing validation for property: {}", propName);

        if (field.isAnnotationPresent(Min.class)) {
            var value = field.getAnnotation(Min.class).value();
            jsonSchemaProperties.setMinimum(value);
            log.debug("Validation: Added Min validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Max.class)) {
            var value = field.getAnnotation(Max.class).value();
            jsonSchemaProperties.setMaximum(value);
            log.debug("Validation: Added Max validation with value {} for field {}", value, field.getName());
        }
        if (field.isAnnotationPresent(Size.class)) {
            var size = field.getAnnotation(Size.class);
            if (size.min() > 0) {
                jsonSchemaProperties.setMinLength(size.min());
                log.info("Validation: Added Size.min validation with value {} for field {}", size.min(), field.getName());
            }
            if (size.max() < Integer.MAX_VALUE) {
                jsonSchemaProperties.setMaxLength(size.max());
                log.info("Validation: Added Size.max validation with value {} for field {}", size.max(), field.getName());
            }
        }
        if (field.isAnnotationPresent(Pattern.class)) {
            var pattern = field.getAnnotation(Pattern.class).regexp();
            jsonSchemaProperties.setPattern(pattern);
            log.info("Validation: Added Pattern validation with regexp {} for field {}", pattern, field.getName());
        }
//        if (field.isAnnotationPresent(NotNull.class)) {
//            propDef.put("required", true);
//            log.debug("Validation: Added NotNull validation for field {}", field.getName());
//        }
        if (field.isAnnotationPresent(NotEmpty.class)) {
            jsonSchemaProperties.setMinLength(1);
            log.debug("Validation: Added NotEmpty validation for field {}", field.getName());
        }
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        for (String include : included) {
            if (propertyPath.startsWith(include)) return true;
        }
        return false;
    }
    
    public Map<String,JsonSchemaProperties> processComplexType(String type, Property bootProp, Set<String> visited) {
        if (visited == null) {
            visited = new HashSet<>();
        }
        if (visited.contains(type)) {
            log.warn("Detected cyclic reference for type: {}. Skipping nested properties. for Property {}", type, bootProp.getName());
            return new HashMap<>();
        }
        if (config.getAllExcludedClasses().contains(type)) {
            log.warn("Excluding type {}. Skipping nested properties. for Property {}", type, bootProp.getName());
            return new HashMap<>();
        }
        visited.add(type);
        Map<String, JsonSchemaProperties> properties = new TreeMap<>();
        try {
            Class<?> clazz = Class.forName(type);
            for (Field field : clazz.getDeclaredFields()) {
                JsonSchemaProperties fieldProperty;
                Map<String, Object> fieldDef = new HashMap<>();
                String fieldType = field.getType().getName();
                String fieldGenName = field.getGenericType().getTypeName();
                if (config.getExcludeClasses().contains(fieldGenName)) {
                    log.warn("Excluding type {}. Skipping nested properties. for Property {}", fieldGenName, bootProp.getName());
                }
                fieldProperty = typeMappingService.typeProp(fieldGenName, null);
                if (fieldProperty.getReference() != null) {
                    log.warn("Skipping further processing, $ref is defined");
                } else if (fieldProperty.getType().equals(JsonSchemaType.ARRAY)) {
                    processArray(bootProp, fieldGenName, fieldProperty, visited);
                } else if (fieldProperty.getType().equals(JsonSchemaType.OBJECT)) {
                    if (typeMappingService.isMap(fieldType)) {
                        processMap(bootProp, fieldGenName, fieldProperty, visited);
                    } else {
                        try {
                            if ((Class.forName(fieldType)).getTypeParameters().length > 0) {
                                fieldProperty.setType(JsonSchemaType.OBJECT);
                            } else {
                                Map<String, JsonSchemaProperties> nestedProperties = processComplexType(fieldType, bootProp, visited);
                                if (nestedProperties != null) {
                                    fieldProperty.setType(JsonSchemaType.OBJECT);
                                    fieldProperty.setProperties(nestedProperties);
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            log.debug("Cannot find class for property type: {}, treating as object", fieldType);
                            fieldProperty.setType(JsonSchemaType.OBJECT);
                        }
                    }
                }
                properties.put(toKebabCase(field.getName()), fieldProperty);
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

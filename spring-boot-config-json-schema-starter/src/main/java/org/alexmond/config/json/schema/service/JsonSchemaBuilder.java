package org.alexmond.config.json.schema.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaRoot;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.metamodel.Deprecation;
import org.alexmond.config.json.schema.metamodel.Property;
import org.apache.commons.text.CaseUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Builder class responsible for generating JSON Schema definitions from Spring configuration metadata.
 * Handles type mapping, property validation, and schema structure generation according to configured rules.
 */
@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    @Getter private final JsonSchemaBuilderHelper helper;
    private final DefinitionsHelper definitionsHelper;

    public JsonSchemaBuilder(JsonConfigSchemaConfig config, TypeMappingService typeMappingService) {
        this.config = config;
        this.typeMappingService = typeMappingService;
        helper = new JsonSchemaBuilderHelper(config, typeMappingService);
        definitionsHelper = new DefinitionsHelper(helper);
    }

    /**
     * Builds a complete JSON Schema from the provided configuration metadata.
     *
     * @param meta     Map of property metadata keyed by property path
     * @param included List of property paths to include in the schema
     * @return Map representing the complete JSON Schema structure
     */
    public Map<String, Object> buildSchema(Map<String, Property> meta, List<String> included) {
        log.info("Starting JSON schema generation");
        JsonSchemaRoot schemaRoot = JsonSchemaRoot.builder()
                .schema(config.getSchemaSpec())
                .id(config.getSchemaId())
                .title(config.getTitle())
                .description(config.getDescription())
                .type(JsonSchemaType.OBJECT)
                .additionalProperties(config.isAllowAdditionalProperties())
                .definitions(definitionsHelper.getDefinitions())
                .build();

        Map<String, JsonSchemaProperties> properties = new TreeMap<>();
        meta.forEach((key, value) -> {
            if (matchesIncluded(key, included) && !isDeprecatedError(value)) {
                addProperty(properties, key.split("\\."), 0, value);
            }
        });
        schemaRoot.setProperties(properties);

        return schemaRoot.toMap();
    }

    /**
     * Recursively adds a property to the schema tree following the property path.
     *
     * @param node Current node in the schema tree
     * @param path Array of path segments to the property
     * @param idx  Current index in the path array
     * @param prop Property metadata to add
     */
    private void addProperty(Map<String, JsonSchemaProperties> node, String[] path, int idx, Property prop) {
        log.debug("Processing property at path: {}, index: {}", String.join(".", path), idx);

        if (node == null) {
            log.error("Null node encountered while adding property at path: {}, index: {}", String.join(".", path), idx);
            throw new IllegalArgumentException("Node must not be null in addProperty: idx=" + idx + ", path=" + String.join(".", path));
        }
        String key = path[idx];
        if (idx == path.length - 1) {
            if (config.getExcludeClasses().contains(prop.getType())) {
                log.warn("Excluding type {}. Skipping nested properties. for Property {}", prop.getName(), prop.getType());
            } else {
                JsonSchemaProperties jsonSchemaProperties = new JsonSchemaProperties();
//            if (node.get(key) == null) {
                var processed = processLeaf(jsonSchemaProperties, prop, new HashSet<>());
//            } else {
//                log.info("Duplicate leaf {}", key);
//                if (prop.getDescription() != null) {
//                    processLeaf(jsonSchemaProperties, prop, null);
//                }
//            }
                if (processed) {
                    node.put(key, jsonSchemaProperties);
                }
            }
        } else {
            Map<String, JsonSchemaProperties> properties = ensureObjectNode(node, key);
            addProperty(properties, path, idx + 1, prop);
        }
    }

    /**
     * Ensures that `node` contains an OBJECT-typed JsonSchemaProperties under `key`
     * with a non-null, mutable `properties` map, and returns that map.
     * <p>
     * Ensures that a node exists at the given key and has the correct structure for an object type.
     *
     * @param node Parent node to check/update
     * @param key  Key where the object node should exist
     * @return Properties map of the ensured object node
     */
    private Map<String, JsonSchemaProperties> ensureObjectNode(Map<String, JsonSchemaProperties> node, String key) {
        JsonSchemaProperties propNode = node.get(key);
        if (propNode == null) {
            Map<String, JsonSchemaProperties> properties = new TreeMap<>();
            propNode = JsonSchemaProperties.builder()
                    .type(JsonSchemaType.OBJECT)
                    .properties(properties)
                    .build();
            node.put(key, propNode);
            return properties;
        }

        Map<String, JsonSchemaProperties> properties = propNode.getProperties();
        if (properties == null) {
            properties = new TreeMap<>();
            propNode.setProperties(properties);
        }
        return properties;
    }


    /**
     * Processes a leaf property, handling type mapping, validation rules, and nested type definitions.
     *
     * @param jsonSchemaProperties Schema properties to update
     * @param prop                 Property metadata to process
     * @param visited              Set of already processed types to prevent cycles
     * @return True if property was processed successfully, false otherwise
     */
    private Boolean processLeaf(JsonSchemaProperties jsonSchemaProperties, Property prop, Set<String> visited) {
        String propType;
        Field field = null;

        if (prop.getType() == null) {
            log.error("property {} prop.type is null", prop.getName());
            return false;
        } else {
            propType = prop.getType();
        }

        if (prop.getSourceType() != null) {
            String propertyName = prop.getName();
            String lastField = propertyName.substring(propertyName.lastIndexOf('.') + 1);
            String classField = CaseUtils.toCamelCase(lastField, false, '-');

            try {
                Class<?> clazz = Class.forName(prop.getSourceType());
                field = ReflectionUtils.findField(clazz, classField);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.debug("Unable to find class for property sourceType: {}, class: {}", prop.getName(), prop.getSourceType());
            }

            if (field != null) {
                if (!propType.equals(field.getGenericType().getTypeName())) {
                    log.warn("Property {} type {} mismatch with real one {}",
                            prop.getName(), propType, field.getGenericType().getTypeName());
                    propType = field.getGenericType().getTypeName();
                }
            }
        }

        jsonSchemaProperties.merge(typeMappingService.typeProp(propType, prop));

        if (prop.getDescription() != null) {
            jsonSchemaProperties.setDescription(prop.getDescription());
        }

        if (prop.getDefaultValue() != null) {
            jsonSchemaProperties.setDefaultValue(prop.getDefaultValue());
        }

        if (prop.getHint() != null) {
            helper.processHints(jsonSchemaProperties, prop);
        }
        if (prop.getDeprecated() != null && prop.getDeprecated()) {
            helper.processDeprecation(jsonSchemaProperties, prop);
        }

        if (config.isUseValidation() && field != null) {
            helper.processValidated(jsonSchemaProperties, field, prop.getName());
        }
        if (config.isUseOpenapi() && field != null) {
            helper.processOpenapi(jsonSchemaProperties, field, prop.getName());
        }

        if (jsonSchemaProperties.getReference() != null) {
            return true;
        }

        if (prop.getType() != null) {
            Class<?> propClazz = null;
            try {
                propClazz = Class.forName(prop.getType());
            } catch (ClassNotFoundException e) {
                log.debug("Unable to find class for property type: {}, class: {}", prop.getName(), prop.getType());
            }

            if (propClazz != null && propClazz.isEnum()) {
                Set<String> values = helper.processEnumItem(propClazz);
                if (values != null) {
                    jsonSchemaProperties.setEnumValues(values);
                }
                return true;
            }
        }

        if (jsonSchemaProperties.getType().equals(JsonSchemaType.ARRAY)) {
            processArray(prop, propType, jsonSchemaProperties, visited);
            return true;
        }
        if (typeMappingService.isMap(propType)) {
            processMap(prop, propType, jsonSchemaProperties, visited);
            return true;
        }
        if (jsonSchemaProperties.getType().equals(JsonSchemaType.OBJECT)) {
            var complexProperties = processComplexType(propType, prop, visited);
            if (complexProperties != null) {
                jsonSchemaProperties.setProperties(complexProperties);
            }
        }
        return true;
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
                Map<String, JsonSchemaProperties> valueJsonSchemaProperties = processComplexType(valueType, prop, visited);
                if (valueJsonSchemaProperties != null) {
                    var newProp = org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties.builder()
                            .type(JsonSchemaType.OBJECT)
                            .properties(valueJsonSchemaProperties)
                            .build();
                    propDef.setAdditionalProperties(newProp);
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

            if (jsonSchemaPropertiesItem.getType() == JsonSchemaType.OBJECT) {
                Map<String, JsonSchemaProperties> complexProperties = processComplexType(itemType, prop, visited);
                if (complexProperties != null) {
                    jsonSchemaPropertiesItem.setProperties(complexProperties);
                }
            } else if (itemClass.isEnum()) {
                Set<String> values = helper.processEnumItem(itemClass);
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

    private boolean isDeprecatedError(Property prop) {
        return prop.getDeprecated() != null &&
               prop.getDeprecated() &&
               prop.getDeprecation() != null &&
               (prop.getDeprecation().getLevel() == Deprecation.Level.ERROR ||
                prop.getDeprecation().getLevel() == Deprecation.Level.error);
    }

    private boolean matchesIncluded(String propertyPath, List<String> included) {
        for (String include : included) {
            if (propertyPath.startsWith(include)) return true;
        }
        return false;
    }

    public Map<String, JsonSchemaProperties> processComplexType(String type, Property bootProp, Set<String> visited) {
        if (visited.contains(type)) {
            log.warn("Detected cyclic reference for type: {}. Skipping nested properties. for Property {}", type, bootProp.getName());
            return null;
        }
        if (config.getAllExcludedClasses().contains(type)) {
            log.warn("Excluding type {}. Skipping nested properties. for Property {}", type, bootProp.getName());
            return null;
        }
        visited.add(type);
        Map<String, JsonSchemaProperties> properties = new TreeMap<>();
        try {
            Class<?> clazz = Class.forName(type);
            for (Field field : clazz.getDeclaredFields()) {
                String fieldGenName = field.getGenericType().getTypeName();
                if (config.getExcludeClasses().contains(fieldGenName)) {
                    log.warn("Excluding type {}. Skipping nested properties. for Property {}", fieldGenName, bootProp.getName());
                } else {
                    JsonSchemaProperties fieldProperty = new JsonSchemaProperties();
                    String propName = toKebabCase(field.getName());
                    Property filedProp = Property.builder()
                            .name(bootProp.getName() + "." + propName)
                            .type(fieldGenName)
                            .sourceType(type)
                            .build();
                    var processed = processLeaf(fieldProperty, filedProp, visited);
                    if (processed) {
                        properties.put(propName, fieldProperty);
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.debug("Type not found: {},{}", type, e.getMessage());
        }

        visited.remove(type);
        if (properties.isEmpty()) {
            return null;
        }
        return properties;
    }

    public String extractListItemType(String type) {
        if(type == null) return null;
        if (type.contains("<") && type.contains(">")) {
            String inner = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
            return inner.trim();
        }
        return "object";
    }

    public String extractMapValueType(String type) {
        if(type == null) return null;
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

        // Split on underscores, process each segment separately, then join back with "_"
        String[] segments = input.split("_");
        for (int i = 0; i < segments.length; i++) {
            String s = segments[i];
            // Add space before every uppercase letter (splits acronyms into single letters too)
            s = s.replaceAll("([A-Z])", " $1");
            // Insert spaces between letters and numbers
            s = s.replaceAll("([a-zA-Z])([0-9])", "$1 $2");
            s = s.replaceAll("([0-9])([a-zA-Z])", "$1 $2");
            // Collapse spaces
            s = s.trim().replaceAll("\\s+", " ");
            // Replace spaces with hyphens
            segments[i] = String.join("-", s.split(" ")).toLowerCase();
        }
        // Join the processed segments back with underscores
        return String.join("_", segments);
    }

}

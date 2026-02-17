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
 *
 * <p>This class provides functionality to:
 * <ul>
 *   <li>Convert Spring configuration metadata to JSON Schema format</li>
 *   <li>Handle complex type mappings and nested structures</li>
 *   <li>Support validation rules and constraints</li>
 *   <li>Process deprecation information</li>
 *   <li>Generate schema definitions and references</li>
 * </ul>
 *
 * <p>The builder supports features like:
 * <ul>
 *   <li>Anchor-based references</li>
 *   <li>Definition-based references</li>
 *   <li>OpenAPI annotations processing</li>
 *   <li>Bean validation constraints</li>
 * </ul>
 */
@Slf4j
public class JsonSchemaBuilder {

    private final JsonConfigSchemaConfig config;
    private final TypeMappingService typeMappingService;

    @Getter
    private final JsonSchemaBuilderHelper helper;
    private final DefinitionsHelper definitionsHelper;
    Map<String, JsonSchemaProperties> definitions;
    Map<String, JsonSchemaProperties> extraDefinitions;
    private Set<String> anchors;
    private Set<String> defs;
    private Set<String> processedProp;
    private Map<String, Property> allMeta;

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
    public JsonSchemaRoot buildSchema(Map<String, Property> meta, List<String> included) {
        allMeta = meta;
        anchors = new HashSet<>();
        defs = new HashSet<>();
        processedProp = new HashSet<>();
        definitions = null;
        extraDefinitions = new TreeMap<>();

        log.info("Starting JSON schema generation");
        JsonSchemaRoot schemaRoot = JsonSchemaRoot.builder()
                .schema(config.getSchemaSpec())
                .id(config.getSchemaId())
                .title(config.getTitle())
                .description(config.getDescription())
                .type(JsonSchemaType.OBJECT)
                .additionalProperties(config.isAllowAdditionalProperties())
                .build();

        definitions = definitionsHelper.getDefinitions();

        Map<String, JsonSchemaProperties> properties = new TreeMap<>();
        meta.forEach((key, value) -> {
            if (matchesIncluded(key, included) && !isDeprecatedError(value) && !processedProp.contains(key)) {
                addProperty(properties, key.split("\\."), 0, value);
            }
        });

        if (config.isEnableDefinitionRefs()) {
            defs.forEach(def -> {
                definitions.put(def, extraDefinitions.get(def).toBuilder().build());
            });
            definitions.forEach((key, value) -> {
            });
            removeReferecedProperrties(properties, 0);
        }

        schemaRoot.setDefinitions(definitions);
        schemaRoot.setProperties(properties);

        return schemaRoot;
    }

    private void removeReferecedProperrties(Map<String, JsonSchemaProperties> properties, int depth) {

        properties.forEach((key, value) -> {
            if (value.getAnchor() != null && definitions.containsKey(value.getAnchor())) {
                log.debug("anchor: {} {}", "=======".repeat(depth), value.getAnchor());
                value.setReference("#/$defs/" + value.getAnchor());
                value.setAnchor(null);
                value.setType(null);
                value.setProperties(null);
                value.setAdditionalProperties(null);
                value.setItems(null);
                value.setDescription(null);
            }
            if (value.getProperties() != null) {
                removeReferecedProperrties(value.getProperties(), depth + 1);
            }
            if (value.getAdditionalProperties() != null && (value.getAdditionalProperties() instanceof JsonSchemaProperties)) {
                removeReferecedProperrties(Map.of("#addProp", (JsonSchemaProperties) value.getAdditionalProperties()), depth + 1);
            }
            if (value.getItems() != null) {
                removeReferecedProperrties(Map.of("#itemProp", value.getItems()), depth + 1);
            }
        });
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
        log.trace("Processing property at path: {}, index: {}", String.join(".", path), idx);

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
                Boolean processed;
                if (node.get(key) == null) {
                    processed = processLeaf(jsonSchemaProperties, prop, new HashSet<>());
                } else {
                    log.info("Duplicate leaf {}", key);
                    jsonSchemaProperties = node.get(key);
                    processed = processLeaf(jsonSchemaProperties, prop, new HashSet<>());
                }
                if (processed) {
                    node.put(key, jsonSchemaProperties);
                }
            }
        } else {
            Map<String, JsonSchemaProperties> properties = ensureObjectNode(node, key, prop);
            addProperty(properties, path, idx + 1, prop);
        }
    }

    /**
     * Ensures that a node exists at the given key with proper object type structure.
     * Creates or updates the node to have required object properties if needed.
     *
     * @param node Parent node where the object node should exist
     * @param key  Key under which the object node should be stored
     * @param prop Property metadata for the node
     * @return The properties map of the ensured object node
     */
    private Map<String, JsonSchemaProperties> ensureObjectNode(Map<String, JsonSchemaProperties> node, String key, Property prop) {
        JsonSchemaProperties propNode = node.get(key);
        if (propNode == null) {
            Map<String, JsonSchemaProperties> properties = new TreeMap<>();
            propNode = JsonSchemaProperties.builder()
                    .type(JsonSchemaType.OBJECT)
                    .properties(properties)
                    .build();
            if (prop.getSourceType() != null && !prop.isGroupProperty()) {
                addAnchor(prop.getSourceType(), propNode);
            }
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

    private void addAnchor(String type, JsonSchemaProperties propNode) {
        Class<?> classType;
        if (config.getExcludeAnchors().contains(type)) {
            return;
        }

        try {
            classType = Class.forName(type);
        } catch (ClassNotFoundException e) {
            log.debug("Cannot find class for type: {}, skipping anchor", type);
            return;
        }

        if (propNode.getType() != JsonSchemaType.OBJECT) {
            log.error("Setting anchor for type {} is not supported", propNode.getType());
        }
        var fixedTypeName = type.replace("$", ":");
        if (anchors.contains(fixedTypeName)) {
            log.error("Duplicate anchor type {}.", fixedTypeName);
            return;
        }
        propNode.setAnchor(fixedTypeName);
        if (config.isUseOpenapi()) {
            helper.processClassOpenapi(propNode, classType);
        }
        anchors.add(fixedTypeName);
        extraDefinitions.put(fixedTypeName, propNode);
    }

    private void removeAnchor(String type, JsonSchemaProperties propNode) {
        var fixedTypeName = type.replace("$", ":");
        propNode.setAnchor(null);
        anchors.remove(fixedTypeName);
        extraDefinitions.remove(fixedTypeName);
    }

    private boolean addReference(JsonSchemaProperties jsonSchemaProperties, String type) {
        var fixedTypeName = type.replace("$", ":");
        if (anchors.contains(fixedTypeName) && jsonSchemaProperties.getAnchor() == null) {
            if (config.isEnableAnchorRefs()) {
                jsonSchemaProperties.setReference("#" + fixedTypeName);
                return true;
            } else if (config.isEnableDefinitionRefs()) {
                jsonSchemaProperties.setReference("#/$defs/" + fixedTypeName);
                defs.add(fixedTypeName);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Processes a leaf property, handling type mapping, validation rules, and nested type definitions.
     * This method is the core processor for individual properties in the schema generation process.
     *
     * @param jsonSchemaProperties Schema properties object to be updated with processed information
     * @param prop                 Property metadata containing type, validation, and other configuration details
     * @param visited              Set of already processed types to prevent infinite recursion in cyclic references
     * @return True if property was processed successfully, false if processing should be skipped
     *
     * <p>The method handles:
     * <ul>
     *   <li>Basic type mapping and validation</li>
     *   <li>Complex object structures and nested properties</li>
     *   <li>Array and Map type special processing</li>
     *   <li>Enum value processing</li>
     *   <li>Reference and anchor management</li>
     *   <li>OpenAPI and validation annotation processing</li>
     * </ul>
     */
    private Boolean processLeaf(JsonSchemaProperties jsonSchemaProperties, Property prop, Set<String> visited) {
        processedProp.add(prop.getName());
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
                    log.debug("Property {} type {} mismatch with real one {}",
                            prop.getName(), propType, field.getGenericType().getTypeName());
                    propType = field.getGenericType().getTypeName();
                }
            }
        }

        jsonSchemaProperties.merge(typeMappingService.typeProp(propType, prop));

        addReference(jsonSchemaProperties, propType);

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
            addAnchor(propType, jsonSchemaProperties);
            var complexProperties = processComplexType(propType, prop, visited);
            if (complexProperties != null) {
                jsonSchemaProperties.setProperties(complexProperties);
                extraDefinitions.put(propType, jsonSchemaProperties);
            } else {
                removeAnchor(propType, jsonSchemaProperties);
            }
        }
        return true;
    }

    /**
     * Processes map-type properties by handling their value types and creating appropriate schema definitions.
     * Handles special cases for Properties objects and generic Object values.
     *
     * @param prop     Property metadata for the map
     * @param propType The full type description of the map
     * @param propDef  The schema properties object to be updated
     * @param visited  Set of visited types to prevent cycles
     */
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

                var refProp = org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties.builder()
                        .type(JsonSchemaType.OBJECT)
                        .build();
                if (addReference(refProp, valueType)) {
                    propDef.setAdditionalProperties(refProp);
                } else {
                    var newProp = org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties.builder()
                            .type(JsonSchemaType.OBJECT)
                            .anchor(valueType)
                            .build();
                    anchors.add(valueType);
                    extraDefinitions.put(valueType, newProp);
                    Map<String, JsonSchemaProperties> valueJsonSchemaProperties = processComplexType(valueType, prop, visited);
                    if (valueJsonSchemaProperties != null) {
                        newProp.setProperties(valueJsonSchemaProperties);
                        propDef.setAdditionalProperties(newProp);
                        extraDefinitions.put(valueType, newProp);
                    } else {
                        anchors.remove(valueType);
                        extraDefinitions.remove(valueType);
                        propDef.setAdditionalProperties(JsonSchemaProperties);
                    }
                }
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

    /**
     * Processes array-type properties by handling their item types and creating appropriate schema definitions.
     * Supports both simple arrays and complex object arrays.
     *
     * @param prop                 Property metadata for the array
     * @param propType             The full type description of the array
     * @param jsonSchemaProperties The schema properties object to be updated
     * @param visited              Set of visited types to prevent cycles
     */
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
                if (!addReference(jsonSchemaPropertiesItem, itemType)) {
                    addAnchor(itemType, jsonSchemaPropertiesItem);
                    Map<String, JsonSchemaProperties> complexProperties = processComplexType(itemType, prop, visited);
                    if (complexProperties != null) {
                        jsonSchemaPropertiesItem.setProperties(complexProperties);
                        extraDefinitions.put(itemType, jsonSchemaPropertiesItem);
                    } else {
                        removeAnchor(itemType, jsonSchemaPropertiesItem);
                    }
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
        Map<String, JsonSchemaProperties> newProperties = new TreeMap<>();
        try {
            Class<?> clazz = Class.forName(type);
            for (Field field : clazz.getDeclaredFields()) {
                String fieldGenName = field.getGenericType().getTypeName();
                if (config.getExcludeClasses().contains(fieldGenName)) {
                    log.warn("Excluding type {}. Skipping nested properties. for Property {}", fieldGenName, bootProp.getName());
                } else {
                    JsonSchemaProperties fieldProperty = new JsonSchemaProperties();
                    String propName = toKebabCase(field.getName());
                    Property filedProp;
                    filedProp = allMeta.get(bootProp.getName() + "." + propName);
                    if (filedProp == null) {
                        filedProp = Property.builder()
                                .name(bootProp.getName() + "." + propName)
                                .type(fieldGenName)
                                .sourceType(type)
                                .build();
                    }
                    var processed = processLeaf(fieldProperty, filedProp, visited);
                    if (processed) {
                        newProperties.put(propName, fieldProperty);
                    }
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.debug("Type not found: {},{}", type, e.getMessage());
        }

        visited.remove(type);
        if (newProperties.isEmpty()) {
            return null;
        }
        return newProperties;
    }

    public String extractListItemType(String type) {
        if (type == null) return null;
        if (type.contains("<") && type.contains(">")) {
            String inner = type.substring(type.indexOf('<') + 1, type.lastIndexOf('>'));
            return inner.trim();
        }
        return "object";
    }

    public String extractMapValueType(String type) {
        if (type == null) return null;
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
            // Collapse spaces
            s = s.trim().replaceAll("\\s+", " ");
            // Replace spaces with hyphens
            segments[i] = String.join("-", s.split(" ")).toLowerCase();
        }
        // Join the processed segments back with underscores
        return String.join("_", segments);
    }

}


package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;


@Slf4j
public class TypeMappingService {

    private final MissingTypeCollector missingTypeCollector;

    public TypeMappingService(MissingTypeCollector missingTypeCollector) {
        this.missingTypeCollector = missingTypeCollector;
    }

    public String mapType(String springType) {
        log.debug("Mapping Spring type: {}", springType);
        if (springType == null) return "string";
        switch (springType) {
            case "java.lang.String":
            case "java.lang.String[]":
            case "java.nio.charset.Charset":
            case "java.time.Duration": // can improve by introducing some regexp
            case "java.util.Locale":
            case "java.util.Date":
            case "java.util.Calendar":
            case "java.util.TimeZone":
            case "org.springframework.util.unit.DataSize":
            case "java.lang.Character":
            case "char":
            case "java.io.File":
            case "org.springframework.http.MediaType":
            case "java.net.InetAddress":
                return "string";
            case "java.lang.Boolean":
            case "boolean":
                return "boolean";
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
            case "java.lang.Short":
            case "short":
            case "java.math.BigInteger":
                return "integer";
            case "java.lang.Float":
            case "float":
            case "double":
            case "java.lang.Double":
            case "java.math.BigDecimal":
                return "number";
        }
        if (springType.startsWith("java.util.List") || springType.startsWith("java.util.Set")) return "array";
        if (springType.startsWith("java.util.Map")) return "object";
        try {
            Class<?> type = Class.forName(springType);
            if (type.isEnum()) return "string";
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) return "object";
        } catch (ClassNotFoundException e) {
            if (springType.contains("Enum")) return "string";
        }
        missingTypeCollector.addType(springType);
        return "string";
    }

//    public String mapType(String springType) {
//        if (springType == null) return "object";
//        switch (springType) {
//            case "java.lang.String":
//            case "java.lang.String[]":
//            case "java.nio.charset.Charset":
//            case "java.time.Duration": // can improve by introducing some regexp
//            case "java.util.Locale":
//            case "java.util.Date":
//            case "java.util.Calendar":
//            case "java.util.TimeZone":
//            case "org.springframework.util.unit.DataSize":
//                return "string";
//            case "java.lang.Boolean":
//            case "boolean":
//                return "boolean";
//            case "java.lang.Integer":
//            case "int":
//            case "java.lang.Long":
//            case "long":
//            case "java.lang.Short":
//            case "short":
//            case "java.math.BigInteger":
//                return "integer";
//            case "java.lang.Float":
//            case "float":
//            case "double":
//            case "java.lang.Double":
//            case "java.math.BigDecimal":
//                return "number";
//        }
//        try {
//            Class<?> clazz = Class.forName(springType);
//            if (String.class.isAssignableFrom(clazz)) {
//                return "string";
//            }
//        } catch (ClassNotFoundException e) {
//            // Ignore and continue with string comparison
//        }
//        try {
//            Class<?> clazz = Class.forName(springType);
//            if (Set.class.isAssignableFrom(clazz)) {
//                return "array";
//            }
//        } catch (ClassNotFoundException e) {
//            // Ignore and continue with string comparison
//        }
//        try {
//            Class<?> clazz = Class.forName(springType);
//            if (List.class.isAssignableFrom(clazz)) {
//                return "array";
//            }
//        } catch (ClassNotFoundException e) {
//            // Ignore and continue with string comparison
//        }
//        try {
//            Class<?> clazz = Class.forName(springType);
//            if (Map.class.isAssignableFrom(clazz)) {
//                return "object";
//            }
//        } catch (ClassNotFoundException e) {
//            // Ignore and continue with string comparison
//        }
//        if(!(springType.startsWith("java.util.List") || springType.startsWith("java.util.Set") || springType.startsWith("java.util.Map"))) {
//            log.error("Unknown springType: {}, returning object", springType);
//        }else{
//            log.debug("Found springType: {}, returning object", springType);
//        }
//        return "object";
//    }

    public Map<String, Object> processComplexType(String type) {
        return processComplexType(type, new HashSet<>());
    }

    public Map<String, Object> processComplexType(String type, Set<String> visited) {
        if (visited.contains(type)) {
            log.warn("Detected cyclic reference for type: {}. Skipping nested properties.", type);
            return new HashMap<>();
        }
        visited.add(type);
        Map<String, Object> properties = new HashMap<>();
        try {
            Class<?> clazz = Class.forName(type);
            for (Field field : clazz.getDeclaredFields()) {
                Map<String, Object> fieldDef = new HashMap<>();
                String fieldType = field.getType().getName();
                fieldDef.put("type", mapType(fieldType));
                
                if (mapType(fieldType).equals("array")) {
                    String itemType = extractListItemType(field.getGenericType().getTypeName());
                    Map<String, Object> items = new HashMap<>();
                    items.put("type", mapType(itemType));
                    if (mapType(itemType).equals("object")) {
                        Map<String, Object> itemProperties = processComplexType(itemType, visited);
                        if (itemProperties != null) {
                            items.put("properties", itemProperties);
                        }
                    }
                    fieldDef.put("items", items);
                } else if (mapType(fieldType).equals("object")) {
                    if (fieldType.startsWith("java.util.Map")) {
                        String valueType = extractMapValueType(field.getGenericType().getTypeName());
                        if (mapType(valueType).equals("object")) {
                            Map<String, Object> valueTypeProperties = processComplexType(valueType, visited);
                            if (valueTypeProperties != null) {
                                fieldDef.put("additionalProperties", Map.of(
                                        "type", "object",
                                        "properties", valueTypeProperties
                                ));
                            }
                        } else {
                            fieldDef.put("additionalProperties", Map.of("type", mapType(valueType)));
                        }
                    } else {
                        Map<String, Object> nestedProperties = processComplexType(fieldType, visited);
                        if (nestedProperties != null) {
                            fieldDef.put("properties", nestedProperties);
                        }
                    }
                }
                properties.put(toSnakeCase(field.getName()), fieldDef);
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
        return "object";
    }

    public String toSnakeCase(String input) {
        if (input == null) return null;
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}

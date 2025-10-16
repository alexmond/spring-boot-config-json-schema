package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaFormat;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaProperties;
import org.alexmond.config.json.schema.jsonschemamodel.JsonSchemaType;
import org.alexmond.config.json.schema.metamodel.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Service responsible for mapping Spring configuration property types to JSON Schema types.
 * This service provides functionality to convert Java/Spring types into their corresponding
 * JSON Schema property definitions, including type information and format specifications.
 */
@Slf4j
public class TypeMappingService {

    private final MissingTypeCollector missingTypeCollector;
    private final JsonConfigSchemaConfig jsonConfigSchemaConfig;

    public TypeMappingService(MissingTypeCollector missingTypeCollector, JsonConfigSchemaConfig jsonConfigSchemaConfig) {
        this.missingTypeCollector = missingTypeCollector;
        this.jsonConfigSchemaConfig = jsonConfigSchemaConfig;
    }

    /**
     * Maps a Spring configuration property type to a basic JSON Schema property definition.
     *
     * @param springType the fully qualified name of the Spring/Java type
     * @param prop       the configuration property metadata
     * @return JsonSchemaProperties containing the basic type mapping
     */
    public JsonSchemaProperties typeProp(String springType, Property prop) {
        log.debug("mapTypeProp({}, {})", springType, prop);
        JsonSchemaProperties jsonSchemaProperties;
        jsonSchemaProperties = extendedTypeProp(springType, prop);
        if (jsonSchemaProperties != null) {
            return jsonSchemaProperties;
        }


        switch (springType) {
            // Text-like
            case "java.lang.String":
            case "java.lang.CharSequence":
            case "java.lang.Character":
            case "char":
            case "char[]":
                return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();

            // Binary
            case "byte[]":
            case "java.lang.Byte[]":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .contentEncoding("base64")
                        .build();
            case "byte":
            case "java.lang.Byte":
                return JsonSchemaProperties.builder().type(JsonSchemaType.INTEGER).build();

            // Temporal
            case "java.util.TimeZone":
            case "java.time.ZoneId":
                return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();

            case "java.time.Instant":
            case "java.time.OffsetDateTime":
            case "java.time.ZonedDateTime":
            case "java.time.LocalDateTime":
            case "java.util.Date":
            case "java.util.Calendar":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
//                        .format(JsonSchemaFormat.DATE_TIME)
                        .build();
            case "java.time.LocalDate":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
//                        .format(JsonSchemaFormat.DATE)
                        .build();
            case "java.time.LocalTime":
            case "java.time.OffsetTime":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
//                        .format(JsonSchemaFormat.TIME)
                        .build();
            case "java.time.Duration":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
//                        .format(JsonSchemaFormat.DURATION)
                        .build();

            // Files and resources
            case "java.io.File":
            case "java.nio.file.Path":
                return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();
            case "org.springframework.core.io.Resource":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
//                        .format(JsonSchemaFormat.URI_REFERENCE)
                        .build();
            // Identifiers and network
            case "java.util.UUID":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .format(JsonSchemaFormat.UUID)
                        .build();
            case "java.net.URI":
            case "java.net.URL":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .format(JsonSchemaFormat.URI)
                        .build();
            case "java.net.Inet4Address":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .format(JsonSchemaFormat.IPV4)
                        .build();
            case "java.net.Inet6Address":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .format(JsonSchemaFormat.IPV6)
                        .build();
            case "java.net.InetAddress":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .build();

            // Spring-specific simple types
            case "org.springframework.util.unit.DataSize":
            case "org.springframework.http.MediaType":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .build();

            case "java.lang.Boolean":
            case "boolean":
            case "java.util.concurrent.atomic.AtomicBoolean":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.BOOLEAN)
                        .build();

            // Integers
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
            case "java.lang.Short":
            case "short":
            case "java.math.BigInteger":
            case "java.util.concurrent.atomic.AtomicInteger":
            case "java.util.concurrent.atomic.AtomicLong":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.INTEGER)
                        .build();

            // Numbers
            case "java.lang.Float":
            case "float":
            case "double":
            case "java.lang.Double":
            case "java.lang.Number":
            case "java.math.BigDecimal":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.NUMBER)
                        .build();

            // Generic object-like
            case "java.lang.Object":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.OBJECT)
                        .build();

            // Class names: better represented as string (FQCN)
            case "java.lang.Class":
                return JsonSchemaProperties.builder()
                        .type(JsonSchemaType.STRING)
                        .build();
        }

        if (isArray(springType)) {
            return JsonSchemaProperties.builder().type(JsonSchemaType.ARRAY).build();
        }
        if (isMap(springType)) {
            return JsonSchemaProperties.builder().type(JsonSchemaType.OBJECT).build();
        }
        if (isEnum(springType)) {
            return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();
        }


        missingTypeCollector.addType(springType, prop);
        log.debug("Mapping Spring type: {}  for Property {}", springType, prop);
        try {
            Class<?> type = Class.forName(springType);
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) {
                return JsonSchemaProperties.builder().type(JsonSchemaType.OBJECT).build();
            } else {
                log.error("Missing primitive type {} for Property {}", springType, prop);
                return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();
            }
        } catch (ClassNotFoundException e) {
            return JsonSchemaProperties.builder().type(JsonSchemaType.STRING).build();
        }
    }


    private JsonSchemaProperties extendedTypeProp(String springType, Property prop) {

        Map<String, JsonSchemaProperties> extendedTypeProps = new HashMap<>() {{
            put("java.util.Locale", JsonSchemaProperties.builder().reference("#/$defs/java.util.Locale").build());
            put("java.nio.charset.Charset", JsonSchemaProperties.builder().reference("#/$defs/java.nio.charset.Charset").build());
            put("logging.level", JsonSchemaProperties.builder().reference("#/$defs/loggerLevelProp").build());
            put("logging.threshold.console", JsonSchemaProperties.builder().reference("#/$defs/loggerLevel").build());
            put("logging.threshold.file", JsonSchemaProperties.builder().reference("#/$defs/loggerLevel").build());
        }};


        JsonSchemaProperties JsonSchemaProperties;
        if (prop != null) {
            JsonSchemaProperties = jsonConfigSchemaConfig.getJsonSchemaPropertiesMap().get(prop.getName());
            if (JsonSchemaProperties != null) return JsonSchemaProperties;
        }
        if (springType != null) {
            JsonSchemaProperties = jsonConfigSchemaConfig.getJsonSchemaPropertiesMap().get(springType);
            if (JsonSchemaProperties != null) return JsonSchemaProperties;
        }
        if (prop != null) {
            JsonSchemaProperties = extendedTypeProps.get(prop.getName());
            if (JsonSchemaProperties != null) return JsonSchemaProperties;
        }
        if (springType != null) {
            JsonSchemaProperties = extendedTypeProps.get(springType);
            return JsonSchemaProperties;
        }

        return null;
    }

    /**
     * Checks if the given Spring type represents an array or collection.
     *
     * @param springType the fully qualified name of the Spring/Java type
     * @return true if the type is an array or collection, false otherwise
     */
    public boolean isArray(String springType) {
        Class<?> clazz;
        try {
            if (springType.contains("[]")) {
                return true;
            }
            if (springType.contains("<")) {
                springType = springType.split("<")[0];
            }
            clazz = Class.forName(springType);
            if (List.class.isAssignableFrom(clazz)) {
                return true;
            }
            if (Set.class.isAssignableFrom(clazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            log.debug("Error while mapping Spring type: {}  for Property {}", springType, e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Checks if the given Spring type represents a Map.
     *
     * @param springType the fully qualified name of the Spring/Java type
     * @return true if the type is a Map, false otherwise
     */
    public boolean isMap(String springType) {
        try {
            if (springType.contains("<")) {
                springType = springType.split("<")[0];
            }
            Class<?> clazz = Class.forName(springType);
            if (Map.class.isAssignableFrom(clazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    /**
     * Checks if the given Spring type represents an Enum.
     *
     * @param springType the fully qualified name of the Spring/Java type
     * @return true if the type is an Enum, false otherwise
     */
    public boolean isEnum(String springType) {
        try {
            Class<?> clazz = Class.forName(springType);
            if (clazz.isEnum()) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }
}

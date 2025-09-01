
package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class TypeMappingService {

    private final MissingTypeCollector missingTypeCollector;

    public TypeMappingService(MissingTypeCollector missingTypeCollector) {
        this.missingTypeCollector = missingTypeCollector;
    }
    public Map<String,String> mapType(String springType) {
        return mapTypeProp(springType,null);
    }

    public Map<String,String> mapTypeProp(String springType, Property prop) {
        log.debug("mapTypeProp({}, {})", springType, prop);
        if (prop != null) {
            if (prop.getName().equals("logging.level")) return Map.of("$ref", "#/$defs/loggerLevel");
        }
        if (springType.equals("java.util.Locale")) return Map.of("$ref","#/$defs/Locales");
        if (springType.equals("java.nio.charset.Charset")) return Map.of("$ref","#/$defs/Charsets");
        switch (springType) {
            case "java.lang.String":
//            case "java.lang.String[]":
            case "java.time.Duration": // can improve by introducing some regexp
            case "java.util.Date":
            case "java.util.Calendar":
            case "java.util.TimeZone":
            case "org.springframework.util.unit.DataSize":
            case "java.lang.Character":
            case "char":
            case "java.io.File":
            case "org.springframework.http.MediaType":
            case "java.net.InetAddress":
            case "java.net.URI":
            case "org.springframework.core.io.Resource":
                return Map.of("type","string");
            case "java.lang.Boolean":
            case "boolean":
                return Map.of("type","boolean");
            case "java.lang.Integer":
            case "int":
            case "java.lang.Long":
            case "long":
            case "java.lang.Short":
            case "short":
            case "java.math.BigInteger":
                return Map.of("type","integer");
            case "java.lang.Float":
            case "float":
            case "double":
            case "java.lang.Double":
            case "java.math.BigDecimal":
                return Map.of("type","number");
            case "java.lang.Object":
                return Map.of("type","object");
        }
        if (isArray(springType)) return Map.of("type","array");
        if (isMap(springType)) return Map.of("type","object");
        if (isEnum(springType)) return Map.of("type","string");
        try {
            Class<?> type = Class.forName(springType);
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) {
                missingTypeCollector.addType(springType,prop);
                return Map.of("type","object");
            }
        } catch (ClassNotFoundException e) {
            if (springType.contains("Enum")) return Map.of("type","string");
        }
        missingTypeCollector.addType(springType,prop);
        log.debug("Mapping Spring type: {}  for Property {}", springType,prop);
        return Map.of("type","string");
    }

    public boolean isArray(String springType) {
        Class<?> clazz;
        try{
            if(springType.contains("java.lang.String[]")){return true;}
            if(springType.contains("<")){
                springType=springType.split("<")[0];
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
    public boolean isMap(String springType) {
        Class<?> clazz;
        try {
            if(springType.contains("<")){
                springType=springType.split("<")[0];
            }
            clazz = Class.forName(springType);
            if (Map.class.isAssignableFrom(clazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }
    public boolean isEnum(String springType) {
        Class<?> clazz;
        try {
            clazz = Class.forName(springType);
            if (clazz.isEnum()) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }
}

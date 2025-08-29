
package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class TypeMappingService {

    private final MissingTypeCollector missingTypeCollector;

    public TypeMappingService(MissingTypeCollector missingTypeCollector) {
        this.missingTypeCollector = missingTypeCollector;
    }
    public String mapType(String springType) {
        return mapTypeProp(springType,"null");
    }

    public String mapTypeProp(String springType, String prop) {
//        log.debug("Mapping Spring type: {}", springType);
        switch (springType) {
            case "java.lang.String":
//            case "java.lang.String[]":
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
            case "java.lang.Object":
                return "object";
        }
        if (isArray(springType)) return "array";
        if (isMap(springType)) return "object";
        if (isEnum(springType)) return "string";
        try {
            Class<?> type = Class.forName(springType);
            if (!type.isPrimitive() && !type.getName().startsWith("java.lang.")) return "object";
        } catch (ClassNotFoundException e) {
            if (springType.contains("Enum")) return "string";
        }
        missingTypeCollector.addType(springType,prop);
        log.debug("Mapping Spring type: {}  for Property {}", springType,prop);
        return "string";
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

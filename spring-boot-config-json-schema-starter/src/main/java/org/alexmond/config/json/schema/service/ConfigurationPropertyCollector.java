package org.alexmond.config.json.schema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Service responsible for collecting configuration properties from various sources,
 * including annotated beans and environment properties.
 */
@Slf4j
@RequiredArgsConstructor
public class ConfigurationPropertyCollector {

    private final ApplicationContext context;
    private final ConfigurableEnvironment env;
    private final JsonConfigSchemaConfig config;
    private final JsonSchemaBuilder schemaBuilder;

    /**
     * Collects all included property names from various sources.
     * This includes properties from annotated beans, environment properties,
     * and additional properties specified in the configuration.
     *
     * @return List of property names that should be included in the schema
     */
    public List<String> collectIncludedPropertyNames() {
        List<String> included = new ArrayList<>();
        collectAnnotatedBeanProperties(included);
        collectEnvironmentPropertyKeys(included);
        included.addAll(config.getAdditionalProperties());
        return included;
    }

    /**
     * Collects property names from beans annotated with @ConfigurationProperties.
     * Extracts the prefix from the annotation and adds it to the included properties list.
     *
     * @param included List to which the collected property names will be added
     */
    private void collectAnnotatedBeanProperties(List<String> included) {
        // Inside the loop from the previous step
        Map<String, ConfigurationPropertiesBean> beans = ConfigurationPropertiesBean.getAll(context);

        beans.forEach((beanName, configBean) -> {
            String prefix = configBean.getAnnotation().prefix();
            if (prefix.isEmpty()) {
                prefix = configBean.getAnnotation().value();
            }

            if (!prefix.isEmpty()) {
                log.info("Adding property for processing: {} (from bean: {})", prefix, beanName);
                included.add(prefix);
            } else {
                Class<?> clazz = context.getType(beanName);
                if (clazz != null) {
                    for (Field field : clazz.getDeclaredFields()) {
                        included.add(schemaBuilder.toKebabCase(field.getName()));
                    }
                }
            }
        });
    }


    /**
     * Collects all property keys from the environment.
     * Adds non-null property keys to the included properties list.
     *
     * @param included List to which the collected property keys will be added
     */
    private void collectEnvironmentPropertyKeys(List<String> included) {
        for (String key : getAllPropertyKeys()) {
            if (key != null) {
                log.debug("Found property key: {}", key);
                included.add(key);
            }
        }
    }

    /**
     * Retrieves all property keys from enumerable property sources in the environment.
     *
     * @return Set of all available property keys
     */
    public Set<String> getAllPropertyKeys() {
        Set<String> keys = new HashSet<>();
        for (PropertySource<?> propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?>) {
                keys.addAll(Arrays.asList(((EnumerablePropertySource<?>) propertySource).getPropertyNames()));
            }
        }
        return keys;
    }
}

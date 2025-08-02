package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.*;

@Slf4j
public class ConfigurationPropertyCollector {

    private final ApplicationContext context;
    private final ConfigurableEnvironment env;
    private final JsonConfigSchemaConfig config;

    public ConfigurationPropertyCollector(ApplicationContext context, 
                                        ConfigurableEnvironment env, 
                                        JsonConfigSchemaConfig config) {
        this.context = context;
        this.env = env;
        this.config = config;
    }

    public List<String> collectIncludedPropertyNames() {
        List<String> included = new ArrayList<>();
        collectAnnotatedBeanProperties(included);
        collectEnvironmentPropertyKeys(included);
        included.addAll(config.getAdditionalProperties());
        return included;
    }

    private void collectAnnotatedBeanProperties(List<String> included) {
        context.getBeansWithAnnotation(ConfigurationProperties.class).forEach((key, configBean) -> {
            ConfigurationProperties annotation = AnnotationUtils.findAnnotation(configBean.getClass(), ConfigurationProperties.class);
            if (annotation != null) {
                log.info("Annotation value (AnnotationUtils): {}", annotation.value());
                included.add(annotation.value());
            }
        });
    }

    private void collectEnvironmentPropertyKeys(List<String> included) {
        for (String key : getAllPropertyKeys()) {
            if (key != null) {
                log.debug("Found property key: {}", key);
                included.add(key);
            }
        }
    }

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

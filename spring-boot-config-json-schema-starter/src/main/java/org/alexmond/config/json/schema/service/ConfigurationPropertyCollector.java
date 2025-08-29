package org.alexmond.config.json.schema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.config.JsonConfigSchemaConfig;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class ConfigurationPropertyCollector {

    private final ApplicationContext context;
    private final ConfigurableEnvironment env;
    private final JsonConfigSchemaConfig config;

    public List<String> collectIncludedPropertyNames() {
        List<String> included = new ArrayList<>();
        collectAnnotatedBeanProperties(included);
        collectEnvironmentPropertyKeys(included);
        included.addAll(config.getAdditionalProperties());
        return included;
    }

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

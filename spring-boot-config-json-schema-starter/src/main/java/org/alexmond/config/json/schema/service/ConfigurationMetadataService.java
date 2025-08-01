package org.alexmond.config.json.schema.service;

import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
//import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepositoryJsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


@Slf4j
public class ConfigurationMetadataService {

    public List<InputStream> collectMetadataStreams(ClassLoader classLoader) throws IOException {
        List<InputStream> streams = new ArrayList<>();
        
        Enumeration<URL> resources = classLoader.getResources("META-INF/spring-configuration-metadata.json");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            log.debug("Found configuration metadata file: {}", url);
            streams.add(url.openStream());
        }
        return streams;
    }

    private void closeStreams(List<InputStream> streams) {
        for (InputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException ignored) {
                log.warn("Failed to close metadata stream");
            }
        }
    }

}

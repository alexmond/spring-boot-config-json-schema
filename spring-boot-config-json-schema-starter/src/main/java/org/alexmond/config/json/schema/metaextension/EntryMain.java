package org.alexmond.config.json.schema.metaextension;

import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.metamodel.Property;
import org.alexmond.config.json.schema.service.ConfigurationMetadataService;

import java.util.HashMap;

@Slf4j
public class EntryMain {

    public static void main(String[] args) throws Exception {

        BootConfigMetaLoader  loader = new BootConfigMetaLoader();
        ConfigurationMetadataService metadataService = new ConfigurationMetadataService();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        HashMap<String, Property> propertyMap = loader.loadFromInputStreams(metadataService.collectMetadataStreams(classLoader));

//        System.out.println(propertyMap);
    }

}

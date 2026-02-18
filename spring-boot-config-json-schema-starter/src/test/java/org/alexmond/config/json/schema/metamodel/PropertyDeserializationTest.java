package org.alexmond.config.json.schema.metamodel;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class PropertyDeserializationTest {

    @Test
    void testDeserializeWithNullGroupProperty() throws Exception {
        String json = "{\"name\":\"test\", \"groupProperty\": null}";
        ObjectMapper mapper = new ObjectMapper();
        
        // This is expected to fail with Jackson 3 if groupProperty is primitive boolean
        Property property = mapper.readValue(json, Property.class);
        
        assertEquals("test", property.getName());
        assertFalse(property.isGroupProperty());
    }

    @Test
    void testDeserializeBootConfigMetaWithNullGroupProperty() throws Exception {
        String json = "{\"properties\": [{\"name\":\"test\", \"groupProperty\": null}]}";
        ObjectMapper mapper = new ObjectMapper();
        
        BootConfigMeta meta = mapper.readValue(json, BootConfigMeta.class);
        
        assertEquals(1, meta.getProperties().size());
        assertEquals("test", meta.getProperties().get(0).getName());
        assertFalse(meta.getProperties().get(0).isGroupProperty());
    }
}

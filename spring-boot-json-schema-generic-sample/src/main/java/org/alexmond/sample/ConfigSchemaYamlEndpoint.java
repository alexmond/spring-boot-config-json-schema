// Java
package org.alexmond.sample;

import lombok.RequiredArgsConstructor;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "config-schema.yaml")
@RequiredArgsConstructor
public class ConfigSchemaYamlEndpoint {

    private final JsonSchemaService jsonSchemaService;

    @ReadOperation
    public String schema() throws Exception {
        return jsonSchemaService.generateFullSchemaYaml();
    }
}

package org.alexmond.sample.controller;


import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;

@RestController
@Slf4j
public class GenerateJsonSchema {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @GetMapping("/config-schema")
    public String getConfigSchema() throws Exception {
        return jsonSchemaService.generateFullSchema();
    }

}

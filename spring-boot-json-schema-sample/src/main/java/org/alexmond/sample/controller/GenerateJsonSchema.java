package org.alexmond.sample.controller;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.config.json.schema.service.JsonSchemaService;
import org.alexmond.config.json.schema.service.MissingTypeCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;

@RestController
@Slf4j
public class GenerateJsonSchema {

    @Autowired
    private JsonSchemaService jsonSchemaService;

    @Autowired
    private MissingTypeCollector missingTypeCollector;

    @GetMapping("/configSchema")
    public String Config() throws Exception {

        String jsonConfigSchema;

        jsonConfigSchema = jsonSchemaService.generateFullSchema();

        // Save as JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectWriter jsonWriter = jsonMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing json schema");
        jsonWriter.writeValue(Paths.get("sample-schema.json").toFile(), jsonMapper.readTree(jsonConfigSchema));

        // Save as YAML 
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        ObjectWriter yamlWriter = yamlMapper.writer(new DefaultPrettyPrinter());
        log.info("Writing yaml schema");
        yamlWriter.writeValue(Paths.get("sample-schema.yaml").toFile(), jsonMapper.readTree(jsonConfigSchema));

        missingTypeCollector.getMissingTypes().forEach(type -> log.info("Missing type: {}",type));

        return "configSample";
    }


}

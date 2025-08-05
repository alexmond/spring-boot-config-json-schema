package org.alexmond.sample.test.controller;

import org.alexmond.sample.test.config.ConfigSample;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigPrintRest {
final
ConfigSample configSample;

    public ConfigPrintRest(ConfigSample configSample) {
        this.configSample = configSample;
    }

    @GetMapping("/config")
    public ConfigSample Config(){
        return configSample;
    }
}

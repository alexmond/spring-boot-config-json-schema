package org.alexmond.sample.controller;

import org.alexmond.sample.config.ConfigSample;
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
    public ConfigSample Config() {
        return configSample;
    }
}

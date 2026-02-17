package org.alexmond.sample.test.asciimodel;

import lombok.Data;
import org.alexmond.sample.test.config.AsciiDocGroupConfig;

import java.util.Map;
import java.util.TreeMap;

@Data
public class AsciiDocGroupData {
    private String prefix;
    private String description;
    public AsciiDocGroupData(AsciiDocGroupConfig asciiDocGroupConfig) {
        super();
        this.setPrefix(asciiDocGroupConfig.getPrefix());
        this.setDescription(asciiDocGroupConfig.getDescription());
    }
    public AsciiDocGroupData(String description) {
        super();
        this.setDescription(description);
    }
    Map<String, AsciiDoc> asciiDocs = new TreeMap<>();
}

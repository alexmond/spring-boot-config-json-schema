package org.alexmond.sample.test.asciimodel;

import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class AsciiDocModel {
    Map<String, AsciiDocGroupData> commonGroup = new TreeMap<>();
    Map<String, PropGroup> groups = new TreeMap<>();
}

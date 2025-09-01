package org.alexmond.config.json.schema.service;

import org.alexmond.config.json.schema.metamodel.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MissingTypeCollector {
    Map<String, Integer> missingTypes = new HashMap<>();

    public void addType(String type, Property prop) {
        missingTypes.merge(type, 1, Integer::sum);
    }

    public List<Map.Entry<String,Integer>> getMissingTypes() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(missingTypes.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list;
    }
}

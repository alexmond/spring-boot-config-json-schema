package org.alexmond.config.json.schema.service;

import org.alexmond.config.json.schema.metamodel.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Collects and tracks missing type information during JSON schema generation.
 * This class maintains a count of how many times each missing type is encountered.
 */
public class MissingTypeCollector {
    Map<String, Integer> missingTypes = new HashMap<>();

    /**
     * Records an occurrence of a missing type.
     *
     * @param type The missing type identifier
     * @param prop The property where the missing type was encountered
     */
    public void addType(String type, Property prop) {
        missingTypes.merge(type, 1, Integer::sum);
    }

    /**
     * Returns a sorted list of missing types and their occurrence counts.
     * The list is sorted by the number of occurrences in ascending order.
     *
     * @return List of entries containing type names and their occurrence counts
     */
    public List<Map.Entry<String, Integer>> getMissingTypes() {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(missingTypes.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list;
    }
}

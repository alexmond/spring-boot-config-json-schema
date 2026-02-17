package org.alexmond.sample.test.asciimodel;

import lombok.Data;

import java.util.List;

@Data
public class PropGroup {
    AsciiDocGroupData group;
    List<AsciiDocGroupData> subGroups;
}

package com.project.abac.encryption;

import java.util.*;

public class AttributeGroupingEngine {

    public Map<String, Set<String>> group(Set<String> attrs) {

        Map<String, Set<String>> groups = new LinkedHashMap<>();

        for (String a : attrs) {
            if (a.startsWith("ROLE"))
                groups.computeIfAbsent("ROLE_GROUP", k -> new HashSet<>()).add(a);
            else if (a.startsWith("DEPT"))
                groups.computeIfAbsent("DEPT_GROUP", k -> new HashSet<>()).add(a);
            else
                groups.computeIfAbsent("SECURITY_GROUP", k -> new HashSet<>()).add(a);
        }

        System.out.println("\n[ATTRIBUTE GROUPING]");
        groups.forEach((k,v) -> System.out.println(k + " -> " + v));

        return groups;
    }
}

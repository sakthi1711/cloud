package com.project.abac.dataset;

import java.util.*;

public class AttributeExtractor {

    public Set<String> extractAttributes(String[] row) {

        Set<String> attrs = new HashSet<>();

        // ---- Correct IBM HR Dataset Mapping ----
        String department = row[4];        // Sales, HR, Research & Development
        String jobRole = row[15];          // Sales Executive, Manager, etc.
        String businessTravel = row[2];    // Travel_Rarely, Travel_Frequently
        int jobLevel = Integer.parseInt(row[14]); // 1,2,3,4,5

        // ---- CP-ABE Attributes ----
        attrs.add("DEPT_" + department.replace(" ", "_"));
        attrs.add("ROLE_" + jobRole.replace(" ", "_"));
        attrs.add("TRAVEL_" + businessTravel.replace(" ", "_"));

        // ---- Clearance derived from JobLevel (CORRECT) ----
        if (jobLevel <= 2)
            attrs.add("CLEARANCE_Low");
        else if (jobLevel == 3)
            attrs.add("CLEARANCE_Medium");
        else
            attrs.add("CLEARANCE_High");

        return attrs;
    }
}

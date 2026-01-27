package com.accesscontrol;
import java.util.*;
import java.util.stream.Collectors;

public class AttributeOptimizer {

    // Simulates an "Importance Score" for attributes.
    // In a real ML project, this would use "Information Gain" or "Entropy".
    // Here, we define the "Critical" attributes manually or logically.
    private static final List<String> HIGH_VALUE_ATTRIBUTES = Arrays.asList(
            "Department", "JobRole", "Salary", "EmployeeID", "PerformanceRating"
    );

    public static Set<String> selectEfficientAttributes(Set<String> allAttributes, double keepRatio) {
        System.out.println("   [Optimizer] Analyzing " + allAttributes.size() + " attributes...");
        
        Set<String> optimizedSet = new HashSet<>();
        List<String> sortedAttrs = new ArrayList<>(allAttributes);

        // 1. Always keep the High Value ones if they exist in the dataset
        for (String attr : sortedAttrs) {
            if (HIGH_VALUE_ATTRIBUTES.contains(attr)) {
                optimizedSet.add(attr);
            }
        }

        // 2. If we haven't reached 60%, fill with others until we hit the ratio
        int targetSize = (int) Math.ceil(allAttributes.size() * keepRatio);
        
        for (String attr : sortedAttrs) {
            if (optimizedSet.size() >= targetSize) break;
            if (!optimizedSet.contains(attr)) {
                optimizedSet.add(attr);
            }
        }

        System.out.println("   [Optimizer] Reduced from " + allAttributes.size() + " to " + optimizedSet.size() + " attributes (Top " + (keepRatio*100) + "%).");
        System.out.println("   [Optimizer] Selected: " + optimizedSet);
        return optimizedSet;
    }
}

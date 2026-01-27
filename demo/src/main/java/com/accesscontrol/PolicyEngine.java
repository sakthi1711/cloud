package com.accesscontrol;

import java.util.Set;

public class PolicyEngine {

    // Simulates the Outsourced Policy Check
    // Returns TRUE only if User Attributes match the Data Policy
    public static boolean checkPolicy(String policy, Set<String> userAttributes) {
        // Simple AND logic simulation
        // Policy example: "Department:Sales AND JobRole:Executive"
        
        if (policy == null || policy.isEmpty()) return true;

        String[] rules = policy.split(" AND ");
        for (String rule : rules) {
            if (!userAttributes.contains(rule.trim())) {
                return false; // Access Denied
            }
        }
        return true; // Access Granted
    }

    // Checks Revocation List
    public static boolean isRevoked(String userId, Set<String> revocationList) {
        return revocationList.contains(userId);
    }
}
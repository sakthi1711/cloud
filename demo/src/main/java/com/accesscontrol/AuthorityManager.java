package com.accesscontrol;

import java.util.*;

public class AuthorityManager {

    // Distributes attributes to authorities fairly (Round Robin)
    public static List<Set<String>> distributeAttributes(Set<String> allAttributes, int numAuthorities) {
        List<Set<String>> authorities = new ArrayList<>();
        for (int i = 0; i < numAuthorities; i++) {
            authorities.add(new HashSet<>());
        }
        
        int i = 0;
        for (String attr : allAttributes) {
            authorities.get(i % numAuthorities).add(attr);
            i++;
        }
        return authorities;
    }

    // Fairness Score Calculation (1.0 = Perfect, 0.0 = Bad)
    public static double calculateFairness(List<Set<String>> authorities) {
        if (authorities.isEmpty()) return 0.0;
        
        double mean = authorities.stream().mapToInt(Set::size).average().orElse(0);
        double sumSqDiff = authorities.stream().mapToDouble(a -> Math.pow(a.size() - mean, 2)).sum();
        double variance = sumSqDiff / authorities.size();
        
        // Formula: 1 / (1 + Variance). 
        // If variance is 0 (perfect balance), score is 1.0.
        return 1.0 / (1.0 + variance);
    }
}
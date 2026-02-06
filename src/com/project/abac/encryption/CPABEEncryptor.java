package com.project.abac.encryption;

import com.project.abac.policy.AccessPolicy;
import java.util.*;

public class CPABEEncryptor {

    private AttributeGroupingEngine grouping = new AttributeGroupingEngine();

    public Ciphertext encrypt(Set<String> attrs, AccessPolicy policy) {

        long start = System.currentTimeMillis();
        Map<String, Set<String>> groups = grouping.group(attrs);
        long end = System.currentTimeMillis();

        long encTime = end - start;
        System.out.println("[ENCRYPTION] Time: " + encTime + " ms");

        return new Ciphertext(policy, groups, encTime);
    }
}

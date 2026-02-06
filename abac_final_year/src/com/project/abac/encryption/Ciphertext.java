package com.project.abac.encryption;

import com.project.abac.policy.AccessPolicy;
import java.util.*;

public class Ciphertext {

    private AccessPolicy policy;
    private Map<String, Set<String>> groups;
    private long encryptionTime;

    public Ciphertext(AccessPolicy policy,
                      Map<String, Set<String>> groups,
                      long encryptionTime) {
        this.policy = policy;
        this.groups = groups;
        this.encryptionTime = encryptionTime;
    }

    public AccessPolicy getPolicy() { return policy; }
    public int getGroupCount() { return groups.size(); }
    public long getEncryptionTime() { return encryptionTime; }
}

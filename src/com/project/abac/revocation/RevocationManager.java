package com.project.abac.revocation;

import java.util.HashSet;
import java.util.Set;

public class RevocationManager {
    private static Set<String> revokedUsers = new HashSet<>();

    public static void revokeUser(String userId) {
        revokedUsers.add(userId);
    }

    public static boolean isRevoked(String userId) {
        return revokedUsers.contains(userId);
    }
}


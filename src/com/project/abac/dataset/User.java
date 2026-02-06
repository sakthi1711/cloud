package com.project.abac.dataset;

import java.util.Set;

public class User {
    private String userId;
    private Set<String> attributes;

    public User(String userId, Set<String> attributes) {
        this.userId = userId;
        this.attributes = attributes;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getAttributes() {
        return attributes;
    }
}

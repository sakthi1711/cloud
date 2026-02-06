package com.project.abac.policy;

import java.util.*;

public class AccessPolicy {

    public enum Type { AND, OR, LEAF }

    private Type type;
    private String attribute;
    private List<AccessPolicy> children;

    public AccessPolicy(String attribute) {
        this.type = Type.LEAF;
        this.attribute = attribute;
    }

    public AccessPolicy(Type type, AccessPolicy... nodes) {
        this.type = type;
        this.children = Arrays.asList(nodes);
    }

    public boolean evaluate(Set<String> userAttrs) {
        if (type == Type.LEAF)
            return userAttrs.contains(attribute);

        if (type == Type.AND)
            return children.stream().allMatch(p -> p.evaluate(userAttrs));

        if (type == Type.OR)
            return children.stream().anyMatch(p -> p.evaluate(userAttrs));

        return false;
    }
}

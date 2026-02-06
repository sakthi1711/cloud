package com.project.abac.dataset;

import java.util.*;

public class UserManager {

    public Map<String, User> createUsers(List<String[]> rows) {

        Map<String, User> users = new LinkedHashMap<>();
        AttributeExtractor extractor = new AttributeExtractor();

        int count = 1;
        for (String[] row : rows) {
            String userId = "user" + count++;
            users.put(userId, new User(userId, extractor.extractAttributes(row)));
        }
        return users;
    }
}

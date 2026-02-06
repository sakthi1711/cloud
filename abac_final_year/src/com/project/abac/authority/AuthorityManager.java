package com.project.abac.authority;

public class AuthorityManager {

    public void issueKey(String user, String attribute) {
        System.out.println("Issuing key for " + attribute + " to " + user);
    }
}

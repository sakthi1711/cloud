package com.project.abac.cloud;

public class FileObject {

    private String fileName;
    private String encryptedContent;

    public FileObject(String fileName, String encryptedContent) {
        this.fileName = fileName;
        this.encryptedContent = encryptedContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }
}

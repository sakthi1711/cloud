package com.project.abac;

import com.project.abac.dataset.*;
import com.project.abac.policy.*;
import com.project.abac.encryption.*;
import com.project.abac.decryption.*;
import com.project.abac.cloud.FileObject;
import com.project.abac.cloudsim.CloudSimManager;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== PRIVACY PRESERVING CP-ABE CLOUD SYSTEM ===");

        // 1. Load dataset
        System.out.print("Enter IBM HR Dataset CSV path: ");
        String path = sc.nextLine();

        DatasetLoader loader = new DatasetLoader();
        UserManager userManager = new UserManager();

        List<String[]> rows = loader.loadDataset(path);
        Map<String, User> users = userManager.createUsers(rows);

        System.out.println("Users created: " + users.size());

        // 2. Init CloudSim
        CloudSimManager.initCloudSim();

        // 3. Data owner uploads file
        System.out.println("\n[DATA OWNER] Uploading file: salary_report.pdf");

        // 4. Data owner defines policy
        AccessPolicy policy =
            new AccessPolicy(AccessPolicy.Type.OR,
                new AccessPolicy(AccessPolicy.Type.AND,
                    new AccessPolicy("ROLE_Manager"),
                    new AccessPolicy("DEPT_HR")),
                new AccessPolicy("CLEARANCE_High")
            );

        // -------- DATA OWNER UPLOADS FILE --------
        System.out.println("\n[DATA OWNER] Uploading file: salary_report.pdf");

        String salaryReportContent =
                "Salary Report - Confidential\n" +
                "----------------------------------\n" +
                "Employee Role      : Manager\n" +
                "Department         : Human Resources\n" +
                "Monthly Salary     : ₹1,20,000\n" +
                "Annual Bonus       : ₹3,00,000\n" +
                "----------------------------------";

        // Logical encrypted content (before CP-ABE)
        String encryptedData = "[ENCRYPTED_DATA]";

        FileObject file = new FileObject("salary_report.pdf", encryptedData);


        // 5. Encrypt file
        CPABEEncryptor encryptor = new CPABEEncryptor();
        Ciphertext ciphertext =
                encryptor.encrypt(users.values().iterator().next().getAttributes(), policy);

        // 6. Store encrypted file (logical cloud storage)
        System.out.println("[CLOUD] Encrypted file stored in cloud");

        // 7. User selects userId
        System.out.print("\nEnter user ID (e.g., user5): ");
        String userId = sc.nextLine();

        User user = users.get(userId);
        if (user == null) {
            System.out.println("Invalid user");
            return;
        }

        // 8–9. Outsourced decryption + policy evaluation
        OutsourcedDecryptor decryptor = new OutsourcedDecryptor();
        long totalDecTime = decryptor.decrypt(ciphertext, user.getAttributes());

        boolean granted =
        ciphertext.getPolicy().evaluate(user.getAttributes());

        System.out.println("\n==============================================");

        if (granted) {
            System.out.println("✅ ACCESS GRANTED");
            System.out.println("\n🔓 Decrypted File: " + file.getFileName());
            System.out.println("----------------------------------------------");
            System.out.println(salaryReportContent);
            System.out.println("----------------------------------------------");
        }   else {
                System.out.println("❌ ACCESS DENIED");
                System.out.println("🔒 User is not authorized to view the file");
}

System.out.println("==============================================");

        // 10–11. Result + timing
        System.out.println("\n=== ACCESS RESULT ===");
        System.out.println(granted ? "✅ ACCESS GRANTED" : "❌ ACCESS DENIED");

        System.out.println("\n=== PERFORMANCE SUMMARY ===");
        System.out.println("Encryption Time : " + ciphertext.getEncryptionTime() + " ms");
        System.out.println("Total Decryption Time : " + totalDecTime + " ms");
        System.out.println("Attribute Groups Used : " + ciphertext.getGroupCount());
    }
}

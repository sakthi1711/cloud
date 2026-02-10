package com.project.abac;

import com.project.abac.dataset.*;
import com.project.abac.policy.*;
import com.project.abac.encryption.*;
import com.project.abac.decryption.*;
import com.project.abac.cloud.FileObject;
import com.project.abac.cloudsim.CloudSimManager;
import com.project.abac.revocation.RevocationManager;

import java.io.File;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=== PRIVACY PRESERVING CP-ABE CLOUD SYSTEM ===");

        /* =======================
           1. LOAD DATASET
           ======================= */
        System.out.print("Enter IBM HR Dataset CSV path: ");
        String path = sc.nextLine();

        DatasetLoader loader = new DatasetLoader();
        UserManager userManager = new UserManager();

        List<String[]> rows = loader.loadDataset(path);
        Map<String, User> users = userManager.createUsers(rows);

        System.out.println("Users created: " + users.size());

        /* =======================
           2. INIT CLOUDSIM
           ======================= */
        CloudSimManager.initCloudSim();

        /* =======================
           3. DATA OWNER DEFINES POLICY
           ======================= */
        AccessPolicy policy =
                new AccessPolicy(AccessPolicy.Type.OR,
                        new AccessPolicy(AccessPolicy.Type.AND,
                                new AccessPolicy("ROLE_Manager"),
                                new AccessPolicy("DEPT_HR")),
                        new AccessPolicy("CLEARANCE_High")
                );

        /* =======================
           4. DATA OWNER FILE UPLOAD (RUNTIME)
           ======================= */
        System.out.print("\n[DATA OWNER] Enter file path to upload: ");
        String filePath = sc.nextLine();

        File uploadedFile = new File(filePath);

        if (!uploadedFile.exists() || !uploadedFile.isFile()) {
            System.out.println("Invalid file path. Upload failed.");
            return;
        }

        String fileName = uploadedFile.getName();
        long fileSize = uploadedFile.length();
        String fileType = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf(".") + 1)
                : "unknown";

        System.out.println("File detected: " + fileName);
        System.out.println("File type    : " + fileType);
        System.out.println("File size    : " + fileSize + " bytes");

        String logicalEncryptedContent =
                "[LOGICAL_ENCRYPTED_FILE]\n" +
                "FILE_NAME=" + fileName + "\n" +
                "FILE_TYPE=" + fileType + "\n" +
                "FILE_SIZE=" + fileSize + "\n" +
                "FILE_REF_ID=" + UUID.randomUUID();

        FileObject file = new FileObject(fileName, logicalEncryptedContent);

        System.out.println("[DATA OWNER] Logical encryption completed.");

        /* =======================
           5. CP-ABE ENCRYPTION
           ======================= */
        CPABEEncryptor encryptor = new CPABEEncryptor();
        Ciphertext ciphertext =
                encryptor.encrypt(users.values().iterator().next().getAttributes(), policy);

        System.out.println("[CLOUD] Encrypted file stored in cloud");

        /* =======================
           6. MAIN CONTROL LOOP
           ======================= */
        while (true) {

            System.out.println("\nSelect Mode:");
            System.out.println("1. Admin");
            System.out.println("2. User");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int mode = sc.nextInt();
            sc.nextLine(); // consume newline

            /* =======================
               ADMIN MODE
               ======================= */
            if (mode == 1) {

                System.out.println("\n===== ADMIN MODE =====");
                System.out.print("Do you want to revoke a user? (y/n): ");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("y")) {
                    System.out.print("Enter User ID to revoke: ");
                    String revokeUserId = sc.nextLine();

                    if (users.containsKey(revokeUserId)) {
                        RevocationManager.revokeUser(revokeUserId);
                        System.out.println("User " + revokeUserId + " has been revoked successfully.");
                    } else {
                        System.out.println("Invalid User ID. Revocation failed.");
                    }
                } else {
                    System.out.println("No user revoked.");
                }

                continue; // 🔁 back to menu
            }

            /* =======================
               USER MODE
               ======================= */
            else if (mode == 2) {

                System.out.println("\n===== USER MODE =====");
                System.out.print("Enter user ID (e.g., user5): ");
                String userId = sc.nextLine();

                User user = users.get(userId);
                if (user == null) {
                    System.out.println("Invalid user");
                    continue;
                }

                // 🔐 REVOCATION CHECK
                if (RevocationManager.isRevoked(userId)) {
                    System.out.println("\n==============================================");
                    System.out.println("ACCESS DENIED");
                    System.out.println("Reason: User revoked by admin");
                    System.out.println("==============================================");
                    continue;
                }

                OutsourcedDecryptor decryptor = new OutsourcedDecryptor();
                long totalDecTime = decryptor.decrypt(ciphertext, user.getAttributes());

                boolean granted =
                        ciphertext.getPolicy().evaluate(user.getAttributes());

                System.out.println("\n==============================================");

                if (granted) {
                    System.out.println("ACCESS GRANTED");
                    System.out.println("\nDecrypted File: " + file.getFileName());
                    System.out.println("----------------------------------------------");
                    System.out.println(file.getEncryptedContent());
                    System.out.println("----------------------------------------------");
                } else {
                    System.out.println("ACCESS DENIED");
                    System.out.println("User is not authorized to view the file");
                }

                System.out.println("==============================================");

                System.out.println("\n=== ACCESS RESULT ===");
                System.out.println(granted ? " ACCESS GRANTED" : " ACCESS DENIED");

                System.out.println("\n=== PERFORMANCE SUMMARY ===");
                System.out.println("Encryption Time : " + ciphertext.getEncryptionTime() + " ms");
                System.out.println("Total Decryption Time : " + totalDecTime + " ms");
                System.out.println("Attribute Groups Used : " + ciphertext.getGroupCount());

                continue; // 🔁 back to menu
            }

            /* =======================
               EXIT
               ======================= */
            else if (mode == 3) {
                System.out.println("Exiting system...");
                break;
            }

            else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

package com.accesscontrol;
import org.cloudbus.cloudsim.core.CloudSim;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.*;
import java.util.Base64;

public class CloudSimEncryptionSimulation {

    // TODO: PASTE YOUR SUPABASE DETAILS HERE
    private static final String SUPABASE_URL = "https://rlpwbhbowqiqpxcjmoky.supabase.co"; 
    private static final String SUPABASE_KEY = "sb_publishable_4jSgncnB_VdwSs2Ix-VGvw_UnIZe9b3";

    private static SecretKey secretKey;
    private static final int AES_KEY_SIZE = 128;
    private static final int GCM_TAG_LENGTH = 128;

    static class ResultMetric {
        int dataSize;
        double encryptTime, uploadTime, downloadTime, tradDecryptTime, intelDecryptTime, speedup;

        public ResultMetric(int s, double e, double u, double dl, double td, double id) {
            this.dataSize = s; this.encryptTime = e; this.uploadTime = u;
            this.downloadTime = dl; this.tradDecryptTime = td; this.intelDecryptTime = id;
            this.speedup = (id == 0) ? 0 : td / id;
        }
    }

    public static void main(String[] args) {
        try {
            CloudSim.init(1, Calendar.getInstance(), false);
            SupabaseManager.initialize(SUPABASE_URL, SUPABASE_KEY);
            SupabaseManager.clearTable(); // Clean start
            
            secretKey = generateAESKey();
            List<ResultMetric> results = new ArrayList<>();

            // Load Data
            String filePath = "D:\\final_year\\secure_cloud_access\\demo\\src\\main\\java\\com\\accesscontrol\\HR_Dataset.csv"; 
            List<Map<String, String>> fullDataset = DatasetLoader.loadCSV(filePath);
            if (fullDataset.isEmpty()) {
                System.out.println("❌ Error: HR_Dataset.csv empty/missing.");
                return;
            }

            int[] batchSizes = {10, 50, 100}; 

            System.out.println("\n=== SUPABASE HYBRID SIMULATION STARTED ===");

            for (int size : batchSizes) {
                if (size > fullDataset.size()) break;
                System.out.println(">> Running Batch: " + size + " rows");

                // --- A. ENCRYPTION ---
                List<Map<String, Object>> uploadBatch = new ArrayList<>();
                long startEnc = System.nanoTime();
                
                for (int i = 0; i < size; i++) {
                    Map<String, String> row = fullDataset.get(i);
                    Map<String, String> encryptedContent = new HashMap<>();
                    
                    for (Map.Entry<String, String> entry : row.entrySet()) {
                        encryptedContent.put(entry.getKey(), encryptAES(entry.getValue()));
                    }
                    
                    // Format for Supabase SQL Table: { "id": "exp_10_row_1", "content": { ...encrypted... } }
                    Map<String, Object> supabaseRow = new HashMap<>();
                    supabaseRow.put("id", "batch_" + size + "_row_" + i);
                    supabaseRow.put("content", encryptedContent);
                    uploadBatch.add(supabaseRow);
                }
                double encTime = (System.nanoTime() - startEnc) / 1_000_000.0;

                // --- B. UPLOAD (POST) ---
                long startUp = System.nanoTime();
                SupabaseManager.uploadBatch(uploadBatch);
                double upTime = (System.nanoTime() - startUp) / 1_000_000.0;

                // --- C. DOWNLOAD (GET) ---
                long startDown = System.nanoTime();
                List<Map<String, Object>> fetchedRows = SupabaseManager.fetchAllData();
                double downTime = (System.nanoTime() - startDown) / 1_000_000.0;

                // --- D. DECRYPTION (NOVELTY) ---
                // 1. Traditional (Decrypt Everything)
                long startTrad = System.nanoTime();
                for (Map<String, Object> dbRow : fetchedRows) {
                    // Extract the "content" column which is a LinkedTreeMap
                    Map<String, String> content = (Map<String, String>) dbRow.get("content");
                    if(content != null) {
                        for (String val : content.values()) decryptAES(val);
                    }
                }
                double tradTime = (System.nanoTime() - startTrad) / 1_000_000.0;

                // 2. Intelligent (Decrypt Only Needed)
                long startIntel = System.nanoTime();
                for (Map<String, Object> dbRow : fetchedRows) {
                    Map<String, String> content = (Map<String, String>) dbRow.get("content");
                    if(content != null) {
                        if(content.containsKey("Salary")) decryptAES(content.get("Salary"));
                        if(content.containsKey("Department")) decryptAES(content.get("Department"));
                    }
                }
                double intelTime = (System.nanoTime() - startIntel) / 1_000_000.0;

                results.add(new ResultMetric(size, encTime, upTime, downTime, tradTime, intelTime));
                System.out.println("   Completed Batch.");
            }

            printReport(results);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- AES & UTILS (Keep same as before) ---
    private static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }
    private static String encryptAES(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12]; new Random().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) { return ""; }
    }
    private static String decryptAES(String encryptedText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = Arrays.copyOfRange(decoded, 0, 12);
            byte[] cipherText = Arrays.copyOfRange(decoded, 12, decoded.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) { return ""; }
    }
    private static void printReport(List<ResultMetric> results) {
        System.out.println("\n--- FINAL RESULTS ---");
        System.out.printf("%-10s %-15s %-15s %-15s %-15s %-10s\n", "Rows", "Encrypt", "Upload", "TradDec", "IntelDec", "Speedup");
        for (ResultMetric r : results) {
            System.out.printf("%-10d %-15.2f %-15.2f %-15.4f %-15.4f %-10.2fx\n", r.dataSize, r.encryptTime, r.uploadTime, r.tradDecryptTime, r.intelDecryptTime, r.speedup);
        }
    }
}
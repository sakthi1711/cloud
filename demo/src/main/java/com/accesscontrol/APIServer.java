package com.accesscontrol;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import com.google.gson.Gson;

// Requires CloudSim, Gson, OkHttp dependencies
public class APIServer {

    private static final int PORT = 8080;
    private static final Gson gson = new Gson();
    
    // PASTE YOUR SUPABASE CREDENTIALS HERE
    private static final String SUPABASE_URL = "https://rlpwbhbowqiqpxcjmoky.supabase.co"; 
    private static final String SUPABASE_KEY = "sb_publishable_4jSgncnB_VdwSs2Ix-VGvw_UnIZe9b3";

    public static void main(String[] args) throws IOException {
        // Initialize Core Logic
        SupabaseManager.initialize(SUPABASE_URL, SUPABASE_KEY);
        
        // Start Server
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Define Endpoints (URLs)
        server.createContext("/api/upload", new UploadHandler());
        server.createContext("/api/files", new FileListHandler());
        server.createContext("/api/access", new AccessHandler());
        
        server.setExecutor(null);
        System.out.println(">>> [SERVER] Access Control System running at http://localhost:" + PORT);
        System.out.println(">>> Open your index.html file in the browser now.");
        server.start();
    }

    // --- HANDLER 1: ADMIN UPLOAD (Simulates Owner) ---
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                System.out.println(">>> [ADMIN] Uploading Data...");
                
                // 1. Load Data
                List<Map<String, String>> dataset = DatasetLoader.loadCSV("C://Users//sm912//Desktop//SEM 8//HR_Dataset.csv");
                Set<String> allAttributes = dataset.get(0).keySet();
                
                // 2. NOVELTY: Optimization
                Set<String> optimizedAttrs = AttributeOptimizer.selectEfficientAttributes(allAttributes, 0.60);
                
                // 3. NOVELTY: Fairness
                List<Set<String>> authorityMap = AuthorityManager.distributeAttributes(optimizedAttrs, 3);
                double fairness = AuthorityManager.calculateFairness(authorityMap);

                // 4. Encrypt & Upload (First 10 rows for demo)
                String policy = "Department:Sales AND JobRole:Executive";
                List<Map<String, Object>> packageData = new ArrayList<>();
                
                for(int i=0; i<10; i++) {
                    Map<String, String> row = dataset.get(i);
                    Map<String, String> encRow = new HashMap<>();
                    for(String k : row.keySet()) {
                        if(optimizedAttrs.contains(k)) encRow.put(k, "ENC_" + Base64.getEncoder().encodeToString(row.get(k).getBytes()));
                    }
                    Map<String, Object> file = new HashMap<>();
                    file.put("id", "File_" + i);
                    file.put("content", encRow);
                    file.put("policy", policy);
                    packageData.add(file);
                }
                
                SupabaseManager.clearTable();
                SupabaseManager.uploadBatch(packageData);

                String response = gson.toJson(Map.of("message", "Upload Successful", "fairness", fairness, "optimized", optimizedAttrs.size()));
                sendResponse(exchange, 200, response);

                // After uploadBatch...
                SupabaseManager.logPerformance("Encryption_Upload", 150.5, fairness); // Example time
            }
        }
    }

    // --- HANDLER 2: LIST FILES (For User Dashboard) ---
    static class FileListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Map<String, Object>> files = SupabaseManager.fetchAllData();
                // Send only IDs and Policy to frontend (hide content)
                List<Map<String, String>> safeList = new ArrayList<>();
                for(Map<String, Object> f : files) {
                    Map<String, String> m = new HashMap<>();
                    m.put("id", (String) f.get("id"));
                    m.put("policy", (String) f.get("policy"));
                    safeList.add(m);
                }
                sendResponse(exchange, 200, gson.toJson(safeList));
            }
        }
    }

    // --- HANDLER 3: ACCESS REQUEST (User Clicks 'Decrypt') ---
    static class AccessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read Request
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                Map<String, Object> req = gson.fromJson(isr, Map.class);
                String fileId = (String) req.get("fileId");
                String userRole = (String) req.get("userRole"); // e.g., "Executive"
                String userDept = (String) req.get("userDept"); // e.g., "Sales"

                System.out.println(">>> [USER] Requesting: " + fileId + " | Attrs: " + userRole + ", " + userDept);

                // 1. Fetch File
                List<Map<String, Object>> cloudData = SupabaseManager.fetchAllData();
                Map<String, Object> target = null;
                for(Map<String, Object> f : cloudData) {
                    if(f.get("id").equals(fileId)) target = f;
                }

                if(target == null) { sendResponse(exchange, 404, "File Not Found"); return; }

                // 2. NOVELTY: Outsourced Policy Check
                String policy = (String) target.get("policy");
                Set<String> userAttrs = new HashSet<>(Arrays.asList("Department:"+userDept, "JobRole:"+userRole));
                
                boolean accessGranted = PolicyEngine.checkPolicy(policy, userAttrs);

                if(accessGranted) {
                    // 3. Decrypt
                    Map<String, String> encContent = (Map<String, String>) target.get("content");
                    Map<String, String> plainContent = new HashMap<>();
                    for(String k : encContent.keySet()) {
                        String cipher = encContent.get(k);
                        try {
                            String plain = new String(Base64.getDecoder().decode(cipher.replace("ENC_", "")));
                            plainContent.put(k, plain);
                        } catch(Exception e) { plainContent.put(k, "Error"); }
                    }
                    String json = gson.toJson(Map.of("status", "Granted", "data", plainContent));
                    sendResponse(exchange, 200, json);
                } else {
                    String json = gson.toJson(Map.of("status", "Denied", "reason", "Policy Mismatch"));
                    sendResponse(exchange, 403, json);
                }
                // After decryption...
                SupabaseManager.logPerformance("Decryption_Access", 0.5, 0.0);
            }
        }
    }

    // Helpers
    private static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            try { exchange.sendResponseHeaders(204, -1); } catch (IOException e) {}
        }
    }
}
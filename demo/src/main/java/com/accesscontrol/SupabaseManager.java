package com.accesscontrol; // Keep your package name if you have one

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class SupabaseManager {

    private static String SUPABASE_URL;
    private static String SUPABASE_KEY;
    
    // CHANGE 1: Use the Unsafe Client instead of new OkHttpClient()
    private static final OkHttpClient client = getUnsafeOkHttpClient();
    private static final Gson gson = new Gson();

    public static void initialize(String url, String key) {
        SUPABASE_URL = url;
        SUPABASE_KEY = key;
        System.out.println("✅ Supabase Configuration Set (SSL Verification Disabled for Corporate Network).");
    }

    public static void uploadBatch(List<Map<String, Object>> batchData) {
        String jsonBody = gson.toJson(batchData);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/encrypted_data")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("❌ Upload Error: " + response.code() + " " + (response.body() != null ? response.body().string() : ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logPerformance(String operation, double time, double fairness) {
    Map<String, Object> log = new HashMap<>();
    log.put("operation_type", operation);
    log.put("time_taken_ms", time);
    log.put("fairness_score", fairness);
    
    // We reuse the existing logic but target a different table
    String jsonBody = gson.toJson(log);
    Request request = new Request.Builder()
            .url(SUPABASE_URL + "/rest/v1/experiment_logs")
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
            .build();
    try { client.newCall(request).execute(); } catch (Exception e) {}
}

    public static List<Map<String, Object>> fetchAllData() {
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/encrypted_data?select=*")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                Type listType = new TypeToken<ArrayList<Map<String, Object>>>(){}.getType();
                return gson.fromJson(responseBody, listType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void clearTable() {
         Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/encrypted_data?id=neq.0")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();
         try { client.newCall(request).execute(); } catch (Exception e) {}
    }

    // --- MAGICAL CODE TO FIX CORPORATE NETWORK ERROR ---
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
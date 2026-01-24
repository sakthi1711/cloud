package com.accesscontrol;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class SupabaseManager {

    private static String SUPABASE_URL;
    private static String SUPABASE_KEY;
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static void initialize(String url, String key) {
        SUPABASE_URL = url;
        SUPABASE_KEY = key;
        System.out.println("✅ Supabase Configuration Set.");
    }

    // Uploads the encrypted data row by row
    public static void uploadBatch(List<Map<String, Object>> batchData) {
        // Convert the list of rows to a JSON Array string
        String jsonBody = gson.toJson(batchData);

        // Supabase expects a POST to /rest/v1/table_name
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/encrypted_data")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates") // Updates if ID exists
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("❌ Upload Error: " + response.code() + " " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Downloads data back for decryption
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
    
    // Helper to clear table before run (Optional, for clean results)
    public static void clearTable() {
         Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/encrypted_data?id=neq.0") // Delete all where ID is not 0
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();
         try { client.newCall(request).execute(); } catch (Exception e) {}
    }
}
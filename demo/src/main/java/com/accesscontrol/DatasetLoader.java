package com.accesscontrol;
import java.io.*;
import java.util.*;
public class DatasetLoader {
    public static List<Map<String, String>> loadCSV(String filePath) {
        List<Map<String, String>> dataset = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = br.readLine().split(",");
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    if(i < values.length) row.put(headers[i].trim(), values[i].trim());
                }
                dataset.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return dataset;
    }
}
package com.project.abac.dataset;

import java.io.*;
import java.util.*;

public class DatasetLoader {

    public List<String[]> loadDataset(String path) {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // header
            while ((line = br.readLine()) != null) {
                rows.add(line.split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }
}

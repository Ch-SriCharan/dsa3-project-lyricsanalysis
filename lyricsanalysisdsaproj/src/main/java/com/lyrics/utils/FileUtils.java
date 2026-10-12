package com.lyrics.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * File I/O utilities for reading text lyrics and handling resources.
 */
public class FileUtils {

    /**
     * Reads the entire content of a text file using UTF-8 encoding.
     */
    public static String readFileToString(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File not found: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        if (file.length() == 0) {
            throw new IOException("File is empty: " + file.getName());
        }
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    /**
     * Reads an InputStream into a String.
     */
    public static String readStreamToString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }
}

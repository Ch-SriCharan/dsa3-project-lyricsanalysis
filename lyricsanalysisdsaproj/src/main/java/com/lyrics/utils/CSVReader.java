package com.lyrics.utils;

import com.lyrics.model.Dataset;
import com.lyrics.model.Song;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust CSV Reader compliant with RFC 4180 supporting quoted fields,
 * embedded commas, escaped quotes, and multiline text.
 */
public class CSVReader {

    public static class CSVValidationException extends Exception {
        public CSVValidationException(String message) {
            super(message);
        }
    }

    /**
     * Parses a CSV file from a File object and constructs a Dataset.
     */
    public static Dataset readCSV(File file) throws IOException, CSVValidationException {
        try (InputStream is = new FileInputStream(file)) {
            return readCSV(is, file.getName());
        }
    }

    /**
     * Parses a CSV dataset from an InputStream.
     */
    public static Dataset readCSV(InputStream inputStream, String datasetName) throws IOException, CSVValidationException {
        List<List<String>> rows = parseCSVStream(inputStream);

        if (rows.isEmpty()) {
            throw new CSVValidationException("CSV file is completely empty.");
        }

        List<String> header = rows.get(0);
        int artistCol = -1;
        int titleCol = -1;
        int lyricsCol = -1;

        for (int i = 0; i < header.size(); i++) {
            String col = header.get(i).trim().toLowerCase().replaceAll("[_\\-\\s]+", "");
            if (col.equals("artist") || col.equals("singer") || col.equals("band")) {
                artistCol = i;
            } else if (col.equals("songtitle") || col.equals("song") || col.equals("title") || col.equals("track")) {
                titleCol = i;
            } else if (col.equals("lyrics") || col.equals("lyric") || col.equals("text")) {
                lyricsCol = i;
            }
        }

        List<String> missing = new ArrayList<>();
        if (artistCol == -1) missing.add("Artist");
        if (titleCol == -1) missing.add("Song Title");
        if (lyricsCol == -1) missing.add("Lyrics");

        if (!missing.isEmpty()) {
            throw new CSVValidationException("Invalid dataset header format.\nMissing required column(s): " +
                    String.join(", ", missing) +
                    "\n\nExpected CSV columns: Artist, Song Title, Lyrics");
        }

        List<Song> songs = new ArrayList<>();
        int songIdCounter = 1;

        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            if (row.isEmpty()) continue;

            String artist = (artistCol < row.size()) ? row.get(artistCol).trim() : "";
            String title = (titleCol < row.size()) ? row.get(titleCol).trim() : "";
            String lyrics = (lyricsCol < row.size()) ? row.get(lyricsCol).trim() : "";

            // Ignore blank rows
            if (artist.isEmpty() && title.isEmpty() && lyrics.isEmpty()) {
                continue;
            }

            if (lyrics.isEmpty()) {
                // Skip rows without lyrics or treat gracefully
                continue;
            }

            if (title.isEmpty()) {
                title = "Track " + songIdCounter;
            }
            if (artist.isEmpty()) {
                artist = "Unknown Artist";
            }

            songs.add(new Song(songIdCounter++, artist, title, lyrics));
        }

        if (songs.isEmpty()) {
            throw new CSVValidationException("No valid song records found in CSV file. Ensure the Lyrics column contains text.");
        }

        return new Dataset(datasetName != null ? datasetName : "Uploaded Dataset", songs);
    }

    private static List<List<String>> parseCSVStream(InputStream is) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> currentRow = new ArrayList<>();
            StringBuilder currentField = new StringBuilder();
            boolean inQuotes = false;
            int ch;

            while ((ch = br.read()) != -1) {
                char c = (char) ch;

                if (inQuotes) {
                    if (c == '\"') {
                        br.mark(1);
                        int nextCh = br.read();
                        if (nextCh == '\"') {
                            currentField.append('\"'); // Escaped quote
                        } else {
                            inQuotes = false;
                            if (nextCh != -1) {
                                br.reset();
                            }
                        }
                    } else {
                        currentField.append(c);
                    }
                } else {
                    if (c == '\"') {
                        inQuotes = true;
                    } else if (c == ',') {
                        currentRow.add(currentField.toString().trim());
                        currentField.setLength(0);
                    } else if (c == '\r') {
                        // Look ahead for \n
                        br.mark(1);
                        int nextCh = br.read();
                        if (nextCh != '\n' && nextCh != -1) {
                            br.reset();
                        }
                        currentRow.add(currentField.toString().trim());
                        currentField.setLength(0);
                        if (!currentRow.isEmpty()) {
                            records.add(currentRow);
                            currentRow = new ArrayList<>();
                        }
                    } else if (c == '\n') {
                        currentRow.add(currentField.toString().trim());
                        currentField.setLength(0);
                        if (!currentRow.isEmpty()) {
                            records.add(currentRow);
                            currentRow = new ArrayList<>();
                        }
                    } else {
                        currentField.append(c);
                    }
                }
            }

            if (currentField.length() > 0 || !currentRow.isEmpty()) {
                currentRow.add(currentField.toString().trim());
                records.add(currentRow);
            }
        }
        return records;
    }
}

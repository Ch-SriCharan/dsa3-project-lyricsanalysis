package com.lyrics.model;

import java.util.List;

/**
 * Represents a single song with its metadata and processed textual features.
 */
public class Song {
    private final int id;
    private final String artist;
    private final String title;
    private final String rawLyrics;
    private String processedLyrics;
    private List<String> tokens;

    public Song(int id, String artist, String title, String rawLyrics) {
        this.id = id;
        this.artist = (artist != null) ? artist.trim() : "Unknown Artist";
        this.title = (title != null) ? title.trim() : "Untitled Song";
        this.rawLyrics = (rawLyrics != null) ? rawLyrics.trim() : "";
        this.processedLyrics = "";
    }

    public int getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getRawLyrics() {
        return rawLyrics;
    }

    public String getProcessedLyrics() {
        return processedLyrics;
    }

    public void setProcessedLyrics(String processedLyrics) {
        this.processedLyrics = processedLyrics;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public int getRawWordCount() {
        if (rawLyrics == null || rawLyrics.trim().isEmpty()) {
            return 0;
        }
        return rawLyrics.trim().split("\\s+").length;
    }

    public int getProcessedWordCount() {
        if (tokens != null) {
            return tokens.size();
        }
        if (processedLyrics == null || processedLyrics.trim().isEmpty()) {
            return 0;
        }
        return processedLyrics.trim().split("\\s+").length;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}

package com.lyrics.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a collection of songs serving as the reference dataset corpus.
 */
public class Dataset {
    private final String name;
    private final List<Song> songs;
    private final Set<String> artists;
    private final Map<String, Integer> artistDistribution;

    public Dataset(String name, List<Song> songs) {
        this.name = name;
        this.songs = (songs != null) ? new ArrayList<>(songs) : new ArrayList<>();
        this.artists = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.artistDistribution = new LinkedHashMap<>();

        calculateDistributions();
    }

    private void calculateDistributions() {
        for (Song song : songs) {
            String art = song.getArtist();
            artists.add(art);
            artistDistribution.put(art, artistDistribution.getOrDefault(art, 0) + 1);
        }
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public int getSongCount() {
        return songs.size();
    }

    public int getArtistCount() {
        return artists.size();
    }

    public Set<String> getArtists() {
        return Collections.unmodifiableSet(artists);
    }

    public Map<String, Integer> getArtistDistribution() {
        return Collections.unmodifiableMap(artistDistribution);
    }

    public double getAverageLyricLength() {
        if (songs.isEmpty()) return 0.0;
        int totalWords = 0;
        for (Song song : songs) {
            totalWords += song.getRawWordCount();
        }
        return (double) totalWords / songs.size();
    }

    public double getAverageProcessedLength() {
        if (songs.isEmpty()) return 0.0;
        int totalWords = 0;
        for (Song song : songs) {
            totalWords += song.getProcessedWordCount();
        }
        return (double) totalWords / songs.size();
    }

    public Map<String, Integer> getTopFrequentWords(int limit) {
        Map<String, Integer> freqMap = new HashMap<>();
        for (Song song : songs) {
            if (song.getTokens() != null) {
                for (String token : song.getTokens()) {
                    freqMap.put(token, freqMap.getOrDefault(token, 0) + 1);
                }
            }
        }
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public int getTotalVocabularySize() {
        Set<String> vocab = new HashSet<>();
        for (Song song : songs) {
            if (song.getTokens() != null) {
                vocab.addAll(song.getTokens());
            }
        }
        return vocab.size();
    }

    public boolean hasMultipleArtists() {
        return artists.size() >= 2;
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }
}

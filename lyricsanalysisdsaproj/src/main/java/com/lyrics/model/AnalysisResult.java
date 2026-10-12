package com.lyrics.model;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the results of lyrics similarity analysis and artist attribution.
 */
public class AnalysisResult {

    public static class SimilarityMatch {
        private final int rank;
        private final Song song;
        private final double similarityScore; // 0.0 to 1.0
        private final double similarityPercentage; // 0.0 to 100.0%

        public SimilarityMatch(int rank, Song song, double similarityScore) {
            this.rank = rank;
            this.song = song;
            this.similarityScore = Math.max(0.0, Math.min(1.0, similarityScore));
            this.similarityPercentage = this.similarityScore * 100.0;
        }

        public int getRank() {
            return rank;
        }

        public Song getSong() {
            return song;
        }

        public String getSongTitle() {
            return song.getTitle();
        }

        public String getArtist() {
            return song.getArtist();
        }

        public double getSimilarityScore() {
            return similarityScore;
        }

        public double getSimilarityPercentage() {
            return similarityPercentage;
        }
    }

    public static class ArtistProbability {
        private final String artist;
        private final double probability; // 0.0 to 1.0
        private final double percentage; // 0.0 to 100.0%

        public ArtistProbability(String artist, double probability) {
            this.artist = artist;
            this.probability = Math.max(0.0, Math.min(1.0, probability));
            this.percentage = this.probability * 100.0;
        }

        public String getArtist() {
            return artist;
        }

        public double getProbability() {
            return probability;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    public static class PreprocessingStats {
        private final String originalText;
        private final String processedText;
        private final int rawWordCount;
        private final int rawCharCount;
        private final int tokensAfterCleaning;
        private final int stopWordsRemoved;
        private final int finalTokenCount;
        private final int uniqueWordCount;
        private final List<String> tokens;

        public PreprocessingStats(String originalText, String processedText, int rawWordCount,
                                  int rawCharCount, int tokensAfterCleaning, int stopWordsRemoved,
                                  int finalTokenCount, int uniqueWordCount, List<String> tokens) {
            this.originalText = originalText;
            this.processedText = processedText;
            this.rawWordCount = rawWordCount;
            this.rawCharCount = rawCharCount;
            this.tokensAfterCleaning = tokensAfterCleaning;
            this.stopWordsRemoved = stopWordsRemoved;
            this.finalTokenCount = finalTokenCount;
            this.uniqueWordCount = uniqueWordCount;
            this.tokens = tokens;
        }

        public String getOriginalText() {
            return originalText;
        }

        public String getProcessedText() {
            return processedText;
        }

        public int getRawWordCount() {
            return rawWordCount;
        }

        public int getRawCharCount() {
            return rawCharCount;
        }

        public int getTokensAfterCleaning() {
            return tokensAfterCleaning;
        }

        public int getStopWordsRemoved() {
            return stopWordsRemoved;
        }

        public int getFinalTokenCount() {
            return finalTokenCount;
        }

        public int getUniqueWordCount() {
            return uniqueWordCount;
        }

        public List<String> getTokens() {
            return tokens;
        }
    }

    private final String querySourceName;
    private final PreprocessingStats preprocessingStats;
    private final List<SimilarityMatch> similarityMatches;
    private final String predictedArtist;
    private final double attributionConfidence; // 0.0 to 1.0
    private final List<ArtistProbability> candidateProbabilities;
    private final boolean attributionAvailable;

    public AnalysisResult(String querySourceName,
                          PreprocessingStats preprocessingStats,
                          List<SimilarityMatch> similarityMatches,
                          String predictedArtist,
                          double attributionConfidence,
                          List<ArtistProbability> candidateProbabilities,
                          boolean attributionAvailable) {
        this.querySourceName = querySourceName;
        this.preprocessingStats = preprocessingStats;
        this.similarityMatches = similarityMatches;
        this.predictedArtist = predictedArtist;
        this.attributionConfidence = attributionConfidence;
        this.candidateProbabilities = candidateProbabilities;
        this.attributionAvailable = attributionAvailable;
    }

    public String getQuerySourceName() {
        return querySourceName;
    }

    public PreprocessingStats getPreprocessingStats() {
        return preprocessingStats;
    }

    public List<SimilarityMatch> getSimilarityMatches() {
        return similarityMatches;
    }

    public String getPredictedArtist() {
        return predictedArtist;
    }

    public double getAttributionConfidence() {
        return attributionConfidence;
    }

    public double getAttributionConfidencePercentage() {
        return attributionConfidence * 100.0;
    }

    public List<ArtistProbability> getCandidateProbabilities() {
        return candidateProbabilities;
    }

    public boolean isAttributionAvailable() {
        return attributionAvailable;
    }
}

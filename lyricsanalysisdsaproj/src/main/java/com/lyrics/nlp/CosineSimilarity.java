package com.lyrics.nlp;

import com.lyrics.model.AnalysisResult.SimilarityMatch;
import com.lyrics.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes Cosine Similarity between TF-IDF feature vectors
 * and ranks most similar songs from a reference corpus.
 */
public class CosineSimilarity {

    /**
     * Calculates the Cosine Similarity between two feature vectors:
     * (A · B) / (||A|| × ||B||)
     */
    public static double compute(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length || vectorA.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            double a = vectorA[i];
            double b = vectorB[i];
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA <= 1e-12 || normB <= 1e-12) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        // Clamp to [0.0, 1.0] to account for any floating point imprecision
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    /**
     * Finds and ranks the Top-K most similar songs in the reference dataset.
     */
    public static List<SimilarityMatch> findTopSimilarSongs(double[] queryVector,
                                                           double[][] corpusVectors,
                                                           List<Song> corpusSongs,
                                                           int topK) {
        if (queryVector == null || corpusVectors == null || corpusSongs == null) {
            return Collections.emptyList();
        }

        int count = Math.min(corpusVectors.length, corpusSongs.size());
        List<SimilarityMatch> matches = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            double score = compute(queryVector, corpusVectors[i]);
            matches.add(new SimilarityMatch(0, corpusSongs.get(i), score));
        }

        // Sort descending by similarity score
        matches.sort((m1, m2) -> Double.compare(m2.getSimilarityScore(), m1.getSimilarityScore()));

        // Assign ranks 1..K
        int limit = Math.min(topK > 0 ? topK : 5, matches.size());
        List<SimilarityMatch> topMatches = new ArrayList<>(limit);

        for (int rank = 1; rank <= limit; rank++) {
            SimilarityMatch raw = matches.get(rank - 1);
            topMatches.add(new SimilarityMatch(rank, raw.getSong(), raw.getSimilarityScore()));
        }

        return topMatches;
    }
}

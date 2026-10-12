package com.lyrics.nlp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * High-performance TF-IDF Vectorizer with smooth Inverse Document Frequency
 * calculation and L2 unit-norm vector normalization.
 */
public class TFIDFVectorizer {
    private final int minDocFreq;
    private final double maxDocFreqRatio;
    private final int maxFeatures;
    private final int ngramMax;

    private Map<String, Integer> vocabulary;
    private String[] featureNames;
    private double[] idfValues;
    private int documentCount;
    private boolean isFitted;

    public TFIDFVectorizer() {
        this(1, 1.0, 5000, 1);
    }

    public TFIDFVectorizer(int minDocFreq, double maxDocFreqRatio, int maxFeatures, int ngramMax) {
        this.minDocFreq = Math.max(1, minDocFreq);
        this.maxDocFreqRatio = maxDocFreqRatio;
        this.maxFeatures = maxFeatures > 0 ? maxFeatures : Integer.MAX_VALUE;
        this.ngramMax = Math.max(1, ngramMax);
        this.vocabulary = new HashMap<>();
        this.isFitted = false;
    }

    /**
     * Extracts n-grams (unigrams, bigrams) from token list or space-delimited text.
     */
    private List<String> extractNgrams(String document) {
        if (document == null || document.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] tokens = document.trim().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            StringBuilder sb = new StringBuilder();
            for (int n = 0; n < ngramMax && (i + n) < tokens.length; n++) {
                if (n > 0) sb.append(" ");
                sb.append(tokens[i + n]);
                ngrams.add(sb.toString());
            }
        }
        return ngrams;
    }

    /**
     * Fits the vocabulary and IDF weights strictly on the given corpus.
     */
    public synchronized void fit(List<String> documents) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Cannot fit TFIDFVectorizer on empty corpus.");
        }

        this.documentCount = documents.size();
        Map<String, Integer> docFreqMap = new HashMap<>();

        for (String doc : documents) {
            List<String> ngrams = extractNgrams(doc);
            Set<String> uniqueInDoc = new HashSet<>(ngrams);
            for (String term : uniqueInDoc) {
                docFreqMap.put(term, docFreqMap.getOrDefault(term, 0) + 1);
            }
        }

        // Filter terms by minDocFreq and maxDocFreqRatio
        int maxDocAllowed = (int) Math.floor(documentCount * maxDocFreqRatio);
        List<Map.Entry<String, Integer>> filteredTerms = docFreqMap.entrySet().stream()
                .filter(e -> e.getValue() >= minDocFreq && e.getValue() <= maxDocAllowed)
                .sorted((e1, e2) -> {
                    int cmp = Integer.compare(e2.getValue(), e1.getValue());
                    return cmp != 0 ? cmp : e1.getKey().compareTo(e2.getKey());
                })
                .limit(maxFeatures)
                .collect(Collectors.toList());

        // Sort terms alphabetically for deterministic feature ordering
        filteredTerms.sort(Comparator.comparing(Map.Entry::getKey));

        int vocabSize = filteredTerms.size();
        this.vocabulary = new HashMap<>(vocabSize);
        this.featureNames = new String[vocabSize];
        this.idfValues = new double[vocabSize];

        for (int i = 0; i < vocabSize; i++) {
            Map.Entry<String, Integer> entry = filteredTerms.get(i);
            String term = entry.getKey();
            int df = entry.getValue();

            vocabulary.put(term, i);
            featureNames[i] = term;
            // Smooth IDF formula: ln((1 + N) / (1 + df)) + 1.0
            this.idfValues[i] = Math.log((1.0 + documentCount) / (1.0 + df)) + 1.0;
        }

        this.isFitted = true;
    }

    /**
     * Transforms a single document into an L2-normalized TF-IDF vector.
     */
    public double[] transform(String document) {
        if (!isFitted) {
            throw new IllegalStateException("TFIDFVectorizer must be fitted before transform.");
        }

        int vocabSize = featureNames.length;
        double[] vector = new double[vocabSize];
        if (document == null || document.trim().isEmpty() || vocabSize == 0) {
            return vector;
        }

        List<String> ngrams = extractNgrams(document);
        Map<Integer, Integer> termCounts = new HashMap<>();

        for (String term : ngrams) {
            Integer idx = vocabulary.get(term);
            if (idx != null) {
                termCounts.put(idx, termCounts.getOrDefault(idx, 0) + 1);
            }
        }

        double sumSquares = 0.0;
        for (Map.Entry<Integer, Integer> entry : termCounts.entrySet()) {
            int idx = entry.getKey();
            int count = entry.getValue();
            double tf = count;
            double tfidf = tf * idfValues[idx];
            vector[idx] = tfidf;
            sumSquares += tfidf * tfidf;
        }

        // L2 Unit Normalization
        if (sumSquares > 0.0) {
            double norm = Math.sqrt(sumSquares);
            for (int i = 0; i < vocabSize; i++) {
                if (vector[i] > 0.0) {
                    vector[i] /= norm;
                }
            }
        }

        return vector;
    }

    /**
     * Transforms a list of documents into a 2D matrix.
     */
    public double[][] transformAll(List<String> documents) {
        if (documents == null) return new double[0][0];
        double[][] matrix = new double[documents.size()][];
        for (int i = 0; i < documents.size(); i++) {
            matrix[i] = transform(documents.get(i));
        }
        return matrix;
    }

    /**
     * Fits and transforms the corpus in a single pass.
     */
    public double[][] fitTransform(List<String> documents) {
        fit(documents);
        return transformAll(documents);
    }

    public boolean isFitted() {
        return isFitted;
    }

    public int getVocabularySize() {
        return featureNames != null ? featureNames.length : 0;
    }

    public String[] getFeatureNames() {
        return featureNames != null ? featureNames.clone() : new String[0];
    }

    public Map<String, Integer> getVocabulary() {
        return Collections.unmodifiableMap(vocabulary);
    }

    public double[] getIdfValues() {
        return idfValues != null ? idfValues.clone() : new double[0];
    }
}

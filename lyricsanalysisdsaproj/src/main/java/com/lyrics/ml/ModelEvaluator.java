package com.lyrics.ml;

import com.lyrics.model.Dataset;
import com.lyrics.model.Song;
import com.lyrics.nlp.TFIDFVectorizer;
import com.lyrics.preprocessing.TextPreprocessor;

import java.util.*;

/**
 * Handles rigorous ML training, stratified Train/Test validation splitting,
 * strict no-data-leakage feature extraction, and multi-class metric evaluation.
 */
public class ModelEvaluator {

    public static class TrainingPipelineResult {
        private final ArtistClassifier classifier;
        private final TFIDFVectorizer vectorizer;
        private final EvaluationMetrics evaluationMetrics;
        private final double[][] fullCorpusVectors;
        private final int trainSize;
        private final int testSize;

        public TrainingPipelineResult(ArtistClassifier classifier,
                                      TFIDFVectorizer vectorizer,
                                      EvaluationMetrics evaluationMetrics,
                                      double[][] fullCorpusVectors,
                                      int trainSize,
                                      int testSize) {
            this.classifier = classifier;
            this.vectorizer = vectorizer;
            this.evaluationMetrics = evaluationMetrics;
            this.fullCorpusVectors = fullCorpusVectors;
            this.trainSize = trainSize;
            this.testSize = testSize;
        }

        public ArtistClassifier getClassifier() {
            return classifier;
        }

        public TFIDFVectorizer getVectorizer() {
            return vectorizer;
        }

        public EvaluationMetrics getEvaluationMetrics() {
            return evaluationMetrics;
        }

        public double[][] getFullCorpusVectors() {
            return fullCorpusVectors;
        }

        public int getTrainSize() {
            return trainSize;
        }

        public int getTestSize() {
            return testSize;
        }
    }

    /**
     * Executes the complete ML training and validation pipeline.
     */
    public static TrainingPipelineResult trainAndEvaluate(Dataset dataset,
                                                          TextPreprocessor preprocessor,
                                                          ArtistClassifier classifier,
                                                          double testSplitRatio,
                                                          int maxFeatures,
                                                          int ngramMax) {
        if (dataset == null || dataset.isEmpty()) {
            throw new IllegalArgumentException("Cannot train model on empty dataset.");
        }
        if (!dataset.hasMultipleArtists()) {
            throw new IllegalArgumentException("Artist attribution requires a labeled dataset containing lyrics from at least two artists.");
        }

        List<Song> allSongs = dataset.getSongs();
        if (allSongs.size() < 4) {
            throw new IllegalArgumentException("Dataset too small (" + allSongs.size() + " songs). Please upload at least 4 songs across multiple artists.");
        }

        // Ensure all songs have processed lyrics
        for (Song s : allSongs) {
            if (s.getProcessedLyrics() == null || s.getProcessedLyrics().isEmpty()) {
                String proc = preprocessor.preprocess(s.getRawLyrics());
                s.setProcessedLyrics(proc);
                s.setTokens(preprocessor.extractProcessedTokens(s.getRawLyrics()));
            }
        }

        // Stratified Train/Test Split by Artist
        Map<String, List<Song>> byArtist = new LinkedHashMap<>();
        for (Song s : allSongs) {
            byArtist.computeIfAbsent(s.getArtist(), k -> new ArrayList<>()).add(s);
        }

        List<Song> trainSongs = new ArrayList<>();
        List<Song> testSongs = new ArrayList<>();
        Random rng = new Random(42); // Seed for reproducible evaluation

        for (Map.Entry<String, List<Song>> entry : byArtist.entrySet()) {
            List<Song> songs = new ArrayList<>(entry.getValue());
            Collections.shuffle(songs, rng);

            int totalArtistSongs = songs.size();
            int testCount = (int) Math.round(totalArtistSongs * testSplitRatio);
            // Ensure at least 1 training sample per artist
            if (testCount >= totalArtistSongs) {
                testCount = Math.max(0, totalArtistSongs - 1);
            }
            if (totalArtistSongs >= 2 && testCount == 0) {
                testCount = 1;
            }

            for (int i = 0; i < songs.size(); i++) {
                if (i < testCount) {
                    testSongs.add(songs.get(i));
                } else {
                    trainSongs.add(songs.get(i));
                }
            }
        }

        // Fallback safety if test set is empty
        if (testSongs.isEmpty()) {
            testSongs.add(trainSongs.remove(trainSongs.size() - 1));
        }

        // Strict No Data Leakage:
        // Fit vectorizer ONLY on training set
        List<String> trainDocs = new ArrayList<>(trainSongs.size());
        String[] y_train = new String[trainSongs.size()];
        for (int i = 0; i < trainSongs.size(); i++) {
            trainDocs.add(trainSongs.get(i).getProcessedLyrics());
            y_train[i] = trainSongs.get(i).getArtist();
        }

        TFIDFVectorizer trainVectorizer = new TFIDFVectorizer(1, 1.0, maxFeatures, ngramMax);
        trainVectorizer.fit(trainDocs);

        double[][] X_train = trainVectorizer.transformAll(trainDocs);

        List<String> classList = new ArrayList<>(dataset.getArtists());
        classifier.train(X_train, y_train, trainVectorizer.getFeatureNames(), classList);

        // Evaluate on Test Set
        List<String> testDocs = new ArrayList<>(testSongs.size());
        String[] y_test = new String[testSongs.size()];
        for (int i = 0; i < testSongs.size(); i++) {
            testDocs.add(testSongs.get(i).getProcessedLyrics());
            y_test[i] = testSongs.get(i).getArtist();
        }

        double[][] X_test = trainVectorizer.transformAll(testDocs);
        String[] y_pred = new String[testSongs.size()];

        for (int i = 0; i < testSongs.size(); i++) {
            y_pred[i] = classifier.predict(X_test[i]);
        }

        EvaluationMetrics metrics = calculateMetrics(classList, y_test, y_pred);

        // Now fit full corpus vectorizer for reference similarity index & full inference
        List<String> fullDocs = new ArrayList<>(allSongs.size());
        String[] fullY = new String[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            fullDocs.add(allSongs.get(i).getProcessedLyrics());
            fullY[i] = allSongs.get(i).getArtist();
        }

        TFIDFVectorizer fullVectorizer = new TFIDFVectorizer(1, 1.0, maxFeatures, ngramMax);
        fullVectorizer.fit(fullDocs);
        double[][] fullCorpusVectors = fullVectorizer.transformAll(fullDocs);

        // Re-train classifier on full corpus for maximum deployment accuracy
        classifier.train(fullCorpusVectors, fullY, fullVectorizer.getFeatureNames(), classList);

        return new TrainingPipelineResult(
                classifier,
                fullVectorizer,
                metrics,
                fullCorpusVectors,
                trainSongs.size(),
                testSongs.size()
        );
    }

    private static EvaluationMetrics calculateMetrics(List<String> classList, String[] y_true, String[] y_pred) {
        int numClasses = classList.size();
        int numSamples = y_true.length;

        Map<String, Integer> classToIndex = new HashMap<>();
        for (int i = 0; i < numClasses; i++) {
            classToIndex.put(classList.get(i), i);
        }

        int[][] confusionMatrix = new int[numClasses][numClasses];
        int correct = 0;

        for (int i = 0; i < numSamples; i++) {
            Integer trueIdx = classToIndex.get(y_true[i]);
            Integer predIdx = classToIndex.get(y_pred[i]);

            if (trueIdx != null && predIdx != null) {
                confusionMatrix[trueIdx][predIdx]++;
                if (trueIdx.equals(predIdx)) {
                    correct++;
                }
            }
        }

        double accuracy = (numSamples > 0) ? ((double) correct / numSamples) : 0.0;

        List<EvaluationMetrics.ClassMetric> classMetrics = new ArrayList<>();
        double sumPrecision = 0.0;
        double sumRecall = 0.0;
        double sumF1 = 0.0;
        double weightedSumPrecision = 0.0;
        double weightedSumRecall = 0.0;
        double weightedSumF1 = 0.0;
        int activeClasses = 0;

        for (int c = 0; c < numClasses; c++) {
            String className = classList.get(c);
            int tp = confusionMatrix[c][c];

            int fn = 0;
            for (int col = 0; col < numClasses; col++) {
                if (col != c) fn += confusionMatrix[c][col];
            }

            int fp = 0;
            for (int row = 0; row < numClasses; row++) {
                if (row != c) fp += confusionMatrix[row][c];
            }

            int support = tp + fn;

            double prec = (tp + fp > 0) ? ((double) tp / (tp + fp)) : 0.0;
            double rec = (tp + fn > 0) ? ((double) tp / (tp + fn)) : 0.0;
            double f1 = (prec + rec > 0.0) ? (2.0 * prec * rec / (prec + rec)) : 0.0;

            classMetrics.add(new EvaluationMetrics.ClassMetric(className, prec, rec, f1, support));

            sumPrecision += prec;
            sumRecall += rec;
            sumF1 += f1;

            weightedSumPrecision += prec * support;
            weightedSumRecall += rec * support;
            weightedSumF1 += f1 * support;

            if (support > 0 || tp + fp > 0) {
                activeClasses++;
            }
        }

        int divisor = (numClasses > 0) ? numClasses : 1;
        double macroPrecision = sumPrecision / divisor;
        double macroRecall = sumRecall / divisor;
        double macroF1 = sumF1 / divisor;

        double weightedPrecision = (numSamples > 0) ? (weightedSumPrecision / numSamples) : 0.0;
        double weightedRecall = (numSamples > 0) ? (weightedSumRecall / numSamples) : 0.0;
        double weightedF1 = (numSamples > 0) ? (weightedSumF1 / numSamples) : 0.0;

        return new EvaluationMetrics(
                accuracy,
                macroPrecision,
                macroRecall,
                macroF1,
                weightedPrecision,
                weightedRecall,
                weightedF1,
                numSamples,
                correct,
                classList.toArray(new String[0]),
                confusionMatrix,
                classMetrics
        );
    }
}

package com.lyrics;

import com.lyrics.ml.*;
import com.lyrics.model.*;
import com.lyrics.nlp.*;
import com.lyrics.preprocessing.*;
import com.lyrics.utils.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NLPAndMLTest {

    private static Dataset demoDataset;
    private static TextPreprocessor preprocessor;

    @BeforeAll
    public static void setUp() throws Exception {
        preprocessor = TextPreprocessor.getInstance();
        try (InputStream is = NLPAndMLTest.class.getResourceAsStream("/demo_dataset.csv")) {
            assertNotNull(is, "demo_dataset.csv must exist in classpath");
            demoDataset = CSVReader.readCSV(is, "Demo Test Dataset");
        }
    }

    @Test
    public void testDatasetLoading() {
        assertNotNull(demoDataset);
        assertTrue(demoDataset.getSongCount() >= 40, "Should have at least 40 songs in demo dataset");
        assertTrue(demoDataset.getArtistCount() >= 5, "Should have at least 5 artists");
        assertTrue(demoDataset.hasMultipleArtists());
        assertTrue(demoDataset.getAverageLyricLength() > 50, "Average lyric length should be non-trivial");
    }

    @Test
    public void testTextPreprocessing() {
        String input = "I've been [Chorus] walking through the night and dancing in the rain!";
        String processed = preprocessor.preprocess(input);
        assertNotNull(processed);
        assertFalse(processed.contains("[Chorus]"), "Tags should be removed");
        assertFalse(processed.contains("!"), "Punctuation should be removed");
        assertFalse(processed.contains("I've"), "Contractions should be cleaned");

        AnalysisResult.PreprocessingStats stats = preprocessor.analyzePreprocessing(input);
        assertTrue(stats.getRawWordCount() > 0);
        assertTrue(stats.getFinalTokenCount() > 0);
        assertTrue(stats.getStopWordsRemoved() > 0);
    }

    @Test
    public void testTFIDFAndCosineSimilarity() {
        List<Song> songs = demoDataset.getSongs();
        for (Song s : songs) {
            s.setProcessedLyrics(preprocessor.preprocess(s.getRawLyrics()));
        }

        TFIDFVectorizer vectorizer = new TFIDFVectorizer(1, 1.0, 5000, 1);
        List<String> docs = songs.stream().map(Song::getProcessedLyrics).toList();
        double[][] corpusMatrix = vectorizer.fitTransform(docs);

        assertTrue(vectorizer.getVocabularySize() > 100);
        assertEquals(songs.size(), corpusMatrix.length);

        // Test identical query gives ~100% similarity to itself
        String query = songs.get(0).getProcessedLyrics();
        double[] queryVector = vectorizer.transform(query);
        List<AnalysisResult.SimilarityMatch> matches = CosineSimilarity.findTopSimilarSongs(queryVector, corpusMatrix, songs, 5);

        assertEquals(5, matches.size());
        assertEquals(songs.get(0).getTitle(), matches.get(0).getSongTitle());
        assertTrue(matches.get(0).getSimilarityPercentage() > 95.0, "Identical lyrics should have >95% similarity");
    }

    @Test
    public void testMultinomialNBAndModelEvaluator() {
        MultinomialNBClassifier classifier = new MultinomialNBClassifier(1.0);
        ModelEvaluator.TrainingPipelineResult result = ModelEvaluator.trainAndEvaluate(
                demoDataset,
                preprocessor,
                classifier,
                0.20,
                5000,
                1
        );

        assertNotNull(result);
        assertTrue(result.getTrainSize() > 0);
        assertTrue(result.getTestSize() > 0);

        EvaluationMetrics metrics = result.getEvaluationMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.getAccuracy() >= 0.0 && metrics.getAccuracy() <= 1.0);
        assertTrue(metrics.getMacroPrecision() >= 0.0 && metrics.getMacroPrecision() <= 1.0);
        assertTrue(metrics.getMacroRecall() >= 0.0 && metrics.getMacroRecall() <= 1.0);
        assertTrue(metrics.getMacroF1() >= 0.0 && metrics.getMacroF1() <= 1.0);
        assertNotNull(metrics.getConfusionMatrix());

        // Test sample prediction on Taylor Swift lyrics
        String tsQuery = preprocessor.preprocess("I was enchanted to meet you dancing under the sparkling night");
        double[] qVec = result.getVectorizer().transform(tsQuery);
        String pred = result.getClassifier().predict(qVec);
        Map<String, Double> probs = result.getClassifier().predictProbabilities(qVec);

        assertNotNull(pred);
        assertNotNull(probs);
        double sumProbs = probs.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(1.0, sumProbs, 1e-4, "Probabilities should sum to 1.0");
    }

    @Test
    public void testLogisticRegressionClassifier() {
        LogisticRegressionClassifier lr = new LogisticRegressionClassifier(0.1, 1e-4, 150);
        ModelEvaluator.TrainingPipelineResult result = ModelEvaluator.trainAndEvaluate(
                demoDataset,
                preprocessor,
                lr,
                0.20,
                3000,
                1
        );

        assertNotNull(result);
        assertTrue(result.getClassifier().isTrained());
        assertTrue(result.getEvaluationMetrics().getAccuracy() >= 0.0);
    }
}

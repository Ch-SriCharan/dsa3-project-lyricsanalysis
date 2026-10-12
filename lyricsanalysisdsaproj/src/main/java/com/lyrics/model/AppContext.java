package com.lyrics.model;

import com.lyrics.ml.ArtistClassifier;
import com.lyrics.ml.EvaluationMetrics;
import com.lyrics.nlp.TFIDFVectorizer;
import com.lyrics.utils.CSVReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Global shared application context managing state between UI screens.
 */
public class AppContext {
    private static AppContext instance;

    private Dataset dataset;
    private TFIDFVectorizer vectorizer;
    private ArtistClassifier classifier;
    private EvaluationMetrics evaluationMetrics;
    private double[][] corpusVectors;
    private AnalysisResult lastAnalysisResult;

    private final List<Runnable> stateListeners;

    private AppContext() {
        this.stateListeners = new ArrayList<>();
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public synchronized void setDataset(Dataset dataset) {
        this.dataset = dataset;
        // Invalidate previous trained model since dataset changed
        this.vectorizer = null;
        this.classifier = null;
        this.evaluationMetrics = null;
        this.corpusVectors = null;
        notifyListeners();
    }

    public synchronized Dataset getDataset() {
        return dataset;
    }

    public synchronized boolean hasDataset() {
        return dataset != null && !dataset.isEmpty();
    }

    public synchronized boolean isModelTrained() {
        return vectorizer != null && classifier != null && classifier.isTrained() && corpusVectors != null;
    }

    public synchronized void setTrainedPipeline(TFIDFVectorizer vectorizer,
                                                ArtistClassifier classifier,
                                                EvaluationMetrics metrics,
                                                double[][] corpusVectors) {
        this.vectorizer = vectorizer;
        this.classifier = classifier;
        this.evaluationMetrics = metrics;
        this.corpusVectors = corpusVectors;
        notifyListeners();
    }

    public synchronized TFIDFVectorizer getVectorizer() {
        return vectorizer;
    }

    public synchronized ArtistClassifier getClassifier() {
        return classifier;
    }

    public synchronized EvaluationMetrics getEvaluationMetrics() {
        return evaluationMetrics;
    }

    public synchronized double[][] getCorpusVectors() {
        return corpusVectors;
    }

    public synchronized AnalysisResult getLastAnalysisResult() {
        return lastAnalysisResult;
    }

    public synchronized void setLastAnalysisResult(AnalysisResult lastAnalysisResult) {
        this.lastAnalysisResult = lastAnalysisResult;
        notifyListeners();
    }

    public synchronized void addStateChangeListener(Runnable listener) {
        if (listener != null) {
            stateListeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (Runnable r : stateListeners) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the built-in demo dataset for testing and demonstration.
     */
    public synchronized Dataset loadDemoDataset() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/demo_dataset.csv")) {
            if (is == null) {
                throw new IllegalStateException("Demo dataset resource /demo_dataset.csv not found.");
            }
            Dataset demo = CSVReader.readCSV(is, "Demo Dataset — For Testing Only");
            setDataset(demo);
            return demo;
        }
    }
}

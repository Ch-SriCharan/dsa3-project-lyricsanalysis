package com.lyrics.ml;

import java.util.*;

/**
 * Multinomial Naive Bayes Classifier with Laplace Smoothing
 * and Calibrated Softmax Posterior Probabilities.
 */
public class MultinomialNBClassifier implements ArtistClassifier {
    private final double alpha; // Laplace smoothing parameter (default 1.0)
    private List<String> classes;
    private double[] logPriors;
    private double[][] logFeatureLikelihoods; // [num_classes][num_features]
    private int numFeatures;
    private boolean isTrained;

    public MultinomialNBClassifier() {
        this(1.0);
    }

    public MultinomialNBClassifier(double alpha) {
        this.alpha = Math.max(0.01, alpha);
        this.isTrained = false;
    }

    @Override
    public synchronized void train(double[][] X_train, String[] y_train, String[] featureNames, List<String> classList) {
        if (X_train == null || y_train == null || X_train.length == 0 || X_train.length != y_train.length) {
            throw new IllegalArgumentException("Invalid training data dimensions.");
        }
        if (classList == null || classList.size() < 2) {
            throw new IllegalArgumentException("Artist attribution requires at least 2 distinct artist classes.");
        }

        this.classes = new ArrayList<>(classList);
        int numClasses = classes.size();
        this.numFeatures = X_train[0].length;
        int numSamples = X_train.length;

        Map<String, Integer> classToIndex = new HashMap<>();
        for (int i = 0; i < numClasses; i++) {
            classToIndex.put(classes.get(i), i);
        }

        int[] classCounts = new int[numClasses];
        double[][] classFeatureSums = new double[numClasses][numFeatures];
        double[] classTotalTokens = new double[numClasses];

        for (int i = 0; i < numSamples; i++) {
            String label = y_train[i];
            Integer classIdx = classToIndex.get(label);
            if (classIdx == null) continue;

            classCounts[classIdx]++;
            double[] features = X_train[i];

            for (int f = 0; f < numFeatures; f++) {
                double val = features[f];
                if (val > 0.0) {
                    classFeatureSums[classIdx][f] += val;
                    classTotalTokens[classIdx] += val;
                }
            }
        }

        // Calculate Log Priors: log( (N_k + alpha) / (N + alpha * K) )
        this.logPriors = new double[numClasses];
        for (int c = 0; c < numClasses; c++) {
            double prior = (classCounts[c] + alpha) / (numSamples + alpha * numClasses);
            this.logPriors[c] = Math.log(prior);
        }

        // Calculate Log Feature Likelihoods with Laplace Smoothing:
        // log( (sum_features + alpha) / (total_tokens + alpha * V) )
        this.logFeatureLikelihoods = new double[numClasses][numFeatures];
        for (int c = 0; c < numClasses; c++) {
            double denominator = classTotalTokens[c] + (alpha * numFeatures);
            for (int f = 0; f < numFeatures; f++) {
                double numerator = classFeatureSums[c][f] + alpha;
                this.logFeatureLikelihoods[c][f] = Math.log(numerator / denominator);
            }
        }

        this.isTrained = true;
    }

    private double[] computeLogPosteriors(double[] x) {
        if (!isTrained) {
            throw new IllegalStateException("MultinomialNBClassifier is not trained.");
        }
        int numClasses = classes.size();
        double[] logPosteriors = new double[numClasses];

        for (int c = 0; c < numClasses; c++) {
            double logPost = logPriors[c];
            for (int f = 0; f < Math.min(x.length, numFeatures); f++) {
                if (x[f] > 0.0) {
                    logPost += x[f] * logFeatureLikelihoods[c][f];
                }
            }
            logPosteriors[c] = logPost;
        }
        return logPosteriors;
    }

    @Override
    public String predict(double[] x) {
        double[] logPosteriors = computeLogPosteriors(x);
        int bestIdx = 0;
        double maxLogPost = logPosteriors[0];

        for (int i = 1; i < logPosteriors.length; i++) {
            if (logPosteriors[i] > maxLogPost) {
                maxLogPost = logPosteriors[i];
                bestIdx = i;
            }
        }
        return classes.get(bestIdx);
    }

    @Override
    public Map<String, Double> predictProbabilities(double[] x) {
        double[] logPosteriors = computeLogPosteriors(x);

        // Numerically stable Softmax
        double maxLog = Double.NEGATIVE_INFINITY;
        for (double val : logPosteriors) {
            if (val > maxLog) maxLog = val;
        }

        double sumExp = 0.0;
        double[] probs = new double[logPosteriors.length];
        for (int i = 0; i < logPosteriors.length; i++) {
            probs[i] = Math.exp(logPosteriors[i] - maxLog);
            sumExp += probs[i];
        }

        Map<String, Double> resultMap = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> entries = new ArrayList<>();

        for (int i = 0; i < classes.size(); i++) {
            double normalizedProb = (sumExp > 0.0) ? (probs[i] / sumExp) : (1.0 / classes.size());
            entries.add(new AbstractMap.SimpleEntry<>(classes.get(i), normalizedProb));
        }

        // Sort descending by probability
        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        for (Map.Entry<String, Double> entry : entries) {
            resultMap.put(entry.getKey(), entry.getValue());
        }

        return resultMap;
    }

    @Override
    public boolean isTrained() {
        return isTrained;
    }

    @Override
    public String getClassifierName() {
        return "Multinomial Naive Bayes (Laplace Smoothed)";
    }
}

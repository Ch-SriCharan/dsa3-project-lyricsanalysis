package com.lyrics.ml;

import java.util.*;

/**
 * Multinomial Softmax Logistic Regression with L2 Regularization.
 */
public class LogisticRegressionClassifier implements ArtistClassifier {
    private final double learningRate;
    private final double l2Penalty;
    private final int maxEpochs;

    private List<String> classes;
    private double[][] weights; // [num_classes][num_features]
    private double[] biases;    // [num_classes]
    private int numFeatures;
    private boolean isTrained;

    public LogisticRegressionClassifier() {
        this(0.1, 1e-4, 300);
    }

    public LogisticRegressionClassifier(double learningRate, double l2Penalty, int maxEpochs) {
        this.learningRate = learningRate;
        this.l2Penalty = l2Penalty;
        this.maxEpochs = maxEpochs;
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

        this.weights = new double[numClasses][numFeatures];
        this.biases = new double[numClasses];

        // One-hot encoded labels
        int[] targetIndices = new int[numSamples];
        for (int i = 0; i < numSamples; i++) {
            targetIndices[i] = classToIndex.getOrDefault(y_train[i], 0);
        }

        // Gradient Descent with Momentum
        double[][] vW = new double[numClasses][numFeatures];
        double[] vb = new double[numClasses];
        double momentum = 0.9;
        double currentLr = learningRate;

        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            double[][] gradW = new double[numClasses][numFeatures];
            double[] gradB = new double[numClasses];

            for (int i = 0; i < numSamples; i++) {
                double[] x = X_train[i];
                int trueClass = targetIndices[i];

                double[] probs = computeSoftmax(x);

                for (int c = 0; c < numClasses; c++) {
                    double target = (c == trueClass) ? 1.0 : 0.0;
                    double error = probs[c] - target;

                    gradB[c] += error;
                    for (int f = 0; f < numFeatures; f++) {
                        if (x[f] > 0.0) {
                            gradW[c][f] += error * x[f];
                        }
                    }
                }
            }

            // Update weights and biases with L2 Regularization & Momentum
            for (int c = 0; c < numClasses; c++) {
                gradB[c] /= numSamples;
                vb[c] = momentum * vb[c] + currentLr * gradB[c];
                biases[c] -= vb[c];

                for (int f = 0; f < numFeatures; f++) {
                    gradW[c][f] = (gradW[c][f] / numSamples) + (l2Penalty * weights[c][f]);
                    vW[c][f] = momentum * vW[c][f] + currentLr * gradW[c][f];
                    weights[c][f] -= vW[c][f];
                }
            }

            // Decay learning rate slightly
            if ((epoch + 1) % 50 == 0) {
                currentLr *= 0.95;
            }
        }

        this.isTrained = true;
    }

    private double[] computeSoftmax(double[] x) {
        int numClasses = classes.size();
        double[] logits = new double[numClasses];
        double maxLogit = Double.NEGATIVE_INFINITY;

        for (int c = 0; c < numClasses; c++) {
            double z = biases[c];
            for (int f = 0; f < Math.min(x.length, numFeatures); f++) {
                if (x[f] > 0.0) {
                    z += weights[c][f] * x[f];
                }
            }
            logits[c] = z;
            if (z > maxLogit) maxLogit = z;
        }

        double sumExp = 0.0;
        double[] probs = new double[numClasses];
        for (int c = 0; c < numClasses; c++) {
            probs[c] = Math.exp(logits[c] - maxLogit);
            sumExp += probs[c];
        }

        for (int c = 0; c < numClasses; c++) {
            probs[c] = (sumExp > 0.0) ? (probs[c] / sumExp) : (1.0 / numClasses);
        }

        return probs;
    }

    @Override
    public String predict(double[] x) {
        if (!isTrained) {
            throw new IllegalStateException("LogisticRegressionClassifier is not trained.");
        }
        double[] probs = computeSoftmax(x);
        int bestIdx = 0;
        double maxProb = probs[0];

        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                bestIdx = i;
            }
        }
        return classes.get(bestIdx);
    }

    @Override
    public Map<String, Double> predictProbabilities(double[] x) {
        if (!isTrained) {
            throw new IllegalStateException("LogisticRegressionClassifier is not trained.");
        }
        double[] probs = computeSoftmax(x);

        List<Map.Entry<String, Double>> entries = new ArrayList<>();
        for (int i = 0; i < classes.size(); i++) {
            entries.add(new AbstractMap.SimpleEntry<>(classes.get(i), probs[i]));
        }

        entries.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        Map<String, Double> resultMap = new LinkedHashMap<>();
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
        return "Softmax Logistic Regression (L2 Regularized)";
    }
}

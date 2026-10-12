package com.lyrics.ml;

import java.util.List;
import java.util.Map;

/**
 * Common interface for Supervised Machine Learning Artist Classifiers.
 */
public interface ArtistClassifier {

    /**
     * Trains the classification model on feature vectors and labels.
     *
     * @param X_train Feature matrix of size [N_samples, N_features]
     * @param y_train Array of ground-truth artist class labels
     * @param featureNames Vocabulary term array
     * @param classList Distinct artist class list
     */
    void train(double[][] X_train, String[] y_train, String[] featureNames, List<String> classList);

    /**
     * Predicts the most likely artist for a query feature vector.
     */
    String predict(double[] x);

    /**
     * Computes calibrated probability distribution across all artist classes.
     *
     * @return Map of Artist Name -> Probability (0.0 to 1.0, summing to 1.0)
     */
    Map<String, Double> predictProbabilities(double[] x);

    /**
     * Returns true if the model has been trained and is ready for inference.
     */
    boolean isTrained();

    /**
     * Returns the human-readable model name.
     */
    String getClassifierName();
}

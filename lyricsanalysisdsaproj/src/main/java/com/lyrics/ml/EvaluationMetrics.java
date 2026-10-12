package com.lyrics.ml;

import java.util.*;

/**
 * Encapsulates multi-class model evaluation metrics including Accuracy,
 * Macro/Weighted Precision, Recall, F1-Score, and the Confusion Matrix.
 */
public class EvaluationMetrics {

    public static class ClassMetric {
        private final String className;
        private final double precision;
        private final double recall;
        private final double f1Score;
        private final int support; // Number of true instances in test set

        public ClassMetric(String className, double precision, double recall, double f1Score, int support) {
            this.className = className;
            this.precision = precision;
            this.recall = recall;
            this.f1Score = f1Score;
            this.support = support;
        }

        public String getClassName() {
            return className;
        }

        public double getPrecision() {
            return precision;
        }

        public double getPrecisionPercent() {
            return precision * 100.0;
        }

        public double getRecall() {
            return recall;
        }

        public double getRecallPercent() {
            return recall * 100.0;
        }

        public double getF1Score() {
            return f1Score;
        }

        public double getF1ScorePercent() {
            return f1Score * 100.0;
        }

        public int getSupport() {
            return support;
        }
    }

    private final double accuracy;
    private final double macroPrecision;
    private final double macroRecall;
    private final double macroF1;
    private final double weightedPrecision;
    private final double weightedRecall;
    private final double weightedF1;
    private final int totalTestSamples;
    private final int correctPredictions;
    private final String[] classLabels;
    private final int[][] confusionMatrix; // [true_idx][pred_idx]
    private final List<ClassMetric> classMetrics;

    public EvaluationMetrics(double accuracy,
                             double macroPrecision,
                             double macroRecall,
                             double macroF1,
                             double weightedPrecision,
                             double weightedRecall,
                             double weightedF1,
                             int totalTestSamples,
                             int correctPredictions,
                             String[] classLabels,
                             int[][] confusionMatrix,
                             List<ClassMetric> classMetrics) {
        this.accuracy = accuracy;
        this.macroPrecision = macroPrecision;
        this.macroRecall = macroRecall;
        this.macroF1 = macroF1;
        this.weightedPrecision = weightedPrecision;
        this.weightedRecall = weightedRecall;
        this.weightedF1 = weightedF1;
        this.totalTestSamples = totalTestSamples;
        this.correctPredictions = correctPredictions;
        this.classLabels = classLabels != null ? classLabels.clone() : new String[0];
        this.confusionMatrix = confusionMatrix;
        this.classMetrics = classMetrics != null ? Collections.unmodifiableList(classMetrics) : Collections.emptyList();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getAccuracyPercent() {
        return accuracy * 100.0;
    }

    public double getMacroPrecision() {
        return macroPrecision;
    }

    public double getMacroPrecisionPercent() {
        return macroPrecision * 100.0;
    }

    public double getMacroRecall() {
        return macroRecall;
    }

    public double getMacroRecallPercent() {
        return macroRecall * 100.0;
    }

    public double getMacroF1() {
        return macroF1;
    }

    public double getMacroF1Percent() {
        return macroF1 * 100.0;
    }

    public double getWeightedPrecision() {
        return weightedPrecision;
    }

    public double getWeightedRecall() {
        return weightedRecall;
    }

    public double getWeightedF1() {
        return weightedF1;
    }

    public int getTotalTestSamples() {
        return totalTestSamples;
    }

    public int getCorrectPredictions() {
        return correctPredictions;
    }

    public String[] getClassLabels() {
        return classLabels.clone();
    }

    public int[][] getConfusionMatrix() {
        return confusionMatrix;
    }

    public List<ClassMetric> getClassMetrics() {
        return classMetrics;
    }
}

package com.lyrics.ui;

import com.lyrics.ml.EvaluationMetrics;
import com.lyrics.ml.EvaluationMetrics.ClassMetric;
import com.lyrics.model.AppContext;
import com.lyrics.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern ML Model Evaluation Dashboard with KPI cards, heatmap confusion matrix,
 * per-class precision/recall/F1 metrics, and empty state handler.
 */
public class EvaluationPanel extends JPanel {
    private final Consumer<String> navigationCallback;

    private CardLayout evalCardLayout;
    private JPanel evalWrapper;
    private JPanel emptyStatePanel;
    private JPanel activeMetricsPanel;

    private JLabel accuracyVal;
    private JLabel precisionVal;
    private JLabel recallVal;
    private JLabel f1Val;
    private JLabel evalInfoLabel;

    private JTable classMetricsTable;
    private DefaultTableModel classMetricsModel;

    private JTable confusionTable;
    private DefaultTableModel confusionModel;

    public EvaluationPanel(Consumer<String> navigationCallback) {
        this.navigationCallback = navigationCallback;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(18, 28, 18, 28));

        initUI();
        AppContext.getInstance().addStateChangeListener(this::refreshMetrics);
    }

    private void initUI() {
        evalCardLayout = new CardLayout();
        evalWrapper = new JPanel(evalCardLayout);
        evalWrapper.setOpaque(false);

        emptyStatePanel = createEmptyState();
        activeMetricsPanel = createActiveMetricsDashboard();

        evalWrapper.add(emptyStatePanel, "EMPTY");
        evalWrapper.add(activeMetricsPanel, "ACTIVE");

        add(evalWrapper, BorderLayout.CENTER);
        refreshMetrics();
    }

    private JPanel createEmptyState() {
        JPanel p = UITheme.createCardPanel(16);
        p.setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel icon = new JLabel("◈", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 42));
        icon.setForeground(new Color(0x3B, 0x4D, 0x6E));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Attribution model isn't trained yet.", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Upload a dataset and train the classifier to inspect test evaluation metrics.", SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton trainBtn = UITheme.createPrimaryButton("Train Model →");
        trainBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        trainBtn.addActionListener(e -> navigationCallback.accept("DATASET"));

        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(desc);
        center.add(Box.createVerticalStrut(16));
        center.add(trainBtn);

        p.add(center);
        return p;
    }

    private JPanel createActiveMetricsDashboard() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_DARK);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 2));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(1600, 45));

        JLabel titleLbl = new JLabel("MODEL EVALUATION");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        evalInfoLabel = new JLabel("Evaluated on stratified held-out test split (Zero data leakage)");
        evalInfoLabel.setFont(UITheme.FONT_SMALL);
        evalInfoLabel.setForeground(UITheme.SECONDARY_CYAN);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        headerPanel.add(evalInfoLabel, BorderLayout.EAST);

        scrollContent.add(headerPanel);
        scrollContent.add(Box.createVerticalStrut(14));

        // 4 KPI Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setMaximumSize(new Dimension(1600, 85));

        accuracyVal = new JLabel("0.00%", SwingConstants.CENTER);
        precisionVal = new JLabel("0.00%", SwingConstants.CENTER);
        recallVal = new JLabel("0.00%", SwingConstants.CENTER);
        f1Val = new JLabel("0.00%", SwingConstants.CENTER);

        kpiPanel.add(createMetricCard("ACCURACY", accuracyVal, UITheme.SUCCESS_GREEN));
        kpiPanel.add(createMetricCard("PRECISION", precisionVal, UITheme.SECONDARY_CYAN));
        kpiPanel.add(createMetricCard("RECALL", recallVal, UITheme.PRIMARY_PURPLE));
        kpiPanel.add(createMetricCard("F1 SCORE", f1Val, UITheme.ACCENT_PINK));

        scrollContent.add(kpiPanel);
        scrollContent.add(Box.createVerticalStrut(14));

        // Two Section Tables: Classification Report (Left), Confusion Matrix (Right)
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesPanel.setOpaque(false);
        tablesPanel.setMaximumSize(new Dimension(1600, 360));
        tablesPanel.setPreferredSize(new Dimension(1100, 360));

        // Left: Classification Report Table Card
        JPanel reportCard = UITheme.createCardPanel(14);
        reportCard.setLayout(new BorderLayout(0, 10));
        reportCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel reportHeader = new JLabel("CLASSIFICATION REPORT");
        reportHeader.setFont(UITheme.FONT_CARD_TITLE);
        reportHeader.setForeground(UITheme.TEXT_PRIMARY);
        reportCard.add(reportHeader, BorderLayout.NORTH);

        String[] reportCols = {"Artist", "Precision", "Recall", "F1", "Support"};
        classMetricsModel = new DefaultTableModel(reportCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        classMetricsTable = new JTable(classMetricsModel);
        setupTableStyle(classMetricsTable);
        classMetricsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        classMetricsTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        classMetricsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        classMetricsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        classMetricsTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        classMetricsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        classMetricsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        classMetricsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        classMetricsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane reportScroll = new JScrollPane(classMetricsTable);
        reportScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        reportScroll.getViewport().setBackground(UITheme.BG_PANEL);

        reportCard.add(reportScroll, BorderLayout.CENTER);

        // Right: Heatmap Confusion Matrix Card
        JPanel matrixCard = UITheme.createCardPanel(14);
        matrixCard.setLayout(new BorderLayout(0, 10));
        matrixCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel matrixHeader = new JLabel("CONFUSION MATRIX (TRUE \\ PRED)");
        matrixHeader.setFont(UITheme.FONT_CARD_TITLE);
        matrixHeader.setForeground(UITheme.TEXT_PRIMARY);
        matrixCard.add(matrixHeader, BorderLayout.NORTH);

        confusionModel = new DefaultTableModel();
        confusionTable = new JTable(confusionModel) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        setupTableStyle(confusionTable);

        JScrollPane matrixScroll = new JScrollPane(confusionTable);
        matrixScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        matrixScroll.getViewport().setBackground(UITheme.BG_PANEL);

        matrixCard.add(matrixScroll, BorderLayout.CENTER);

        tablesPanel.add(reportCard);
        tablesPanel.add(matrixCard);

        scrollContent.add(tablesPanel);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(mainScroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(UITheme.TEXT_MUTED);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(3));
        card.add(valueLabel);
        return card;
    }

    private void setupTableStyle(JTable table) {
        table.setBackground(UITheme.BG_PANEL);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setSelectionBackground(UITheme.BG_PANEL_HOVER);
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.BG_INPUT);
        header.setForeground(UITheme.TEXT_MUTED);
        header.setFont(UITheme.FONT_SMALL_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 28));
    }

    private void refreshMetrics() {
        AppContext ctx = AppContext.getInstance();
        if (ctx.isModelTrained() && ctx.getEvaluationMetrics() != null) {
            EvaluationMetrics metrics = ctx.getEvaluationMetrics();

            accuracyVal.setText(String.format("%.2f%%", metrics.getAccuracyPercent()));
            precisionVal.setText(String.format("%.2f%%", metrics.getMacroPrecisionPercent()));
            recallVal.setText(String.format("%.2f%%", metrics.getMacroRecallPercent()));
            f1Val.setText(String.format("%.2f%%", metrics.getMacroF1Percent()));

            evalInfoLabel.setText(String.format("Evaluated on %d test instances | Model: %s",
                    metrics.getTotalTestSamples(), ctx.getClassifier().getClassifierName().split(" ")[0]));

            // Populate Class Metrics Table
            classMetricsModel.setRowCount(0);
            for (ClassMetric cm : metrics.getClassMetrics()) {
                classMetricsModel.addRow(new Object[]{
                        cm.getClassName(),
                        String.format("%.1f%%", cm.getPrecisionPercent()),
                        String.format("%.1f%%", cm.getRecallPercent()),
                        String.format("%.1f%%", cm.getF1ScorePercent()),
                        cm.getSupport()
                });
            }

            // Weighted Avg
            classMetricsModel.addRow(new Object[]{
                    "Weighted Avg",
                    String.format("%.1f%%", metrics.getWeightedPrecision() * 100.0),
                    String.format("%.1f%%", metrics.getWeightedRecall() * 100.0),
                    String.format("%.1f%%", metrics.getWeightedF1() * 100.0),
                    metrics.getTotalTestSamples()
            });

            // Populate Confusion Matrix Heatmap
            String[] labels = metrics.getClassLabels();
            int numClasses = labels.length;

            String[] confCols = new String[numClasses + 1];
            confCols[0] = "True \\ Pred";
            System.arraycopy(labels, 0, confCols, 1, numClasses);

            confusionModel.setDataVector(new Object[0][0], confCols);

            int[][] matrix = metrics.getConfusionMatrix();
            for (int r = 0; r < numClasses; r++) {
                Object[] rowData = new Object[numClasses + 1];
                rowData[0] = labels[r];
                for (int c = 0; c < numClasses; c++) {
                    rowData[c + 1] = matrix[r][c];
                }
                confusionModel.addRow(rowData);
            }

            // Heatmap Cell Renderer
            confusionTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus, int row, int column) {
                    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (column == 0) {
                        setFont(UITheme.FONT_SMALL_BOLD);
                        setForeground(UITheme.SECONDARY_CYAN);
                        setBackground(UITheme.BG_INPUT);
                    } else {
                        setFont(UITheme.FONT_BODY);
                        int predCol = column - 1;
                        if (row == predCol && value instanceof Number && ((Number) value).intValue() > 0) {
                            setBackground(new Color(0x06, 0x4E, 0x3B)); // Forest green
                            setForeground(new Color(0x6E, 0xEE, 0xA4));
                        } else if (value instanceof Number && ((Number) value).intValue() > 0) {
                            setBackground(new Color(0x7F, 0x1D, 0x1D)); // Crimson red
                            setForeground(new Color(0xFC, 0xA5, 0xA5));
                        } else {
                            setBackground(UITheme.BG_PANEL);
                            setForeground(UITheme.TEXT_MUTED);
                        }
                    }
                    return comp;
                }
            });

            evalCardLayout.show(evalWrapper, "ACTIVE");
        } else {
            evalCardLayout.show(evalWrapper, "EMPTY");
        }
    }
}

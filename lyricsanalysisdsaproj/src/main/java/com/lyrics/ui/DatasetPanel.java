package com.lyrics.ui;

import com.lyrics.ml.ArtistClassifier;
import com.lyrics.ml.LogisticRegressionClassifier;
import com.lyrics.ml.ModelEvaluator;
import com.lyrics.ml.MultinomialNBClassifier;
import com.lyrics.model.AppContext;
import com.lyrics.model.Dataset;
import com.lyrics.model.Song;
import com.lyrics.preprocessing.TextPreprocessor;
import com.lyrics.ui.theme.UITheme;
import com.lyrics.utils.CSVReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * Modern Reference Dataset Analytics Dashboard with search filters,
 * KPI metric cards, CSV upload, and model training controls.
 */
public class DatasetPanel extends JPanel {
    private final Consumer<String> navigationCallback;

    private JLabel totalSongsValue;
    private JLabel totalArtistsValue;
    private JLabel vocabSizeValue;
    private JLabel avgLengthValue;

    private JTextField searchField;
    private JTable previewTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JComboBox<String> classifierCombo;
    private JComboBox<String> ngramCombo;
    private JComboBox<String> maxFeaturesCombo;
    private JButton trainButton;
    private JProgressBar progressBar;
    private JLabel trainingStatusLabel;

    public DatasetPanel(Consumer<String> navigationCallback) {
        this.navigationCallback = navigationCallback;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(18, 28, 18, 28));

        initUI();
        AppContext.getInstance().addStateChangeListener(this::refreshData);
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_DARK);

        // 1. Top Header & Actions
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(1600, 42));

        JLabel titleLbl = new JLabel("REFERENCE DATASET");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton loadDemoBtn = UITheme.createAccentButton("⚡ Load Demo Dataset", UITheme.SECONDARY_CYAN, new Color(0x08, 0x91, 0xB2));
        loadDemoBtn.addActionListener(e -> loadDemoDataset());

        JButton uploadBtn = UITheme.createPrimaryButton("📁 Upload CSV");
        uploadBtn.addActionListener(e -> chooseAndUploadCSV());

        btnRow.add(loadDemoBtn);
        btnRow.add(uploadBtn);

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(btnRow, BorderLayout.EAST);

        scrollContent.add(topBar);
        scrollContent.add(Box.createVerticalStrut(14));

        // 2. 4 Compact Statistic Cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(1600, 85));

        totalSongsValue = new JLabel("0", SwingConstants.CENTER);
        totalArtistsValue = new JLabel("0", SwingConstants.CENTER);
        vocabSizeValue = new JLabel("0", SwingConstants.CENTER);
        avgLengthValue = new JLabel("0 words", SwingConstants.CENTER);

        kpiRow.add(createKpiCard("TOTAL SONGS", totalSongsValue, UITheme.SECONDARY_CYAN));
        kpiRow.add(createKpiCard("ARTISTS", totalArtistsValue, UITheme.PRIMARY_PURPLE));
        kpiRow.add(createKpiCard("VOCABULARY", vocabSizeValue, UITheme.SUCCESS_GREEN));
        kpiRow.add(createKpiCard("AVG. LENGTH", avgLengthValue, UITheme.ACCENT_PINK));

        scrollContent.add(kpiRow);
        scrollContent.add(Box.createVerticalStrut(14));

        // 3. Training Controls Card
        JPanel trainCard = UITheme.createCardPanel(14);
        trainCard.setLayout(new BorderLayout(14, 8));
        trainCard.setBorder(new EmptyBorder(12, 18, 12, 18));
        trainCard.setMaximumSize(new Dimension(1600, 115));

        JPanel trainConfig = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        trainConfig.setOpaque(false);

        JLabel modelLabel = new JLabel("Model:");
        modelLabel.setFont(UITheme.FONT_SMALL_BOLD);
        modelLabel.setForeground(UITheme.TEXT_SECONDARY);

        classifierCombo = new JComboBox<>(new String[]{
                "Multinomial Naive Bayes",
                "Softmax Logistic Regression"
        });
        styleCombo(classifierCombo);

        JLabel ngramLabel = new JLabel("N-Grams:");
        ngramLabel.setFont(UITheme.FONT_SMALL_BOLD);
        ngramLabel.setForeground(UITheme.TEXT_SECONDARY);

        ngramCombo = new JComboBox<>(new String[]{"Unigrams (1-gram)", "Unigrams + Bigrams (1-2)"});
        styleCombo(ngramCombo);

        JLabel featLabel = new JLabel("Max Features:");
        featLabel.setFont(UITheme.FONT_SMALL_BOLD);
        featLabel.setForeground(UITheme.TEXT_SECONDARY);

        maxFeaturesCombo = new JComboBox<>(new String[]{"5000 terms", "2000 terms", "10000 terms"});
        styleCombo(maxFeaturesCombo);

        trainButton = UITheme.createAccentButton("▶ Train Attribution Model", UITheme.SUCCESS_GREEN, new Color(0x16, 0xA3, 0x4A));
        trainButton.addActionListener(e -> executeModelTraining());

        trainConfig.add(modelLabel);
        trainConfig.add(classifierCombo);
        trainConfig.add(ngramLabel);
        trainConfig.add(ngramCombo);
        trainConfig.add(featLabel);
        trainConfig.add(maxFeaturesCombo);
        trainConfig.add(trainButton);

        JPanel trainFooter = new JPanel(new BorderLayout(10, 0));
        trainFooter.setOpaque(false);

        trainingStatusLabel = new JLabel("Ready to train. Select parameters and click Train Attribution Model.");
        trainingStatusLabel.setFont(UITheme.FONT_SMALL);
        trainingStatusLabel.setForeground(UITheme.TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(UITheme.PRIMARY_PURPLE);
        progressBar.setBackground(UITheme.BG_INPUT);
        progressBar.setPreferredSize(new Dimension(160, 6));
        progressBar.setVisible(false);

        trainFooter.add(trainingStatusLabel, BorderLayout.WEST);
        trainFooter.add(progressBar, BorderLayout.EAST);

        trainCard.add(trainConfig, BorderLayout.CENTER);
        trainCard.add(trainFooter, BorderLayout.SOUTH);

        scrollContent.add(trainCard);
        scrollContent.add(Box.createVerticalStrut(14));

        // 4. Dataset Table Preview with Search Field
        JPanel tableCard = UITheme.createCardPanel(14);
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(14, 18, 14, 18));
        tableCard.setMaximumSize(new Dimension(1600, 310));
        tableCard.setPreferredSize(new Dimension(1100, 310));

        // Search Bar Row
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);

        JLabel previewTitle = new JLabel("DATASET PREVIEW");
        previewTitle.setFont(UITheme.FONT_CARD_TITLE);
        previewTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchWrap.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(UITheme.TEXT_MUTED);

        searchField = new JTextField(18);
        searchField.setFont(UITheme.FONT_SMALL);
        searchField.setBackground(UITheme.BG_INPUT);
        searchField.setForeground(UITheme.TEXT_PRIMARY);
        searchField.setCaretColor(UITheme.SECONDARY_CYAN);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)
        ));
        searchField.setToolTipText("Filter by artist or title...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText().trim();
                if (rowSorter != null) {
                    if (text.isEmpty()) rowSorter.setRowFilter(null);
                    else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        searchWrap.add(searchIcon);
        searchWrap.add(searchField);

        searchRow.add(previewTitle, BorderLayout.WEST);
        searchRow.add(searchWrap, BorderLayout.EAST);
        tableCard.add(searchRow, BorderLayout.NORTH);

        String[] cols = {"Artist", "Song Title", "Lyrics Preview", "Words"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        previewTable = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        previewTable.setRowSorter(rowSorter);
        setupTableStyle(previewTable);

        previewTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        previewTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        previewTable.getColumnModel().getColumn(2).setPreferredWidth(520);
        previewTable.getColumnModel().getColumn(3).setPreferredWidth(70);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        previewTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(previewTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableScroll.getViewport().setBackground(UITheme.BG_PANEL);

        tableCard.add(tableScroll, BorderLayout.CENTER);
        scrollContent.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
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

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(UITheme.FONT_SMALL);
        combo.setBackground(UITheme.BG_INPUT);
        combo.setForeground(UITheme.TEXT_PRIMARY);
        combo.setFocusable(false);
    }

    private void setupTableStyle(JTable table) {
        table.setBackground(UITheme.BG_PANEL);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setSelectionBackground(UITheme.BG_PANEL_HOVER);
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.BG_INPUT);
        header.setForeground(UITheme.TEXT_MUTED);
        header.setFont(UITheme.FONT_SMALL_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 28));
    }

    private void chooseAndUploadCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Dataset (.csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Dataset (*.csv)", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                Dataset dataset = CSVReader.readCSV(selectedFile);
                AppContext.getInstance().setDataset(dataset);
                JOptionPane.showMessageDialog(this,
                        String.format("Dataset '%s' loaded!\nSongs: %d | Artists: %d\nClick 'Train Attribution Model' to build ML classifier.",
                                selectedFile.getName(), dataset.getSongCount(), dataset.getArtistCount()),
                        "Dataset Loaded",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV Error: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDemoDataset() {
        try {
            Dataset demo = AppContext.getInstance().loadDemoDataset();
            JOptionPane.showMessageDialog(this,
                    String.format("Demo Dataset loaded!\nSongs: %d | Artists: %d\nClick 'Train Attribution Model' to train.",
                            demo.getSongCount(), demo.getArtistCount()),
                    "Demo Dataset Ready",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load demo dataset: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeModelTraining() {
        AppContext ctx = AppContext.getInstance();
        if (!ctx.hasDataset()) {
            JOptionPane.showMessageDialog(this, "Please upload or load a dataset first.", "No Dataset", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Dataset dataset = ctx.getDataset();
        if (!dataset.hasMultipleArtists()) {
            JOptionPane.showMessageDialog(this, "Artist attribution requires at least 2 distinct artists.", "Insufficient Classes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maxFeatures = 5000;
        if (maxFeaturesCombo.getSelectedIndex() == 1) maxFeatures = 2000;
        else if (maxFeaturesCombo.getSelectedIndex() == 2) maxFeatures = 10000;

        int ngramMax = (ngramCombo.getSelectedIndex() == 1) ? 2 : 1;

        ArtistClassifier selectedClassifier;
        if (classifierCombo.getSelectedIndex() == 1) {
            selectedClassifier = new LogisticRegressionClassifier(0.1, 1e-4, 300);
        } else {
            selectedClassifier = new MultinomialNBClassifier(1.0);
        }

        trainButton.setEnabled(false);
        progressBar.setVisible(true);
        trainingStatusLabel.setText("Training model on stratified split...");

        final int fMax = maxFeatures;
        final int fNgram = ngramMax;

        SwingWorker<ModelEvaluator.TrainingPipelineResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ModelEvaluator.TrainingPipelineResult doInBackground() {
                TextPreprocessor preprocessor = TextPreprocessor.getInstance();
                return ModelEvaluator.trainAndEvaluate(dataset, preprocessor, selectedClassifier, 0.20, fMax, fNgram);
            }

            @Override
            protected void done() {
                trainButton.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    ModelEvaluator.TrainingPipelineResult res = get();
                    ctx.setTrainedPipeline(res.getVectorizer(), res.getClassifier(), res.getEvaluationMetrics(), res.getFullCorpusVectors());

                    trainingStatusLabel.setText(String.format(
                            "✓ Model Trained: %s | Test Accuracy: %.2f%%",
                            res.getClassifier().getClassifierName().split(" ")[0],
                            res.getEvaluationMetrics().getAccuracyPercent()
                    ));

                    int opt = JOptionPane.showConfirmDialog(DatasetPanel.this,
                            String.format("Model trained successfully!\n\nAccuracy: %.2f%%\nMacro F1: %.2f%%\n\nView Model Evaluation dashboard?",
                                    res.getEvaluationMetrics().getAccuracyPercent(),
                                    res.getEvaluationMetrics().getMacroF1Percent()),
                            "Training Complete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);

                    if (opt == JOptionPane.YES_OPTION) {
                        navigationCallback.accept("EVALUATION");
                    }
                } catch (Exception e) {
                    trainingStatusLabel.setText("Training failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(DatasetPanel.this, "Training error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void refreshData() {
        AppContext ctx = AppContext.getInstance();
        if (ctx.hasDataset()) {
            Dataset d = ctx.getDataset();
            totalSongsValue.setText(String.valueOf(d.getSongCount()));
            totalArtistsValue.setText(String.valueOf(d.getArtistCount()));
            avgLengthValue.setText(String.format("%.0f words", d.getAverageLyricLength()));

            tableModel.setRowCount(0);
            for (Song s : d.getSongs()) {
                String snippet = s.getRawLyrics().replace("\n", " ");
                if (snippet.length() > 80) snippet = snippet.substring(0, 80) + "...";
                tableModel.addRow(new Object[]{
                        s.getArtist(),
                        s.getTitle(),
                        snippet,
                        s.getRawWordCount()
                });
            }

            TextPreprocessor preprocessor = TextPreprocessor.getInstance();
            for (Song s : d.getSongs()) {
                if (s.getTokens() == null) {
                    s.setTokens(preprocessor.extractProcessedTokens(s.getRawLyrics()));
                }
            }
            vocabSizeValue.setText(String.format("%,d", d.getTotalVocabularySize()));

            if (ctx.isModelTrained()) {
                trainingStatusLabel.setText(String.format(
                        "✓ Active Model: %s | Test Accuracy: %.2f%%",
                        ctx.getClassifier().getClassifierName().split(" ")[0],
                        ctx.getEvaluationMetrics().getAccuracyPercent()));
            } else {
                trainingStatusLabel.setText("Dataset loaded. Click 'Train Attribution Model' to build ML pipeline.");
            }
        } else {
            totalSongsValue.setText("0");
            totalArtistsValue.setText("0");
            vocabSizeValue.setText("0");
            avgLengthValue.setText("0 words");
            tableModel.setRowCount(0);
            trainingStatusLabel.setText("Upload a CSV dataset or click 'Load Demo Dataset' to begin.");
        }
    }
}

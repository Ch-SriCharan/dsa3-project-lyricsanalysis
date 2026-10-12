package com.lyrics.ui;

import com.lyrics.ml.ModelEvaluator;
import com.lyrics.ml.MultinomialNBClassifier;
import com.lyrics.model.AnalysisResult;
import com.lyrics.model.AnalysisResult.ArtistProbability;
import com.lyrics.model.AnalysisResult.PreprocessingStats;
import com.lyrics.model.AnalysisResult.SimilarityMatch;
import com.lyrics.model.AppContext;
import com.lyrics.model.Dataset;
import com.lyrics.nlp.CosineSimilarity;
import com.lyrics.nlp.TFIDFVectorizer;
import com.lyrics.preprocessing.TextPreprocessor;
import com.lyrics.ui.theme.UITheme;
import com.lyrics.utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern AI Music Analysis screen featuring drop/paste lyrics input,
 * animated step loading, radial similarity dials, and artist attribution cards.
 */
public class AnalyzePanel extends JPanel {
    private JTextArea lyricsInputArea;
    private JLabel selectedSourceLabel;
    private String currentSourceName = "Pasted Text";

    // Dynamic Results Container & States
    private CardLayout resultsCardLayout;
    private JPanel resultsWrapper;
    private JPanel emptyStatePanel;
    private JPanel loadingStatePanel;
    private JLabel loadingStatusLabel;
    private JProgressBar loadingProgressBar;
    private JPanel activeResultsPanel;

    // Results UI Elements
    private UITheme.CircularGaugePanel similarityGauge;
    private JLabel topMatchSongLabel;
    private JLabel topMatchArtistLabel;

    private JTable similarityTable;
    private DefaultTableModel similarityTableModel;

    private JLabel predictedArtistLabel;
    private JLabel attributionScoreLabel;
    private JPanel candidateBarsContainer;

    private JLabel statsRawWords;
    private JLabel statsStopwords;
    private JLabel statsFinalTokens;
    private JLabel statsUniqueWords;
    private JTextArea preprocessedPreviewArea;

    private JButton analyzeButton;

    public AnalyzePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(18, 26, 18, 26));

        initUI();
    }

    private void initUI() {
        // Main 2-column layout: Left Input (42%), Right Results (58%)
        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        mainGrid.setOpaque(false);

        // ================= LEFT: INPUT PANEL =================
        JPanel inputCard = UITheme.createCardPanel(16);
        inputCard.setLayout(new BorderLayout(0, 12));
        inputCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Top Drop/Select Header Box
        JPanel dropSelectBox = UITheme.createCardPanel(12, UITheme.BG_SECONDARY, UITheme.BG_PANEL, UITheme.BORDER_COLOR);
        dropSelectBox.setLayout(new BoxLayout(dropSelectBox, BoxLayout.Y_AXIS));
        dropSelectBox.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel iconMusic = new JLabel("♫", SwingConstants.CENTER);
        iconMusic.setFont(new Font("Segoe UI", Font.BOLD, 22));
        iconMusic.setForeground(UITheme.PRIMARY_PURPLE);
        iconMusic.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dropTitle = new JLabel("Drop / Select TXT File", SwingConstants.CENTER);
        dropTitle.setFont(UITheme.FONT_BODY_BOLD);
        dropTitle.setForeground(UITheme.TEXT_PRIMARY);
        dropTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dropSub = new JLabel("or paste song lyrics below", SwingConstants.CENTER);
        dropSub.setFont(UITheme.FONT_SMALL);
        dropSub.setForeground(UITheme.TEXT_MUTED);
        dropSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnRow.setOpaque(false);

        JButton uploadTxtBtn = UITheme.createSecondaryButton("Choose Lyrics File");
        uploadTxtBtn.addActionListener(e -> chooseTXTFile());

        JButton sampleBtn = UITheme.createSecondaryButton("🎵 Sample Lyrics");
        sampleBtn.addActionListener(e -> showSampleLyricsMenu(sampleBtn));

        btnRow.add(uploadTxtBtn);
        btnRow.add(sampleBtn);

        selectedSourceLabel = new JLabel("Source: Pasted Text", SwingConstants.CENTER);
        selectedSourceLabel.setFont(UITheme.FONT_SMALL);
        selectedSourceLabel.setForeground(UITheme.SECONDARY_CYAN);
        selectedSourceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dropSelectBox.add(iconMusic);
        dropSelectBox.add(Box.createVerticalStrut(2));
        dropSelectBox.add(dropTitle);
        dropSelectBox.add(dropSub);
        dropSelectBox.add(btnRow);
        dropSelectBox.add(selectedSourceLabel);

        inputCard.add(dropSelectBox, BorderLayout.NORTH);

        // Center Lyrics TextArea
        lyricsInputArea = new JTextArea();
        lyricsInputArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lyricsInputArea.setBackground(UITheme.BG_INPUT);
        lyricsInputArea.setForeground(UITheme.TEXT_PRIMARY);
        lyricsInputArea.setCaretColor(UITheme.SECONDARY_CYAN);
        lyricsInputArea.setLineWrap(true);
        lyricsInputArea.setWrapStyleWord(true);
        lyricsInputArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane inputScroll = new JScrollPane(lyricsInputArea);
        inputScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        inputCard.add(inputScroll, BorderLayout.CENTER);

        // Bottom Actions
        JPanel inputBottom = new JPanel(new BorderLayout(10, 0));
        inputBottom.setOpaque(false);
        inputBottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            lyricsInputArea.setText("");
            currentSourceName = "Pasted Text";
            selectedSourceLabel.setText("Source: Pasted Text");
            resultsCardLayout.show(resultsWrapper, "EMPTY");
        });

        analyzeButton = UITheme.createPrimaryButton("Analyze Lyrics →");
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        analyzeButton.setPreferredSize(new Dimension(analyzeButton.getPreferredSize().width, 40));
        analyzeButton.addActionListener(e -> runAnalysis());

        inputBottom.add(clearBtn, BorderLayout.WEST);
        inputBottom.add(analyzeButton, BorderLayout.CENTER);

        inputCard.add(inputBottom, BorderLayout.SOUTH);

        // ================= RIGHT: RESULTS WRAPPER =================
        resultsCardLayout = new CardLayout();
        resultsWrapper = new JPanel(resultsCardLayout);
        resultsWrapper.setOpaque(false);

        // State 1: Empty State
        emptyStatePanel = createEmptyStatePanel();
        // State 2: Loading State
        loadingStatePanel = createLoadingStatePanel();
        // State 3: Active Results Panel
        activeResultsPanel = createActiveResultsPanel();

        resultsWrapper.add(emptyStatePanel, "EMPTY");
        resultsWrapper.add(loadingStatePanel, "LOADING");
        resultsWrapper.add(activeResultsPanel, "RESULTS");

        resultsCardLayout.show(resultsWrapper, "EMPTY");

        mainGrid.add(inputCard);
        mainGrid.add(resultsWrapper);

        add(mainGrid, BorderLayout.CENTER);
    }

    private JPanel createEmptyStatePanel() {
        JPanel p = UITheme.createCardPanel(16);
        p.setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel icon = new JLabel("♫", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 42));
        icon.setForeground(new Color(0x3B, 0x4D, 0x6E));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Ready to analyze", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Upload lyrics to begin similarity and attribution analysis.", SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(desc);

        p.add(center);
        return p;
    }

    private JPanel createLoadingStatePanel() {
        JPanel p = UITheme.createCardPanel(16);
        p.setLayout(new GridBagLayout());

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setIndeterminate(true);
        loadingProgressBar.setForeground(UITheme.PRIMARY_PURPLE);
        loadingProgressBar.setBackground(UITheme.BG_INPUT);
        loadingProgressBar.setPreferredSize(new Dimension(280, 8));
        loadingProgressBar.setMaximumSize(new Dimension(280, 8));
        loadingProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        loadingStatusLabel = new JLabel("PREPROCESSING LYRICS...", SwingConstants.CENTER);
        loadingStatusLabel.setFont(UITheme.FONT_SMALL_BOLD);
        loadingStatusLabel.setForeground(UITheme.SECONDARY_CYAN);
        loadingStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(loadingStatusLabel);
        center.add(Box.createVerticalStrut(14));
        center.add(loadingProgressBar);

        p.add(center);
        return p;
    }

    private JPanel createActiveResultsPanel() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_DARK);

        // 1. Top Row: Similarity Dial (Left) + Artist Attribution (Right)
        JPanel topRow = new JPanel(new GridLayout(1, 2, 14, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(1600, 185));

        // Similarity Gauge Card
        JPanel simCard = UITheme.createCardPanel(14);
        simCard.setLayout(new BorderLayout(10, 0));
        simCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        similarityGauge = new UITheme.CircularGaugePanel(UITheme.SECONDARY_CYAN);

        JPanel simInfo = new JPanel();
        simInfo.setLayout(new BoxLayout(simInfo, BoxLayout.Y_AXIS));
        simInfo.setOpaque(false);

        JLabel simCardHeader = new JLabel("SIMILARITY SCORE");
        simCardHeader.setFont(UITheme.FONT_SMALL_BOLD);
        simCardHeader.setForeground(UITheme.SECONDARY_CYAN);

        JLabel simCardSub = new JLabel("Similarity to closest matching lyric");
        simCardSub.setFont(UITheme.FONT_SMALL);
        simCardSub.setForeground(UITheme.TEXT_MUTED);

        topMatchSongLabel = new JLabel("—");
        topMatchSongLabel.setFont(UITheme.FONT_BODY_BOLD);
        topMatchSongLabel.setForeground(UITheme.TEXT_PRIMARY);

        topMatchArtistLabel = new JLabel("—");
        topMatchArtistLabel.setFont(UITheme.FONT_SMALL);
        topMatchArtistLabel.setForeground(UITheme.TEXT_SECONDARY);

        simInfo.add(simCardHeader);
        simInfo.add(Box.createVerticalStrut(2));
        simInfo.add(simCardSub);
        simInfo.add(Box.createVerticalStrut(8));
        simInfo.add(topMatchSongLabel);
        simInfo.add(topMatchArtistLabel);

        simCard.add(similarityGauge, BorderLayout.WEST);
        simCard.add(simInfo, BorderLayout.CENTER);

        // Artist Attribution Card
        JPanel attrCard = UITheme.createCardPanel(14);
        attrCard.setLayout(new BoxLayout(attrCard, BoxLayout.Y_AXIS));
        attrCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel attrCardHeader = new JLabel("ARTIST ATTRIBUTION");
        attrCardHeader.setFont(UITheme.FONT_SMALL_BOLD);
        attrCardHeader.setForeground(UITheme.PRIMARY_PURPLE);

        JLabel predLabelPrompt = new JLabel("Predicted Artist");
        predLabelPrompt.setFont(UITheme.FONT_SMALL);
        predLabelPrompt.setForeground(UITheme.TEXT_MUTED);

        predictedArtistLabel = new JLabel("Artist Name");
        predictedArtistLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        predictedArtistLabel.setForeground(UITheme.PRIMARY_PURPLE);

        attributionScoreLabel = new JLabel("Confidence: —");
        attributionScoreLabel.setFont(UITheme.FONT_SMALL_BOLD);
        attributionScoreLabel.setForeground(UITheme.ACCENT_PINK);

        // Candidate Probabilities Container
        candidateBarsContainer = new JPanel();
        candidateBarsContainer.setLayout(new BoxLayout(candidateBarsContainer, BoxLayout.Y_AXIS));
        candidateBarsContainer.setOpaque(false);

        attrCard.add(attrCardHeader);
        attrCard.add(Box.createVerticalStrut(4));
        attrCard.add(predLabelPrompt);
        attrCard.add(predictedArtistLabel);
        attrCard.add(attributionScoreLabel);
        attrCard.add(Box.createVerticalStrut(6));
        attrCard.add(candidateBarsContainer);

        topRow.add(simCard);
        topRow.add(attrCard);

        scrollContent.add(topRow);
        scrollContent.add(Box.createVerticalStrut(14));

        // 2. Top Matches Table Card
        JPanel tableCard = UITheme.createCardPanel(14);
        tableCard.setLayout(new BorderLayout(0, 8));
        tableCard.setBorder(new EmptyBorder(12, 16, 12, 16));
        tableCard.setMaximumSize(new Dimension(1600, 210));
        tableCard.setPreferredSize(new Dimension(600, 210));

        JLabel tableTitle = new JLabel("TOP MATCHES");
        tableTitle.setFont(UITheme.FONT_CARD_TITLE);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"#", "SONG", "ARTIST", "SCORE"};
        similarityTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        similarityTable = new JTable(similarityTableModel);
        setupTableStyle(similarityTable);

        similarityTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        similarityTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        similarityTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        similarityTable.getColumnModel().getColumn(3).setPreferredWidth(110);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        similarityTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        similarityTable.getColumnModel().getColumn(3).setCellRenderer(new UITheme.PercentageBarRenderer(UITheme.SECONDARY_CYAN));

        JScrollPane tableScroll = new JScrollPane(similarityTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableScroll.getViewport().setBackground(UITheme.BG_PANEL);

        tableCard.add(tableScroll, BorderLayout.CENTER);

        scrollContent.add(tableCard);
        scrollContent.add(Box.createVerticalStrut(14));

        // 3. Preprocessing Status Breakdown Card
        JPanel prepCard = UITheme.createCardPanel(14);
        prepCard.setLayout(new BorderLayout(0, 8));
        prepCard.setBorder(new EmptyBorder(10, 16, 10, 16));
        prepCard.setMaximumSize(new Dimension(1600, 130));

        JPanel prepHeader = new JPanel(new BorderLayout());
        prepHeader.setOpaque(false);

        JLabel prepTitle = new JLabel("NLP TEXT PREPROCESSING PIPELINE");
        prepTitle.setFont(UITheme.FONT_SMALL_BOLD);
        prepTitle.setForeground(UITheme.SUCCESS_GREEN);
        prepHeader.add(prepTitle, BorderLayout.WEST);

        JPanel statsPills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statsPills.setOpaque(false);

        statsRawWords = new JLabel("Raw Words: 0");
        statsRawWords.setFont(UITheme.FONT_SMALL);
        statsRawWords.setForeground(UITheme.TEXT_SECONDARY);

        statsStopwords = new JLabel("Stop Words: 0");
        statsStopwords.setFont(UITheme.FONT_SMALL);
        statsStopwords.setForeground(UITheme.TEXT_SECONDARY);

        statsFinalTokens = new JLabel("Tokens: 0");
        statsFinalTokens.setFont(UITheme.FONT_SMALL);
        statsFinalTokens.setForeground(UITheme.TEXT_SECONDARY);

        statsUniqueWords = new JLabel("Unique: 0");
        statsUniqueWords.setFont(UITheme.FONT_SMALL);
        statsUniqueWords.setForeground(UITheme.TEXT_SECONDARY);

        statsPills.add(statsRawWords);
        statsPills.add(statsStopwords);
        statsPills.add(statsFinalTokens);
        statsPills.add(statsUniqueWords);

        prepHeader.add(statsPills, BorderLayout.EAST);
        prepCard.add(prepHeader, BorderLayout.NORTH);

        preprocessedPreviewArea = new JTextArea();
        preprocessedPreviewArea.setFont(UITheme.FONT_CODE);
        preprocessedPreviewArea.setForeground(UITheme.TEXT_SECONDARY);
        preprocessedPreviewArea.setBackground(UITheme.BG_INPUT);
        preprocessedPreviewArea.setLineWrap(true);
        preprocessedPreviewArea.setWrapStyleWord(true);
        preprocessedPreviewArea.setEditable(false);
        preprocessedPreviewArea.setBorder(new EmptyBorder(4, 6, 4, 6));

        JScrollPane prepScroll = new JScrollPane(preprocessedPreviewArea);
        prepScroll.setPreferredSize(new Dimension(500, 48));
        prepScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        prepCard.add(prepScroll, BorderLayout.CENTER);

        scrollContent.add(prepCard);

        JScrollPane mainResultsScroll = new JScrollPane(scrollContent);
        mainResultsScroll.setBorder(null);
        mainResultsScroll.setOpaque(false);
        mainResultsScroll.getViewport().setOpaque(false);
        mainResultsScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(mainResultsScroll, BorderLayout.CENTER);
        return container;
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

    private void chooseTXTFile() {
        JFileChooser chooser = new JFileChooser(new File("sample_lyrics"));
        chooser.setDialogTitle("Select Lyrics File (.txt)");
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                String text = FileUtils.readFileToString(file);
                lyricsInputArea.setText(text);
                currentSourceName = file.getName();
                selectedSourceLabel.setText("Selected: " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to read file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSampleLyricsMenu(JButton button) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(UITheme.BG_PANEL);

        String[][] samples = {
                {"Taylor Swift (Romantic Pop)", "sample_lyrics/taylor_swift_test.txt"},
                {"Eminem (Fast Rap)", "sample_lyrics/eminem_test.txt"},
                {"Ed Sheeran (Acoustic Ballad)", "sample_lyrics/ed_sheeran_test.txt"},
                {"Queen (Rock Anthem)", "sample_lyrics/queen_test.txt"},
                {"Adele (Soul Ballad)", "sample_lyrics/adele_test.txt"},
                {"Billie Eilish (Indie Pop)", "sample_lyrics/billie_eilish_test.txt"},
                {"Unknown Song", "sample_lyrics/unknown_song.txt"}
        };

        for (String[] s : samples) {
            JMenuItem item = new JMenuItem(s[0]);
            item.setBackground(UITheme.BG_PANEL);
            item.setForeground(UITheme.TEXT_PRIMARY);
            item.setFont(UITheme.FONT_SMALL);
            item.addActionListener(e -> {
                try {
                    File f = new File(s[1]);
                    if (f.exists()) {
                        lyricsInputArea.setText(FileUtils.readFileToString(f));
                        currentSourceName = f.getName();
                        selectedSourceLabel.setText("Selected: " + f.getName());
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading sample: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            popup.add(item);
        }

        popup.show(button, 0, button.getHeight());
    }

    private void runAnalysis() {
        String rawText = lyricsInputArea.getText().trim();
        if (rawText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please paste lyrics or choose a TXT file before running analysis.",
                    "No Lyrics Provided",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        AppContext ctx = AppContext.getInstance();
        if (!ctx.hasDataset()) {
            JOptionPane.showMessageDialog(this,
                    "No reference dataset loaded.\nPlease upload a CSV dataset or click 'Load Demo Dataset' to perform similarity and artist attribution.",
                    "Reference Dataset Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show loading state
        resultsCardLayout.show(resultsWrapper, "LOADING");
        analyzeButton.setEnabled(false);

        Dataset dataset = ctx.getDataset();
        TextPreprocessor preprocessor = TextPreprocessor.getInstance();

        SwingWorker<AnalysisResult, String> worker = new SwingWorker<>() {
            @Override
            protected AnalysisResult doInBackground() throws Exception {
                publish("PREPROCESSING LYRICS...");
                Thread.sleep(120);

                PreprocessingStats prepStats = preprocessor.analyzePreprocessing(rawText);
                if (prepStats.getFinalTokenCount() == 0) {
                    throw new IllegalArgumentException("Lyrics contain only stop words or symbols.");
                }

                publish("TF-IDF VECTORIZATION...");
                Thread.sleep(120);

                if (!ctx.isModelTrained()) {
                    publish("TRAINING ATTRIBUTION CLASSIFIER...");
                    MultinomialNBClassifier nb = new MultinomialNBClassifier(1.0);
                    ModelEvaluator.TrainingPipelineResult pipe = ModelEvaluator.trainAndEvaluate(dataset, preprocessor, nb, 0.20, 5000, 1);
                    ctx.setTrainedPipeline(pipe.getVectorizer(), pipe.getClassifier(), pipe.getEvaluationMetrics(), pipe.getFullCorpusVectors());
                }

                publish("CALCULATING COSINE SIMILARITY...");
                Thread.sleep(100);

                TFIDFVectorizer vectorizer = ctx.getVectorizer();
                double[] queryVector = vectorizer.transform(prepStats.getProcessedText());
                List<SimilarityMatch> matches = CosineSimilarity.findTopSimilarSongs(queryVector, ctx.getCorpusVectors(), dataset.getSongs(), 5);

                publish("RUNNING CLASSIFIER & ATTRIBUTION...");
                Thread.sleep(100);

                String predictedArtist = ctx.getClassifier().predict(queryVector);
                Map<String, Double> probs = ctx.getClassifier().predictProbabilities(queryVector);

                List<ArtistProbability> candList = new ArrayList<>();
                double topConfidence = 0.0;
                int count = 0;
                for (Map.Entry<String, Double> entry : probs.entrySet()) {
                    if (count == 0) topConfidence = entry.getValue();
                    candList.add(new ArtistProbability(entry.getKey(), entry.getValue()));
                    count++;
                }

                publish("GENERATING RESULTS...");
                Thread.sleep(80);

                return new AnalysisResult(currentSourceName, prepStats, matches, predictedArtist, topConfidence, candList, true);
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    loadingStatusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                analyzeButton.setEnabled(true);
                try {
                    AnalysisResult res = get();
                    ctx.setLastAnalysisResult(res);

                    // 1. Update Preprocessing Stats
                    PreprocessingStats p = res.getPreprocessingStats();
                    statsRawWords.setText("Raw Words: " + p.getRawWordCount());
                    statsStopwords.setText("Stop Words: " + p.getStopWordsRemoved());
                    statsFinalTokens.setText("Tokens: " + p.getFinalTokenCount());
                    statsUniqueWords.setText("Unique: " + p.getUniqueWordCount());
                    preprocessedPreviewArea.setText(p.getProcessedText());

                    // 2. Update Similarity Gauge & Matches Table
                    if (!res.getSimilarityMatches().isEmpty()) {
                        SimilarityMatch top = res.getSimilarityMatches().get(0);
                        similarityGauge.setPercentage(top.getSimilarityPercentage());
                        topMatchSongLabel.setText(top.getSongTitle());
                        topMatchArtistLabel.setText("by " + top.getArtist());
                    } else {
                        similarityGauge.setPercentage(0.0);
                        topMatchSongLabel.setText("No Match");
                        topMatchArtistLabel.setText("");
                    }

                    similarityTableModel.setRowCount(0);
                    for (SimilarityMatch m : res.getSimilarityMatches()) {
                        similarityTableModel.addRow(new Object[]{
                                m.getRank(),
                                m.getSongTitle(),
                                m.getArtist(),
                                m.getSimilarityPercentage()
                        });
                    }

                    // 3. Update Artist Attribution Card & Candidate Bars
                    predictedArtistLabel.setText(res.getPredictedArtist());
                    attributionScoreLabel.setText(String.format("Confidence: %.1f%%", res.getAttributionConfidencePercentage()));

                    candidateBarsContainer.removeAll();
                    int maxBars = Math.min(3, res.getCandidateProbabilities().size());
                    for (int i = 0; i < maxBars; i++) {
                        ArtistProbability ap = res.getCandidateProbabilities().get(i);
                        candidateBarsContainer.add(createCandidateBar(ap.getArtist(), ap.getPercentage(), i == 0));
                        if (i < maxBars - 1) {
                            candidateBarsContainer.add(Box.createVerticalStrut(3));
                        }
                    }
                    candidateBarsContainer.revalidate();
                    candidateBarsContainer.repaint();

                    resultsCardLayout.show(resultsWrapper, "RESULTS");

                } catch (Exception ex) {
                    resultsCardLayout.show(resultsWrapper, "EMPTY");
                    JOptionPane.showMessageDialog(AnalyzePanel.this,
                            "Analysis Error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private JPanel createCandidateBar(String artist, double percent, boolean isTop) {
        JPanel barRow = new JPanel(new BorderLayout(8, 0));
        barRow.setOpaque(false);

        JLabel nameLbl = new JLabel(artist);
        nameLbl.setFont(UITheme.FONT_SMALL);
        nameLbl.setForeground(isTop ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);
        nameLbl.setPreferredSize(new Dimension(85, 16));

        JPanel barTrack = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int fillWidth = (int) Math.round((Math.max(0.0, Math.min(100.0, percent)) / 100.0) * w);

                g2.setColor(UITheme.BG_INPUT);
                g2.fillRoundRect(0, (h - 6) / 2, w, 6, 4, 4);

                if (fillWidth > 0) {
                    g2.setColor(isTop ? UITheme.PRIMARY_PURPLE : UITheme.BORDER_LIGHT);
                    g2.fillRoundRect(0, (h - 6) / 2, fillWidth, 6, 4, 4);
                }
                g2.dispose();
            }
        };
        barTrack.setOpaque(false);
        barTrack.setPreferredSize(new Dimension(100, 16));

        JLabel valLbl = new JLabel(String.format("%.0f%%", percent), SwingConstants.RIGHT);
        valLbl.setFont(UITheme.FONT_SMALL_BOLD);
        valLbl.setForeground(isTop ? UITheme.PRIMARY_PURPLE : UITheme.TEXT_MUTED);
        valLbl.setPreferredSize(new Dimension(38, 16));

        barRow.add(nameLbl, BorderLayout.WEST);
        barRow.add(barTrack, BorderLayout.CENTER);
        barRow.add(valLbl, BorderLayout.EAST);
        return barRow;
    }
}

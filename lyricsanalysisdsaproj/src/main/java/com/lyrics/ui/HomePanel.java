package com.lyrics.ui;

import com.lyrics.model.AppContext;
import com.lyrics.model.Dataset;
import com.lyrics.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Modern AI/music analytics Home Screen with stylized waveform visualizer,
 * compact feature cards, and real-time system status indicators.
 */
public class HomePanel extends JPanel {
    private final Consumer<String> navigationCallback;

    private JLabel datasetStatValue;
    private JLabel modelStatValue;
    private JLabel vocabStatValue;
    private JButton trainModelBtn;

    public HomePanel(Consumer<String> navigationCallback) {
        this.navigationCallback = navigationCallback;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(20, 32, 20, 32));

        initUI();
        AppContext.getInstance().addStateChangeListener(this::updateStatus);
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_DARK);

        // ================= 1. HERO SECTION =================
        JPanel heroCard = UITheme.createCardPanel(16, new Color(0x11, 0x1A, 0x2B), new Color(0x15, 0x20, 0x36), UITheme.BORDER_COLOR);
        heroCard.setLayout(new BorderLayout(24, 0));
        heroCard.setBorder(new EmptyBorder(22, 28, 22, 28));
        heroCard.setMaximumSize(new Dimension(1600, 200));

        // Hero Left Text
        JPanel heroLeft = new JPanel();
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));
        heroLeft.setOpaque(false);

        JLabel heroTitle = new JLabel("Lyrics Similarity & Attribution");
        heroTitle.setFont(UITheme.FONT_HERO);
        heroTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel heroSubtitle = new JLabel("Understand the words behind the music.");
        heroSubtitle.setFont(UITheme.FONT_SUBTITLE);
        heroSubtitle.setForeground(UITheme.SECONDARY_CYAN);

        JLabel heroDesc = new JLabel("Compare lyrics, discover textual patterns, and identify artist writing patterns using NLP and machine learning.");
        heroDesc.setFont(UITheme.FONT_BODY);
        heroDesc.setForeground(UITheme.TEXT_SECONDARY);

        // Small Accent Badges
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);

        badgeRow.add(createBadge("TF-IDF", UITheme.SECONDARY_CYAN));
        badgeRow.add(createBadge("Cosine Similarity", UITheme.SECONDARY_CYAN));
        badgeRow.add(createBadge("Machine Learning", UITheme.PRIMARY_PURPLE));
        badgeRow.add(createBadge("Artist Attribution", UITheme.ACCENT_PINK));

        heroLeft.add(heroTitle);
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(heroSubtitle);
        heroLeft.add(Box.createVerticalStrut(8));
        heroLeft.add(heroDesc);
        heroLeft.add(Box.createVerticalStrut(14));
        heroLeft.add(badgeRow);

        // Hero Right Waveform Graphic
        JPanel heroRight = new JPanel(new BorderLayout());
        heroRight.setOpaque(false);
        heroRight.setPreferredSize(new Dimension(280, 130));
        heroRight.add(new UITheme.WaveformVectorCanvas(), BorderLayout.CENTER);

        heroCard.add(heroLeft, BorderLayout.CENTER);
        heroCard.add(heroRight, BorderLayout.EAST);

        scrollContent.add(heroCard);
        scrollContent.add(Box.createVerticalStrut(18));

        // ================= 2. CORE FEATURE CARDS =================
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(1600, 220));

        // Card 1: Lyrics Similarity
        JPanel simCard = UITheme.createCardPanel(14);
        simCard.setLayout(new BoxLayout(simCard, BoxLayout.Y_AXIS));
        simCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel simHeader = new JLabel("♫  LYRICS SIMILARITY");
        simHeader.setFont(UITheme.FONT_CARD_TITLE);
        simHeader.setForeground(UITheme.SECONDARY_CYAN);

        JLabel simText = new JLabel("Compare uploaded lyrics against your reference corpus.");
        simText.setFont(UITheme.FONT_BODY);
        simText.setForeground(UITheme.TEXT_SECONDARY);

        // Visual Pipeline
        JPanel simPipeline = createPipelineBox(new String[]{"Lyrics", "TF-IDF", "Cosine Similarity"}, UITheme.SECONDARY_CYAN);

        JButton simBtn = UITheme.createSecondaryButton("Analyze Similarity →");
        simBtn.addActionListener(e -> navigationCallback.accept("ANALYZE"));

        simCard.add(simHeader);
        simCard.add(Box.createVerticalStrut(6));
        simCard.add(simText);
        simCard.add(Box.createVerticalStrut(12));
        simCard.add(simPipeline);
        simCard.add(Box.createVerticalGlue());
        simCard.add(simBtn);

        // Card 2: Artist Attribution
        JPanel attrCard = UITheme.createCardPanel(14);
        attrCard.setLayout(new BoxLayout(attrCard, BoxLayout.Y_AXIS));
        attrCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel attrHeader = new JLabel("◈  ARTIST ATTRIBUTION");
        attrHeader.setFont(UITheme.FONT_CARD_TITLE);
        attrHeader.setForeground(UITheme.PRIMARY_PURPLE);

        JLabel attrText = new JLabel("Predict the artist whose writing patterns best match the uploaded lyrics.");
        attrText.setFont(UITheme.FONT_BODY);
        attrText.setForeground(UITheme.TEXT_SECONDARY);

        // Visual Pipeline
        JPanel attrPipeline = createPipelineBox(new String[]{"Lyrics", "TF-IDF", "Classifier", "Artist"}, UITheme.PRIMARY_PURPLE);

        JButton attrBtn = UITheme.createPrimaryButton("Run Attribution →");
        attrBtn.addActionListener(e -> navigationCallback.accept("ANALYZE"));

        attrCard.add(attrHeader);
        attrCard.add(Box.createVerticalStrut(6));
        attrCard.add(attrText);
        attrCard.add(Box.createVerticalStrut(12));
        attrCard.add(attrPipeline);
        attrCard.add(Box.createVerticalGlue());
        attrCard.add(attrBtn);

        cardsGrid.add(simCard);
        cardsGrid.add(attrCard);

        scrollContent.add(cardsGrid);
        scrollContent.add(Box.createVerticalStrut(18));

        // ================= 3. SYSTEM STATUS CARD =================
        JPanel statusCard = UITheme.createCardPanel(14);
        statusCard.setLayout(new BorderLayout(16, 12));
        statusCard.setBorder(new EmptyBorder(16, 22, 16, 22));
        statusCard.setMaximumSize(new Dimension(1600, 140));

        JLabel statusHeader = new JLabel("SYSTEM STATUS & REFERENCE CORPUS");
        statusHeader.setFont(UITheme.FONT_SMALL_BOLD);
        statusHeader.setForeground(UITheme.TEXT_MUTED);
        statusCard.add(statusHeader, BorderLayout.NORTH);

        // 3 Compact Stat Cards Grid
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setOpaque(false);

        datasetStatValue = new JLabel("No Dataset Loaded", SwingConstants.CENTER);
        modelStatValue = new JLabel("Untrained", SwingConstants.CENTER);
        vocabStatValue = new JLabel("0 Terms", SwingConstants.CENTER);

        statsRow.add(createCompactStatCard("DATASET", datasetStatValue, UITheme.SECONDARY_CYAN));
        statsRow.add(createCompactStatCard("MODEL", modelStatValue, UITheme.PRIMARY_PURPLE));
        statsRow.add(createCompactStatCard("VOCABULARY", vocabStatValue, UITheme.SUCCESS_GREEN));

        // Action Buttons Row below stats
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionRow.setOpaque(false);

        JButton loadDemoBtn = UITheme.createAccentButton("⚡ Load Demo Dataset", UITheme.SECONDARY_CYAN, new Color(0x08, 0x91, 0xB2));
        loadDemoBtn.setToolTipText("Loads 48 songs across 6 artists for testing");
        loadDemoBtn.addActionListener(e -> {
            try {
                Dataset demo = AppContext.getInstance().loadDemoDataset();
                JOptionPane.showMessageDialog(this,
                        "Demo dataset loaded successfully!\n" +
                                "Total Songs: " + demo.getSongCount() + "\n" +
                                "Total Artists: " + demo.getArtistCount() + "\n\n" +
                                "Proceed to Dataset panel to train model or Analyze panel to test lyrics.",
                        "Demo Dataset Ready",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load demo dataset: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton manageDatasetBtn = UITheme.createSecondaryButton("Load Dataset");
        manageDatasetBtn.addActionListener(e -> navigationCallback.accept("DATASET"));

        trainModelBtn = UITheme.createPrimaryButton("Train Model");
        trainModelBtn.addActionListener(e -> navigationCallback.accept("DATASET"));

        actionRow.add(loadDemoBtn);
        actionRow.add(manageDatasetBtn);
        actionRow.add(trainModelBtn);

        JPanel statusCenter = new JPanel(new BorderLayout(0, 10));
        statusCenter.setOpaque(false);
        statusCenter.add(statsRow, BorderLayout.CENTER);
        statusCenter.add(actionRow, BorderLayout.SOUTH);

        statusCard.add(statusCenter, BorderLayout.CENTER);

        scrollContent.add(statusCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        updateStatus();
    }

    private JPanel createBadge(String text, Color accentColor) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        badge.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
        badge.setBorder(BorderFactory.createLineBorder(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 90), 1, true));

        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL_BOLD);
        lbl.setForeground(accentColor);
        badge.add(lbl);
        return badge;
    }

    private JPanel createPipelineBox(String[] steps, Color accentColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);

        for (int i = 0; i < steps.length; i++) {
            JLabel stepLbl = new JLabel(steps[i]);
            stepLbl.setFont(UITheme.FONT_SMALL_BOLD);
            stepLbl.setForeground(UITheme.TEXT_PRIMARY);
            stepLbl.setBackground(UITheme.BG_SECONDARY);
            stepLbl.setOpaque(true);
            stepLbl.setBorder(new EmptyBorder(3, 7, 3, 7));
            p.add(stepLbl);

            if (i < steps.length - 1) {
                JLabel arrow = new JLabel("→");
                arrow.setFont(UITheme.FONT_SMALL);
                arrow.setForeground(accentColor);
                p.add(arrow);
            }
        }
        return p;
    }

    private JPanel createCompactStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_SECONDARY);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(UITheme.TEXT_MUTED);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(3));
        card.add(valueLabel);
        return card;
    }

    private void updateStatus() {
        AppContext ctx = AppContext.getInstance();
        if (ctx.hasDataset()) {
            Dataset d = ctx.getDataset();
            datasetStatValue.setText(String.format("%d Songs • %d Artists", d.getSongCount(), d.getArtistCount()));
            datasetStatValue.setForeground(UITheme.SUCCESS_GREEN);

            if (ctx.isModelTrained()) {
                modelStatValue.setText("Trained (" + ctx.getClassifier().getClassifierName().split(" ")[0] + ")");
                modelStatValue.setForeground(UITheme.SUCCESS_GREEN);
                vocabStatValue.setText(String.format("%,d Terms", ctx.getVectorizer().getVocabularySize()));
                vocabStatValue.setForeground(UITheme.SUCCESS_GREEN);
            } else {
                modelStatValue.setText("Needs Training");
                modelStatValue.setForeground(UITheme.WARNING_AMBER);
                vocabStatValue.setText("Vocab Pending");
                vocabStatValue.setForeground(UITheme.WARNING_AMBER);
            }
        } else {
            datasetStatValue.setText("No Dataset Loaded");
            datasetStatValue.setForeground(UITheme.WARNING_AMBER);
            modelStatValue.setText("Untrained");
            modelStatValue.setForeground(UITheme.DANGER_RED);
            vocabStatValue.setText("0 Terms");
            vocabStatValue.setForeground(UITheme.TEXT_MUTED);
        }
    }
}

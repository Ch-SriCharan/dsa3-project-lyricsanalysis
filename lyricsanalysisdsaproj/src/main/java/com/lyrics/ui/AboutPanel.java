package com.lyrics.ui;

import com.lyrics.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern About Screen explaining LyriSense's architecture, NLP modules,
 * core machine learning mechanics, and project student contributors.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(20, 32, 20, 32));

        initUI();
    }

    private void initUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UITheme.BG_DARK);

        // Header Card
        JPanel headerCard = UITheme.createCardPanel(16, UITheme.BG_PANEL, UITheme.BG_SECONDARY, UITheme.BORDER_COLOR);
        headerCard.setLayout(new BorderLayout(15, 6));
        headerCard.setBorder(new EmptyBorder(20, 26, 20, 26));
        headerCard.setMaximumSize(new Dimension(1600, 130));

        JLabel titleLbl = new JLabel("About LyriSense");
        titleLbl.setFont(UITheme.FONT_HERO);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("LyriSense analyzes song lyrics using Natural Language Processing and Machine Learning.");
        subLbl.setFont(UITheme.FONT_SUBTITLE);
        subLbl.setForeground(UITheme.SECONDARY_CYAN);

        headerCard.add(titleLbl, BorderLayout.NORTH);
        headerCard.add(subLbl, BorderLayout.CENTER);

        scrollContent.add(headerCard);
        scrollContent.add(Box.createVerticalStrut(18));

        // Two Column Grid
        JPanel grid = new JPanel(new GridLayout(1, 2, 18, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(1600, 290));

        // Left: Core Modules & Technologies
        JPanel techCard = UITheme.createCardPanel(14);
        techCard.setLayout(new BoxLayout(techCard, BoxLayout.Y_AXIS));
        techCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel techTitle = new JLabel("CORE TECHNOLOGIES & MODULES");
        techTitle.setFont(UITheme.FONT_CARD_TITLE);
        techTitle.setForeground(UITheme.PRIMARY_PURPLE);

        JLabel techContent = new JLabel("<html><body style='width: 360px; color: #94A3B8; font-size: 11px;'>"
                + "<b>1. Core Technologies:</b><br>"
                + "• <b>TF-IDF (Term Frequency-Inverse Document Frequency):</b> Sparse numerical feature extraction with smooth IDF weighting.<br>"
                + "• <b>Cosine Similarity:</b> Vector dot-product distance calculation over L2-normalized unit spheres.<br>"
                + "• <b>Text Classification:</b> Supervised multi-class machine learning.<br>"
                + "• <b>Machine Learning:</b> Multinomial Naive Bayes & Softmax Logistic Regression.<br><br>"
                + "<b>2. Project Modules:</b><br>"
                + "• <b>Lyrics Similarity:</b> Top-5 ranking against reference corpus.<br>"
                + "• <b>Artist Attribution:</b> Stylometric author classification with posterior confidence."
                + "</body></html>");

        techCard.add(techTitle);
        techCard.add(Box.createVerticalStrut(10));
        techCard.add(techContent);

        // Right: Mathematical Foundations & Pipeline
        JPanel mathCard = UITheme.createCardPanel(14);
        mathCard.setLayout(new BoxLayout(mathCard, BoxLayout.Y_AXIS));
        mathCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel mathTitle = new JLabel("MATHEMATICAL FORMULATIONS");
        mathTitle.setFont(UITheme.FONT_CARD_TITLE);
        mathTitle.setForeground(UITheme.SECONDARY_CYAN);

        JLabel mathContent = new JLabel("<html><body style='width: 360px; color: #94A3B8; font-size: 11px;'>"
                + "<b>TF-IDF Weighting:</b><br>"
                + "<code>IDF(t) = ln((1 + N) / (1 + DF(t))) + 1.0</code><br><br>"
                + "<b>Cosine Similarity:</b><br>"
                + "<code>CosineSimilarity(A, B) = (A · B) / (||A|| × ||B||)</code><br><br>"
                + "<b>Multinomial Naive Bayes:</b><br>"
                + "<code>P(w_j | C_k) = (count(w_j, C_k) + α) / (total_tokens + α·V)</code><br><br>"
                + "<b>Evaluation:</b> Stratified train/test split with zero data leakage."
                + "</body></html>");

        mathCard.add(mathTitle);
        mathCard.add(Box.createVerticalStrut(10));
        mathCard.add(mathContent);

        grid.add(techCard);
        grid.add(mathCard);

        scrollContent.add(grid);
        scrollContent.add(Box.createVerticalStrut(18));

        // Bottom Row: Audio Extension & Student Credits
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(1600, 130));

        // Audio Extension
        JPanel audioCard = UITheme.createCardPanel(14);
        audioCard.setLayout(new BoxLayout(audioCard, BoxLayout.Y_AXIS));
        audioCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel audioTitle = new JLabel("FUTURE SCOPE: AUDIO / MP3 SUPPORT");
        audioTitle.setFont(UITheme.FONT_CARD_TITLE);
        audioTitle.setForeground(UITheme.ACCENT_PINK);

        JLabel audioDesc = new JLabel("<html><body style='width: 360px; color: #94A3B8; font-size: 11px;'>"
                + "MP3/audio can be supported in future releases by adding an <b>Automatic Speech Recognition (ASR)</b> "
                + "transcription stage to convert vocal audio tracks into text lyrics prior to NLP analysis."
                + "</body></html>");

        audioCard.add(audioTitle);
        audioCard.add(Box.createVerticalStrut(6));
        audioCard.add(audioDesc);

        // Student Credits Card
        JPanel studentCard = UITheme.createCardPanel(14, UITheme.BG_SECONDARY, UITheme.BG_PANEL, UITheme.BORDER_COLOR);
        studentCard.setLayout(new BoxLayout(studentCard, BoxLayout.Y_AXIS));
        studentCard.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel studentTitle = new JLabel("STUDENTS");
        studentTitle.setFont(UITheme.FONT_CARD_TITLE);
        studentTitle.setForeground(UITheme.SUCCESS_GREEN);

        JLabel s1 = new JLabel("• Sricharan — 2520090019");
        s1.setFont(UITheme.FONT_BODY_BOLD);
        s1.setForeground(UITheme.TEXT_PRIMARY);

        JLabel s2 = new JLabel("• Ganesh — 2520090052");
        s2.setFont(UITheme.FONT_BODY_BOLD);
        s2.setForeground(UITheme.TEXT_PRIMARY);

        studentCard.add(studentTitle);
        studentCard.add(Box.createVerticalStrut(8));
        studentCard.add(s1);
        studentCard.add(Box.createVerticalStrut(4));
        studentCard.add(s2);

        bottomRow.add(audioCard);
        bottomRow.add(studentCard);

        scrollContent.add(bottomRow);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }
}

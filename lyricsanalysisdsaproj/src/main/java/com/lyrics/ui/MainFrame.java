package com.lyrics.ui;

import com.lyrics.model.AppContext;
import com.lyrics.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern Spotify/AI-style desktop shell with sleek sidebar navigation
 * and real-time system status indicators.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final Map<String, JButton> navButtons;

    private JLabel datasetDotBadge;
    private JLabel datasetStatusText;
    private JLabel modelDotBadge;
    private JLabel modelStatusText;

    public MainFrame() {
        super("LyriSense — Lyrics Similarity & Artist Attribution");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);

        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        this.navButtons = new HashMap<>();

        initUI();
        AppContext.getInstance().addStateChangeListener(this::updateStatusIndicators);
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_DARK);

        // 1. Sidebar Navigation (Left)
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Main Center Area (Top Header + Multi-Screen Content Panel)
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.BG_DARK);

        // Top Header
        JPanel headerBar = createTopHeader();
        centerContainer.add(headerBar, BorderLayout.NORTH);

        // Screen Panels in CardLayout
        contentPanel.setOpaque(false);
        contentPanel.add(new HomePanel(this::navigateTo), "HOME");
        contentPanel.add(new AnalyzePanel(), "ANALYZE");
        contentPanel.add(new DatasetPanel(this::navigateTo), "DATASET");
        contentPanel.add(new EvaluationPanel(this::navigateTo), "EVALUATION");
        contentPanel.add(new AboutPanel(), "ABOUT");

        centerContainer.add(contentPanel, BorderLayout.CENTER);
        rootPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
        navigateTo("HOME");
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(UITheme.BG_SIDEBAR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(12, 28, 12, 28)
        ));

        // Left Branding
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBrand.setOpaque(false);

        JLabel brandTitle = new JLabel("LYRISENSE");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel pipeLabel = new JLabel("•");
        pipeLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel brandSubtitle = new JLabel("Lyrics Intelligence • NLP + Machine Learning");
        brandSubtitle.setFont(UITheme.FONT_SMALL);
        brandSubtitle.setForeground(UITheme.TEXT_SECONDARY);

        leftBrand.add(brandTitle);
        leftBrand.add(pipeLabel);
        leftBrand.add(brandSubtitle);

        // Right Status Indicators
        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightStatus.setOpaque(false);

        // Dataset indicator
        JPanel dsBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dsBlock.setOpaque(false);
        datasetDotBadge = new JLabel("●");
        datasetDotBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        datasetDotBadge.setForeground(UITheme.WARNING_AMBER);
        datasetStatusText = new JLabel("No Dataset");
        datasetStatusText.setFont(UITheme.FONT_SMALL_BOLD);
        datasetStatusText.setForeground(UITheme.TEXT_SECONDARY);
        dsBlock.add(datasetDotBadge);
        dsBlock.add(datasetStatusText);

        JLabel divider = new JLabel("|");
        divider.setForeground(UITheme.BORDER_COLOR);

        // Model indicator
        JPanel mlBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        mlBlock.setOpaque(false);
        modelDotBadge = new JLabel("●");
        modelDotBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modelDotBadge.setForeground(UITheme.DANGER_RED);
        modelStatusText = new JLabel("Model Untrained");
        modelStatusText.setFont(UITheme.FONT_SMALL_BOLD);
        modelStatusText.setForeground(UITheme.TEXT_SECONDARY);
        mlBlock.add(modelDotBadge);
        mlBlock.add(modelStatusText);

        rightStatus.add(dsBlock);
        rightStatus.add(divider);
        rightStatus.add(mlBlock);

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightStatus, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        // Brand Logo Section
        JPanel logoSection = new JPanel();
        logoSection.setLayout(new BoxLayout(logoSection, BoxLayout.Y_AXIS));
        logoSection.setOpaque(false);
        logoSection.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel logoTitle = new JLabel("♫ LyriSense");
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoTitle.setForeground(UITheme.PRIMARY_PURPLE);

        JLabel logoSubtitle = new JLabel("NLP + ML Lyrics Intelligence");
        logoSubtitle.setFont(UITheme.FONT_SMALL);
        logoSubtitle.setForeground(UITheme.TEXT_MUTED);

        logoSection.add(logoTitle);
        logoSection.add(Box.createVerticalStrut(3));
        logoSection.add(logoSubtitle);
        sidebar.add(logoSection);

        sidebar.add(Box.createVerticalStrut(10));

        // Navigation Items
        sidebar.add(createNavButton("HOME", "⌂  Home"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("ANALYZE", "♫  Analyze"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("DATASET", "▣  Dataset"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("EVALUATION", "◈  Evaluation"));
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(createNavButton("ABOUT", "ⓘ  About"));

        sidebar.add(Box.createVerticalGlue());

        // Sidebar Footer
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 20, 20, 20));

        JLabel f1 = new JLabel("Java 17+");
        f1.setFont(UITheme.FONT_SMALL_BOLD);
        f1.setForeground(UITheme.TEXT_SECONDARY);

        JLabel f2 = new JLabel("Desktop NLP Application");
        f2.setFont(UITheme.FONT_SMALL);
        f2.setForeground(UITheme.TEXT_MUTED);

        footer.add(f1);
        footer.add(Box.createVerticalStrut(2));
        footer.add(f2);

        sidebar.add(footer);
        return sidebar;
    }

    private JButton createNavButton(String pageKey, String label) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getForeground().equals(UITheme.PRIMARY_PURPLE) && !btn.getForeground().equals(Color.WHITE)) {
                    btn.setBackground(UITheme.BG_PANEL);
                    btn.setForeground(UITheme.TEXT_PRIMARY);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!navButtons.get(pageKey).equals(getActiveButton())) {
                    btn.setBackground(UITheme.BG_SIDEBAR);
                    btn.setForeground(UITheme.TEXT_SECONDARY);
                }
            }
        });

        btn.addActionListener(e -> navigateTo(pageKey));
        navButtons.put(pageKey, btn);
        return btn;
    }

    private String currentPageKey = "HOME";

    private JButton getActiveButton() {
        return navButtons.get(currentPageKey);
    }

    public void navigateTo(String pageKey) {
        this.currentPageKey = pageKey;
        cardLayout.show(contentPanel, pageKey);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            if (entry.getKey().equals(pageKey)) {
                b.setBackground(new Color(0x1F, 0x18, 0x36)); // Deep purple accent tint
                b.setForeground(UITheme.TEXT_PRIMARY);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, UITheme.PRIMARY_PURPLE),
                        new EmptyBorder(8, 13, 8, 16)
                ));
            } else {
                b.setBackground(UITheme.BG_SIDEBAR);
                b.setForeground(UITheme.TEXT_SECONDARY);
                b.setBorder(new EmptyBorder(8, 16, 8, 16));
            }
        }
    }

    private void updateStatusIndicators() {
        AppContext ctx = AppContext.getInstance();
        if (ctx.hasDataset()) {
            datasetDotBadge.setForeground(UITheme.SUCCESS_GREEN);
            datasetStatusText.setText("Dataset Ready (" + ctx.getDataset().getSongCount() + ")");
            datasetStatusText.setForeground(UITheme.SUCCESS_GREEN);
        } else {
            datasetDotBadge.setForeground(UITheme.WARNING_AMBER);
            datasetStatusText.setText("No Dataset");
            datasetStatusText.setForeground(UITheme.WARNING_AMBER);
        }

        if (ctx.isModelTrained()) {
            modelDotBadge.setForeground(UITheme.SUCCESS_GREEN);
            modelStatusText.setText("Model Trained");
            modelStatusText.setForeground(UITheme.SUCCESS_GREEN);
        } else if (ctx.hasDataset()) {
            modelDotBadge.setForeground(UITheme.WARNING_AMBER);
            modelStatusText.setText("Needs Training");
            modelStatusText.setForeground(UITheme.WARNING_AMBER);
        } else {
            modelDotBadge.setForeground(UITheme.DANGER_RED);
            modelStatusText.setText("No Model");
            modelStatusText.setForeground(UITheme.DANGER_RED);
        }
    }
}

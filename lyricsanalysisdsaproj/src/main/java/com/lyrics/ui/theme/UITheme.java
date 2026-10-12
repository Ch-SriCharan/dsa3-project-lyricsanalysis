package com.lyrics.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.*;

/**
 * Global design system, Spotify-inspired AI/music dark color tokens,
 * typography, and custom graphical components.
 */
public class UITheme {

    // Core Theme Palette
    public static final Color BG_DARK = new Color(0x08, 0x0D, 0x1A);         // #080D1A
    public static final Color BG_SIDEBAR = new Color(0x0B, 0x11, 0x20);      // #0B1120
    public static final Color BG_PANEL = new Color(0x11, 0x1A, 0x2B);        // #111A2B
    public static final Color BG_PANEL_HOVER = new Color(0x16, 0x22, 0x38);  // #162238
    public static final Color BG_SECONDARY = new Color(0x17, 0x22, 0x38);    // #172238
    public static final Color BG_INPUT = new Color(0x0E, 0x15, 0x24);        // #0E1524
    public static final Color BORDER_COLOR = new Color(0x26, 0x35, 0x50);    // #263550
    public static final Color BORDER_LIGHT = new Color(0x3B, 0x4D, 0x6E);    // #3B4D6E

    // Accent Colors
    public static final Color PRIMARY_PURPLE = new Color(0x8B, 0x5C, 0xF6);  // #8B5CF6
    public static final Color PRIMARY_PURPLE_DARK = new Color(0x7C, 0x3A, 0xED); // #7C3AED
    public static final Color SECONDARY_CYAN = new Color(0x06, 0xB6, 0xD4);  // #06B6D4
    public static final Color ACCENT_PINK = new Color(0xEC, 0x48, 0x99);     // #EC4899
    public static final Color SUCCESS_GREEN = new Color(0x22, 0xC5, 0x5E);   // #22C55E
    public static final Color WARNING_AMBER = new Color(0xF5, 0x9E, 0x0B);   // #F59E0B
    public static final Color DANGER_RED = new Color(0xEF, 0x44, 0x44);       // #EF4444

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);    // #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);  // #94A3B8
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);      // #64748B

    // Typography
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 30);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Creates a rounded card panel with gradient background and subtle border.
     */
    public static JPanel createCardPanel(int cornerRadius) {
        return new GlassCardPanel(cornerRadius, BG_PANEL, BG_SECONDARY, BORDER_COLOR);
    }

    public static JPanel createCardPanel(int cornerRadius, Color bgTop, Color bgBottom, Color borderColor) {
        return new GlassCardPanel(cornerRadius, bgTop, bgBottom, borderColor);
    }

    public static JButton createPrimaryButton(String text) {
        return new ModernButton(text, PRIMARY_PURPLE, PRIMARY_PURPLE_DARK, Color.WHITE, true);
    }

    public static JButton createSecondaryButton(String text) {
        return new ModernButton(text, BG_SECONDARY, BG_PANEL_HOVER, TEXT_PRIMARY, false, BORDER_COLOR);
    }

    public static JButton createAccentButton(String text, Color baseColor, Color hoverColor) {
        return new ModernButton(text, baseColor, hoverColor, Color.WHITE, true);
    }

    /**
     * Modern Glassmorphism Card Panel with gradient and subtle glow.
     */
    public static class GlassCardPanel extends JPanel {
        private final int cornerRadius;
        private final Color bgTop;
        private final Color bgBottom;
        private final Color borderColor;

        public GlassCardPanel(int radius, Color bgTop, Color bgBottom, Color borderColor) {
            this.cornerRadius = radius;
            this.bgTop = bgTop;
            this.bgBottom = bgBottom;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Gradient Background
            GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, h, bgBottom);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));

            // Subtle Border
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));
            }

            g2.dispose();
        }
    }

    /**
     * Custom Modern Button with gradient highlights and hover animations.
     */
    public static class ModernButton extends JButton {
        private final Color normalColor;
        private final Color hoverColor;
        private final Color textColor;
        private final Color borderColor;
        private final boolean isGradient;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Color normalColor, Color hoverColor, Color textColor, boolean isGradient) {
            this(text, normalColor, hoverColor, textColor, isGradient, null);
        }

        public ModernButton(String text, Color normalColor, Color hoverColor, Color textColor, boolean isGradient, Color borderColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.textColor = textColor;
            this.isGradient = isGradient;
            this.borderColor = borderColor;

            setFont(FONT_BODY_BOLD);
            setForeground(textColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 18, 8, 18));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (!isEnabled()) {
                g2.setColor(new Color(0x1F, 0x29, 0x3D));
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 10, 10));
            } else if (isGradient) {
                Color c1 = isPressed ? hoverColor.darker() : (isHovered ? hoverColor : normalColor);
                Color c2 = isPressed ? normalColor.darker() : (isHovered ? normalColor : hoverColor);
                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 10, 10));
            } else {
                Color c = isPressed ? normalColor.darker() : (isHovered ? hoverColor : normalColor);
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 10, 10));
            }

            if (borderColor != null && isEnabled()) {
                g2.setColor(isHovered ? BORDER_LIGHT : borderColor);
                g2.draw(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Circular gauge component for prominent similarity percentage display.
     */
    public static class CircularGaugePanel extends JPanel {
        private double percentage = 0.0;
        private final Color arcColor;

        public CircularGaugePanel(Color arcColor) {
            this.arcColor = arcColor;
            setOpaque(false);
            setPreferredSize(new Dimension(140, 140));
            setMinimumSize(new Dimension(120, 120));
        }

        public void setPercentage(double percentage) {
            this.percentage = Math.max(0.0, Math.min(100.0, percentage));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 16;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            int strokeWidth = 10;

            // Background Track Circle
            g2.setColor(new Color(0x17, 0x22, 0x38));
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x + strokeWidth / 2, y + strokeWidth / 2, size - strokeWidth, size - strokeWidth);

            // Active Progress Arc
            if (percentage > 0) {
                g2.setColor(arcColor);
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int angle = (int) Math.round((percentage / 100.0) * 360.0);
                g2.drawArc(x + strokeWidth / 2, y + strokeWidth / 2, size - strokeWidth, size - strokeWidth, 90, -angle);
            }

            // Central Percentage Text
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.setColor(TEXT_PRIMARY);
            String text = String.format("%.1f%%", percentage);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(text)) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 4;
            g2.drawString(text, tx, ty);

            // Subtitle text inside circle
            g2.setFont(FONT_SMALL);
            g2.setColor(TEXT_MUTED);
            String sub = "MATCH";
            FontMetrics fms = g2.getFontMetrics();
            int sx = (getWidth() - fms.stringWidth(sub)) / 2;
            int sy = ty + 16;
            g2.drawString(sub, sx, sy);

            g2.dispose();
        }
    }

    /**
     * Waveform & glowing vector graph decorative visual for Home hero section.
     */
    public static class WaveformVectorCanvas extends JPanel {
        public WaveformVectorCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(280, 130));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int centerY = h / 2;

            // Draw audio frequency bars (Spotify-inspired visualizer)
            int numBars = 24;
            int barWidth = 4;
            int gap = (w - (numBars * barWidth)) / (numBars + 1);

            double[] heights = {
                    0.25, 0.40, 0.65, 0.85, 0.50, 0.90, 0.70, 0.35,
                    0.80, 0.95, 0.60, 0.45, 0.75, 0.88, 0.55, 0.30,
                    0.70, 0.85, 0.92, 0.60, 0.40, 0.75, 0.50, 0.30
            };

            for (int i = 0; i < numBars; i++) {
                int bx = gap + i * (barWidth + gap);
                int bh = (int) (heights[i % heights.length] * (h - 30));
                int by = centerY - bh / 2;

                Color c = (i % 2 == 0) ? PRIMARY_PURPLE : SECONDARY_CYAN;
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
                g2.fill(new RoundRectangle2D.Double(bx, by, barWidth, bh, 3, 3));
            }

            // Draw subtle vector connection lines over top
            g2.setColor(new Color(PRIMARY_PURPLE.getRed(), PRIMARY_PURPLE.getGreen(), PRIMARY_PURPLE.getBlue(), 120));
            g2.setStroke(new BasicStroke(1.5f));
            int[][] nodes = {
                    {gap + 2 * (barWidth + gap), centerY - 25},
                    {gap + 8 * (barWidth + gap), centerY + 30},
                    {gap + 13 * (barWidth + gap), centerY - 35},
                    {gap + 18 * (barWidth + gap), centerY + 20},
                    {gap + 22 * (barWidth + gap), centerY - 15}
            };

            for (int i = 0; i < nodes.length - 1; i++) {
                g2.drawLine(nodes[i][0], nodes[i][1], nodes[i + 1][0], nodes[i + 1][1]);
            }

            // Draw glowing node circles
            for (int[] node : nodes) {
                g2.setColor(new Color(SECONDARY_CYAN.getRed(), SECONDARY_CYAN.getGreen(), SECONDARY_CYAN.getBlue(), 220));
                g2.fillOval(node[0] - 4, node[1] - 4, 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(node[0] - 2, node[1] - 2, 4, 4);
            }

            g2.dispose();
        }
    }

    /**
     * Table Cell Renderer for progress percentage bars.
     */
    public static class PercentageBarRenderer extends DefaultTableCellRenderer {
        private final Color barColor;

        public PercentageBarRenderer(Color barColor) {
            this.barColor = barColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            double percent = 0.0;
            String text = "0.0%";
            if (value instanceof Number) {
                percent = ((Number) value).doubleValue();
                text = String.format("%.1f%%", percent);
            } else if (value != null) {
                text = value.toString();
                try {
                    String clean = text.replace("%", "").trim();
                    percent = Double.parseDouble(clean);
                } catch (Exception ignored) {}
            }

            final double finalPercent = Math.max(0.0, Math.min(100.0, percent));
            final String displayText = text;

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = getWidth() - 10;
                    int height = getHeight() - 10;
                    int barWidth = (int) Math.round((finalPercent / 100.0) * width);

                    // Track background
                    g2.setColor(new Color(0x0E, 0x15, 0x24));
                    g2.fill(new RoundRectangle2D.Double(5, 5, width, height, 6, 6));

                    // Filled Bar with gradient
                    if (barWidth > 0) {
                        GradientPaint gp = new GradientPaint(5, 5, barColor, 5 + barWidth, 5, barColor.brighter());
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Double(5, 5, barWidth, height, 6, 6));
                    }

                    // Centered Text
                    g2.setFont(FONT_SMALL_BOLD);
                    g2.setColor(TEXT_PRIMARY);
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(displayText);
                    int textX = (getWidth() - textWidth) / 2;
                    int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(displayText, textX, textY);

                    g2.dispose();
                }
            };
            panel.setOpaque(true);
            panel.setBackground(isSelected ? BG_PANEL_HOVER : ((row % 2 == 0) ? BG_PANEL : BG_SECONDARY));
            return panel;
        }
    }
}

package com.lyrics;

import com.formdev.flatlaf.FlatDarkLaf;
import com.lyrics.model.AppContext;
import com.lyrics.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Entry Point for LyriSense Desktop.
 *
 * Lyrics Similarity and Attribution (NLP + Machine Learning)
 */
public class Main {
    public static void main(String[] args) {
        // Configure high-DPI and smooth font rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize Modern FlatLaf Dark Look and Feel
        try {
            FlatDarkLaf.setup();
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            System.err.println("FlatLaf could not be initialized. Falling back to default L&F: " + e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Launch Application GUI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);

            // Optional auto-load demo dataset for smooth presentation/evaluation
            try {
                AppContext.getInstance().loadDemoDataset();
            } catch (Exception ignored) {
                // User can still upload dataset manually
            }
        });
    }
}

package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Metric Card - Card hiển thị số liệu tổng quan (Dashboard)
 * Tương đương với div metric card trong web
 */
public class MetricCard extends JPanel {

    private final String icon;
    private final String label;
    private String value;
    private String badge;
    private final Color accentColor;

    public MetricCard(String icon, String label, String value, String badge, Color accentColor) {
        this.icon = icon;
        this.label = label;
        this.value = value;
        this.badge = badge;
        this.accentColor = accentColor;

        setOpaque(false);
        setPreferredSize(new Dimension(0, 120));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int arc = AppTheme.CARD_ARC;

        // Background
        g2.setColor(AppTheme.SURFACE_HIGH);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Border
        g2.setColor(AppTheme.OUTLINE_VARIANT);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Accent glow (top right)
        Color glow = AppTheme.withAlpha(accentColor, 20);
        g2.setColor(glow);
        g2.fillOval(w - 60, -20, 80, 80);

        // Icon badge (top left)
        g2.setColor(AppTheme.withAlpha(accentColor, 30));
        g2.fillRoundRect(16, 16, 36, 36, 8, 8);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        g2.setColor(accentColor);
        FontMetrics fm = g2.getFontMetrics();
        int iconX = 16 + (36 - fm.stringWidth(icon)) / 2;
        int iconY = 16 + (36 + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(icon, iconX, iconY);

        // Badge (top right)
        if (badge != null && !badge.isEmpty()) {
            g2.setFont(AppTheme.FONT_LABEL);
            fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 12;
            int bh = 20;
            int bx = w - bw - 12;
            int by = 12;
            g2.setColor(AppTheme.withAlpha(AppTheme.TERTIARY, 30));
            g2.fillRoundRect(bx, by, bw, bh, 10, 10);
            g2.setColor(AppTheme.TERTIARY);
            g2.drawString(badge, bx + 6, by + fm.getAscent() + 2);
        }

        // Label (metric name)
        g2.setFont(AppTheme.FONT_LABEL);
        g2.setColor(AppTheme.ON_SURFACE_VAR);
        g2.drawString(label.toUpperCase(), 16, 68);

        // Value (big number)
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(accentColor);
        g2.drawString(value, 16, 100);

        g2.dispose();
    }

    public void setValue(String value) {
        this.value = value;
        repaint();
    }

    public void setBadge(String badge) {
        this.badge = badge;
        repaint();
    }
}

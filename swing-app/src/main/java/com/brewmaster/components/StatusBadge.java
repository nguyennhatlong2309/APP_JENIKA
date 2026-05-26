package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Badge/chip hiển thị trạng thái (Paid, Pending, Cancelled...)
 * Custom-painted pill shape
 */
public class StatusBadge extends JLabel {

    public StatusBadge(String text, Color bg, Color fg) {
        super(text);
        setFont(AppTheme.FONT_LABEL);
        setForeground(fg);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.CENTER);

        // Store colors as client properties for painting
        putClientProperty("badge.bg", bg);
        putClientProperty("badge.fg", fg);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = (Color) getClientProperty("badge.bg");
        if (bg != null) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Insets getInsets() {
        return new Insets(3, 10, 3, 10);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += 20;
        d.height = 22;
        return d;
    }

    /** Factory method tạo badge theo trạng thái hóa đơn */
    public static StatusBadge forSalesStatus(String status) {
        Color[] colors = AppTheme.getSalesStatusColor(status);
        return new StatusBadge(status, colors[0], colors[1]);
    }

    /** Factory method tạo badge theo trạng thái kho */
    public static StatusBadge forStockStatus(String status) {
        Color[] colors = AppTheme.getStockStatusColor(status);
        return new StatusBadge(status, colors[0], colors[1]);
    }

    /** Factory method cho Thu/Chi */
    public static StatusBadge forTransaction(String loai) {
        Color[] colors = AppTheme.getTransactionColor(loai);
        return new StatusBadge(loai.toUpperCase(), colors[0], colors[1]);
    }
}

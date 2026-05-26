package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Thanh phân trang (Pagination) cho các bảng dữ liệu
 */
public class Pagination extends JPanel {

    private int currentPage = 1;
    private int totalPages  = 1;
    private int totalItems  = 0;
    private int pageSize    = 10;

    private JLabel infoLabel;
    private JPanel btnPanel;
    private Consumer<Integer> pageChangeCallback;

    public Pagination(Consumer<Integer> onPageChange) {
        this.pageChangeCallback = onPageChange;
        setBackground(AppTheme.SURFACE_HIGH);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT),
            new EmptyBorder(8, 16, 8, 16)
        ));
        setLayout(new BorderLayout(0, 0));

        infoLabel = new JLabel("Chưa có dữ liệu");
        infoLabel.setFont(AppTheme.FONT_BODY_SM);
        infoLabel.setForeground(AppTheme.ON_SURFACE_VAR);

        btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        btnPanel.setOpaque(false);

        add(infoLabel, BorderLayout.WEST);
        add(btnPanel, BorderLayout.EAST);
    }

    public void update(int total, int pageSize, int currentPage) {
        this.totalItems  = total;
        this.pageSize    = pageSize;
        this.currentPage = currentPage;
        this.totalPages  = Math.max(1, (int) Math.ceil((double) total / pageSize));

        // Update info text
        if (total == 0) {
            infoLabel.setText("Không có dữ liệu");
        } else {
            int from = (currentPage - 1) * pageSize + 1;
            int to   = Math.min(currentPage * pageSize, total);
            infoLabel.setText(String.format("Hiển thị %d-%d của %d mục", from, to, total));
        }

        // Rebuild buttons
        btnPanel.removeAll();

        // Prev button
        JButton prev = makePageBtn("‹", false);
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> {
            if (pageChangeCallback != null)
                pageChangeCallback.accept(this.currentPage - 1);
        });
        btnPanel.add(prev);

        // Page number buttons (show up to 5 around current page)
        int start = Math.max(1, currentPage - 2);
        int end   = Math.min(totalPages, start + 4);
        // Adjust start if end was capped
        start = Math.max(1, end - 4);

        if (start > 1) {
            JButton firstBtn = makePageBtn("1", false);
            firstBtn.addActionListener(e -> {
                if (pageChangeCallback != null) pageChangeCallback.accept(1);
            });
            btnPanel.add(firstBtn);
            if (start > 2) btnPanel.add(makeEllipsis());
        }

        for (int p = start; p <= end; p++) {
            final int page = p;
            JButton btn = makePageBtn(String.valueOf(p), p == currentPage);
            btn.addActionListener(e -> {
                if (pageChangeCallback != null) pageChangeCallback.accept(page);
            });
            btnPanel.add(btn);
        }

        if (end < totalPages) {
            if (end < totalPages - 1) btnPanel.add(makeEllipsis());
            JButton lastBtn = makePageBtn(String.valueOf(totalPages), false);
            lastBtn.addActionListener(e -> {
                if (pageChangeCallback != null) pageChangeCallback.accept(totalPages);
            });
            btnPanel.add(lastBtn);
        }

        // Next button
        JButton next = makePageBtn("›", false);
        next.setEnabled(currentPage < totalPages);
        next.addActionListener(e -> {
            if (pageChangeCallback != null)
                pageChangeCallback.accept(this.currentPage + 1);
        });
        btnPanel.add(next);

        btnPanel.revalidate();
        btnPanel.repaint();
        revalidate();
        repaint();
    }

    private JButton makePageBtn(String text, boolean active) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(AppTheme.PRIMARY_CONTAINER);
                } else if (getModel().isRollover()) {
                    g2.setColor(AppTheme.SURFACE_VARIANT);
                } else {
                    g2.setColor(AppTheme.SURFACE_HIGH);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                if (!active) {
                    g2.setColor(AppTheme.OUTLINE_VARIANT);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_LABEL);
        btn.setForeground(active ? AppTheme.ON_PRIMARY : AppTheme.ON_SURFACE_VAR);
        // Use padding margin so button auto-sizes to fit any number of digits
        btn.setMargin(new Insets(2, 8, 2, 8));
        btn.setMinimumSize(new Dimension(28, 28));
        btn.setPreferredSize(null); // let layout manager size it
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makeEllipsis() {
        JLabel lbl = new JLabel("...");
        lbl.setFont(AppTheme.FONT_BODY_SM);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        return lbl;
    }
}

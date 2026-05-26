package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * TopBar header - tương đương với header của web
 */
public class TopBar extends JPanel {

    private JLabel titleLabel;
    private JTextField searchField;

    public TopBar(String title) {
        setPreferredSize(new Dimension(0, AppTheme.TOPBAR_HEIGHT));
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
            new EmptyBorder(0, 24, 0, 24)
        ));
        setLayout(new BorderLayout(16, 0));

        // === LEFT: Title + Search ===
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        titleLabel = new JLabel("BrewMaster Pro");
        titleLabel.setFont(AppTheme.FONT_TITLE_SM);
        titleLabel.setForeground(AppTheme.ON_SURFACE);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 32));
        searchField.setFont(AppTheme.FONT_BODY_MD);
        searchField.setForeground(AppTheme.ON_SURFACE);
        searchField.setCaretColor(AppTheme.PRIMARY);
        searchField.putClientProperty("JTextField.placeholderText", "🔍  Tìm kiếm...");

        left.add(titleLabel);
        left.add(searchField);

        // === RIGHT: Notifications + User ===
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Notification bell
        JButton notifBtn = new JButton("🔔");
        notifBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        notifBtn.setForeground(AppTheme.ON_SURFACE_VAR);
        notifBtn.setOpaque(false);
        notifBtn.setContentAreaFilled(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setFocusPainted(false);
        notifBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Divider
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(AppTheme.OUTLINE_VARIANT);

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        userPanel.setOpaque(false);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        textPanel.setOpaque(false);
        JLabel userName = new JLabel("Nguyễn Văn An");
        userName.setFont(AppTheme.FONT_LABEL);
        userName.setForeground(AppTheme.ON_SURFACE);
        userName.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel userRole = new JLabel("Quản lý");
        userRole.setFont(AppTheme.FONT_LABEL);
        userRole.setForeground(AppTheme.PRIMARY);
        userRole.setHorizontalAlignment(SwingConstants.RIGHT);
        textPanel.add(userName);
        textPanel.add(userRole);

        // Avatar circle
        JLabel avatar = new JLabel("NVA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_CONTAINER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(AppTheme.OUTLINE_VARIANT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 1, getWidth()-2, getHeight()-2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        avatar.setForeground(AppTheme.ON_PRIMARY);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(false);

        userPanel.add(textPanel);
        userPanel.add(avatar);

        right.add(notifBtn);
        right.add(sep);
        right.add(userPanel);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public JTextField getSearchField() {
        return searchField;
    }
}

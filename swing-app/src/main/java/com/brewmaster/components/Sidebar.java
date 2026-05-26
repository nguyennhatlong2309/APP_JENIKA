package com.brewmaster.components;

import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Sidebar navigation - tương đương với aside nav của web
 */
public class Sidebar extends JPanel {

    public interface NavigationListener {
        void onNavigate(String page);
    }

    private static final String[][] NAV_ITEMS = {
            { "dashboard", "📊", "Dashboard" },
            { "inventory", "▦", "Quản lý Hàng Hoá" },
            { "sales", "💵", "Bán Hàng" },
            { "purchases", "🛒", "Nhập Hàng" },
            { "partners", "🤝", "Đối tác & Nhân viên" },
            { "expenses", "📋", "Thu chi" },
            { "activityLog", "📓", "Nhật Ký Hoạt Động" },
            { "settings", "⚙️", "Cấu hình Cửa hàng" },
    };

    private String activePage = "dashboard";
    private NavigationListener listener;
    private JButton[] navButtons;

    public Sidebar(NavigationListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 0));
        setBackground(AppTheme.SURFACE_LOW);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BorderLayout());

        // Right border line
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.OUTLINE_VARIANT));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(AppTheme.SURFACE_LOW);
        inner.setBorder(new EmptyBorder(0, 0, 0, 0));

        // === LOGO ===
        JPanel logoPanel = buildLogo();
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(logoPanel);

        Component logoStrut = Box.createVerticalStrut(8);
        ((JComponent) logoStrut).setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(logoStrut);

        // === NAVIGATION ITEMS ===
        navButtons = new JButton[NAV_ITEMS.length];
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(AppTheme.SURFACE_LOW);
        navPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < NAV_ITEMS.length; i++) {
            navButtons[i] = buildNavButton(NAV_ITEMS[i][0], NAV_ITEMS[i][1], NAV_ITEMS[i][2]);
            navButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            navPanel.add(navButtons[i]);

            Component itemStrut = Box.createVerticalStrut(2);
            ((JComponent) itemStrut).setAlignmentX(Component.LEFT_ALIGNMENT);
            navPanel.add(itemStrut);
        }

        inner.add(navPanel);

        Component glue = Box.createVerticalGlue();
        ((JComponent) glue).setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(glue);

        // === USER PROFILE ===
        JPanel userProfilePanel = buildUserProfile();
        userProfilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(userProfilePanel);

        Component profileStrut = Box.createVerticalStrut(12);
        ((JComponent) profileStrut).setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(profileStrut);

        add(inner, BorderLayout.CENTER);

        // Set initial active
        updateActiveState();
    }

    private JPanel buildLogo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(new EmptyBorder(20, 10, 16, 10));
        panel.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 80));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel iconNameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        iconNameRow.setOpaque(false);

        // Coffee icon badge
        JLabel iconBadge = new JLabel("☕");
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconBadge.setForeground(AppTheme.ON_PRIMARY);
        iconBadge.setOpaque(true);
        iconBadge.setBackground(AppTheme.PRIMARY_CONTAINER);
        iconBadge.setPreferredSize(new Dimension(40, 40));
        iconBadge.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        namePanel.setOpaque(false);

        JLabel nameLabel = new JLabel("BrewMaster Pro");
        nameLabel.setFont(AppTheme.FONT_TITLE_SM);
        nameLabel.setForeground(AppTheme.PRIMARY);

        JLabel branchLabel = new JLabel("Chi nhánh Trung tâm");
        branchLabel.setFont(AppTheme.FONT_LABEL);
        branchLabel.setForeground(AppTheme.ON_SURFACE_VAR);

        namePanel.add(nameLabel);
        namePanel.add(branchLabel);

        iconNameRow.add(iconBadge);
        iconNameRow.add(namePanel);
        panel.add(iconNameRow, BorderLayout.CENTER);

        return panel;
    }

    private JButton buildNavButton(String page, String icon, String label) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = activePage.equals(page);
                boolean isHover = getModel().isRollover();

                if (isActive) {
                    g2.setColor(AppTheme.PRIMARY_CONTAINER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.BORDER_RADIUS, AppTheme.BORDER_RADIUS);
                } else if (isHover) {
                    g2.setColor(AppTheme.SURFACE_VARIANT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.BORDER_RADIUS, AppTheme.BORDER_RADIUS);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new BorderLayout(8, 0));
        btn.setBorder(new EmptyBorder(0, 8, 0, 8));
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 20, 42));
        btn.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH - 20, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(AppTheme.FONT_BODY_MD);
        textLabel.setVerticalAlignment(SwingConstants.CENTER);

        btn.add(iconLabel, BorderLayout.WEST);
        btn.add(textLabel, BorderLayout.CENTER);

        // Hover + click handlers
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                activePage = page;
                updateActiveState();
                if (listener != null)
                    listener.onNavigate(page);
            }
        });

        return btn;
    }

    private void updateActiveState() {
        if (navButtons == null)
            return;
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            JButton btn = navButtons[i];
            String page = NAV_ITEMS[i][0];
            boolean isActive = activePage.equals(page);

            // Update colors of icon and text labels inside button
            for (Component c : btn.getComponents()) {
                if (c instanceof JLabel) {
                    ((JLabel) c).setForeground(
                            isActive ? AppTheme.ON_PRIMARY : AppTheme.ON_SURFACE_VAR);
                }
            }
            btn.repaint();
        }
    }

    private JPanel buildUserProfile() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(AppTheme.SURFACE_MED);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(4, 10, 4, 10)));
        panel.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 60));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JLabel avatar = new JLabel("NVA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY_CONTAINER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        avatar.setForeground(AppTheme.ON_PRIMARY);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(false);

        JPanel namePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        namePanel.setOpaque(false);

        JLabel name = new JLabel("Nguyễn Văn An");
        name.setFont(AppTheme.FONT_LABEL);
        name.setForeground(AppTheme.ON_SURFACE);

        JLabel role = new JLabel("Quản lý");
        role.setFont(AppTheme.FONT_LABEL);
        role.setForeground(AppTheme.ON_SURFACE_VAR);

        namePanel.add(name);
        namePanel.add(role);

        panel.add(avatar);
        panel.add(namePanel);
        return panel;
    }

    public void setActivePage(String page) {
        this.activePage = page;
        updateActiveState();
    }
}

package com.brewmaster;

import com.brewmaster.components.Sidebar;
import com.brewmaster.panels.*;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * JFrame chính của ứng dụng BrewMaster Pro
 * Layout: Sidebar (trái) | ContentArea (phải)
 * CardLayout để chuyển đổi giữa các màn hình
 */
public class AppFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Sidebar sidebar;

    // Panel names (key cho CardLayout)
    public static final String PANEL_DASHBOARD    = "dashboard";
    public static final String PANEL_INVENTORY    = "inventory";
    public static final String PANEL_SALES        = "sales";
    public static final String PANEL_PURCHASES    = "purchases";
    public static final String PANEL_PARTNERS     = "partners";
    public static final String PANEL_EXPENSES     = "expenses";
    public static final String PANEL_ACTIVITY_LOG = "activityLog";
    public static final String PANEL_SETTINGS     = "settings";

    public AppFrame() {
        initFrame();
        buildLayout();
        navigateTo(PANEL_DASHBOARD);
    }

    private void initFrame() {
        setTitle("☕ BrewMaster Pro - Quản lý Quán Cà Phê");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);

        // App icon text (no image needed)
        getContentPane().setBackground(AppTheme.BACKGROUND);
    }

    private void buildLayout() {
        // === ROOT LAYOUT: sidebar | right-panel ===
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(AppTheme.BACKGROUND);

        // SIDEBAR
        sidebar = new Sidebar(this::navigateTo);

        // RIGHT PANEL: content
        JPanel rightPanel = new JPanel(new BorderLayout(0, 0));
        rightPanel.setBackground(AppTheme.BACKGROUND);

        // Content with CardLayout
        cardLayout  = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(AppTheme.BACKGROUND);

        // Add all panels directly
        JPanel dashboard = new DashboardPanel();
        dashboard.setName(PANEL_DASHBOARD);
        contentPanel.add(dashboard, PANEL_DASHBOARD);

        JPanel inventory = new InventoryPanel();
        inventory.setName(PANEL_INVENTORY);
        contentPanel.add(inventory, PANEL_INVENTORY);

        JPanel sales = new SalesOrdersPanel();
        sales.setName(PANEL_SALES);
        contentPanel.add(sales, PANEL_SALES);

        JPanel purchases = new PurchasesPanel();
        purchases.setName(PANEL_PURCHASES);
        contentPanel.add(purchases, PANEL_PURCHASES);

        JPanel partners = new PartnersPanel();
        partners.setName(PANEL_PARTNERS);
        contentPanel.add(partners, PANEL_PARTNERS);

        JPanel expenses = new ExpensesPanel();
        expenses.setName(PANEL_EXPENSES);
        contentPanel.add(expenses, PANEL_EXPENSES);

        JPanel activityLog = new ActivityLogPanel();
        activityLog.setName(PANEL_ACTIVITY_LOG);
        contentPanel.add(activityLog, PANEL_ACTIVITY_LOG);

        JPanel settings = new SettingsPanel();
        settings.setName(PANEL_SETTINGS);
        contentPanel.add(settings, PANEL_SETTINGS);

        rightPanel.add(contentPanel, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    /** Chuyển đến màn hình theo key */
    public void navigateTo(String panelName) {
        // Show panel
        cardLayout.show(contentPanel, panelName);

        // Call loadData() dynamically on the active card panel
        for (Component c : contentPanel.getComponents()) {
            if (panelName.equals(c.getName())) {
                try {
                    java.lang.reflect.Method loadMethod = c.getClass().getMethod("loadData");
                    loadMethod.invoke(c);
                } catch (Exception ignored) {
                    // Bỏ qua nếu Panel không có phương thức loadData() công khai
                }
                break;
            }
        }

        // Update sidebar highlight
        sidebar.setActivePage(panelName);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}

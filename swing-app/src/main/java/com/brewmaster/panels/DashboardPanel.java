package com.brewmaster.panels;

import com.brewmaster.AppFrame;
import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Dashboard Panel - Tổng quan hoạt động
 * Tương đương dashboard.html
 */
public class DashboardPanel extends JPanel {

    private final NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // Metric cards
    private MetricCard cardSales, cardInventory, cardExpenses, cardOrders;

    // Recent sales table
    private DefaultTableModel salesTableModel;
    private StyledTable salesTable;

    // Stock alerts panel
    private JPanel stockAlertsPanel;

    public DashboardPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        // === NORTH PANEL: TITLE + CARDS ===
        JPanel northPanel = new JPanel();
        northPanel.setOpaque(false);
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        // === PAGE HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Tổng quan hoạt động");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel subtitle = new JLabel("Theo dõi số liệu kinh doanh theo thời gian thực");
        subtitle.setFont(AppTheme.FONT_BODY_SM);
        subtitle.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(subtitle);

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadData());

        header.add(titles, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        northPanel.add(header);
        northPanel.add(Box.createVerticalStrut(20));

        // === METRIC CARDS (4 columns) ===
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cardsRow.setPreferredSize(new Dimension(0, 130));
        cardsRow.setAlignmentX(LEFT_ALIGNMENT);

        cardSales = new MetricCard("💰", "Tổng doanh thu", "Đang tải...", null, AppTheme.PRIMARY);
        cardInventory = new MetricCard("📦", "Giá trị kho hàng", "Đang tải...", null, AppTheme.SECONDARY);
        cardExpenses = new MetricCard("📋", "Tổng chi phí", "Đang tải...", null, AppTheme.ERROR);
        cardOrders = new MetricCard("🔄", "Đơn hẹn", "Đang tải...", null, AppTheme.TERTIARY);

        cardsRow.add(cardSales);
        cardsRow.add(cardInventory);
        cardsRow.add(cardExpenses);
        cardsRow.add(cardOrders);

        northPanel.add(cardsRow);
        content.add(northPanel, BorderLayout.NORTH);

        // === CENTER PANEL: GRID: RECENT SALES + STOCK ALERTS ===
        JPanel salesSection = buildRecentSalesSection();

        // Use a wrapper with specific weight
        JPanel leftGrid = new JPanel(new BorderLayout());
        leftGrid.setOpaque(false);
        leftGrid.add(salesSection, BorderLayout.CENTER);

        JPanel rightGrid = new JPanel(new BorderLayout());
        rightGrid.setOpaque(false);
        rightGrid.add(buildStockAlertsSection(), BorderLayout.CENTER);

        // 2:1 split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftGrid, rightGrid);
        splitPane.setDividerLocation(0.65);
        splitPane.setResizeWeight(0.65);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(12);
        splitPane.setBackground(AppTheme.BACKGROUND);
        splitPane.setAlignmentX(LEFT_ALIGNMENT);

        content.add(splitPane, BorderLayout.CENTER);

        return content;
    }

    private JPanel buildRecentSalesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_HIGH);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        panel.putClientProperty("roundedCorner", true);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.withAlpha(AppTheme.SURFACE_HIGHEST, 30));
        header.setBorder(new EmptyBorder(14, 16, 14, 16));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 3));
        headerLeft.setOpaque(false);
        JLabel title = new JLabel("Đơn hàng gần đây");
        title.setFont(AppTheme.FONT_TITLE_SM);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Theo dõi giao dịch theo thời gian thực");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        headerLeft.add(title);
        headerLeft.add(sub);

        JButton viewAllBtn = new JButton("Xem tất cả");
        viewAllBtn.setFont(AppTheme.FONT_LABEL);
        viewAllBtn.setForeground(AppTheme.ON_PRIMARY);
        viewAllBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof AppFrame) {
                ((AppFrame) window).navigateTo(AppFrame.PANEL_SALES);
            }
        });

        header.add(headerLeft, BorderLayout.CENTER);
        header.add(viewAllBtn, BorderLayout.EAST);

        // Table
        String[] cols = { "Mã HĐ", "Khách hàng", "Tổng tiền", "Trạng thái", "Thao tác" };
        salesTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        salesTable = new StyledTable(salesTableModel);

        // Status column with badge renderer
        salesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
                wrapper.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
                String status = v == null ? "" : v.toString();
                wrapper.add(StatusBadge.forSalesStatus(status));
                return wrapper;
            }
        });
        // Action column
        salesTable.getColumnModel().getColumn(4).setCellRenderer((t, v, sel, foc, row, col) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton viewBtn = new JButton("👁");
            viewBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            viewBtn.setForeground(AppTheme.PRIMARY);
            viewBtn.setOpaque(false);
            viewBtn.setBorderPainted(false);
            viewBtn.setContentAreaFilled(false);
            viewBtn.setFocusPainted(false);
            p.add(viewBtn);
            return p;
        });

        // Column widths
        salesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        // Mouse listener to trigger View Dialog
        salesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = salesTable.rowAtPoint(evt.getPoint());
                int col = salesTable.columnAtPoint(evt.getPoint());
                if (col == 4 && row >= 0) {
                    Object val = salesTableModel.getValueAt(row, 0);
                    if (val == null)
                        return;
                    try {
                        int orderId = Integer.parseInt(val.toString().replace("BH-", ""));
                        openViewDialog(orderId);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(salesTable.wrapInScrollPane(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStockAlertsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_HIGH);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 3));
        header.setBackground(AppTheme.withAlpha(AppTheme.ERROR, 8));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        JLabel warningIcon = new JLabel("⚠");
        warningIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        warningIcon.setForeground(AppTheme.ERROR);
        JLabel title = new JLabel("Cảnh báo Tồn kho");
        title.setFont(AppTheme.FONT_TITLE_SM);
        title.setForeground(AppTheme.ERROR);
        titleRow.add(warningIcon);
        titleRow.add(title);

        JLabel sub = new JLabel("Hàng hóa cần nhập thêm");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        header.add(titleRow);
        header.add(sub);

        // Stock items
        stockAlertsPanel = new JPanel();
        stockAlertsPanel.setBackground(AppTheme.SURFACE_HIGH);
        stockAlertsPanel.setLayout(new BoxLayout(stockAlertsPanel, BoxLayout.Y_AXIS));
        stockAlertsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(stockAlertsPanel) {
            {
                setBorder(BorderFactory.createEmptyBorder());
                getViewport().setBackground(AppTheme.SURFACE_HIGH);
            }
        }, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStockItem(String name, String current, String min, String pct, String severity) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_MED);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(10, 12, 10, 12)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(AppTheme.FONT_LABEL);
        nameLabel.setForeground(AppTheme.ON_SURFACE);

        Color badgeColor = "critical".equals(severity) ? AppTheme.ERROR : AppTheme.PRIMARY;
        String badgeText = "critical".equals(severity) ? "Hết hàng" : "Sắp hết";
        JLabel badge = new JLabel(badgeText);
        badge.setFont(AppTheme.FONT_LABEL);
        badge.setForeground(badgeColor);
        badge.setOpaque(true);
        badge.setBackground(AppTheme.withAlpha(badgeColor, 30));
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        topRow.add(nameLabel, BorderLayout.WEST);
        topRow.add(badge, BorderLayout.EAST);

        JPanel midRow = new JPanel(new BorderLayout());
        midRow.setOpaque(false);
        midRow.setBorder(new EmptyBorder(4, 0, 4, 0));
        JLabel currLabel = new JLabel("Hiện có: " + current);
        currLabel.setFont(AppTheme.FONT_BODY_SM);
        currLabel.setForeground(AppTheme.ON_SURFACE_VAR);
        JLabel minLabel = new JLabel("Tối thiểu: " + min);
        minLabel.setFont(AppTheme.FONT_BODY_SM);
        minLabel.setForeground(AppTheme.ON_SURFACE_VAR);
        midRow.add(currLabel, BorderLayout.WEST);
        midRow.add(minLabel, BorderLayout.EAST);

        // Progress bar
        int percent = Integer.parseInt(pct);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(percent);
        bar.setStringPainted(false);
        bar.setBackground(AppTheme.SURFACE_HIGHEST);
        bar.setForeground(badgeColor);
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setPreferredSize(new Dimension(0, 4));

        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.add(topRow, BorderLayout.NORTH);
        inner.add(midRow, BorderLayout.CENTER);
        inner.add(bar, BorderLayout.SOUTH);

        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    // ===================================================================
    // DATA LOADING
    // ===================================================================
    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String totalRevenue = "0 ₫", inventoryValue = "0 ₫",
                    totalExpense = "0 ₫", pendingOrders = "0";
            private final java.util.List<Object[]> recentSalesRows = new java.util.ArrayList<>();
            private final java.util.List<Object[]> stockAlertItems = new java.util.ArrayList<>();

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();
                    loadDashboardStats(conn);
                    loadRecentSales(conn);
                    loadStockAlerts(conn);
                } catch (Exception e) {
                    System.err.println("Lỗi tải Dashboard: " + e.getMessage());
                }
                return null;
            }

            private void loadDashboardStats(Connection conn) throws SQLException {
                // Tổng doanh thu: bảng ban_hang, cột tong_tien, trang_thai, thoi_gian
                // cfe_di_rom không có bảng hoa_don_xuat – dùng ban_hang thay thế
                String sqlTotalSales = "SELECT COALESCE(SUM(tong_tien),0) FROM ban_hang " +
                        "WHERE trang_thai='Hoàn thành'";
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlTotalSales)) {
                    if (rs.next())
                        totalRevenue = formatVND(rs.getLong(1));
                }

                // Giá trị kho hàng: bảng san_pham
                String sqlInv = "SELECT COALESCE(SUM(gia_nhap_hien_tai * so_luong_ton), 0) FROM san_pham WHERE bi_xoa = 0";
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlInv)) {
                    if (rs.next())
                        inventoryValue = formatVND(rs.getLong(1));
                }

                // Tổng chi phí: bảng thu_chi + giá nhập của sản phẩm bán hàng
                String sqlTotalExp = "SELECT COALESCE(SUM(COALESCE(tc.tien_chi, 0) + COALESCE((SELECT SUM(ct.so_luong * sp.gia_nhap_hien_tai) FROM chi_tiet_ban_hang ct JOIN san_pham sp ON ct.id_san_pham = sp.id WHERE ct.id_ban_hang = tc.id_ban_hang), 0)), 0) FROM thu_chi tc";
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlTotalExp)) {
                    if (rs.next())
                        totalExpense = formatVND(rs.getLong(1));
                }

                // Đơn chờ xử lý: ban_hang có trang_thai 'Hẹn'
                String sqlPend = "SELECT COUNT(*) FROM ban_hang WHERE trang_thai='Hẹn'";
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlPend)) {
                    if (rs.next())
                        pendingOrders = String.valueOf(rs.getInt(1));
                }
            }

            private void loadRecentSales(Connection conn) throws SQLException {
                // cfe_di_rom: bảng ban_hang có id, thoi_gian, id_khach_hang, tong_tien,
                // trang_thai
                // Không có ten_khach trực tiếp – JOIN với khach_hang
                String sql = "SELECT bh.id, bh.thoi_gian, " +
                        "IFNULL(kh.ten, 'Khách vãng lai') AS ten_khach, " +
                        "bh.tong_tien, bh.trang_thai " +
                        "FROM ban_hang bh " +
                        "LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id " +
                        "ORDER BY bh.thoi_gian DESC LIMIT 8";
                recentSalesRows.clear();
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        recentSalesRows.add(new Object[] {
                                "BH-" + rs.getInt("id"),
                                rs.getString("ten_khach"),
                                formatVND(rs.getLong("tong_tien")),
                                rs.getString("trang_thai"),
                                "👁"
                        });
                    }
                }
            }

            private void loadStockAlerts(Connection conn) throws SQLException {
                String sql = "SELECT sp.ten_san_pham, sp.so_luong_ton, sp.canh_bao_ton_kho, sp.trang_thai, dv.ten_don_vi "
                        +
                        "FROM san_pham sp " +
                        "LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id " +
                        "WHERE sp.trang_thai = 'Cảnh báo' AND sp.bi_xoa = 0 " +
                        "ORDER BY sp.so_luong_ton ASC " +
                        "LIMIT 5";
                stockAlertItems.clear();
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next()) {
                        double soLuong = rs.getDouble("so_luong_ton");
                        double minTon = rs.getDouble("canh_bao_ton_kho");
                        String donVi = rs.getString("ten_don_vi") != null ? rs.getString("ten_don_vi") : "cái";

                        String currentStr = formatQty(soLuong) + " " + donVi;
                        String minStr = formatQty(minTon) + " " + donVi;

                        int percent = 0;
                        if (minTon > 0) {
                            percent = (int) Math.min(100, Math.max(0, (soLuong / minTon) * 100));
                        }
                        String pctStr = String.valueOf(percent);

                        String severity = "Hết hàng".equals(rs.getString("trang_thai")) ? "critical" : "low";

                        stockAlertItems.add(new Object[] {
                                rs.getString("ten_san_pham"),
                                currentStr,
                                minStr,
                                pctStr,
                                severity
                        });
                    }
                }
            }

            private String formatQty(double val) {
                if (val == (long) val) {
                    return String.format("%d", (long) val);
                } else {
                    return String.format("%.2f", val);
                }
            }

            @Override
            protected void done() {
                // Cập nhật giá trị của các card đã có trên UI (không tạo lại đối tượng)
                cardSales.setValue(totalRevenue);
                cardInventory.setValue(inventoryValue);
                cardExpenses.setValue(totalExpense);
                cardOrders.setValue(pendingOrders);

                salesTableModel.setRowCount(0);
                for (Object[] row : recentSalesRows) {
                    salesTableModel.addRow(row);
                }

                // Cập nhật cảnh báo tồn kho thực tế
                if (stockAlertsPanel != null) {
                    stockAlertsPanel.removeAll();
                    if (stockAlertItems.isEmpty()) {
                        JLabel noAlertLabel = new JLabel("✅ Tất cả sản phẩm đều đủ hàng");
                        noAlertLabel.setFont(AppTheme.FONT_BODY_MD);
                        noAlertLabel.setForeground(AppTheme.PRIMARY);
                        noAlertLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                        JPanel noAlertPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
                        noAlertPanel.setOpaque(false);
                        noAlertPanel.add(noAlertLabel);
                        stockAlertsPanel.add(noAlertPanel);
                    } else {
                        for (Object[] item : stockAlertItems) {
                            stockAlertsPanel.add(buildStockItem(
                                    (String) item[0],
                                    (String) item[1],
                                    (String) item[2],
                                    (String) item[3],
                                    (String) item[4]));
                            stockAlertsPanel.add(Box.createVerticalStrut(8));
                        }
                    }
                    stockAlertsPanel.revalidate();
                    stockAlertsPanel.repaint();
                }
            }
        };
        worker.execute();
    }

    private void openViewDialog(int orderId) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SalesOrderDialog dlg = new SalesOrderDialog(owner, orderId, true);
        dlg.setVisible(true);
    }

    private String formatVND(long amount) {
        return vndFormat.format(amount) + " ₫";
    }
}

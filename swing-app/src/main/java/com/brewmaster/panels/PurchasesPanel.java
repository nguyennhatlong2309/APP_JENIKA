package com.brewmaster.panels;

import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.ExcelExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.io.*;

/**
 * Màn hình Đơn Nhập Hàng - purchase-orders.html
 */
public class PurchasesPanel extends JPanel {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1, totalItems = 0;
    private final NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private DefaultTableModel tableModel;
    private StyledTable table;
    private Pagination pagination;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private DatePicker dpFrom;
    private DatePicker dpTo;
    private JLabel lblTotalOrders, lblTotalValue, lblDebtValue;

    public PurchasesPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Đơn Nhập Hàng");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Quản lý mua hàng từ nhà cung cấp");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(sub);

        JButton addBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font>  Tạo đơn nhập</html>");
        addBtn.setFont(AppTheme.FONT_LABEL);
        addBtn.setForeground(AppTheme.ON_PRIMARY);
        addBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(150, 36));
        addBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openCreateDialog());

        header.add(titles, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Stats
        lblTotalOrders = new JLabel("...");
        lblTotalValue = new JLabel("...");
        lblDebtValue = new JLabel("...");
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 96));
        statsRow.add(buildStatCard("📋", "Tổng đơn nhập", lblTotalOrders, AppTheme.PRIMARY));
        statsRow.add(buildStatCard("💵", "Giá trị mua hàng", lblTotalValue, AppTheme.SECONDARY));
        statsRow.add(buildStatCard("⚡", "Còn nợ nhà CC", lblDebtValue, AppTheme.STATUS_CANC_FG));

        // Filters
        JPanel filters = buildFilters();

        // Table
        JPanel tableSection = buildTableSection();

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(statsRow, BorderLayout.NORTH);
        topPanel.add(filters, BorderLayout.CENTER);

        main.add(topPanel, BorderLayout.NORTH);
        main.add(tableSection, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(main, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildStatCard(String icon, String label, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconLbl.setPreferredSize(new Dimension(40, 40));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);

        valueLabel.setFont(AppTheme.FONT_TITLE_MD);
        valueLabel.setForeground(color);

        JPanel col = new JPanel(new GridLayout(2, 1, 0, 2));
        col.setOpaque(false);
        col.add(lbl);
        col.add(valueLabel);

        p.add(iconLbl, BorderLayout.WEST);
        p.add(col, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.setFont(AppTheme.FONT_BODY_MD);
        searchField.putClientProperty("JTextField.placeholderText", " Tìm nhà cung cấp, mã đơn...");
        searchField.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        statusFilter = new JComboBox<>(new String[] { "Tất cả", "Đã nhận", "Chờ Nhận", "Đã hủy" });
        statusFilter.setFont(AppTheme.FONT_BODY_MD);
        statusFilter.setPreferredSize(new Dimension(130, 32));
        statusFilter.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        dpFrom = new DatePicker();
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpFrom.setPreferredSize(new Dimension(130, 32));
        dpFrom.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        dpTo = new DatePicker();
        dpTo.setValue(LocalDate.now());
        dpTo.setPreferredSize(new Dimension(130, 32));
        dpTo.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
            dpTo.setValue(LocalDate.now());
            currentPage = 1;
            loadData();
        });

        // Xuất Excel (danh sách) – nay dùng ExcelExporter để xuất .xlsx thật sự
        JButton exportBtn = new JButton("<html><font face='Segoe UI'>⬇</font>  Xuất Excel</html>");
        exportBtn.setFont(AppTheme.FONT_LABEL);
        exportBtn.setBackground(AppTheme.SURFACE_VARIANT);
        exportBtn.setForeground(AppTheme.ON_SURFACE);
        exportBtn.setBorderPainted(true);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportListToExcel());

        JLabel s1 = label("Nhà CC / Mã đơn:");
        JLabel s2 = label("Trạng thái:");
        JLabel fromLbl = label("Từ ngày:");
        JLabel toLbl = label("Đến ngày:");
        p.add(s1);
        p.add(searchField);
        p.add(s2);
        p.add(statusFilter);
        p.add(fromLbl);
        p.add(dpFrom);
        p.add(toLbl);
        p.add(dpTo);
        p.add(refreshBtn);
        p.add(exportBtn);
        return p;
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] cols = { "Mã đơn", "Ngày lập", "Ngày nhận", "Nhà cung cấp", "Người nhập", "Tổng tiền", "Đã thanh toán",
                "Còn nợ",
                "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new StyledTable(tableModel);

        // Mã đơn
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.SECONDARY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // Status badge
        table.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel w = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
            w.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            String status = v == null ? "" : v.toString();
            Color[] colors;
            if ("Đã nhận".equals(status))
                colors = new Color[] { AppTheme.STATUS_PAID_BG, AppTheme.STATUS_PAID_FG };
            else if ("Chờ Nhận".equals(status))
                colors = new Color[] { AppTheme.STATUS_PEND_BG, AppTheme.STATUS_PEND_FG };
            else
                colors = new Color[] { AppTheme.STATUS_CANC_BG, AppTheme.STATUS_CANC_FG };
            w.add(new StatusBadge(status, colors[0], colors[1]));
            return w;
        });

        // Action column – renderer
        table.getColumnModel().getColumn(9).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton vBtn = new JButton("👁");
            JButton eBtn = new JButton("✏");
            JButton xBtn = new JButton("🖨");
            for (JButton b : new JButton[] { vBtn, eBtn, xBtn }) {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setOpaque(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setForeground(AppTheme.ON_SURFACE_VAR);
            }
            p.add(vBtn);
            p.add(eBtn);
            p.add(xBtn);
            return p;
        });

        // Action column – mouse listener for view / edit / export click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0) {
                    String maDon = tableModel.getValueAt(row, 0).toString(); // "NH-{id}"
                    try {
                        int id = Integer.parseInt(maDon.replace("NH-", ""));
                        if (col == 9) {
                            int columnWidth = table.getColumnModel().getColumn(9).getWidth();
                            int x = evt.getX() - table.getCellRect(row, 9, true).x;
                            if (x < columnWidth / 3) {
                                openViewDialog(id);
                            } else if (x < (columnWidth * 2) / 3) {
                                openEditDialog(id);
                            } else {
                                exportSingleOrder(id);
                            }
                        } else {
                            openViewDialog(id);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });

        int[] widths = { 80, 110, 100, 130, 100, 105, 105, 95, 90, 95 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        pagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });
        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(pagination, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_LABEL);
        l.setForeground(AppTheme.ON_SURFACE_VAR);
        return l;
    }

    private void openCreateDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        PurchaseOrderDialog dlg = new PurchaseOrderDialog(owner);
        dlg.setOnSaveCallback(() -> {
            currentPage = 1;
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog(int orderId) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        PurchaseOrderDialog dlg = new PurchaseOrderDialog(owner, orderId);
        dlg.setOnSaveCallback(() -> loadData());
        dlg.setVisible(true);
    }

    private void openViewDialog(int orderId) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        PurchaseOrderDialog dlg = new PurchaseOrderDialog(owner, orderId, true);
        dlg.setVisible(true);
    }

    public void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count = 0;
            private String totalOrders = "...", totalVal = "...", debtVal = "...";

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();
                    StringBuilder cond = new StringBuilder(" WHERE 1=1");

                    String search = searchField != null ? searchField.getText().trim() : "";
                    if (!search.isEmpty())
                        cond.append(" AND (ncc.ten LIKE '%").append(search)
                                .append("%' OR CAST(nh.id AS CHAR) LIKE '%").append(search).append("%')");

                    String st = statusFilter != null ? (String) statusFilter.getSelectedItem() : "Tất cả";
                    if (st != null && !st.equals("Tất cả")) {
                        cond.append(" AND nh.trang_thai = '").append(st).append("'");
                    }

                    if (dpFrom != null && dpFrom.getValue() != null) {
                        cond.append(" AND nh.thoi_gian >= '").append(dpFrom.getValue().toString()).append(" 00:00:00'");
                    }
                    if (dpTo != null && dpTo.getValue() != null) {
                        cond.append(" AND nh.thoi_gian <= '").append(dpTo.getValue().toString()).append(" 23:59:59'");
                    }

                    // Stats
                    String statsSql = "SELECT COUNT(*), COALESCE(SUM(nh.tong_tien),0), COALESCE(SUM(nh.tien_no),0) FROM nhap_hang nh"
                            + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                            + cond.toString();
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(statsSql)) {
                        if (rs.next()) {
                            totalOrders = rs.getInt(1) + " đơn";
                            totalVal = vndFormat.format(rs.getLong(2)) + " ₫";
                            debtVal = vndFormat.format(rs.getLong(3)) + " ₫";
                        }
                    }

                    // Count
                    String countSql = "SELECT COUNT(*) FROM nhap_hang nh"
                            + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id" + cond;
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
                        if (rs.next())
                            count = rs.getInt(1);
                    }

                    // Data
                    int offset = (currentPage - 1) * PAGE_SIZE;
                    String sql = "SELECT nh.id, nh.thoi_gian, nh.ngay_nhan,"
                            + " IFNULL(ncc.ten, '---') AS ten_ncc,"
                            + " IFNULL(nv.ten_nhan_vien, '---') AS ten_nv,"
                            + " nh.tong_tien, nh.da_thanh_toan, nh.tien_no, nh.trang_thai"
                            + " FROM nhap_hang nh"
                            + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                            + " LEFT JOIN nhan_vien nv ON nh.id_nhan_vien = nv.id"
                            + cond
                            + " ORDER BY nh.thoi_gian DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {
                            rows.add(new Object[] {
                                    "NH-" + rs.getInt("id"),
                                    rs.getTimestamp("thoi_gian") != null
                                            ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                    rs.getTimestamp("thoi_gian"))
                                            : "",
                                    rs.getDate("ngay_nhan") != null
                                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(
                                                    rs.getDate("ngay_nhan"))
                                            : "--",
                                    rs.getString("ten_ncc"),
                                    rs.getString("ten_nv"),
                                    vndFormat.format(rs.getLong("tong_tien")) + " ₫",
                                    vndFormat.format(rs.getLong("da_thanh_toan")) + " ₫",
                                    vndFormat.format(rs.getLong("tien_no")) + " ₫",
                                    rs.getString("trang_thai"),
                                    ""
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                totalItems = count;
                tableModel.setRowCount(0);
                for (Object[] row : rows)
                    tableModel.addRow(row);
                pagination.update(totalItems, PAGE_SIZE, currentPage);
                lblTotalOrders.setText(totalOrders);
                lblTotalValue.setText(totalVal);
                lblDebtValue.setText(debtVal);
            }
        };
        worker.execute();
    }

    /**
     * Xuất PHIẾU NHẬP HÀNG của một đơn cụ thể ra file .xlsx theo mẫu.
     */
    private void exportSingleOrder(int orderId) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu phiếu nhập hàng #NH-" + orderId);
        fc.setSelectedFile(new File("Phieu_Nhap_Hang_NH" + orderId + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File target = fc.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith(".xlsx"))
            target = new File(target.getAbsolutePath() + ".xlsx");
        final File finalTarget = target;

        SwingWorker<Void, Void> w = new SwingWorker<>() {
            private String err = null;

            @Override
            protected Void doInBackground() {
                try {
                    ExcelExporter.exportPurchaseOrder(orderId, finalTarget);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    err = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (err == null) {
                    int opt = JOptionPane.showConfirmDialog(PurchasesPanel.this,
                            "Xuất phiếu nhập hàng thành công!\nMở file ngay?",
                            "Xuất Excel", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(finalTarget);
                        } catch (Exception ignore) {
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(PurchasesPanel.this,
                            "Lỗi xuất Excel: " + err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    /**
     * Xuất DANH SÁCH đơn nhập hàng ra file .xlsx (thay thế xuất CSV cũ).
     * Sử dụng ExcelExporter.exportPurchaseOrderList() để xuất file Excel thật sự.
     */
    private void exportListToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu danh sách đơn nhập hàng");
        fileChooser.setSelectedFile(new File("danh_sach_don_nhap_hang.xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(filePath + ".xlsx");
        }

        final File targetFile = fileToSave;

        // Lấy filter hiện tại
        final String searchText = searchField != null ? searchField.getText().trim() : "";
        final String statusText = statusFilter != null ? (String) statusFilter.getSelectedItem() : "Tất cả";
        final String fromDateStr = (dpFrom != null && dpFrom.getValue() != null)
                ? dpFrom.getValue().toString() + " 00:00:00"
                : null;
        final String toDateStr = (dpTo != null && dpTo.getValue() != null) ? dpTo.getValue().toString() + " 23:59:59"
                : null;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;
            private String errorMessage = "";

            @Override
            protected Void doInBackground() {
                try {
                    ExcelExporter.exportPurchaseOrderList(searchText, statusText, fromDateStr, toDateStr, targetFile);
                    success = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    int opt = JOptionPane.showConfirmDialog(PurchasesPanel.this,
                            "Đã xuất danh sách đơn nhập hàng ra file Excel thành công!\nMở file ngay?",
                            "Xuất Excel thành công",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(targetFile);
                        } catch (Exception ignore) {
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(PurchasesPanel.this,
                            "Gặp lỗi khi xuất dữ liệu: " + errorMessage,
                            "Lỗi xuất Excel",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}

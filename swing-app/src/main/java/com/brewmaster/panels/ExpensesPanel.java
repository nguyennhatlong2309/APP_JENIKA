package com.brewmaster.panels;

import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.ActivityLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Màn hình Thu Chi Tổng Hợp
 *
 * Mỗi dòng có thể có cả khoản Thu và/hoặc khoản Chi
 * Data source: bảng thu_chi + loai_thu_chi trong DB
 */
public class ExpensesPanel extends JPanel {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1, totalItems = 0;

    private final NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private DefaultTableModel tableModel;
    private StyledTable table;
    private Pagination pagination;
    private JButton categoryFilterBtn;
    private JPopupMenu categoryMenu;
    private JTextField categorySearchField;
    private JPanel categoryCheckBoxPanel;
    private final java.util.Map<String, JCheckBox> categoryCheckboxes = new java.util.LinkedHashMap<>();
    private DatePicker dpFrom;
    private DatePicker dpTo;

    private JLabel lblTotalIncome, lblTotalExpense, lblNetFlow;

    public ExpensesPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    // ═══════════════════════ Build UI ════════════════════════════════

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Header ──
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Thu Chi Tổng Hợp");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Quản lý toàn bộ thu nhập và chi phí của cửa hàng");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(sub);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        JButton addBtn = new JButton("<html><font face='Segoe UI Emoji'>📋</font>  Lập phiếu Thu Chi</html>");
        addBtn.setFont(AppTheme.FONT_LABEL);
        addBtn.setForeground(AppTheme.ON_PRIMARY);
        addBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openDialog());

        btnRow.add(addBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(btnRow, BorderLayout.EAST);

        // ── Summary cards ──
        lblTotalIncome = new JLabel("...");
        lblTotalExpense = new JLabel("...");
        lblNetFlow = new JLabel("...");
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setPreferredSize(new Dimension(0, 90));
        summaryRow.add(buildSummaryCard("↑ Tổng thu", lblTotalIncome, AppTheme.STATUS_PAID_FG, "📥"));
        summaryRow.add(buildSummaryCard("↓ Tổng chi", lblTotalExpense, AppTheme.STATUS_CANC_FG, "📤"));
        summaryRow.add(buildSummaryCard("≈ Dòng tiền thuần", lblNetFlow, AppTheme.PRIMARY, "💰"));

        // ── Filters ──
        JPanel filters = buildFilters();

        // ── Table ──
        JPanel tableSection = buildTableSection();

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(summaryRow, BorderLayout.NORTH);
        topPanel.add(filters, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);
        main.add(topPanel, BorderLayout.NORTH);
        main.add(tableSection, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(main, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildSummaryCard(String label, JLabel valueLabel, Color color, String emoji) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        topRow.setOpaque(false);
        topRow.add(dot);
        topRow.add(lbl);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        JPanel col = new JPanel(new GridLayout(2, 1, 0, 8));
        col.setOpaque(false);
        col.add(topRow);
        col.add(valueLabel);
        p.add(col, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(AppTheme.SURFACE_MED);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        categoryFilterBtn = new JButton("Danh mục ↓");
        categoryFilterBtn.setFont(AppTheme.FONT_BODY_MD);
        categoryFilterBtn.setPreferredSize(new Dimension(170, 32));
        categoryFilterBtn.setBackground(AppTheme.SURFACE_LOW);
        categoryFilterBtn.setForeground(AppTheme.ON_SURFACE);
        categoryFilterBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        categoryFilterBtn.setFocusPainted(false);
        categoryFilterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        categoryFilterBtn.setHorizontalAlignment(SwingConstants.LEFT);

        categoryMenu = new JPopupMenu();
        categoryMenu.setBackground(AppTheme.SURFACE_LOW);
        categoryMenu.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        categoryMenu.setLayout(new BorderLayout());

        JPanel popupContent = new JPanel(new BorderLayout(0, 6));
        popupContent.setBackground(AppTheme.SURFACE_LOW);
        popupContent.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        categorySearchField = new JTextField();
        categorySearchField.setFont(AppTheme.FONT_BODY_MD);
        categorySearchField.putClientProperty("JTextField.placeholderText", " Tìm danh mục...");
        categorySearchField.setPreferredSize(new Dimension(220, 32));
        categorySearchField.setBackground(AppTheme.SURFACE_MED);
        categorySearchField.setForeground(AppTheme.ON_SURFACE);
        categorySearchField.setCaretColor(AppTheme.ON_SURFACE);
        categorySearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        categoryCheckBoxPanel = new JPanel();
        categoryCheckBoxPanel.setLayout(new BoxLayout(categoryCheckBoxPanel, BoxLayout.Y_AXIS));
        categoryCheckBoxPanel.setBackground(AppTheme.SURFACE_LOW);

        JScrollPane scrollPane = new JScrollPane(categoryCheckBoxPanel);
        scrollPane.setPreferredSize(new Dimension(220, 200));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        popupContent.add(categorySearchField, BorderLayout.NORTH);
        popupContent.add(scrollPane, BorderLayout.CENTER);
        categoryMenu.add(popupContent);

        // Document Listener for filtering categories
        categorySearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = categorySearchField.getText().trim().toLowerCase();
                categoryCheckBoxPanel.removeAll();
                for (JCheckBox cb : categoryCheckboxes.values()) {
                    if (text.isEmpty() || cb.getText().toLowerCase().contains(text)) {
                        categoryCheckBoxPanel.add(cb);
                    }
                }
                scrollPane.getVerticalScrollBar().setValue(0);
                categoryCheckBoxPanel.revalidate();
                categoryCheckBoxPanel.repaint();
            }
        });

        categoryFilterBtn.addActionListener(e -> {
            categoryMenu.show(categoryFilterBtn, 0, categoryFilterBtn.getHeight());
            categorySearchField.requestFocusInWindow();
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
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            if (categorySearchField != null) {
                categorySearchField.setText("");
            }
            for (JCheckBox cb : categoryCheckboxes.values()) {
                cb.setSelected(false);
            }
            updateCategoryFilterButtonText();
            dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
            dpTo.setValue(LocalDate.now());
            currentPage = 1;
            loadData();
        });

        p.add(label("Danh mục:"));
        p.add(categoryFilterBtn);
        p.add(label("Từ ngày:"));
        p.add(dpFrom);
        p.add(label("Đến ngày:"));
        p.add(dpTo);
        p.add(refreshBtn);

        // Load danh mục lần đầu
        reloadCategoryFilter();
        return p;
    }

    /** Load danh mục vào panel lọc */
    private void reloadCategoryFilter() {
        // Save previously selected categories
        java.util.Set<String> prevSelected = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, JCheckBox> entry : categoryCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                prevSelected.add(entry.getKey());
            }
        }

        categoryCheckboxes.clear();
        if (categoryCheckBoxPanel != null) {
            categoryCheckBoxPanel.removeAll();
        }

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT DISTINCT ten FROM loai_thu_chi ORDER BY ten";

            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String catName = rs.getString("ten");
                    JCheckBox cb = new JCheckBox(catName);
                    cb.setFont(AppTheme.FONT_BODY_MD);
                    cb.setForeground(AppTheme.ON_SURFACE);
                    cb.setBackground(AppTheme.SURFACE_LOW);
                    if (prevSelected.contains(catName)) {
                        cb.setSelected(true);
                    }
                    cb.addActionListener(e -> {
                        currentPage = 1;
                        updateCategoryFilterButtonText();
                        loadData();
                    });
                    categoryCheckboxes.put(catName, cb);
                    if (categoryCheckBoxPanel != null) {
                        categoryCheckBoxPanel.add(cb);
                    }
                }
            }
            if (categoryCheckBoxPanel != null) {
                categoryCheckBoxPanel.revalidate();
                categoryCheckBoxPanel.repaint();
            }
            updateCategoryFilterButtonText();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        // Cột: 0=Mã phiếu(ẩn), 1=Ngày, 2=Danh mục, 3=Mô tả, 4=Thu(₫), 5=Chi(₫), 6=Lợi nhuận(₫), 7=Nhân viên, 8=Thao tác
        String[] cols = {"Mã phiếu", "Ngày", "Danh mục", "Mô tả", "Thu (₫)", "Chi (₫)", "Lợi nhuận (₫)", "Nhân viên", "Thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new StyledTable(tableModel);

        // Ẩn cột Mã phiếu (index 0) — vẫn giữ trong model để xử lý actions
        table.removeColumn(table.getColumnModel().getColumn(0));

        // Sau khi ẩn cột 0, visible indices: 0=Ngày, 1=DanhMuc, 2=MoTa, 3=Thu, 4=Chi, 5=LoiNhuan, 6=NhanVien, 7=ThaoTac

        // Cột Thu (₫) — xanh lá
        table.getColumnModel().getColumn(3).setCellRenderer((t, v, sel, foc, r, c) -> {
            String val = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(val);
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.STATUS_PAID_FG);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            return lbl;
        });

        // Cột Chi (₫) — đỏ
        table.getColumnModel().getColumn(4).setCellRenderer((t, v, sel, foc, r, c) -> {
            String val = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(val);
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.STATUS_CANC_FG);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            return lbl;
        });

        // Cột Lợi nhuận (₫) — xanh nếu dương, đỏ nếu âm, xám nếu bằng 0 (index 5 visible)
        table.getColumnModel().getColumn(5).setCellRenderer((t, v, sel, foc, r, c) -> {
            String val = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(val);
            lbl.setFont(AppTheme.FONT_LABEL.deriveFont(java.awt.Font.BOLD));
            Color fg;
            if (val.startsWith("+")) fg = AppTheme.STATUS_PAID_FG;
            else if (val.startsWith("-")) fg = AppTheme.STATUS_CANC_FG;
            else fg = AppTheme.ON_SURFACE_VAR;
            lbl.setForeground(fg);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            return lbl;
        });

        // Cột Thao tác — nút sửa và xóa (index 7 visible)
        table.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel w = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 7));
            w.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton eBtn = iconBtn("✏", AppTheme.PRIMARY);
            JButton dBtn = iconBtn("🗑", AppTheme.ERROR);
            w.add(eBtn);
            w.add(dBtn);
            return w;
        });

        // Mouse listener — click anywhere in the row to view, except the Actions column
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    Object maPhieu = tableModel.getValueAt(modelRow, 0);
                    if (maPhieu == null)
                        return;
                    try {
                        int id = Integer.parseInt(maPhieu.toString().replace("TC-", ""));
                        if (col == 7) {
                            java.awt.Rectangle cellRect = table.getCellRect(row, col, false);
                            if (evt.getX() > cellRect.x + cellRect.width / 2) {
                                confirmDelete(id);
                            } else {
                                openEditDialog(id);
                            }
                        } else {
                            if (evt.getClickCount() == 2) {
                                openViewDialog(id);
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });

        // Độ rộng: Ngày, DanhMục, MoTa, Thu, Chi, LoiNhuan, NhanVien, ThaoTac
        int[] widths = {105, 130, 160, 100, 100, 110, 115, 80};
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

    // ═══════════════════════ Data Loading ════════════════════════════

    public void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count = 0;
            private String incomeText = "...", expenseText = "...", netText = "...";
            private Color netColor = AppTheme.STATUS_PAID_FG;

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    java.util.List<String> selectedCats = new java.util.ArrayList<>();
                    if (categoryCheckboxes != null) {
                        for (java.util.Map.Entry<String, JCheckBox> entry : categoryCheckboxes.entrySet()) {
                            if (entry.getValue().isSelected()) {
                                selectedCats.add(entry.getKey());
                            }
                        }
                    }

                    StringBuilder cond = new StringBuilder(" WHERE 1=1");
                    if (!selectedCats.isEmpty() && selectedCats.size() < categoryCheckboxes.size()) {
                        cond.append(" AND ltc.ten IN (");
                        for (int i = 0; i < selectedCats.size(); i++) {
                            if (i > 0) cond.append(",");
                            cond.append("'").append(selectedCats.get(i).replace("'", "''")).append("'");
                        }
                        cond.append(")");
                    }

                    if (dpFrom != null && dpFrom.getValue() != null) {
                        cond.append(" AND tc.thoi_gian >= '").append(dpFrom.getValue().toString()).append(" 00:00:00'");
                    }
                    if (dpTo != null && dpTo.getValue() != null) {
                        cond.append(" AND tc.thoi_gian <= '").append(dpTo.getValue().toString()).append(" 23:59:59'");
                    }

                    // Summary stats
                    String summarySQL = "SELECT " +
                            "  COALESCE(SUM(tc.tien_thu),0) AS thu," +
                            "  COALESCE(SUM(COALESCE(tc.tien_chi, 0) + COALESCE((SELECT SUM(ct.so_luong * sp.gia_nhap_hien_tai) FROM chi_tiet_ban_hang ct JOIN san_pham sp ON ct.id_san_pham = sp.id WHERE ct.id_ban_hang = tc.id_ban_hang), 0)), 0) AS chi" +
                            " FROM thu_chi tc" +
                            " LEFT JOIN loai_thu_chi ltc ON tc.id_loai = ltc.id" +
                            cond.toString();
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(summarySQL)) {
                        if (rs.next()) {
                            long thu = rs.getLong("thu");
                            long chi = rs.getLong("chi");
                            long net = thu - chi;
                            incomeText  = vndFormat.format(thu) + " ₫";
                            expenseText = vndFormat.format(chi) + " ₫";
                            netText     = (net >= 0 ? "+" : "") + vndFormat.format(net) + " ₫";
                            netColor    = net >= 0 ? AppTheme.STATUS_PAID_FG : AppTheme.STATUS_CANC_FG;
                        }
                    }

                    // Count
                    String countSQL = "SELECT COUNT(*) FROM thu_chi tc"
                            + " LEFT JOIN loai_thu_chi ltc ON tc.id_loai = ltc.id"
                            + cond;
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSQL)) {
                        if (rs.next()) count = rs.getInt(1);
                    }

                    // Data page
                    int offset = (currentPage - 1) * PAGE_SIZE;
                    String dataSQL = "SELECT tc.id, tc.thoi_gian,"
                            + " IFNULL(ltc.ten, '---') AS ten_loai,"
                            + " IFNULL(tc.mo_ta, '') AS mo_ta,"
                            + " tc.tien_thu,"
                            + " (CASE WHEN tc.tien_chi IS NULL AND tc.id_ban_hang IS NULL THEN NULL "
                            + "       ELSE COALESCE(tc.tien_chi, 0) + COALESCE((SELECT SUM(ct.so_luong * sp.gia_nhap_hien_tai) FROM chi_tiet_ban_hang ct JOIN san_pham sp ON ct.id_san_pham = sp.id WHERE ct.id_ban_hang = tc.id_ban_hang), 0) "
                            + "  END) AS tien_chi,"
                            + " IFNULL(nv.ten_nhan_vien, '---') AS ten_nv"
                            + " FROM thu_chi tc"
                            + " LEFT JOIN loai_thu_chi ltc ON tc.id_loai = ltc.id"
                            + " LEFT JOIN nhan_vien nv ON tc.id_nhan_vien = nv.id"
                            + cond
                            + " ORDER BY tc.thoi_gian DESC"
                            + " LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(dataSQL)) {
                        while (rs.next()) {
                            long tienThu = rs.getLong("tien_thu");
                            boolean hasThu = !rs.wasNull();
                            long tienChi = rs.getLong("tien_chi");
                            boolean hasChi = !rs.wasNull();

                            // Lợi nhuận = Thu - Chi (null → 0)
                            long thuVal = hasThu ? tienThu : 0L;
                            long chiVal = hasChi ? tienChi : 0L;
                            long loiNhuan = thuVal - chiVal;
                            String loiNhuanStr;
                            if (loiNhuan > 0)
                                loiNhuanStr = "+" + vndFormat.format(loiNhuan) + " ₫";
                            else if (loiNhuan < 0)
                                loiNhuanStr = "-" + vndFormat.format(-loiNhuan) + " ₫";
                            else
                                loiNhuanStr = "0 ₫";

                            rows.add(new Object[]{
                                "TC-" + rs.getInt("id"),
                                rs.getTimestamp("thoi_gian") != null
                                        ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                                .format(rs.getTimestamp("thoi_gian"))
                                        : "",
                                rs.getString("ten_loai"),
                                rs.getString("mo_ta"),
                                vndFormat.format(hasThu ? tienThu : 0L) + " ₫",
                                vndFormat.format(hasChi ? tienChi : 0L) + " ₫",
                                loiNhuanStr,
                                rs.getString("ten_nv"),
                                ""
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    incomeText = "Lỗi DB";
                    expenseText = "Lỗi DB";
                    netText = "Lỗi DB";
                }
                return null;
            }

            @Override
            protected void done() {
                totalItems = count;
                tableModel.setRowCount(0);
                for (Object[] row : rows) tableModel.addRow(row);
                pagination.update(totalItems, PAGE_SIZE, currentPage);
                lblTotalIncome.setText(incomeText);
                lblTotalExpense.setText(expenseText);
                lblNetFlow.setText(netText);
                lblNetFlow.setForeground(netColor);
            }
        };
        worker.execute();
    }


    // ═══════════════════════ Actions ═════════════════════════════════

    private void openDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        TransactionDialog dlg = new TransactionDialog(owner);
        dlg.setOnSaveCallback(() -> {
            currentPage = 1;
            reloadCategoryFilter();
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog(int id) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        TransactionDialog dlg = new TransactionDialog(owner, id);
        dlg.setOnSaveCallback(() -> {
            reloadCategoryFilter();
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openViewDialog(int id) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        TransactionDialog dlg = new TransactionDialog(owner, id, true);
        dlg.setVisible(true);
    }

    private void confirmDelete(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phiếu TC-" + id + " không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM thu_chi WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            // Ghi nhật ký xóa
            ActivityLogger.log(ActivityLogger.ACTION_XOA, ActivityLogger.TAB_THU_CHI,
                    "TC-" + id, "Xóa phiếu thu chi TC-" + id);
            loadData();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xóa phiếu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═══════════════════════ UI Helpers ══════════════════════════════

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_LABEL);
        l.setForeground(AppTheme.ON_SURFACE_VAR);
        return l;
    }

    private JButton iconBtn(String icon, Color color) {
        JButton b = new JButton(icon);
        b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        b.setForeground(color);
        return b;
    }

    private void updateCategoryFilterButtonText() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (JCheckBox cb : categoryCheckboxes.values()) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        if (selected.isEmpty()) {
            categoryFilterBtn.setText("Danh mục ↓");
        } else if (selected.size() == categoryCheckboxes.size()) {
            categoryFilterBtn.setText("Tất cả danh mục ↓");
        } else if (selected.size() == 1) {
            categoryFilterBtn.setText(selected.get(0) + " ↓");
        } else {
            categoryFilterBtn.setText(selected.size() + " danh mục ↓");
        }
    }
}

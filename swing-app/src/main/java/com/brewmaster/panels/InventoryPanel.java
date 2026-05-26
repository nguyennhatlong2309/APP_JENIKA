package com.brewmaster.panels;

import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Quản lý Hàng Hoá & Kho - goods-management.html
 */
public class InventoryPanel extends JPanel {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1, totalItems = 0;

    private final NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private DefaultTableModel tableModel;
    private StyledTable table;
    private Pagination pagination;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    // Status filter menu checkboxes
    private JButton statusFilterBtn;
    private JCheckBoxMenuItem menuConHang;
    private JCheckBoxMenuItem menuCanhBao;
    private JCheckBoxMenuItem menuHetHang;

    private JLabel lblTotalItems, lblLowStock, lblStockValue;

    // Lưu reference để dùng lại
    private JButton addBtn;

    public InventoryPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Page header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Quản lý Hàng Hoá & Kho");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Theo dõi tồn kho, giá nhập và phát hiện sai lệch");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(sub);

        addBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font>  Thêm hàng hóa</html>");
        addBtn.setFont(AppTheme.FONT_LABEL);
        addBtn.setForeground(AppTheme.ON_PRIMARY);
        addBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(160, 36));
        addBtn.addActionListener(e -> openAddDialog());

        header.add(titles, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // Summary cards
        lblTotalItems = makeValueLabel("...");
        lblLowStock = makeValueLabel("...");
        lblStockValue = makeValueLabel("...");
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 96));
        statsRow.add(buildStatCard("🗂", "Tổng mặt hàng", lblTotalItems, AppTheme.PRIMARY, "mặt hàng trong kho"));
        statsRow.add(buildStockValueCard());
        statsRow.add(buildStatCard("⚠", "Cảnh báo tồn kho", lblLowStock, AppTheme.STATUS_CANC_FG, "cần xử lý gấp"));

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

    private JPanel buildStatCard(String icon, String label, JLabel valueLabel, Color color, String hint) {
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

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(AppTheme.FONT_LABEL);
        hintLbl.setForeground(AppTheme.ON_SURFACE_VAR);

        JPanel col = new JPanel(new GridLayout(3, 1, 0, 2));
        col.setOpaque(false);
        col.add(lbl);
        col.add(valueLabel);
        col.add(hintLbl);

        p.add(iconLbl, BorderLayout.WEST);
        p.add(col, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildStockValueCard() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel icon = new JLabel("📊");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setPreferredSize(new Dimension(40, 40));
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textCol = new JPanel(new GridLayout(3, 1, 0, 2));
        textCol.setOpaque(false);

        JLabel lbl = new JLabel("GIÁ TRỊ TỒN KHO");
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);

        lblStockValue.setFont(AppTheme.FONT_TITLE_MD);
        lblStockValue.setForeground(AppTheme.SECONDARY);

        JLabel hint = new JLabel("Ước tính theo tồn thực tế");
        hint.setFont(AppTheme.FONT_LABEL);
        hint.setForeground(AppTheme.ON_SURFACE_VAR);

        textCol.add(lbl);
        textCol.add(lblStockValue);
        textCol.add(hint);

        p.add(icon, BorderLayout.WEST);
        p.add(textCol, BorderLayout.CENTER);
        return p;
    }

    private void updateStatusFilterButtonText() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        if (menuConHang.isSelected())
            selected.add("Còn hàng");
        if (menuCanhBao.isSelected())
            selected.add("Cảnh báo");
        if (menuHetHang.isSelected())
            selected.add("Hết hàng");

        if (selected.isEmpty()) {
            statusFilterBtn.setText("Trạng thái kho ↓");
        } else if (selected.size() == 3) {
            statusFilterBtn.setText("Tất cả trạng thái ↓");
        } else {
            statusFilterBtn.setText(String.join(", ", selected) + " ↓");
        }
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(AppTheme.withAlpha(AppTheme.SURFACE_LOW, 200));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.setFont(AppTheme.FONT_BODY_MD);
        searchField.putClientProperty("JTextField.placeholderText", " Tìm kiếm theo tên, mã...");
        searchField.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        categoryFilter = new JComboBox<>(
                new String[] { "Tất cả danh mục", "Thiết bị", "Dụng cụ", "Nguyên liệu", "Đồ uống", "Bao bì", "Khác" });
        categoryFilter.setFont(AppTheme.FONT_BODY_MD);
        categoryFilter.setPreferredSize(new Dimension(150, 32));
        categoryFilter.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        // Dropdown button for status filter with JCheckBoxMenuItems
        statusFilterBtn = new JButton("Trạng thái kho ↓");
        statusFilterBtn.setFont(AppTheme.FONT_BODY_MD);
        statusFilterBtn.setPreferredSize(new Dimension(170, 32));
        statusFilterBtn.setBackground(AppTheme.SURFACE_LOW);
        statusFilterBtn.setForeground(AppTheme.ON_SURFACE);
        statusFilterBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        statusFilterBtn.setFocusPainted(false);
        statusFilterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusFilterBtn.setHorizontalAlignment(SwingConstants.LEFT);

        JPopupMenu statusMenu = new JPopupMenu();
        statusMenu.setBackground(AppTheme.SURFACE_LOW);
        statusMenu.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        menuConHang = new JCheckBoxMenuItem("Còn hàng");
        menuCanhBao = new JCheckBoxMenuItem("Cảnh báo");
        menuHetHang = new JCheckBoxMenuItem("Hết hàng");

        for (JCheckBoxMenuItem item : new JCheckBoxMenuItem[] { menuConHang, menuCanhBao, menuHetHang }) {
            item.setFont(AppTheme.FONT_BODY_MD);
            item.setForeground(AppTheme.ON_SURFACE);
            item.setBackground(AppTheme.SURFACE_LOW);
            item.addActionListener(e -> {
                currentPage = 1;
                updateStatusFilterButtonText();
                loadData();
            });
            statusMenu.add(item);
        }

        statusFilterBtn.addActionListener(e -> {
            statusMenu.show(statusFilterBtn, 0, statusFilterBtn.getHeight());
        });

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilter.setSelectedIndex(0);
            menuConHang.setSelected(false);
            menuCanhBao.setSelected(false);
            menuHetHang.setSelected(false);
            updateStatusFilterButtonText();
            currentPage = 1;
            loadData();
        });

        JButton exportBtn = new JButton("<html><font face='Segoe UI'>⬇</font>  Xuất Excel</html>");
        exportBtn.setFont(AppTheme.FONT_LABEL);
        exportBtn.setForeground(AppTheme.ON_SURFACE);
        exportBtn.setBackground(AppTheme.SURFACE_VARIANT);
        exportBtn.setBorderPainted(true);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportToExcel());

        p.add(searchField);
        p.add(categoryFilter);
        p.add(statusFilterBtn);
        p.add(refreshBtn);
        p.add(exportBtn);
        return p;
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] cols = { "Mã", "Tên", "Đơn vị", "Giá bán", "Giá nhập", "Tồn kho", "Vốn tồn", "Trạng thái",
                "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new StyledTable(tableModel);

        // Render cột Trạng thái (cột 7) dạng badge bo tròn với màu sắc đồng bộ
        table.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
            wrapper.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            wrapper.add(StatusBadge.forStockStatus(v == null ? "" : v.toString()));
            return wrapper;
        });

        // Cột Thao tác (cột 8)
        table.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 7));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton e = new JButton("✏");
            JButton d = new JButton("🗑");
            for (JButton b : new JButton[] { e, d }) {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setOpaque(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
            }
            e.setForeground(AppTheme.PRIMARY);
            d.setForeground(AppTheme.ERROR);
            p.add(e);
            p.add(d);
            return p;
        });

        int[] widths = { 60, 200, 75, 95, 95, 70, 110, 85, 75 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        pagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });

        // Xử lý click vào cột Thao tác (cột 7)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || col != 8) // cột Thao tác giờ là số 8
                    return;

                String maHH = tableModel.getValueAt(row, 0).toString();

                // Xác định khu vực click: nửa trái = Sửa, nửa phải = Xóa
                Rectangle cellRect = table.getCellRect(row, 8, false);
                int relX = e.getX() - cellRect.x;
                boolean isEdit = relX < cellRect.width / 2;

                if (isEdit) {
                    openEditDialog(maHH);
                } else {
                    deleteProduct(maHH);
                }
            }
        });

        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(pagination, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== Dialog Helpers ====================

    /** Mở dialog thêm mới sản phẩm */
    private void openAddDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ProductDialog dlg = new ProductDialog(owner);
        dlg.setOnSaveCallback(this::loadData);
        dlg.setVisible(true);
    }

    /** Mở dialog sửa sản phẩm */
    private void openEditDialog(String maHH) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ProductDialog dlg = new ProductDialog(owner, maHH);
        dlg.setOnSaveCallback(this::loadData);
        dlg.setVisible(true);
    }

    /** Xóa sản phẩm sau khi xác nhận */
    private void deleteProduct(String idStr) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa sản phẩm ID '" + idStr + "' không?\nHành động này không thể hoàn tác.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // Bảng cfe_di_rom dùng id (int) làm khóa chính cho san_pham
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM san_pham WHERE id = ?")) {
                ps.setInt(1, Integer.parseInt(idStr));
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Đã xóa sản phẩm ID '" + idStr + "' thành công.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xóa: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel makeValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_TITLE_SM);
        l.setForeground(AppTheme.ON_SURFACE);
        return l;
    }

    public void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count = 0;
            private String statTotal = "...", statLow = "...", statValue = "...";

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    StringBuilder cond = new StringBuilder(" WHERE 1=1");
                    String search = searchField != null ? searchField.getText().trim() : "";
                    if (!search.isEmpty())
                        cond.append(" AND (sp.ten_san_pham LIKE '%").append(search)
                                .append("%' OR CAST(sp.id AS CHAR) LIKE '%").append(search).append("%')");

                    String cat = categoryFilter != null ? (String) categoryFilter.getSelectedItem() : "Tất cả danh mục";
                    if (cat != null && !cat.startsWith("Tất cả"))
                        cond.append(" AND d.ten_danh_muc='").append(cat).append("'");

                    java.util.List<String> activeStatuses = new java.util.ArrayList<>();
                    if (menuConHang != null && menuConHang.isSelected())
                        activeStatuses.add("Còn hàng");
                    if (menuCanhBao != null && menuCanhBao.isSelected())
                        activeStatuses.add("Cảnh báo");
                    if (menuHetHang != null && menuHetHang.isSelected())
                        activeStatuses.add("Hết hàng");

                    if (!activeStatuses.isEmpty() && activeStatuses.size() < 3) {
                        cond.append(" AND sp.trang_thai IN (");
                        for (int i = 0; i < activeStatuses.size(); i++) {
                            if (i > 0)
                                cond.append(",");
                            cond.append("'").append(activeStatuses.get(i)).append("'");
                        }
                        cond.append(")");
                    }

                    // Count
                    String countSql = "SELECT COUNT(*) FROM san_pham sp"
                            + " LEFT JOIN danh_muc d ON sp.id_danh_muc = d.id" + cond;
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
                        if (rs.next())
                            count = rs.getInt(1);
                    }

                    // Stats (thu thập vào biến local — không chạm UI)
                    try (Statement s = conn.createStatement()) {
                        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM san_pham");
                        if (rs.next())
                            statTotal = rs.getInt(1) + " mặt hàng";
                        rs.close();
                        // Đếm sản phẩm có trạng_thái cảnh báo hoặc hết hàng
                        rs = s.executeQuery(
                                "SELECT COUNT(*) FROM san_pham WHERE trang_thai IN ('Cảnh báo','Hết hàng')");
                        if (rs.next())
                            statLow = String.valueOf(rs.getInt(1));
                        rs.close();
                        rs = s.executeQuery("SELECT COALESCE(SUM(gia_nhap_hien_tai * so_luong_ton), 0) FROM san_pham");
                        if (rs.next())
                            statValue = vndFormat.format(rs.getLong(1)) + " ₫";
                        rs.close();
                    }

                    // Data page
                    int offset = (currentPage - 1) * PAGE_SIZE;
                    String sql = "SELECT sp.id, sp.ten_san_pham,"
                            + " d.ten_danh_muc AS danh_muc,"
                            + " dv.ten_don_vi AS don_vi,"
                            + " sp.so_luong_ton,"
                            + " sp.gia_nhap_hien_tai,"
                            + " sp.gia_ban_hien_tai,"
                            + " sp.trang_thai"
                            + " FROM san_pham sp"
                            + " LEFT JOIN danh_muc d ON sp.id_danh_muc = d.id"
                            + " LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id"
                            + cond
                            + " ORDER BY sp.id LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                        while (rs.next()) {
                            long soLuong = rs.getLong("so_luong_ton");
                            long giaNhap = rs.getLong("gia_nhap_hien_tai");
                            long giaBan = rs.getLong("gia_ban_hien_tai");
                            rows.add(new Object[] {
                                    String.valueOf(rs.getInt("id")),
                                    rs.getString("ten_san_pham")
                                            + (rs.getString("danh_muc") != null ? "\n" + rs.getString("danh_muc") : ""),
                                    rs.getString("don_vi"),
                                    vndFormat.format(giaBan) + " ₫",
                                    vndFormat.format(giaNhap) + " ₫",
                                    soLuong,
                                    vndFormat.format(soLuong * giaNhap) + " ₫",
                                    rs.getString("trang_thai"), // cột 7: Trạng thái
                                    "" // cột 8: Thao tác
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
                // Cập nhật toàn bộ UI trên EDT
                totalItems = count;
                tableModel.setRowCount(0);
                for (Object[] row : rows)
                    tableModel.addRow(row);
                pagination.update(totalItems, PAGE_SIZE, currentPage);
                lblTotalItems.setText(statTotal);
                lblLowStock.setText(statLow);
                lblStockValue.setText(statValue);
            }
        };
        worker.execute();
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu danh sách hàng hóa");
        fileChooser.setSelectedFile(new File("danh_sach_hang_hoa.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".csv")) {
            fileToSave = new File(filePath + ".csv");
        }

        final File targetFile = fileToSave;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;
            private String errorMessage = "";

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    StringBuilder cond = new StringBuilder(" WHERE 1=1");
                    String search = searchField != null ? searchField.getText().trim() : "";
                    if (!search.isEmpty())
                        cond.append(" AND (sp.ten_san_pham LIKE '%").append(search)
                                .append("%' OR CAST(sp.id AS CHAR) LIKE '%").append(search).append("%')");

                    String cat = categoryFilter != null ? (String) categoryFilter.getSelectedItem() : "Tất cả danh mục";
                    if (cat != null && !cat.startsWith("Tất cả"))
                        cond.append(" AND d.ten_danh_muc='").append(cat).append("'");

                    java.util.List<String> activeStatuses = new java.util.ArrayList<>();
                    if (menuConHang != null && menuConHang.isSelected())
                        activeStatuses.add("Còn hàng");
                    if (menuCanhBao != null && menuCanhBao.isSelected())
                        activeStatuses.add("Cảnh báo");
                    if (menuHetHang != null && menuHetHang.isSelected())
                        activeStatuses.add("Hết hàng");

                    if (!activeStatuses.isEmpty() && activeStatuses.size() < 3) {
                        cond.append(" AND sp.trang_thai IN (");
                        for (int i = 0; i < activeStatuses.size(); i++) {
                            if (i > 0)
                                cond.append(",");
                            cond.append("'").append(activeStatuses.get(i)).append("'");
                        }
                        cond.append(")");
                    }

                    String sql = "SELECT sp.id, sp.ten_san_pham,"
                            + " d.ten_danh_muc AS danh_muc,"
                            + " dv.ten_don_vi AS don_vi,"
                            + " sp.so_luong_ton,"
                            + " sp.gia_nhap_hien_tai,"
                            + " sp.gia_ban_hien_tai,"
                            + " sp.trang_thai"
                            + " FROM san_pham sp"
                            + " LEFT JOIN danh_muc d ON sp.id_danh_muc = d.id"
                            + " LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id"
                            + cond
                            + " ORDER BY sp.id";

                    try (Statement s = conn.createStatement();
                            ResultSet rs = s.executeQuery(sql);
                            FileOutputStream fos = new FileOutputStream(targetFile);
                            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                            BufferedWriter writer = new BufferedWriter(osw)) {

                        // Ghi UTF-8 BOM
                        writer.write('\uFEFF');

                        // Tiêu đề cột
                        writer.write(
                                "Mã sản phẩm,Tên sản phẩm,Danh mục,Đơn vị tính,Số lượng tồn,Giá nhập,Giá bán,Vốn tồn,Trạng thái");
                        writer.newLine();

                        while (rs.next()) {
                            int id = rs.getInt("id");
                            String tenSP = rs.getString("ten_san_pham");
                            String danhMuc = rs.getString("danh_muc");
                            String donVi = rs.getString("don_vi");
                            long soLuong = rs.getLong("so_luong_ton");
                            long giaNhap = rs.getLong("gia_nhap_hien_tai");
                            long giaBan = rs.getLong("gia_ban_hien_tai");
                            long vonTon = soLuong * giaNhap;
                            String trangThai = rs.getString("trang_thai");

                            writer.write(id + ",");
                            writer.write(escapeCsv(tenSP) + ",");
                            writer.write(escapeCsv(danhMuc) + ",");
                            writer.write(escapeCsv(donVi) + ",");
                            writer.write(soLuong + ",");
                            writer.write(giaNhap + ",");
                            writer.write(giaBan + ",");
                            writer.write(vonTon + ",");
                            writer.write(escapeCsv(trangThai) + "");
                            writer.newLine();
                        }
                        success = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                            "Đã xuất dữ liệu hàng tồn kho ra file Excel (CSV) thành công!",
                            "Xuất Excel thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                            "Gặp lỗi khi xuất dữ liệu: " + errorMessage,
                            "Lỗi xuất Excel",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim();
        if (result.contains(",") || result.contains("\"") || result.contains("\n") || result.contains("\r")) {
            result = result.replace("\"", "\"\"");
            return "\"" + result + "\"";
        }
        return result;
    }
}

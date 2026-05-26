package com.brewmaster.panels;

import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

/**
 * Màn hình Đối tác & Nhân viên
 */
public class PartnersPanel extends JPanel {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;

    private DefaultTableModel partnerModel, staffModel;
    private StyledTable partnerTable, staffTable;
    private Pagination partnerPagination, staffPagination;
    private JTextField searchField;
    private JTabbedPane tabs;

    public PartnersPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Đối tác & Nhân viên");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Quản lý nhà cung cấp, khách hàng và đội ngũ nhân viên");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(sub);

        JButton addBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font>  Thêm mới</html>");
        addBtn.setFont(AppTheme.FONT_LABEL);
        addBtn.setForeground(AppTheme.ON_PRIMARY);
        addBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setPreferredSize(new Dimension(120, 36));
        // *** ACTION LISTENER ***
        addBtn.addActionListener(e -> openAddDialog());

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(110, 36));
        refreshBtn.addActionListener(e -> {
            if (searchField != null) {
                searchField.setText("");
            }
            currentPage = 1;
            loadData();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        // ---- Tabs ----
        tabs = new JTabbedPane();
        tabs.setFont(AppTheme.FONT_LABEL);
        tabs.setBackground(AppTheme.SURFACE_LOW);
        tabs.setForeground(AppTheme.ON_SURFACE_VAR);

        tabs.add("", buildPartnersTab());
        tabs.setTabComponentAt(0, buildTabHeader("🤝", "Đối tác / Nhà cung cấp"));

        tabs.add("", buildStaffTab());
        tabs.setTabComponentAt(1, buildTabHeader("👤", "Nhân viên"));

        content.add(header, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    /** Mở dialog thêm mới theo tab đang chọn */
    private void openAddDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame owner = (window instanceof Frame) ? (Frame) window : null;

        if (tabs.getSelectedIndex() == 0) {
            PartnerDialog dlg = new PartnerDialog(owner);
            dlg.setOnSaveCallback(this::loadData);
            dlg.setVisible(true);
        } else {
            StaffDialog dlg = new StaffDialog(owner);
            dlg.setOnSaveCallback(this::loadData);
            dlg.setVisible(true);
        }
    }

    // ===================== Partners Tab =====================

    private JPanel buildPartnersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Filter
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setBackground(AppTheme.SURFACE_LOW);
        filter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 32));
        searchField.setFont(AppTheme.FONT_BODY_MD);
        searchField.putClientProperty("JTextField.placeholderText", " Tìm tên, số điện thoại...");
        searchField.addActionListener(e -> loadData());

        JButton searchBtn = new JButton("Tìm kiếm");
        searchBtn.setFont(AppTheme.FONT_LABEL);
        searchBtn.setBackground(AppTheme.SURFACE_VARIANT);
        searchBtn.setForeground(AppTheme.ON_SURFACE);
        searchBtn.setBorderPainted(true);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> loadData());

        filter.add(new JLabel("Tìm:") {
            {
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.ON_SURFACE_VAR);
            }
        });
        filter.add(searchField);
        filter.add(searchBtn);

        // Table — cột 5 (_id ẩn)
        String[] cols = { "STT", "Tên đối tác", "Điện thoại", "Email", "Thao tác", "_id" };
        partnerModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        partnerTable = new StyledTable(partnerModel);

        // Ẩn cột _id
        hideColumn(partnerTable, 5);

        // Nút Sửa trong cột Thao tác
        partnerTable.getColumnModel().getColumn(4).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel w = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            w.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            w.add(makeActionButton("✏", AppTheme.PRIMARY));
            return w;
        });
        partnerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = partnerTable.rowAtPoint(e.getPoint());
                int col = partnerTable.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    int rawId = (int) partnerModel.getValueAt(row, 5);
                    Window window = SwingUtilities.getWindowAncestor(PartnersPanel.this);
                    Frame owner = (window instanceof Frame) ? (Frame) window : null;
                    PartnerDialog dlg = new PartnerDialog(owner, rawId, "");
                    dlg.setOnSaveCallback(PartnersPanel.this::loadData);
                    dlg.setVisible(true);
                }
            }
        });

        int[] widths = { 55, 240, 140, 180, 70 };
        for (int i = 0; i < widths.length; i++)
            partnerTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        partnerPagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(AppTheme.SURFACE_LOW);
        tablePanel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        tablePanel.add(partnerTable.wrapInScrollPane(), BorderLayout.CENTER);
        tablePanel.add(partnerPagination, BorderLayout.SOUTH);

        panel.add(filter, BorderLayout.NORTH);
        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    // ===================== Staff Tab =====================

    private JPanel buildStaffTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // cột 8 (_id ẩn)
        String[] cols = { "Mã NV", "Tên nhân viên", "Vai trò", "Điện thoại", "Email", "Ngày vào làm", "Trạng thái",
                "Thao tác", "_id" };
        staffModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        staffTable = new StyledTable(staffModel);
        hideColumn(staffTable, 8);

        // Badge trạng thái
        staffTable.getColumnModel().getColumn(6).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel w = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
            w.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            String status = v == null ? "" : v.toString();
            Color col2 = "Đang làm".equals(status) ? AppTheme.STATUS_PAID_FG : AppTheme.STATUS_CANC_FG;
            w.add(new StatusBadge(status, AppTheme.withAlpha(col2, 25), col2));
            return w;
        });

        // Nút Sửa
        staffTable.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel w = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            w.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            w.add(makeActionButton("✏", AppTheme.PRIMARY));
            return w;
        });
        staffTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = staffTable.rowAtPoint(e.getPoint());
                int col = staffTable.columnAtPoint(e.getPoint());
                if (col == 7 && row >= 0) {
                    int rawId = (int) staffModel.getValueAt(row, 8);
                    Window window = SwingUtilities.getWindowAncestor(PartnersPanel.this);
                    Frame owner = (window instanceof Frame) ? (Frame) window : null;
                    StaffDialog dlg = new StaffDialog(owner, rawId);
                    dlg.setOnSaveCallback(PartnersPanel.this::loadData);
                    dlg.setVisible(true);
                }
            }
        });

        int[] widths = { 70, 160, 110, 110, 140, 110, 90, 70 };
        for (int i = 0; i < widths.length; i++)
            staffTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        staffPagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(AppTheme.SURFACE_LOW);
        tablePanel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        tablePanel.add(staffTable.wrapInScrollPane(), BorderLayout.CENTER);
        tablePanel.add(staffPagination, BorderLayout.SOUTH);

        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    // ===================== Load Data =====================

    public void loadData() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> partnerRows = new java.util.ArrayList<>();
            private java.util.List<Object[]> staffRows = new java.util.ArrayList<>();

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();
                    String search = searchField != null ? searchField.getText().trim() : "";

                    // Query đơn giản từ doi_tac (không cần UNION ALL nữa)
                    String cond = search.isEmpty() ? ""
                            : " WHERE ten LIKE '%" + search + "%' OR sdt LIKE '%" + search + "%'";

                    String partnerSql = "SELECT id, ten, sdt FROM doi_tac"
                            + cond + " ORDER BY id";

                    try (Statement s = conn.createStatement();
                            ResultSet rs = s.executeQuery(partnerSql)) {
                        int seq = 1;
                        while (rs.next()) {
                            partnerRows.add(new Object[] {
                                    seq++,
                                    rs.getString("ten"),
                                    rs.getString("sdt"),
                                    "",    // email placeholder
                                    "",    // action column
                                    rs.getInt("id")
                            });
                        }
                    }

                    try (Statement s = conn.createStatement();
                            ResultSet rs = s.executeQuery(
                                    "SELECT id, ten_nhan_vien, vai_tro, sdt FROM nhan_vien ORDER BY id")) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            staffRows.add(new Object[] {
                                    "NV-" + id,
                                    rs.getString("ten_nhan_vien"),
                                    rs.getString("vai_tro"),
                                    rs.getString("sdt"),
                                    "", "", "Đang làm", "", id
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
                partnerModel.setRowCount(0);
                for (Object[] row : partnerRows)
                    partnerModel.addRow(row);
                partnerPagination.update(partnerModel.getRowCount(), PAGE_SIZE, 1);

                staffModel.setRowCount(0);
                for (Object[] row : staffRows)
                    staffModel.addRow(row);
                staffPagination.update(staffModel.getRowCount(), PAGE_SIZE, 1);
            }
        };
        worker.execute();
    }

    // ===================== Helpers =====================

    private void hideColumn(JTable table, int col) {
        TableColumn tc = table.getColumnModel().getColumn(col);
        tc.setMinWidth(0);
        tc.setMaxWidth(0);
        tc.setWidth(0);
    }

    private JButton makeActionButton(String icon, Color color) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setBackground(AppTheme.withAlpha(color, 20));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.withAlpha(color, 60), 1),
                new EmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildTabHeader(String icon, String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_LABEL);
        titleLbl.setForeground(AppTheme.ON_SURFACE);
        p.add(iconLbl);
        p.add(titleLbl);
        return p;
    }
}

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
import java.text.SimpleDateFormat;
import java.time.LocalDate;

/**
 * Màn hình Nhật Ký Hoạt Động
 * Hiển thị toàn bộ thao tác Thêm / Sửa / Xóa được ghi vào bảng nhat_ky
 * Cột: Thời gian | Thao tác | Tab | Mã bản ghi | Mô tả
 */
public class ActivityLogPanel extends JPanel {

    private static final int PAGE_SIZE = 15;
    private int currentPage = 1;
    private int totalItems  = 0;

    private DefaultTableModel tableModel;
    private StyledTable       table;
    private Pagination        pagination;

    // Bộ lọc
    private JComboBox<String> cbTab;
    private JComboBox<String> cbAction;
    private JTextField        tfSearch;
    private DatePicker        dpFrom;
    private DatePicker        dpTo;

    // Summary labels
    private JLabel lblTotal, lblToday, lblActions;

    public ActivityLogPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    // ───────────────────────────── Build UI ──────────────────────────────

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ── Header ──
        JPanel header = buildHeader();

        // ── Summary Cards ──
        lblTotal   = new JLabel("...");
        lblToday   = new JLabel("...");
        lblActions = new JLabel("...");
        JPanel summary = buildSummaryRow();

        // ── Filters ──
        JPanel filters = buildFilters();

        // ── Table ──
        JPanel tableSection = buildTableSection();

        JPanel topPanel = new JPanel(new BorderLayout(0, 12));
        topPanel.setOpaque(false);
        topPanel.add(summary, BorderLayout.NORTH);
        topPanel.add(filters, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);
        main.add(topPanel,      BorderLayout.NORTH);
        main.add(tableSection,  BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(main,   BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);

        JLabel title = new JLabel("Nhật Ký Hoạt Động");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);

        JLabel sub = new JLabel("Theo dõi toàn bộ thao tác thay đổi dữ liệu trong hệ thống");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);

        titles.add(title);
        titles.add(sub);

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            tfSearch.setText("");
            cbTab.setSelectedIndex(0);
            cbAction.setSelectedIndex(0);
            dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
            dpTo.setValue(LocalDate.now());
            currentPage = 1;
            loadData();
        });

        header.add(titles,    BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 90));
        row.setBorder(new EmptyBorder(0, 0, 4, 0));

        row.add(buildCard("📋", "Tổng bản ghi",    lblTotal,   AppTheme.PRIMARY));
        row.add(buildCard("📅", "Bản ghi trong kỳ",  lblToday,   new Color(16, 185, 129)));
        row.add(buildCard("✏", "Thao tác trong kỳ", lblActions, new Color(245, 158, 11)));
        return row;
    }

    private JPanel buildCard(String icon, String label, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLbl.setOpaque(true);
        iconLbl.setBackground(AppTheme.withAlpha(color, 25));
        iconLbl.setPreferredSize(new Dimension(48, 48));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        valueLabel.setFont(AppTheme.FONT_TITLE_MD);
        valueLabel.setForeground(color);
        text.add(lbl);
        text.add(valueLabel);

        p.add(iconLbl, BorderLayout.WEST);
        p.add(text,    BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFilters() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        // Tìm kiếm mô tả / mã bản ghi
        tfSearch = new JTextField();
        tfSearch.setPreferredSize(new Dimension(210, 32));
        tfSearch.setFont(AppTheme.FONT_BODY_MD);
        tfSearch.putClientProperty("JTextField.placeholderText", "🔍  Tìm mã / mô tả...");
        tfSearch.addActionListener(e -> { currentPage = 1; loadData(); });

        // Lọc theo Tab
        cbTab = new JComboBox<>(new String[]{
                "Tất cả tab", "Bán hàng", "Nhập hàng", "Thu Chi"
        });
        cbTab.setFont(AppTheme.FONT_BODY_MD);
        cbTab.setPreferredSize(new Dimension(140, 32));
        cbTab.addActionListener(e -> { currentPage = 1; loadData(); });

        // Lọc theo Thao tác
        cbAction = new JComboBox<>(new String[]{
                "Tất cả thao tác", "Thêm", "Sửa", "Xóa"
        });
        cbAction.setFont(AppTheme.FONT_BODY_MD);
        cbAction.setPreferredSize(new Dimension(150, 32));
        cbAction.addActionListener(e -> { currentPage = 1; loadData(); });

        dpFrom = new DatePicker();
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpFrom.setPreferredSize(new Dimension(130, 32));
        dpFrom.addActionListener(e -> { currentPage = 1; loadData(); });

        dpTo = new DatePicker();
        dpTo.setValue(LocalDate.now());
        dpTo.setPreferredSize(new Dimension(130, 32));
        dpTo.addActionListener(e -> { currentPage = 1; loadData(); });

        p.add(makeLabel("Tìm kiếm:"));
        p.add(tfSearch);
        p.add(makeLabel("Tab:"));
        p.add(cbTab);
        p.add(makeLabel("Thao tác:"));
        p.add(cbAction);
        p.add(makeLabel("Từ ngày:"));
        p.add(dpFrom);
        p.add(makeLabel("Đến ngày:"));
        p.add(dpTo);
        return p;
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] cols = {"Thời gian", "Thao tác", "Tab", "Mã bản ghi", "Mô tả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new StyledTable(tableModel);
        table.setAutoCreateRowSorter(false);

        // ── Cột Thời gian (0) ──
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.ON_SURFACE_VAR);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // ── Cột Thao tác (1) ── badge màu sắc
        table.getColumnModel().getColumn(1).setCellRenderer((t, v, sel, foc, r, c) -> {
            String val = v == null ? "" : v.toString();
            Color bg, fg;
            switch (val) {
                case "Thêm":
                    bg = AppTheme.STATUS_PAID_BG;
                    fg = AppTheme.STATUS_PAID_FG;
                    break;
                case "Sửa":
                    bg = AppTheme.STATUS_PEND_BG;
                    fg = AppTheme.STATUS_PEND_FG;
                    break;
                case "Xóa":
                    bg = AppTheme.STATUS_CANC_BG;
                    fg = AppTheme.STATUS_CANC_FG;
                    break;
                default:
                    bg = AppTheme.SURFACE_VARIANT;
                    fg = AppTheme.ON_SURFACE_VAR;
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
            wrapper.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            wrapper.add(new StatusBadge(val, bg, fg));
            return wrapper;
        });

        // ── Cột Tab (2) ──
        table.getColumnModel().getColumn(2).setCellRenderer((t, v, sel, foc, r, c) -> {
            String val = v == null ? "" : v.toString();
            Color tagColor;
            switch (val) {
                case "Bán hàng":  tagColor = AppTheme.PRIMARY;  break;
                case "Nhập hàng": tagColor = AppTheme.SECONDARY; break;
                default:          tagColor = new Color(168, 85, 247); // purple cho Thu Chi
            }
            JLabel lbl = new JLabel("  " + val + "  ");
            lbl.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD));
            lbl.setForeground(tagColor);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            lbl.setBorder(new EmptyBorder(0, 8, 0, 4));
            return lbl;
        });

        // ── Cột Mã bản ghi (3) ──
        table.getColumnModel().getColumn(3).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "—" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD));
            lbl.setForeground(AppTheme.PRIMARY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // ── Cột Mô tả (4) ─ Chỉ hiển thị 1 dòng (ellipsis tự động với JLabel) ──
        table.getColumnModel().getColumn(4).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_BODY_MD);
            lbl.setForeground(AppTheme.ON_SURFACE);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 12));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // Độ rộng cột
        int[] widths = {140, 100, 105, 100, 370};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Row height
        table.setRowHeight(36);

        // Lắng nghe sự kiện double click vào hàng
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        showDetailDialog(modelRow);
                    }
                }
            }
        });

        pagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });

        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(pagination, BorderLayout.SOUTH);
        return panel;
    }

    // ───────────────────────── Data Loading ──────────────────────────────

    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count   = 0;
            private int todayCnt = 0;
            private String totalText   = "...";
            private String todayText   = "...";
            private String actionsText = "...";

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    // Xây điều kiện WHERE
                    StringBuilder cond = new StringBuilder(" WHERE 1=1");

                    String searchText = tfSearch != null ? tfSearch.getText().trim() : "";
                    if (!searchText.isEmpty()) {
                        String escaped = searchText.replace("'", "''");
                        cond.append(" AND (nk.ma_ban_ghi LIKE '%").append(escaped)
                            .append("%' OR nk.mo_ta LIKE '%").append(escaped).append("%')");
                    }

                    String tabVal = cbTab != null ? (String) cbTab.getSelectedItem() : "Tất cả tab";
                    if (tabVal != null && !tabVal.equals("Tất cả tab")) {
                        cond.append(" AND nk.tab = '").append(tabVal.replace("'", "''")).append("'");
                    }

                    String actionVal = cbAction != null ? (String) cbAction.getSelectedItem() : "Tất cả thao tác";
                    if (actionVal != null && !actionVal.equals("Tất cả thao tác")) {
                        cond.append(" AND nk.thao_tac = '").append(actionVal.replace("'", "''")).append("'");
                    }

                    if (dpFrom != null && dpFrom.getValue() != null) {
                        cond.append(" AND nk.thoi_gian >= '").append(dpFrom.getValue().toString()).append(" 00:00:00'");
                    }
                    if (dpTo != null && dpTo.getValue() != null) {
                        cond.append(" AND nk.thoi_gian <= '").append(dpTo.getValue().toString()).append(" 23:59:59'");
                    }

                    // Summary: tổng & trong kỳ
                    try (Statement st = conn.createStatement()) {
                        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM nhat_ky");
                        if (rs.next()) totalText = rs.getInt(1) + " bản ghi";
                        rs.close();

                        String dateCond = "";
                        if (dpFrom != null && dpFrom.getValue() != null) {
                            dateCond += " AND thoi_gian >= '" + dpFrom.getValue().toString() + " 00:00:00'";
                        }
                        if (dpTo != null && dpTo.getValue() != null) {
                            dateCond += " AND thoi_gian <= '" + dpTo.getValue().toString() + " 23:59:59'";
                        }

                        rs = st.executeQuery(
                                "SELECT COUNT(*), COUNT(DISTINCT thao_tac) FROM nhat_ky WHERE 1=1" + dateCond);
                        if (rs.next()) {
                            todayCnt   = rs.getInt(1);
                            todayText  = todayCnt + " bản ghi";
                            actionsText = rs.getInt(2) + " loại";
                        }
                        rs.close();
                    }

                    // Count với filter
                    String countSql = "SELECT COUNT(*) FROM nhat_ky nk" + cond;
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
                        if (rs.next()) count = rs.getInt(1);
                    }

                    // Data page
                    int offset = (currentPage - 1) * PAGE_SIZE;
                    String dataSql = "SELECT nk.thoi_gian, nk.thao_tac, nk.tab, nk.ma_ban_ghi, nk.mo_ta"
                            + " FROM nhat_ky nk"
                            + cond
                            + " ORDER BY nk.thoi_gian DESC"
                            + " LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(dataSql)) {
                        while (rs.next()) {
                            Timestamp ts = rs.getTimestamp("thoi_gian");
                            rows.add(new Object[]{
                                ts != null ? sdf.format(ts) : "",
                                rs.getString("thao_tac"),
                                rs.getString("tab"),
                                rs.getString("ma_ban_ghi") != null ? rs.getString("ma_ban_ghi") : "—",
                                rs.getString("mo_ta") != null ? rs.getString("mo_ta") : ""
                            });
                        }
                    }

                } catch (Exception e) {
                    System.err.println("[ActivityLogPanel] Lỗi tải nhật ký: " + e.getMessage());
                    e.printStackTrace();
                    totalText   = "Lỗi DB";
                    todayText   = "Lỗi DB";
                    actionsText = "Lỗi DB";
                }
                return null;
            }

            @Override
            protected void done() {
                totalItems = count;
                tableModel.setRowCount(0);
                for (Object[] row : rows) tableModel.addRow(row);
                pagination.update(totalItems, PAGE_SIZE, currentPage);
                lblTotal.setText(totalText);
                lblToday.setText(todayText);
                lblActions.setText(actionsText);
            }
        };
        worker.execute();
    }

    // ───────────────────────── Helpers ───────────────────────────────────

    private void showDetailDialog(int modelRow) {
        String thoiGian = tableModel.getValueAt(modelRow, 0).toString();
        String thaoTac = tableModel.getValueAt(modelRow, 1).toString();
        String tab = tableModel.getValueAt(modelRow, 2).toString();
        String maBanGhi = tableModel.getValueAt(modelRow, 3).toString();
        String moTa = tableModel.getValueAt(modelRow, 4).toString();

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner, "Chi Tiết Nhật Ký Hoạt Động  ", true);
        dlg.setSize(560, 500);
        dlg.setMinimumSize(new Dimension(500, 450));
        dlg.setLocationRelativeTo(this);
        
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 20, 12, 14)));

        JLabel title = new JLabel("<html><nobr><font face='Segoe UI Emoji'>📓</font>  Chi Tiết Nhật Ký Hoạt Động</nobr></html>");
        title.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 16f));
        title.setForeground(AppTheme.ON_SURFACE);
        title.setBorder(new EmptyBorder(6, 0, 6, 0));

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setForeground(AppTheme.ON_SURFACE_VAR);
        closeBtn.setBackground(AppTheme.SURFACE_HIGH);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(evt -> dlg.dispose());
        closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { closeBtn.setForeground(AppTheme.ERROR); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { closeBtn.setForeground(AppTheme.ON_SURFACE_VAR); }
        });

        header.add(title, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        // Form
        JPanel form = new JPanel(new BorderLayout(0, 16));
        form.setBackground(AppTheme.SURFACE_HIGH);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Upper info: 2x2 grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 12));
        grid.setOpaque(false);

        grid.add(fieldBlockReadOnly("Thời gian", thoiGian));

        // Thao tác with Badge
        JPanel statusBlock = new JPanel(new BorderLayout(0, 5));
        statusBlock.setOpaque(false);
        JLabel lblThaoTac = new JLabel("Thao tác");
        lblThaoTac.setFont(AppTheme.FONT_LABEL);
        lblThaoTac.setForeground(AppTheme.ON_SURFACE_VAR);
        statusBlock.add(lblThaoTac, BorderLayout.NORTH);

        Color bg, fg;
        switch (thaoTac) {
            case "Thêm":
                bg = AppTheme.STATUS_PAID_BG;
                fg = AppTheme.STATUS_PAID_FG;
                break;
            case "Sửa":
                bg = AppTheme.STATUS_PEND_BG;
                fg = AppTheme.STATUS_PEND_FG;
                break;
            case "Xóa":
                bg = AppTheme.STATUS_CANC_BG;
                fg = AppTheme.STATUS_CANC_FG;
                break;
            default:
                bg = AppTheme.SURFACE_VARIANT;
                fg = AppTheme.ON_SURFACE_VAR;
        }
        
        JPanel badgeContainer = new JPanel(new BorderLayout());
        badgeContainer.setBackground(AppTheme.SURFACE_MED);
        badgeContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        badgeContainer.setPreferredSize(new Dimension(0, 34));
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(new StatusBadge(thaoTac, bg, fg));
        badgeContainer.add(badgeWrapper, BorderLayout.WEST);
        statusBlock.add(badgeContainer, BorderLayout.CENTER);
        grid.add(statusBlock);

        grid.add(fieldBlockReadOnly("Phân hệ (Tab)", tab));
        grid.add(fieldBlockReadOnly("Mã bản ghi", maBanGhi));

        form.add(grid, BorderLayout.NORTH);

        // Lower info: Description JTextArea
        JPanel noteBlock = new JPanel(new BorderLayout(0, 5));
        noteBlock.setOpaque(false);
        JLabel lblNote = new JLabel("Mô tả chi tiết / Ghi chú");
        lblNote.setFont(AppTheme.FONT_LABEL);
        lblNote.setForeground(AppTheme.ON_SURFACE_VAR);
        noteBlock.add(lblNote, BorderLayout.NORTH);

        JTextArea taMoTa = new JTextArea(moTa);
        taMoTa.setEditable(false);
        taMoTa.setFont(AppTheme.FONT_BODY_MD);
        taMoTa.setForeground(AppTheme.ON_SURFACE);
        taMoTa.setBackground(AppTheme.SURFACE_MED);
        taMoTa.setLineWrap(true);
        taMoTa.setWrapStyleWord(true);
        taMoTa.setBorder(new EmptyBorder(8, 10, 8, 10));
        taMoTa.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(taMoTa);
        scroll.setBackground(AppTheme.SURFACE_MED);
        scroll.getViewport().setBackground(AppTheme.SURFACE_MED);
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        noteBlock.add(scroll, BorderLayout.CENTER);
        form.add(noteBlock, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(AppTheme.SURFACE_HIGH);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        btnClose.setForeground(AppTheme.ON_PRIMARY);
        btnClose.setBackground(AppTheme.PRIMARY);
        btnClose.setBorder(new EmptyBorder(8, 24, 8, 24));
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(evt -> dlg.dispose());
        btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnClose.setBackground(AppTheme.PRIMARY_DARK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnClose.setBackground(AppTheme.PRIMARY);
            }
        });

        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private JPanel fieldBlockReadOnly(String labelText, String val) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);

        JTextField tf = new JTextField(val);
        tf.setEditable(false);
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 34));

        p.add(lbl, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.FONT_LABEL);
        l.setForeground(AppTheme.ON_SURFACE_VAR);
        return l;
    }
}

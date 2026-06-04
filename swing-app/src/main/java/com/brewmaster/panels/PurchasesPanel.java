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
    private int prodCurrentPage = 1, prodTotalItems = 0;
    private final NumberFormat vndFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private JTabbedPane tabbedPane;

    // === Tab 1 (Invoice) UI components ===
    private DefaultTableModel tableModel;
    private StyledTable table;
    private Pagination pagination;
    private JTextField searchField;
    private JButton statusFilterBtn;
    private JPopupMenu statusMenu;
    private JTextField statusSearchField;
    private final java.util.Map<String, JCheckBox> statusCheckboxes = new java.util.LinkedHashMap<>();
    private DatePicker dpFrom;
    private DatePicker dpTo;
    private JLabel lblTotalOrders, lblTotalValue, lblDebtValue;

    // === Tab 2 (Product) UI components ===
    private DefaultTableModel productTableModel;
    private StyledTable productTable;
    private Pagination prodPagination;
    private JTextField prodSearchField;
    private DatePicker prodDpFrom;
    private DatePicker prodDpTo;
    private JLabel lblProdTotalValue, lblProdTotalQty, lblProdDistinctCount;

    public PurchasesPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

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
        addBtn.setBackground(AppTheme.PRIMARY);
        addBtn.setBorderPainted(false);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(150, 36));
        addBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openCreateDialog());

        header.add(titles, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        // === TAB 1: DANH SÁCH ĐƠN NHẬP ===
        JPanel tab1Panel = new JPanel(new BorderLayout(0, 12));
        tab1Panel.setOpaque(false);

        lblTotalOrders = new JLabel("...");
        lblTotalValue = new JLabel("...");
        lblDebtValue = new JLabel("...");
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 96));
        statsRow.add(buildStatCard("📋", "Tổng đơn nhập", lblTotalOrders, AppTheme.PRIMARY));
        statsRow.add(buildStatCard("💵", "Giá trị mua hàng", lblTotalValue, AppTheme.SECONDARY));
        statsRow.add(buildStatCard("⚡", "Còn nợ nhà CC", lblDebtValue, AppTheme.STATUS_CANC_FG));

        JPanel topPanel1 = new JPanel(new BorderLayout(0, 12));
        topPanel1.setOpaque(false);
        topPanel1.add(statsRow, BorderLayout.NORTH);
        topPanel1.add(buildFilters(), BorderLayout.CENTER);

        tab1Panel.add(topPanel1, BorderLayout.NORTH);
        tab1Panel.add(buildTableSection(), BorderLayout.CENTER);

        // === TAB 2: THEO SẢN PHẨM ===
        JPanel tab2Panel = new JPanel(new BorderLayout(0, 12));
        tab2Panel.setOpaque(false);

        JPanel topPanel2 = new JPanel(new BorderLayout(0, 12));
        topPanel2.setOpaque(false);
        topPanel2.add(buildProductSummaryCards(), BorderLayout.NORTH);
        topPanel2.add(buildProductFilterSection(), BorderLayout.CENTER);

        tab2Panel.add(topPanel2, BorderLayout.NORTH);
        tab2Panel.add(buildProductTableSection(), BorderLayout.CENTER);

        // === TABBED PANE ===
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppTheme.FONT_LABEL);
        tabbedPane.addTab("Danh sách Đơn Nhập", tab1Panel);
        tabbedPane.addTab("Xem theo Sản phẩm", tab2Panel);

        tabbedPane.addChangeListener(e -> {
            loadData();
        });

        content.add(header, BorderLayout.NORTH);
        content.add(tabbedPane, BorderLayout.CENTER);

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

    private JPanel buildProductSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 96));

        lblProdTotalValue = new JLabel("...");
        lblProdTotalQty = new JLabel("...");
        lblProdDistinctCount = new JLabel("...");

        row.add(buildStatCard("💵", "Tổng giá trị nhập", lblProdTotalValue, AppTheme.PRIMARY));
        row.add(buildStatCard("📦", "Tổng số lượng nhập", lblProdTotalQty, AppTheme.SECONDARY));
        row.add(buildStatCard("📋", "Số mặt hàng nhập", lblProdDistinctCount, AppTheme.STATUS_CANC_FG));

        return row;
    }

    private JPanel buildProductFilterSection() {
        JPanel p = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 5));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(8, 12, 8, 12)));

        prodSearchField = new JTextField();
        prodSearchField.setPreferredSize(new Dimension(200, 32));
        prodSearchField.setFont(AppTheme.FONT_BODY_MD);
        prodSearchField.putClientProperty("JTextField.placeholderText", " Tìm sản phẩm, nhà CC, mã...");
        prodSearchField.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });

        prodDpFrom = new DatePicker();
        prodDpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        prodDpFrom.setPreferredSize(new Dimension(130, 32));
        prodDpFrom.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });

        prodDpTo = new DatePicker();
        prodDpTo.setValue(LocalDate.now());
        prodDpTo.setPreferredSize(new Dimension(130, 32));
        prodDpTo.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });

        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            prodSearchField.setText("");
            prodDpFrom.setValue(LocalDate.now().withDayOfMonth(1));
            prodDpTo.setValue(LocalDate.now());
            prodCurrentPage = 1;
            loadData();
        });

        JButton exportBtn = new JButton("<html><font face='Segoe UI'>⬇</font>  Xuất Excel</html>");
        exportBtn.setFont(AppTheme.FONT_LABEL);
        exportBtn.setBackground(AppTheme.SURFACE_VARIANT);
        exportBtn.setForeground(AppTheme.ON_SURFACE);
        exportBtn.setBorderPainted(true);
        exportBtn.setFocusPainted(false);
        exportBtn.addActionListener(e -> exportListToExcel());

        JLabel s1 = label("Sản phẩm / Nhà CC / Mã:");
        JLabel fromLbl = label("Từ ngày:");
        JLabel toLbl = label("Đến ngày:");

        p.add(s1);
        p.add(prodSearchField);
        p.add(fromLbl);
        p.add(prodDpFrom);
        p.add(toLbl);
        p.add(prodDpTo);
        p.add(refreshBtn);
        p.add(exportBtn);
        return p;
    }

    private JPanel buildProductTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] cols = { "Sản phẩm", "Mã đơn", "Ngày nhập", "Nhà cung cấp", "Giá nhập", "Số lượng", "Thành tiền", "Thao tác" };
        productTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        productTable = new StyledTable(productTableModel);
        productTable.setAutoCreateRowSorter(true);

        // Mã đơn
        productTable.getColumnModel().getColumn(1).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.SECONDARY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // Action column – renderer
        productTable.getColumnModel().getColumn(7).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton vBtn = new JButton("👁");
            JButton eBtn = new JButton("✏");
            for (JButton b : new JButton[] { vBtn, eBtn }) {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setOpaque(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setForeground(AppTheme.ON_SURFACE_VAR);
            }
            p.add(vBtn);
            p.add(eBtn);
            return p;
        });

        // Action column – mouse listener for view / edit click
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = productTable.rowAtPoint(evt.getPoint());
                int col = productTable.columnAtPoint(evt.getPoint());
                if (row >= 0) {
                    int modelRow = productTable.convertRowIndexToModel(row);
                    String maDon = productTableModel.getValueAt(modelRow, 1).toString(); // "NH-{id}"
                    try {
                        int id = Integer.parseInt(maDon.replace("NH-", ""));
                        if (col == 7) {
                            int columnWidth = productTable.getColumnModel().getColumn(7).getWidth();
                            int x = evt.getX() - productTable.getCellRect(row, 7, true).x;
                            if (x < columnWidth / 2) {
                                openViewDialog(id);
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

        int[] widths = { 220, 90, 110, 150, 100, 80, 110, 80 };
        for (int i = 0; i < widths.length; i++)
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        prodPagination = new Pagination(page -> {
            prodCurrentPage = page;
            loadData();
        });
        panel.add(productTable.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(prodPagination, BorderLayout.SOUTH);
        return panel;
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

        // Initialize status filter button
        statusFilterBtn = new JButton("Trạng thái ↓");
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

        statusMenu = new JPopupMenu();
        statusMenu.setBackground(AppTheme.SURFACE_LOW);
        statusMenu.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        statusMenu.setLayout(new BorderLayout());

        JPanel popupContent = new JPanel(new BorderLayout(0, 6));
        popupContent.setBackground(AppTheme.SURFACE_LOW);
        popupContent.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        statusSearchField = new JTextField();
        statusSearchField.setFont(AppTheme.FONT_BODY_MD);
        statusSearchField.putClientProperty("JTextField.placeholderText", " Tìm trạng thái...");
        statusSearchField.setPreferredSize(new Dimension(220, 32));
        statusSearchField.setBackground(AppTheme.SURFACE_MED);
        statusSearchField.setForeground(AppTheme.ON_SURFACE);
        statusSearchField.setCaretColor(AppTheme.ON_SURFACE);
        statusSearchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        checkboxPanel.setBackground(AppTheme.SURFACE_LOW);

        String[] statuses = { "Đã nhận", "Chờ Nhận", "Đã hủy" };
        for (String status : statuses) {
            JCheckBox cb = new JCheckBox(status);
            cb.setFont(AppTheme.FONT_BODY_MD);
            cb.setForeground(AppTheme.ON_SURFACE);
            cb.setBackground(AppTheme.SURFACE_LOW);
            cb.addActionListener(e -> {
                currentPage = 1;
                updateStatusFilterButtonText();
                loadData();
            });
            statusCheckboxes.put(status, cb);
            checkboxPanel.add(cb);
        }

        JScrollPane scrollPane = new JScrollPane(checkboxPanel);
        scrollPane.setPreferredSize(new Dimension(220, 150));
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        popupContent.add(statusSearchField, BorderLayout.NORTH);
        popupContent.add(scrollPane, BorderLayout.CENTER);
        statusMenu.add(popupContent);

        // Document Listener for filtering
        statusSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = statusSearchField.getText().trim().toLowerCase();
                checkboxPanel.removeAll();
                for (JCheckBox cb : statusCheckboxes.values()) {
                    if (text.isEmpty() || cb.getText().toLowerCase().contains(text)) {
                        checkboxPanel.add(cb);
                    }
                }
                scrollPane.getVerticalScrollBar().setValue(0);
                checkboxPanel.revalidate();
                checkboxPanel.repaint();
            }
        });

        statusFilterBtn.addActionListener(e -> {
            statusMenu.show(statusFilterBtn, 0, statusFilterBtn.getHeight());
            statusSearchField.requestFocusInWindow();
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
            if (statusSearchField != null) {
                statusSearchField.setText("");
            }
            for (JCheckBox cb : statusCheckboxes.values()) {
                cb.setSelected(false);
            }
            updateStatusFilterButtonText();
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
        p.add(statusFilterBtn);
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
                    int modelRow = table.convertRowIndexToModel(row);
                    String maDon = tableModel.getValueAt(modelRow, 0).toString(); // "NH-{id}"
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
                            if (evt.getClickCount() == 2) {
                                openViewDialog(id);
                            }
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
        final int activeTab = tabbedPane != null ? tabbedPane.getSelectedIndex() : 0;
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count = 0;

            // Tab 1 stats
            private String totalOrders = "...", totalVal = "...", debtVal = "...";
            // Tab 2 stats
            private String prodTotalVal = "...", prodTotalQty = "...", prodDistinctCount = "...";

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    if (activeTab == 1) {
                        // === Tab 2 (Product) filters ===
                        StringBuilder cond = new StringBuilder(" WHERE 1=1");
                        String search = prodSearchField != null ? prodSearchField.getText().trim() : "";
                        if (!search.isEmpty()) {
                            cond.append(" AND (sp.ten_san_pham LIKE '%").append(search.replace("'", "''"))
                                    .append("%' OR ncc.ten LIKE '%").append(search.replace("'", "''"))
                                    .append("%' OR CAST(nh.id AS CHAR) LIKE '%").append(search.replace("'", "''")).append("%')");
                        }

                        if (prodDpFrom != null && prodDpFrom.getValue() != null) {
                            cond.append(" AND nh.thoi_gian >= '").append(prodDpFrom.getValue().toString()).append(" 00:00:00'");
                        }
                        if (prodDpTo != null && prodDpTo.getValue() != null) {
                            cond.append(" AND nh.thoi_gian <= '").append(prodDpTo.getValue().toString()).append(" 23:59:59'");
                        }

                        // Count product rows
                        String countSql = "SELECT COUNT(*) FROM chi_tiet_nhap_hang ct"
                                + " JOIN nhap_hang nh ON ct.id_nhap_hang = nh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                                + cond.toString();
                        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(countSql)) {
                            if (rs.next())
                                count = rs.getInt(1);
                        }

                        // Load product page data
                        int offset = (prodCurrentPage - 1) * PAGE_SIZE;
                        String sql = "SELECT ct.id_nhap_hang, ct.so_luong, ct.gia_nhap, ct.thanh_tien, "
                                + " sp.ten_san_pham, "
                                + " IFNULL(ncc.ten, '---') AS ten_ncc, nh.thoi_gian"
                                + " FROM chi_tiet_nhap_hang ct"
                                + " JOIN nhap_hang nh ON ct.id_nhap_hang = nh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                                + cond.toString()
                                + " ORDER BY nh.thoi_gian DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                            while (rs.next()) {
                                rows.add(new Object[] {
                                        rs.getString("ten_san_pham"),
                                        "NH-" + rs.getInt("id_nhap_hang"),
                                        rs.getTimestamp("thoi_gian") != null
                                                ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                        rs.getTimestamp("thoi_gian"))
                                                : "",
                                        rs.getString("ten_ncc"),
                                        vndFormat.format(rs.getLong("gia_nhap")) + " ₫",
                                        rs.getInt("so_luong"),
                                        vndFormat.format(rs.getLong("thanh_tien")) + " ₫",
                                        ""
                                });
                            }
                        }

                        // Product summary cards stats (depends on prodStatsDateCond)
                        StringBuilder prodStatsDateCond = new StringBuilder();
                        if (prodDpFrom != null && prodDpFrom.getValue() != null) {
                            prodStatsDateCond.append(" AND nh.thoi_gian >= '").append(prodDpFrom.getValue().toString()).append(" 00:00:00'");
                        }
                        if (prodDpTo != null && prodDpTo.getValue() != null) {
                            prodStatsDateCond.append(" AND nh.thoi_gian <= '").append(prodDpTo.getValue().toString()).append(" 23:59:59'");
                        }

                        String prodStatsSql = "SELECT "
                                + " COALESCE(SUM(ct.thanh_tien),0) AS total_val, "
                                + " COALESCE(SUM(ct.so_luong),0) AS total_qty, "
                                + " COUNT(DISTINCT ct.id_san_pham) AS distinct_cnt "
                                + " FROM chi_tiet_nhap_hang ct"
                                + " JOIN nhap_hang nh ON ct.id_nhap_hang = nh.id"
                                + " WHERE nh.trang_thai = 'Đã nhận'" + prodStatsDateCond.toString();

                        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(prodStatsSql)) {
                            if (rs.next()) {
                                prodTotalVal = vndFormat.format(rs.getLong("total_val")) + " ₫";
                                prodTotalQty = String.valueOf(rs.getInt("total_qty"));
                                prodDistinctCount = String.valueOf(rs.getInt("distinct_cnt")) + " loại";
                            }
                        }

                    } else {
                        // === Tab 1 (Invoice) filters ===
                        StringBuilder cond = new StringBuilder(" WHERE 1=1");

                        String search = searchField != null ? searchField.getText().trim() : "";
                        if (!search.isEmpty())
                            cond.append(" AND (ncc.ten LIKE '%").append(search.replace("'", "''"))
                                    .append("%' OR CAST(nh.id AS CHAR) LIKE '%").append(search.replace("'", "''")).append("%')");

                        java.util.List<String> selectedStatuses = new java.util.ArrayList<>();
                        if (statusCheckboxes != null) {
                            for (java.util.Map.Entry<String, JCheckBox> entry : statusCheckboxes.entrySet()) {
                                if (entry.getValue().isSelected()) {
                                    selectedStatuses.add(entry.getKey());
                                }
                            }
                        }
                        if (!selectedStatuses.isEmpty() && selectedStatuses.size() < statusCheckboxes.size()) {
                            cond.append(" AND nh.trang_thai IN (");
                            for (int i = 0; i < selectedStatuses.size(); i++) {
                                if (i > 0) cond.append(",");
                                cond.append("'").append(selectedStatuses.get(i).replace("'", "''")).append("'");
                            }
                            cond.append(")");
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (activeTab == 1) {
                    prodTotalItems = count;
                    productTableModel.setRowCount(0);
                    for (Object[] row : rows)
                        productTableModel.addRow(row);
                    if (prodPagination != null) {
                        prodPagination.update(prodTotalItems, PAGE_SIZE, prodCurrentPage);
                    }
                    if (lblProdTotalValue != null) lblProdTotalValue.setText(prodTotalVal);
                    if (lblProdTotalQty != null) lblProdTotalQty.setText(prodTotalQty);
                    if (lblProdDistinctCount != null) lblProdDistinctCount.setText(prodDistinctCount);
                } else {
                    totalItems = count;
                    tableModel.setRowCount(0);
                    for (Object[] row : rows)
                        tableModel.addRow(row);
                    pagination.update(totalItems, PAGE_SIZE, currentPage);
                    lblTotalOrders.setText(totalOrders);
                    lblTotalValue.setText(totalVal);
                    lblDebtValue.setText(debtVal);
                }
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
        final int activeTab = tabbedPane != null ? tabbedPane.getSelectedIndex() : 0;
        JFileChooser fileChooser = new JFileChooser();
        if (activeTab == 1) {
            fileChooser.setDialogTitle("Chọn nơi lưu danh sách chi tiết sản phẩm nhập");
            fileChooser.setSelectedFile(new File("danh_sach_san_pham_nhap.xlsx"));
        } else {
            fileChooser.setDialogTitle("Chọn nơi lưu danh sách đơn nhập hàng");
            fileChooser.setSelectedFile(new File("danh_sach_don_nhap_hang.xlsx"));
        }

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

        if (activeTab == 1) {
            final String searchText = prodSearchField != null ? prodSearchField.getText().trim() : "";
            final String fromDateStr = (prodDpFrom != null && prodDpFrom.getValue() != null)
                    ? prodDpFrom.getValue().toString() + " 00:00:00"
                    : null;
            final String toDateStr = (prodDpTo != null && prodDpTo.getValue() != null)
                    ? prodDpTo.getValue().toString() + " 23:59:59"
                    : null;

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                private boolean success = false;
                private String errorMessage = "";

                @Override
                protected Void doInBackground() {
                    try {
                        ExcelExporter.exportPurchaseOrderProductList(searchText, fromDateStr, toDateStr, targetFile);
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
                                "Đã xuất danh sách chi tiết sản phẩm nhập hàng ra file Excel thành công!\nMở file ngay?",
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
        } else {
            final String searchText = searchField != null ? searchField.getText().trim() : "";
            final java.util.List<String> selectedStatuses = new java.util.ArrayList<>();
            if (statusCheckboxes != null) {
                for (java.util.Map.Entry<String, JCheckBox> entry : statusCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        selectedStatuses.add(entry.getKey());
                    }
                }
            }
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
                        ExcelExporter.exportPurchaseOrderList(searchText, selectedStatuses, fromDateStr, toDateStr, targetFile);
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

    private void updateStatusFilterButtonText() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (JCheckBox cb : statusCheckboxes.values()) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        if (selected.isEmpty()) {
            statusFilterBtn.setText("Trạng thái ↓");
        } else if (selected.size() == statusCheckboxes.size()) {
            statusFilterBtn.setText("Tất cả trạng thái ↓");
        } else if (selected.size() == 1) {
            statusFilterBtn.setText(selected.get(0) + " ↓");
        } else {
            statusFilterBtn.setText(selected.size() + " trạng thái ↓");
        }
    }
}

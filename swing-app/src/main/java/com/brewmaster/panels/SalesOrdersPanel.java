package com.brewmaster.panels;

import com.brewmaster.components.*;
import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.ExcelExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Màn hình Hóa đơn Xuất (Sales Orders)
 * Tương đương sales-orders.html
 */
public class SalesOrdersPanel extends JPanel {

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalItems = 0;
    private int prodCurrentPage = 1;
    private int prodTotalItems = 0;

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
    private JLabel lblRevenue, lblOrderCount, lblPending, lblUnpaidCustomers;

    // === Tab 2 (Product) UI components ===
    private DefaultTableModel productTableModel;
    private StyledTable productTable;
    private Pagination prodPagination;
    private JTextField prodSearchField;
    private JButton typeFilterBtn;
    private JPopupMenu typeMenu;
    private final java.util.Map<String, JCheckBox> typeCheckboxes = new java.util.LinkedHashMap<>();
    private DatePicker prodDpFrom;
    private DatePicker prodDpTo;
    private JLabel lblProdRevenue, lblProdProfit, lblProdQtySold, lblProdQtyGifted;

    public SalesOrdersPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // === PAGE HEADER ===
        JPanel pageHeader = new JPanel(new BorderLayout());
        pageHeader.setOpaque(false);
        pageHeader.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleSection = new JPanel(new GridLayout(2, 1, 0, 4));
        titleSection.setOpaque(false);
        JLabel title = new JLabel("Hóa đơn Xuất");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel subtitle = new JLabel("Quản lý và theo dõi tất cả giao dịch bán hàng");
        subtitle.setFont(AppTheme.FONT_BODY_SM);
        subtitle.setForeground(AppTheme.ON_SURFACE_VAR);
        titleSection.add(title);
        titleSection.add(subtitle);

        JButton createBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font>  Tạo hóa đơn</html>");
        createBtn.setFont(AppTheme.FONT_LABEL);
        createBtn.setForeground(AppTheme.ON_PRIMARY);
        createBtn.setBackground(AppTheme.PRIMARY);
        createBtn.setBorderPainted(false);
        createBtn.setFocusPainted(false);
        createBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBtn.setPreferredSize(new Dimension(140, 36));
        createBtn.addActionListener(e -> openCreateDialog());

        pageHeader.add(titleSection, BorderLayout.WEST);
        pageHeader.add(createBtn, BorderLayout.EAST);

        // === TAB 1: THEO HÓA ĐƠN ===
        JPanel tab1Panel = new JPanel(new BorderLayout(0, 12));
        tab1Panel.setOpaque(false);

        JPanel topPanel1 = new JPanel(new BorderLayout(0, 12));
        topPanel1.setOpaque(false);
        topPanel1.add(buildSummaryCards(), BorderLayout.NORTH);
        topPanel1.add(buildFilterSection(), BorderLayout.CENTER);

        tab1Panel.add(topPanel1, BorderLayout.NORTH);
        tab1Panel.add(buildInvoiceTableSection(), BorderLayout.CENTER);

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
        tabbedPane.addTab("Danh sách Hóa đơn", tab1Panel);
        tabbedPane.addTab("Xem theo Sản phẩm", tab2Panel);

        tabbedPane.addChangeListener(e -> {
            loadData();
        });

        content.add(pageHeader, BorderLayout.NORTH);
        content.add(tabbedPane, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
    }

    private JPanel buildFilterSection() {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 5));
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(10, 12, 10, 12)));

        // Search field
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(240, 34));
        searchField = new JTextField();
        searchField.setFont(AppTheme.FONT_BODY_MD);
        searchField.putClientProperty("JTextField.placeholderText", " Tìm kiếm khách hàng...");
        searchField.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });
        searchWrapper.add(searchField, BorderLayout.CENTER);

        // Status filter
        statusFilterBtn = new JButton("Trạng thái ↓");
        statusFilterBtn.setFont(AppTheme.FONT_BODY_MD);
        statusFilterBtn.setPreferredSize(new Dimension(170, 34));
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

        String[] statuses = { "Hoàn thành", "Hẹn", "Đã Hủy" };
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

        // Date filters
        dpFrom = new DatePicker();
        dpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        dpFrom.setPreferredSize(new Dimension(130, 34));
        dpFrom.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        dpTo = new DatePicker();
        dpTo.setValue(LocalDate.now());
        dpTo.setPreferredSize(new Dimension(130, 34));
        dpTo.addActionListener(e -> {
            currentPage = 1;
            loadData();
        });

        // Refresh button
        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

        // Export button
        JButton exportBtn = new JButton("<html><font face='Segoe UI'>⬇</font>  Xuất Excel</html>");
        exportBtn.setFont(AppTheme.FONT_LABEL);
        exportBtn.setBackground(AppTheme.SURFACE_VARIANT);
        exportBtn.setForeground(AppTheme.ON_SURFACE);
        exportBtn.setBorderPainted(true);
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportToExcel());

        // Labels
        JLabel searchLbl = makeLabel("Tìm kiếm:");
        JLabel statusLbl = makeLabel("Trạng thái:");
        JLabel fromLbl = makeLabel("Từ ngày:");
        JLabel toLbl = makeLabel("Đến ngày:");

        panel.add(searchLbl);
        panel.add(searchWrapper);
        panel.add(statusLbl);
        panel.add(statusFilterBtn);
        panel.add(fromLbl);
        panel.add(dpFrom);
        panel.add(toLbl);
        panel.add(dpTo);
        panel.add(refreshBtn);
        panel.add(exportBtn);

        return panel;
    }

    private JPanel buildInvoiceTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] cols = { "Mã HĐ", "Ngày lập", "Ngày lắp", "Khách hàng", "Nhân viên", "Tổng tiền", "Đặt cọc",
                "Khách nợ",
                "Trạng thái", "Thao tác" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new StyledTable(tableModel);
        table.setAutoCreateRowSorter(true);

        // Mã HĐ column - primary color
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.PRIMARY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // Status column - badge (col 8)
        table.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
            wrapper.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            wrapper.add(StatusBadge.forSalesStatus(v == null ? "" : v.toString()));
            return wrapper;
        });

        // Action column (col 9)
        table.getColumnModel().getColumn(9).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton vBtn = new JButton("👁");
            JButton eBtn = new JButton("✏");
            JButton xBtn = new JButton("🖨");
            for (JButton b : new JButton[] { vBtn, eBtn, xBtn }) {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setForeground(AppTheme.ON_SURFACE_VAR);
                b.setOpaque(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
            }
            p.add(vBtn);
            p.add(eBtn);
            p.add(xBtn);
            return p;
        });

        // Column widths
        int[] widths = { 70, 120, 100, 120, 110, 110, 100, 100, 105, 110 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Table actions listener
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String maHD = tableModel.getValueAt(modelRow, 0).toString(); // "BH-{id}"
                    try {
                        int id = Integer.parseInt(maHD.replace("BH-", ""));
                        if (col == 9) {
                            int columnWidth = table.getColumnModel().getColumn(9).getWidth();
                            int x = evt.getX() - table.getCellRect(row, 9, true).x;
                            if (x < columnWidth / 3) {
                                openViewDialog(id);
                            } else if (x < (columnWidth * 2) / 3) {
                                openEditDialog(id);
                            } else {
                                exportSingleSalesOrder(id);
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

        // Pagination
        pagination = new Pagination(page -> {
            currentPage = page;
            loadData();
        });

        panel.add(table.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(pagination, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 96));
        row.setBorder(new EmptyBorder(0, 0, 4, 0));

        lblRevenue = new JLabel("...");
        lblOrderCount = new JLabel("...");
        lblPending = new JLabel("...");
        lblUnpaidCustomers = new JLabel("...");

        row.add(buildSmallCard("💵", "Doanh thu", lblRevenue, new Color(16, 185, 129)));
        row.add(buildSmallCard("💳", "Tiền cọc", lblOrderCount, AppTheme.PRIMARY));
        row.add(buildSmallCard("⏳", "Lịch hẹn", lblPending, new Color(245, 158, 11)));
        row.add(buildSmallCard("📄", "Hóa đơn khách còn nợ", lblUnpaidCustomers, new Color(239, 68, 68)));

        return row;
    }

    private JPanel buildProductSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 96));
        row.setBorder(new EmptyBorder(0, 0, 4, 0));

        lblProdRevenue = new JLabel("...");
        lblProdProfit = new JLabel("...");
        lblProdQtySold = new JLabel("...");
        lblProdQtyGifted = new JLabel("...");

        row.add(buildSmallCard("💵", "Doanh thu", lblProdRevenue, new Color(16, 185, 129)));
        row.add(buildSmallCard("📈", "Lợi nhuận lý thuyết", lblProdProfit, AppTheme.PRIMARY));
        row.add(buildSmallCard("📦", "Số sản phẩm bán được", lblProdQtySold, new Color(245, 158, 11)));
        row.add(buildSmallCard("🎁", "Số sản phẩm tặng", lblProdQtyGifted, new Color(239, 68, 68)));

        return row;
    }

    private JPanel buildProductFilterSection() {
        JPanel panel = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 5));
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(10, 12, 10, 12)));

        // Search field
        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.setPreferredSize(new Dimension(240, 34));
        prodSearchField = new JTextField();
        prodSearchField.setFont(AppTheme.FONT_BODY_MD);
        prodSearchField.putClientProperty("JTextField.placeholderText", " Tìm kiếm sản phẩm...");
        prodSearchField.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });
        searchWrapper.add(prodSearchField, BorderLayout.CENTER);

        // Classification filter
        typeFilterBtn = new JButton("Phân loại ↓");
        typeFilterBtn.setFont(AppTheme.FONT_BODY_MD);
        typeFilterBtn.setPreferredSize(new Dimension(170, 34));
        typeFilterBtn.setBackground(AppTheme.SURFACE_LOW);
        typeFilterBtn.setForeground(AppTheme.ON_SURFACE);
        typeFilterBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        typeFilterBtn.setFocusPainted(false);
        typeFilterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        typeFilterBtn.setHorizontalAlignment(SwingConstants.LEFT);

        typeMenu = new JPopupMenu();
        typeMenu.setBackground(AppTheme.SURFACE_LOW);
        typeMenu.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        typeMenu.setLayout(new BorderLayout());

        JPanel popupContent = new JPanel();
        popupContent.setLayout(new BoxLayout(popupContent, BoxLayout.Y_AXIS));
        popupContent.setBackground(AppTheme.SURFACE_LOW);
        popupContent.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        String[] types = { "Bán", "Tặng" };
        for (String t : types) {
            JCheckBox cb = new JCheckBox(t);
            cb.setFont(AppTheme.FONT_BODY_MD);
            cb.setForeground(AppTheme.ON_SURFACE);
            cb.setBackground(AppTheme.SURFACE_LOW);
            cb.addActionListener(e -> {
                prodCurrentPage = 1;
                updateTypeFilterButtonText();
                loadData();
            });
            typeCheckboxes.put(t, cb);
            popupContent.add(cb);
        }
        typeMenu.add(popupContent);

        typeFilterBtn.addActionListener(e -> {
            typeMenu.show(typeFilterBtn, 0, typeFilterBtn.getHeight());
        });

        // Date filters
        prodDpFrom = new DatePicker();
        prodDpFrom.setValue(LocalDate.now().withDayOfMonth(1));
        prodDpFrom.setPreferredSize(new Dimension(130, 34));
        prodDpFrom.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });

        prodDpTo = new DatePicker();
        prodDpTo.setValue(LocalDate.now());
        prodDpTo.setPreferredSize(new Dimension(130, 34));
        prodDpTo.addActionListener(e -> {
            prodCurrentPage = 1;
            loadData();
        });

        // Refresh button
        JButton refreshBtn = new JButton("<html><font face='Segoe UI'>↻</font>  Làm mới</html>");
        refreshBtn.setFont(AppTheme.FONT_LABEL);
        refreshBtn.setForeground(AppTheme.ON_SURFACE);
        refreshBtn.setBackground(AppTheme.SURFACE_VARIANT);
        refreshBtn.setBorderPainted(true);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            prodSearchField.setText("");
            for (JCheckBox cb : typeCheckboxes.values()) {
                cb.setSelected(false);
            }
            updateTypeFilterButtonText();
            prodDpFrom.setValue(LocalDate.now().withDayOfMonth(1));
            prodDpTo.setValue(LocalDate.now());
            prodCurrentPage = 1;
            loadData();
        });

        // Export button
        JButton exportBtn = new JButton("<html><font face='Segoe UI'>⬇</font>  Xuất Excel</html>");
        exportBtn.setFont(AppTheme.FONT_LABEL);
        exportBtn.setBackground(AppTheme.SURFACE_VARIANT);
        exportBtn.setForeground(AppTheme.ON_SURFACE);
        exportBtn.setBorderPainted(true);
        exportBtn.setFocusPainted(false);
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportToExcel());

        // Labels
        JLabel searchLbl = makeLabel("Sản phẩm:");
        JLabel typeLbl = makeLabel("Phân loại:");
        JLabel fromLbl = makeLabel("Từ ngày:");
        JLabel toLbl = makeLabel("Đến ngày:");

        panel.add(searchLbl);
        panel.add(searchWrapper);
        panel.add(typeLbl);
        panel.add(typeFilterBtn);
        panel.add(fromLbl);
        panel.add(prodDpFrom);
        panel.add(toLbl);
        panel.add(prodDpTo);
        panel.add(refreshBtn);
        panel.add(exportBtn);

        return panel;
    }

    private JPanel buildProductTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(AppTheme.SURFACE_LOW);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        String[] productCols = { "Sản phẩm", "Phân loại", "Mã HĐ", "Ngày bán", "Giá nhập", "Giá bán", "Số lượng", "Lợi nhuận lý thuyết", "Thao tác" };
        productTableModel = new DefaultTableModel(productCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        productTable = new StyledTable(productTableModel);
        productTable.setAutoCreateRowSorter(true);

        // Badge Phân loại (col 1)
        productTable.getColumnModel().getColumn(1).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
            wrapper.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            String val = v == null ? "" : v.toString();
            if ("Tặng".equals(val)) {
                wrapper.add(new StatusBadge("Tặng", new Color(254, 243, 199), new Color(217, 119, 6)));
            } else {
                wrapper.add(new StatusBadge("Bán", new Color(209, 250, 229), new Color(5, 150, 105)));
            }
            return wrapper;
        });

        // Mã HĐ column (col 2)
        productTable.getColumnModel().getColumn(2).setCellRenderer((t, v, sel, foc, r, c) -> {
            JLabel lbl = new JLabel(v == null ? "" : v.toString());
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.PRIMARY);
            lbl.setBorder(new EmptyBorder(0, 12, 0, 4));
            lbl.setOpaque(true);
            lbl.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            return lbl;
        });

        // Action column (col 8)
        productTable.getColumnModel().getColumn(8).setCellRenderer((t, v, sel, foc, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 7));
            p.setBackground(sel ? AppTheme.withAlpha(AppTheme.PRIMARY, 35) : AppTheme.SURFACE_LOW);
            JButton vBtn = new JButton("👁");
            JButton eBtn = new JButton("✏");
            for (JButton b : new JButton[] { vBtn, eBtn }) {
                b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
                b.setForeground(AppTheme.ON_SURFACE_VAR);
                b.setOpaque(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
            }
            p.add(vBtn);
            p.add(eBtn);
            return p;
        });

        // Column widths for products
        int[] pWidths = { 220, 80, 80, 120, 100, 100, 70, 110, 80 };
        for (int i = 0; i < pWidths.length; i++) {
            productTable.getColumnModel().getColumn(i).setPreferredWidth(pWidths[i]);
        }

        // Product table actions listener
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = productTable.rowAtPoint(evt.getPoint());
                int col = productTable.columnAtPoint(evt.getPoint());
                if (row >= 0) {
                    int modelRow = productTable.convertRowIndexToModel(row);
                    String maHD = productTableModel.getValueAt(modelRow, 2).toString(); // "BH-{id}"
                    try {
                        int id = Integer.parseInt(maHD.replace("BH-", ""));
                        if (col == 8) {
                            int columnWidth = productTable.getColumnModel().getColumn(8).getWidth();
                            int x = evt.getX() - productTable.getCellRect(row, 8, true).x;
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

        prodPagination = new Pagination(page -> {
            prodCurrentPage = page;
            loadData();
        });

        panel.add(productTable.wrapInScrollPane(), BorderLayout.CENTER);
        panel.add(prodPagination, BorderLayout.SOUTH);
        return panel;
    }

    private void updateTypeFilterButtonText() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (JCheckBox cb : typeCheckboxes.values()) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        if (selected.isEmpty()) {
            typeFilterBtn.setText("Phân loại ↓");
        } else if (selected.size() == typeCheckboxes.size()) {
            typeFilterBtn.setText("Tất cả phân loại ↓");
        } else {
            typeFilterBtn.setText(selected.get(0) + " ↓");
        }
    }

    private JPanel buildSmallCard(String icon, String label, JLabel valueLabel, Color color) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(AppTheme.SURFACE_LOW);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(16, 16, 16, 16)));
        p.setOpaque(true);

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
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        return lbl;
    }

    public void loadData() {
        final int activeTab = tabbedPane != null ? tabbedPane.getSelectedIndex() : 0;
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            private java.util.List<Object[]> rows = new java.util.ArrayList<>();
            private int count = 0;
            
            // Tab 1 summaries
            private String rev = "...", orderCnt = "...", pend = "...", unpaidCust = "...";
            // Tab 2 summaries
            private String prodRev = "...", prodProfit = "...", prodQtySold = "...", prodQtyGifted = "...";

            @Override
            protected Object[] doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    if (activeTab == 1) {
                        // === Tab 2 filters ===
                        String searchCond = "";
                        String searchText = prodSearchField != null ? prodSearchField.getText().trim() : "";
                        if (!searchText.isEmpty()) {
                            searchCond = " AND (kh.ten LIKE '%" + searchText + "%'"
                                    + " OR CAST(bh.id AS CHAR) LIKE '%" + searchText + "%'"
                                    + " OR sp.ten_san_pham LIKE '%" + searchText + "%')";
                        }

                        String dateCond = "";
                        if (prodDpFrom != null && prodDpFrom.getValue() != null) {
                            dateCond += " AND bh.thoi_gian >= '" + prodDpFrom.getValue().toString() + " 00:00:00'";
                        }
                        if (prodDpTo != null && prodDpTo.getValue() != null) {
                            dateCond += " AND bh.thoi_gian <= '" + prodDpTo.getValue().toString() + " 23:59:59'";
                        }

                        String typeCond = "";
                        java.util.List<Integer> selectedTypes = new java.util.ArrayList<>();
                        if (typeCheckboxes.containsKey("Bán") && typeCheckboxes.get("Bán").isSelected()) {
                            selectedTypes.add(0);
                        }
                        if (typeCheckboxes.containsKey("Tặng") && typeCheckboxes.get("Tặng").isSelected()) {
                            selectedTypes.add(1);
                        }
                        if (!selectedTypes.isEmpty() && selectedTypes.size() < 2) {
                            typeCond = " AND ct.is_gift = " + selectedTypes.get(0);
                        }

                        // Count total for products
                        String countSql = "SELECT COUNT(*) FROM chi_tiet_ban_hang ct"
                                + " JOIN ban_hang bh ON ct.id_ban_hang = bh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " WHERE 1=1" + searchCond + dateCond + typeCond;
                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
                            if (rs.next())
                                count = rs.getInt(1);
                        }

                        // Load product page data
                        int offset = (prodCurrentPage - 1) * PAGE_SIZE;
                        String sql = "SELECT ct.id_ban_hang, ct.id_san_pham, ct.is_gift, ct.so_luong, "
                                + " IF(ct.is_gift = 1, 0, ct.gia_ban) AS gia_ban, "
                                + " sp.ten_san_pham, sp.gia_nhap_hien_tai AS gia_nhap, "
                                + " bh.thoi_gian, "
                                + " ((IF(ct.is_gift = 1, 0, ct.gia_ban) - sp.gia_nhap_hien_tai) * ct.so_luong) AS loi_nhuan "
                                + " FROM chi_tiet_ban_hang ct"
                                + " JOIN ban_hang bh ON ct.id_ban_hang = bh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " WHERE 1=1" + searchCond + dateCond + typeCond
                                + " ORDER BY bh.thoi_gian DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                            while (rs.next()) {
                                String isGiftStr = rs.getInt("is_gift") == 1 ? "Tặng" : "Bán";
                                rows.add(new Object[] {
                                        rs.getString("ten_san_pham"),
                                        isGiftStr,
                                        "BH-" + rs.getInt("id_ban_hang"),
                                        rs.getTimestamp("thoi_gian") != null
                                                ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                        rs.getTimestamp("thoi_gian"))
                                                : "",
                                        formatVND(rs.getLong("gia_nhap")),
                                        formatVND(rs.getLong("gia_ban")),
                                        rs.getInt("so_luong"),
                                        formatVND(rs.getLong("loi_nhuan")),
                                        ""
                                });
                            }
                        }

                        // Product summary cards stats (depends only on prodStatsDateCond)
                        String prodStatsDateCond = "";
                        if (prodDpFrom != null && prodDpFrom.getValue() != null) {
                            prodStatsDateCond += " AND bh.thoi_gian >= '" + prodDpFrom.getValue().toString() + " 00:00:00'";
                        }
                        if (prodDpTo != null && prodDpTo.getValue() != null) {
                            prodStatsDateCond += " AND bh.thoi_gian <= '" + prodDpTo.getValue().toString() + " 23:59:59'";
                        }

                        String prodStatsSql = "SELECT "
                                + " COALESCE(SUM(IF(ct.is_gift = 0, ct.thanh_tien, 0)), 0) AS prod_revenue, "
                                + " COALESCE(SUM((IF(ct.is_gift = 1, 0, ct.gia_ban) - sp.gia_nhap_hien_tai) * ct.so_luong), 0) AS prod_profit, "
                                + " COALESCE(SUM(IF(ct.is_gift = 0, ct.so_luong, 0)), 0) AS prod_qty_sold, "
                                + " COALESCE(SUM(IF(ct.is_gift = 1, ct.so_luong, 0)), 0) AS prod_qty_gifted "
                                + " FROM chi_tiet_ban_hang ct"
                                + " JOIN ban_hang bh ON ct.id_ban_hang = bh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " WHERE bh.trang_thai = 'Hoàn thành'" + prodStatsDateCond;

                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(prodStatsSql)) {
                            if (rs.next()) {
                                prodRev = formatVND(rs.getLong("prod_revenue"));
                                prodProfit = formatVND(rs.getLong("prod_profit"));
                                prodQtySold = String.valueOf(rs.getInt("prod_qty_sold"));
                                prodQtyGifted = String.valueOf(rs.getInt("prod_qty_gifted"));
                            }
                        }

                    } else {
                        // === Tab 1 filters ===
                        String statusCond = "";
                        java.util.List<String> selectedStatuses = new java.util.ArrayList<>();
                        if (statusCheckboxes != null) {
                            for (java.util.Map.Entry<String, JCheckBox> entry : statusCheckboxes.entrySet()) {
                                if (entry.getValue().isSelected()) {
                                    selectedStatuses.add(entry.getKey());
                                }
                            }
                        }
                        if (!selectedStatuses.isEmpty() && selectedStatuses.size() < statusCheckboxes.size()) {
                            StringBuilder sb = new StringBuilder(" AND bh.trang_thai IN (");
                            for (int i = 0; i < selectedStatuses.size(); i++) {
                                if (i > 0) sb.append(",");
                                sb.append("'").append(selectedStatuses.get(i).replace("'", "''")).append("'");
                            }
                            sb.append(")");
                            statusCond = sb.toString();
                        }

                        String searchCond = "";
                        String searchText = searchField != null ? searchField.getText().trim() : "";
                        if (!searchText.isEmpty()) {
                            searchCond = " AND (kh.ten LIKE '%" + searchText + "%'"
                                    + " OR CAST(bh.id AS CHAR) LIKE '%" + searchText + "%')";
                        }

                        String dateCond = "";
                        if (dpFrom != null && dpFrom.getValue() != null) {
                            dateCond += " AND bh.thoi_gian >= '" + dpFrom.getValue().toString() + " 00:00:00'";
                        }
                        if (dpTo != null && dpTo.getValue() != null) {
                            dateCond += " AND bh.thoi_gian <= '" + dpTo.getValue().toString() + " 23:59:59'";
                        }

                        // Count total for invoices
                        String countSql = "SELECT COUNT(*) FROM ban_hang bh"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " WHERE 1=1" + statusCond + searchCond + dateCond;
                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
                            if (rs.next())
                                count = rs.getInt(1);
                        }

                        // Load invoice page data
                        int offset = (currentPage - 1) * PAGE_SIZE;
                        String sql = "SELECT bh.id, bh.thoi_gian, bh.ngay_lap,"
                                + " IFNULL(kh.ten, 'Khách vãng lai') AS ten_khach,"
                                + " IFNULL(nv.ten_nhan_vien, '---') AS ten_nv,"
                                + " bh.tong_tien, bh.tien_da_thanh_toan, bh.tien_no, bh.trang_thai"
                                + " FROM ban_hang bh"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " LEFT JOIN nhan_vien nv ON bh.id_nhan_vien = nv.id"
                                + " WHERE 1=1" + statusCond + searchCond + dateCond
                                + " ORDER BY bh.thoi_gian DESC LIMIT " + PAGE_SIZE + " OFFSET " + offset;

                        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                            while (rs.next()) {
                                rows.add(new Object[] {
                                        "BH-" + rs.getInt("id"),
                                        rs.getTimestamp("thoi_gian") != null
                                                ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                        rs.getTimestamp("thoi_gian"))
                                                : "",
                                        rs.getDate("ngay_lap") != null
                                                ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(
                                                        rs.getDate("ngay_lap"))
                                                : "--",
                                        rs.getString("ten_khach"),
                                        rs.getString("ten_nv"),
                                        formatVND(rs.getLong("tong_tien")),
                                        formatVND(rs.getLong("tien_da_thanh_toan")),
                                        formatVND(rs.getLong("tien_no")),
                                        rs.getString("trang_thai"),
                                        ""
                                });
                            }
                        }

                        // Invoice summary stats
                        String statsDateCond = "";
                        if (dpFrom != null && dpFrom.getValue() != null) {
                            statsDateCond += " AND thoi_gian >= '" + dpFrom.getValue().toString() + " 00:00:00'";
                        }
                        if (dpTo != null && dpTo.getValue() != null) {
                            statsDateCond += " AND thoi_gian <= '" + dpTo.getValue().toString() + " 23:59:59'";
                        }

                        String revSql = "SELECT COALESCE(SUM(tong_tien),0) FROM ban_hang WHERE trang_thai='Hoàn thành'" + statsDateCond;
                        String cntSql = "SELECT COALESCE(SUM(tien_da_thanh_toan),0) FROM ban_hang WHERE trang_thai='Hẹn'" + statsDateCond;
                        String pendSql = "SELECT COUNT(*) FROM ban_hang WHERE trang_thai='Hẹn'" + statsDateCond;
                        String unpaidCustSql = "SELECT COUNT(*) FROM ban_hang WHERE tien_no > 0 AND trang_thai = 'Hoàn thành'" + statsDateCond;
                        try (Statement st = conn.createStatement()) {
                            ResultSet rs = st.executeQuery(revSql);
                            if (rs.next())
                                rev = formatVND(rs.getLong(1));
                            rs.close();
                            rs = st.executeQuery(cntSql);
                            if (rs.next())
                                orderCnt = formatVND(rs.getLong(1));
                            rs.close();
                            rs = st.executeQuery(pendSql);
                            if (rs.next())
                                pend = String.valueOf(rs.getInt(1));
                            rs.close();
                            rs = st.executeQuery(unpaidCustSql);
                            if (rs.next())
                                unpaidCust = String.valueOf(rs.getInt(1));
                            rs.close();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi tải Sales Orders: " + e.getMessage());
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
                    if (lblProdRevenue != null) lblProdRevenue.setText(prodRev);
                    if (lblProdProfit != null) lblProdProfit.setText(prodProfit);
                    if (lblProdQtySold != null) lblProdQtySold.setText(prodQtySold);
                    if (lblProdQtyGifted != null) lblProdQtyGifted.setText(prodQtyGifted);
                } else {
                    totalItems = count;
                    tableModel.setRowCount(0);
                    for (Object[] row : rows)
                        tableModel.addRow(row);
                    if (pagination != null) {
                        pagination.update(totalItems, PAGE_SIZE, currentPage);
                    }
                    if (lblRevenue != null) lblRevenue.setText(rev);
                    if (lblOrderCount != null) lblOrderCount.setText(orderCnt);
                    if (lblPending != null) lblPending.setText(pend);
                    if (lblUnpaidCustomers != null) lblUnpaidCustomers.setText(unpaidCust);
                }
            }
        };
        worker.execute();
    }

    private void openCreateDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SalesOrderDialog dlg = new SalesOrderDialog(owner);
        dlg.setOnSaveCallback(() -> {
            currentPage = 1;
            loadData();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog(int orderId) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SalesOrderDialog dlg = new SalesOrderDialog(owner, orderId);
        dlg.setOnSaveCallback(() -> loadData());
        dlg.setVisible(true);
    }

    private void openViewDialog(int orderId) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        SalesOrderDialog dlg = new SalesOrderDialog(owner, orderId, true);
        dlg.setVisible(true);
    }

    private void printOrder(int orderId) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Hóa đơn bán hàng #BH-" + orderId + "  ",
                true);
        dlg.setSize(380, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(AppTheme.FONT_MONO);
        area.setBackground(AppTheme.SURFACE_MED);
        area.setForeground(AppTheme.ON_SURFACE);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        StringBuilder sb = new StringBuilder();
        sb.append("      ☕ BrewMaster Pro ☕\n");
        sb.append("   -----------------------------\n");
        sb.append(String.format("   Mã HĐ: BH-%05d\n", orderId));

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT bh.*, kh.ten AS ten_khach_hang, nv.ten_nhan_vien FROM ban_hang bh"
                    + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                    + " LEFT JOIN nhan_vien nv ON bh.id_nhan_vien = nv.id"
                    + " WHERE bh.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Timestamp ts = rs.getTimestamp("thoi_gian");
                        if (ts != null) {
                            sb.append("   Ngày:  ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts))
                                    .append("\n");
                        }
                        sb.append("   Khách: ").append(
                                rs.getString("ten_khach_hang") != null ? rs.getString("ten_khach_hang") : "Khách lẻ")
                                .append("\n");
                        sb.append("   Thu ngân: ")
                                .append(rs.getString("ten_nhan_vien") != null ? rs.getString("ten_nhan_vien") : "---")
                                .append("\n");
                        if (rs.getString("dia_chi_giao_hang") != null
                                && !rs.getString("dia_chi_giao_hang").trim().isEmpty()) {
                            sb.append("   Địa chỉ giao: ").append(rs.getString("dia_chi_giao_hang")).append("\n");
                        }
                        sb.append("   Trạng thái: ").append(rs.getString("trang_thai")).append("\n");
                    }
                }
            }

            sb.append("   -----------------------------\n");
            sb.append(String.format("   %-16s %3s %9s\n", "Sản phẩm", "SL", "T.Tiền"));

            String detailSql = "SELECT ct.*, sp.ten_san_pham FROM chi_tiet_ban_hang ct"
                    + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                    + " WHERE ct.id_ban_hang = ?";
            try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ten = rs.getString("ten_san_pham");
                        if (ten.length() > 16)
                            ten = ten.substring(0, 14) + "..";
                        sb.append(String.format("   %-16s %3d %9s\n",
                                ten,
                                rs.getInt("so_luong"),
                                formatVND(rs.getLong("thanh_tien"))));
                    }
                }
            }

            sb.append("   -----------------------------\n");
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT tong_tien, tien_da_thanh_toan, tien_no FROM ban_hang WHERE id=?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sb.append(String.format("   %-18s: %9s\n", "Tổng cộng", formatVND(rs.getLong("tong_tien"))));
                        sb.append(String.format("   %-18s: %9s\n", "Đã trả",
                                formatVND(rs.getLong("tien_da_thanh_toan"))));
                        sb.append(String.format("   %-18s: %9s\n", "Còn nợ", formatVND(rs.getLong("tien_no"))));
                    }
                }
            }

        } catch (Exception ex) {
            sb.append("   Lỗi tải dữ liệu hóa đơn.\n");
            ex.printStackTrace();
        }

        sb.append("   -----------------------------\n");
        sb.append("     CẢM ƠN QUÝ KHÁCH HẸN GẶP LẠI!\n");

        area.setText(sb.toString());
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton btnPrint = new JButton("🖨  In hóa đơn");
        btnPrint.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        btnPrint.setBackground(AppTheme.PRIMARY);
        btnPrint.setForeground(AppTheme.ON_PRIMARY);
        btnPrint.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnPrint.addActionListener(e -> {
            try {
                area.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi in ấn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel pBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pBtn.setOpaque(false);
        pBtn.setBorder(new EmptyBorder(8, 0, 0, 0));
        pBtn.add(btnPrint);
        root.add(pBtn, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private String formatVND(long amount) {
        return vndFormat.format(amount) + " ₫";
    }

    /**
     * Xuất HÓA ĐƠN BÁN HÀNG của một đơn cụ thể ra file .xlsx theo mẫu.
     */
    private void exportSingleSalesOrder(int orderId) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu hóa đơn bán hàng #BH-" + orderId);
        fc.setSelectedFile(new File("Hoa_Don_Ban_Hang_BH" + orderId + ".xlsx"));
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
                    ExcelExporter.exportSalesOrder(orderId, finalTarget);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    err = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (err == null) {
                    int opt = JOptionPane.showConfirmDialog(SalesOrdersPanel.this,
                            "Xuất hóa đơn bán hàng thành công!\nMở file ngay?",
                            "Xuất Excel", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (opt == JOptionPane.YES_OPTION) {
                        try {
                            java.awt.Desktop.getDesktop().open(finalTarget);
                        } catch (Exception ignore) {
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(SalesOrdersPanel.this,
                            "Lỗi xuất Excel: " + err, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }

    private void exportToExcel() {
        final int activeTab = tabbedPane != null ? tabbedPane.getSelectedIndex() : 0;
        JFileChooser fileChooser = new JFileChooser();
        if (activeTab == 1) {
            fileChooser.setDialogTitle("Chọn nơi lưu danh sách chi tiết sản phẩm bán");
            fileChooser.setSelectedFile(new File("danh_sach_san_pham_ban.csv"));
        } else {
            fileChooser.setDialogTitle("Chọn nơi lưu danh sách hóa đơn xuất");
            fileChooser.setSelectedFile(new File("danh_sach_hoa_don_xuat.csv"));
        }

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File targetFile = fileChooser.getSelectedFile();
        String filePath = targetFile.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".csv")) {
            targetFile = new File(filePath + ".csv");
        }

        final File finalTargetFile = targetFile;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private boolean success = false;
            private String errorMessage = "";

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = DatabaseManager.getInstance().getConnection();

                    String statusCond = "";
                    java.util.List<String> selectedStatuses = new java.util.ArrayList<>();
                    if (statusCheckboxes != null) {
                        for (java.util.Map.Entry<String, JCheckBox> entry : statusCheckboxes.entrySet()) {
                            if (entry.getValue().isSelected()) {
                                selectedStatuses.add(entry.getKey());
                            }
                        }
                    }
                    if (!selectedStatuses.isEmpty() && selectedStatuses.size() < statusCheckboxes.size()) {
                        StringBuilder sb = new StringBuilder(" AND bh.trang_thai IN (");
                        for (int i = 0; i < selectedStatuses.size(); i++) {
                            if (i > 0) sb.append(",");
                            sb.append("'").append(selectedStatuses.get(i).replace("'", "''")).append("'");
                        }
                        sb.append(")");
                        statusCond = sb.toString();
                    }

                    String searchCond = "";
                    String searchText = searchField.getText().trim();
                    if (!searchText.isEmpty()) {
                        if (activeTab == 1) {
                            searchCond = " AND (kh.ten LIKE '%" + searchText + "%'"
                                    + " OR CAST(bh.id AS CHAR) LIKE '%" + searchText + "%'"
                                    + " OR sp.ten_san_pham LIKE '%" + searchText + "%')";
                        } else {
                            searchCond = " AND (kh.ten LIKE '%" + searchText + "%'"
                                    + " OR CAST(bh.id AS CHAR) LIKE '%" + searchText + "%')";
                        }
                    }

                    String dateCond = "";
                    if (dpFrom != null && dpFrom.getValue() != null) {
                        dateCond += " AND bh.thoi_gian >= '" + dpFrom.getValue().toString() + " 00:00:00'";
                    }
                    if (dpTo != null && dpTo.getValue() != null) {
                        dateCond += " AND bh.thoi_gian <= '" + dpTo.getValue().toString() + " 23:59:59'";
                    }

                    if (activeTab == 1) {
                        String sql = "SELECT ct.id_ban_hang, ct.is_gift, ct.so_luong, "
                                + " IF(ct.is_gift = 1, 0, ct.gia_ban) AS gia_ban, "
                                + " sp.ten_san_pham, sp.gia_nhap_hien_tai AS gia_nhap, "
                                + " bh.thoi_gian, "
                                + " ((IF(ct.is_gift = 1, 0, ct.gia_ban) - sp.gia_nhap_hien_tai) * ct.so_luong) AS loi_nhuan "
                                + " FROM chi_tiet_ban_hang ct"
                                + " JOIN ban_hang bh ON ct.id_ban_hang = bh.id"
                                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " WHERE 1=1" + statusCond + searchCond + dateCond
                                + " ORDER BY bh.thoi_gian DESC";

                        try (Statement s = conn.createStatement();
                                ResultSet rs = s.executeQuery(sql);
                                java.io.FileOutputStream fos = new java.io.FileOutputStream(finalTargetFile);
                                java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos,
                                        java.nio.charset.StandardCharsets.UTF_8);
                                java.io.BufferedWriter writer = new java.io.BufferedWriter(osw)) {

                            writer.write('\uFEFF');
                            writer.write("Sản phẩm,Phân loại,Mã HĐ,Ngày bán,Giá nhập,Giá bán,Số lượng,Lợi nhuận lý thuyết");
                            writer.newLine();

                            java.text.SimpleDateFormat datetimeFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

                            while (rs.next()) {
                                String tenSp = rs.getString("ten_san_pham");
                                String loai = rs.getInt("is_gift") == 1 ? "Tặng" : "Bán";
                                String maHD = "BH-" + rs.getInt("id_ban_hang");
                                String ngayBan = "";
                                Timestamp ts = rs.getTimestamp("thoi_gian");
                                if (ts != null) {
                                    ngayBan = datetimeFormat.format(ts);
                                }
                                long giaNhap = rs.getLong("gia_nhap");
                                long giaBan = rs.getLong("gia_ban");
                                int soLuong = rs.getInt("so_luong");
                                long loiNhuan = rs.getLong("loi_nhuan");

                                writer.write(escapeCsv(tenSp) + ",");
                                writer.write(escapeCsv(loai) + ",");
                                writer.write(escapeCsv(maHD) + ",");
                                writer.write(escapeCsv(ngayBan) + ",");
                                writer.write(giaNhap + ",");
                                writer.write(giaBan + ",");
                                writer.write(soLuong + ",");
                                writer.write(loiNhuan + "");
                                writer.newLine();
                            }
                            success = true;
                        }
                    } else {
                        String sql = "SELECT bh.id, bh.thoi_gian, bh.ngay_lap,"
                                + " IFNULL(kh.ten, 'Khách vãng lai') AS ten_khach,"
                                + " IFNULL(nv.ten_nhan_vien, '---') AS ten_nv,"
                                + " bh.tong_tien, bh.tien_da_thanh_toan, bh.tien_no, bh.trang_thai"
                                + " FROM ban_hang bh"
                                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                                + " LEFT JOIN nhan_vien nv ON bh.id_nhan_vien = nv.id"
                                + " WHERE 1=1" + statusCond + searchCond + dateCond
                                + " ORDER BY bh.thoi_gian DESC";

                        try (Statement s = conn.createStatement();
                                ResultSet rs = s.executeQuery(sql);
                                java.io.FileOutputStream fos = new java.io.FileOutputStream(finalTargetFile);
                                java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos,
                                        java.nio.charset.StandardCharsets.UTF_8);
                                java.io.BufferedWriter writer = new java.io.BufferedWriter(osw)) {

                            writer.write('\uFEFF');
                            writer.write(
                                    "Mã HĐ,Ngày lập,Ngày lắp,Khách hàng,Nhân viên,Tổng tiền,Đặt cọc,Khách nợ,Trạng thái");
                            writer.newLine();

                            java.text.SimpleDateFormat datetimeFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");

                            while (rs.next()) {
                                String maHD = "BH-" + rs.getInt("id");
                                String ngayLap = "";
                                Timestamp ts = rs.getTimestamp("thoi_gian");
                                if (ts != null) {
                                    ngayLap = datetimeFormat.format(ts);
                                }
                                String ngayLapDat = "--";
                                java.sql.Date dateLap = rs.getDate("ngay_lap");
                                if (dateLap != null) {
                                    ngayLapDat = dateFormat.format(dateLap);
                                }
                                String khachHang = rs.getString("ten_khach");
                                String nhanVien = rs.getString("ten_nv");
                                long tongTien = rs.getLong("tong_tien");
                                long datCoc = rs.getLong("tien_da_thanh_toan");
                                long conNo = rs.getLong("tien_no");
                                String trangThai = rs.getString("trang_thai");

                                writer.write(escapeCsv(maHD) + ",");
                                writer.write(escapeCsv(ngayLap) + ",");
                                writer.write(escapeCsv(ngayLapDat) + ",");
                                writer.write(escapeCsv(khachHang) + ",");
                                writer.write(escapeCsv(nhanVien) + ",");
                                writer.write(tongTien + ",");
                                writer.write(datCoc + ",");
                                writer.write(conNo + ",");
                                writer.write(escapeCsv(trangThai));
                                writer.newLine();
                            }
                            success = true;
                        }
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
                    JOptionPane.showMessageDialog(SalesOrdersPanel.this,
                            activeTab == 1 ? "Đã xuất danh sách chi tiết sản phẩm bán ra file CSV thành công!" : "Đã xuất dữ liệu hóa đơn bán hàng ra file Excel (CSV) thành công!",
                            "Xuất Excel thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(SalesOrdersPanel.this,
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

    private void updateStatusFilterButtonText() {
        java.util.List<String> selected = new java.util.ArrayList<>();
        for (JCheckBox cb : statusCheckboxes.values()) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        if (selected.isEmpty()) {
            statusFilterBtn.setText("Trạng thái kho ↓");
        } else if (selected.size() == statusCheckboxes.size()) {
            statusFilterBtn.setText("Tất cả trạng thái ↓");
        } else if (selected.size() == 1) {
            statusFilterBtn.setText(selected.get(0) + " ↓");
        } else {
            statusFilterBtn.setText(selected.size() + " trạng thái đã chọn ↓");
        }
    }
}

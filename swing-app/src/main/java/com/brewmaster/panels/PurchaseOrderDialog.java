package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.ActivityLogger;
import com.brewmaster.components.SearchableComboBox;
import com.brewmaster.components.SearchableCellEditor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Dialog Tạo / Sửa Đơn Nhập Hàng Nhiều Sản Phẩm
 *
 * Ánh xạ từ Stitch mockup "Nhập Hàng Nhiều Sản Phẩm Modal Overlay"
 * trong project "Cafe Management Dashboard" (Obsidian Brew dark theme).
 *
 * Cấu trúc DB:
 * nhap_hang (id, thoi_gian, tong_tien, id_doi_tac, id_nhan_vien)
 * chi_tiet_nhap_hang (id_nhap_hang, id_san_pham, so_luong, gia_nhap,
 * thanh_tien)
 */
public class PurchaseOrderDialog extends JDialog {

    // ───────────────────────── Constants ─────────────────────────
    private static final int DIALOG_W = 900;
    private static final int DIALOG_H = 680;

    // ───────────────────────── State ─────────────────────────────
    private final boolean isEditMode;
    private final Integer editId; // null khi thêm mới
    private final boolean isReadOnly;
    private boolean saved = false;
    private Runnable onSaveCallback;

    // ───────────────────────── Header fields ─────────────────────
    private JTextField tfId; // Số HĐ / ID
    private JComboBox<String> cbNCC; // Nhà cung cấp
    private JTextField tfSdtNCC;     // SĐT nhà cung cấp (tự điền)
    private JComboBox<String> cbNV; // Nhân viên
    private JTextField tfDate; // Thời gian (dd/MM/yyyy HH:mm)
    private JTextField tfNgayNhan; // Ngày nhận hàng (dd/MM/yyyy)
    private JTextField tfTienDaThanhToan; // Tiền đã thanh toán
    private JComboBox<String> cbTrangThai; // Trạng thái
    private JTextArea tfGhiChu; // Ghi chú

    // ───────────────────────── Product table ─────────────────────
    /**
     * Mỗi dòng: [id_san_pham(Integer), tenSP(String), soLuong(Integer),
     * giaNhap(Long), thanhTien(Long)]
     */
    private DefaultTableModel productTableModel;
    private JTable productTable;
    private JLabel lblTotal;
    private JLabel lblPaid;
    private JLabel lblDebt;

    // ───────────────────────── Lookup maps ───────────────────────
    private final Map<String, Integer> nccMap = new LinkedHashMap<>(); // ten_ncc → id
    private final Map<String, String>  nccSdtMap = new LinkedHashMap<>(); // ten_ncc → sdt
    private final Map<String, Integer> nvMap = new LinkedHashMap<>(); // ten_nhan_vien → id
    private final Map<String, Integer> spMap = new LinkedHashMap<>(); // ten_san_pham → id
    private final Map<String, String>  spUnitMap = new LinkedHashMap<>(); // ten_san_pham → ten_don_vi

    private final NumberFormat vnd = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private String lastSelectedNCC = null;

    // ═════════════════════════ Constructors ══════════════════════

    /** Thêm mới đơn nhập */
    public PurchaseOrderDialog(Frame owner) {
        super(owner, "Nhập Hàng Mới  ", true);
        this.isEditMode = false;
        this.editId = null;
        this.isReadOnly = false;
        init();
    }

    /** Sửa đơn nhập theo id */
    public PurchaseOrderDialog(Frame owner, int orderId) {
        super(owner, "Sửa Đơn Nhập  #NH-" + orderId + "  ", true);
        this.isEditMode = true;
        this.editId = orderId;
        this.isReadOnly = false;
        init();
        loadOrderData();
    }

    /** Xem đơn nhập theo id (Chỉ đọc) */
    public PurchaseOrderDialog(Frame owner, int orderId, boolean isReadOnly) {
        super(owner, isReadOnly ? "Chi Tiết Đơn Nhập  #NH-" + orderId + "  " : "Sửa Đơn Nhập  #NH-" + orderId + "  ", true);
        this.isEditMode = true;
        this.editId = orderId;
        this.isReadOnly = isReadOnly;
        init();
        loadOrderData();
    }

    public void setOnSaveCallback(Runnable cb) {
        this.onSaveCallback = cb;
    }

    public boolean isSaved() {
        return saved;
    }

    // ═════════════════════════ Init ══════════════════════════════

    private void init() {
        setSize(DIALOG_W, DIALOG_H);
        setMinimumSize(new Dimension(780, 560));
        setLocationRelativeTo(getOwner());
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE_HIGH);

        // Load lookup data from DB
        loadLookups();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);

        root.add(buildHeader(), BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(buildBody());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        root.add(scrollPane, BorderLayout.CENTER);
        
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        updatePaymentStats();
    }

    // ═════════════════════════ Header ════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.SURFACE_HIGH);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 20, 12, 16)));

        String titleText = isReadOnly
                ? "<html><nobr><font face='Segoe UI Emoji'>👁</font>  Chi Tiết Đơn Nhập  #NH-" + editId + "  </nobr></html>"
                : (isEditMode
                    ? "<html><nobr><font face='Segoe UI Emoji'>✏</font>  Sửa Đơn Nhập  #NH-" + editId + "  </nobr></html>"
                    : "<html><nobr><font face='Segoe UI Emoji'>📤</font>  Nhập Hàng Mới  </nobr></html>");
        JLabel title = new JLabel(titleText);
        title.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 16f));
        title.setForeground(AppTheme.ON_SURFACE);
        title.setBorder(new EmptyBorder(6, 0, 6, 0));

        JButton closeBtn = iconButton("X");
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(AppTheme.ERROR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(AppTheme.ON_SURFACE_VAR);
            }
        });

        p.add(title, BorderLayout.WEST);
        p.add(closeBtn, BorderLayout.EAST);
        return p;
    }

    // ═════════════════════════ Body ══════════════════════════════

    private JPanel buildBody() {
        JPanel body = new ScrollablePanel(new BorderLayout(0, 16));
        body.setBackground(AppTheme.SURFACE_HIGH);
        body.setBorder(new EmptyBorder(20, 20, 16, 20));

        // ── Top part: Thông tin chung ──
        JPanel topPart = new JPanel();
        topPart.setBackground(AppTheme.SURFACE_HIGH);
        topPart.setLayout(new BoxLayout(topPart, BoxLayout.Y_AXIS));

        topPart.add(sectionLabel("📋THÔNG TIN CHUNG  "));
        topPart.add(Box.createRigidArea(new Dimension(0, 10)));
        topPart.add(fullWidth(buildGeneralInfo()));
        topPart.add(Box.createRigidArea(new Dimension(0, 20)));
        topPart.add(divider());

        body.add(topPart, BorderLayout.NORTH);

        // ── Center part: Danh sách sản phẩm ──
        JPanel centerPart = new JPanel(new BorderLayout());
        centerPart.setBackground(AppTheme.SURFACE_HIGH);

        // Label panel to handle alignment and margin
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(sectionLabel("📦DANH SÁCH SẢN PHẨM  "));
        labelPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        centerPart.add(labelPanel, BorderLayout.NORTH);
        centerPart.add(buildProductSection(), BorderLayout.CENTER);

        body.add(centerPart, BorderLayout.CENTER);

        return body;
    }

    /** Grid 3 hàng, 3 cột chứa thông tin chung */
    private JPanel buildGeneralInfo() {
        JPanel grid = new JPanel(new GridLayout(3, 3, 14, 10));
        grid.setOpaque(false);

        // Instantiate all fields first
        tfId = createTextField();
        if (isEditMode) {
            tfId.setText(String.valueOf(editId));
            tfId.setEditable(false);
            tfId.setEnabled(false);
        }

        cbNCC = createComboBox(nccMap.keySet().toArray(new String[0]), "-- Chọn nhà cung cấp --");
        cbNCC.addItem("+ Nhà cung cấp mới");
        
        JButton searchNCCBtn = new JButton("🔍");
        searchNCCBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        searchNCCBtn.setPreferredSize(new Dimension(36, 36));
        searchNCCBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchNCCBtn.setFocusPainted(false);
        searchNCCBtn.setBackground(AppTheme.SURFACE_MED);
        searchNCCBtn.setForeground(AppTheme.ON_SURFACE);
        searchNCCBtn.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        
        searchNCCBtn.addActionListener(e -> {
            PartnerSearchDialog searchDialog = new PartnerSearchDialog((Frame) SwingUtilities.getWindowAncestor(this), "nha_cung_cap");
            searchDialog.setVisible(true);
            if (searchDialog.isConfirmed()) {
                String name = searchDialog.getSelectedName();
                Integer id = searchDialog.getSelectedId();
                String sdt = searchDialog.getSelectedSdt();
                if (name != null && id != null) {
                    nccMap.put(name, id);
                    nccSdtMap.put(name, sdt != null ? sdt : "");
                    
                    ActionListener[] listeners = cbNCC.getActionListeners();
                    for (ActionListener l : listeners) {
                        cbNCC.removeActionListener(l);
                    }
                    
                    cbNCC.removeAllItems();
                    cbNCC.addItem("-- Chọn nhà cung cấp --");
                    for (String n : nccMap.keySet()) {
                        cbNCC.addItem(n);
                    }
                    cbNCC.addItem("+ Nhà cung cấp mới");
                    
                    cbNCC.setSelectedItem(name);
                    tfSdtNCC.setText(sdt != null ? sdt : "");
                    lastSelectedNCC = name;
                    
                    for (ActionListener l : listeners) {
                        cbNCC.addActionListener(l);
                    }
                    updatePaymentStats();
                }
            }
        });
        
        JPanel nccPanel = new JPanel(new BorderLayout(5, 0));
        nccPanel.setOpaque(false);
        nccPanel.add(cbNCC, BorderLayout.CENTER);
        nccPanel.add(searchNCCBtn, BorderLayout.EAST);
        
        tfSdtNCC = createTextField();
        tfSdtNCC.setEditable(false);
        tfSdtNCC.setBackground(AppTheme.SURFACE_MED);
        tfSdtNCC.putClientProperty("JTextField.placeholderText", "Tự động điền khi chọn NCC");
        
        cbNCC.addActionListener(e -> {
            String sel = (String) cbNCC.getSelectedItem();
            if ("+ Nhà cung cấp mới".equals(sel)) {
                PartnerDialog dialog = new PartnerDialog((Frame) SwingUtilities.getWindowAncestor(this), "nha_cung_cap");
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    Integer newId = dialog.getGeneratedId();
                    if (newId != null) {
                        try {
                            Connection conn = DatabaseManager.getInstance().getConnection();
                            try (PreparedStatement ps = conn.prepareStatement("SELECT ten, sdt FROM doi_tac WHERE id = ?")) {
                                ps.setInt(1, newId);
                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        String ten = rs.getString("ten");
                                        String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                                        
                                        nccMap.put(ten, newId);
                                        nccSdtMap.put(ten, sdt);
                                        
                                        ActionListener[] listeners = cbNCC.getActionListeners();
                                        for (ActionListener l : listeners) {
                                            cbNCC.removeActionListener(l);
                                        }
                                        
                                        cbNCC.removeAllItems();
                                        cbNCC.addItem("-- Chọn nhà cung cấp --");
                                        for (String n : nccMap.keySet()) {
                                            cbNCC.addItem(n);
                                        }
                                        cbNCC.addItem("+ Nhà cung cấp mới");
                                        
                                        cbNCC.setSelectedItem(ten);
                                        tfSdtNCC.setText(sdt);
                                        lastSelectedNCC = ten;
                                        
                                        for (ActionListener l : listeners) {
                                            cbNCC.addActionListener(l);
                                        }
                                        return;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                
                // Revert
                ActionListener[] listeners = cbNCC.getActionListeners();
                for (ActionListener l : listeners) {
                    cbNCC.removeActionListener(l);
                }
                if (lastSelectedNCC != null) {
                    cbNCC.setSelectedItem(lastSelectedNCC);
                    tfSdtNCC.setText(nccSdtMap.getOrDefault(lastSelectedNCC, ""));
                } else {
                    cbNCC.setSelectedIndex(0);
                    tfSdtNCC.setText("");
                }
                for (ActionListener l : listeners) {
                    cbNCC.addActionListener(l);
                }
            } else {
                if (sel != null && nccSdtMap.containsKey(sel)) {
                    tfSdtNCC.setText(nccSdtMap.get(sel));
                    lastSelectedNCC = sel;
                } else {
                    tfSdtNCC.setText("");
                    if (sel == null || sel.startsWith("--")) {
                        lastSelectedNCC = null;
                    }
                }
            }
        });

        cbNV = createComboBox(nvMap.keySet().toArray(new String[0]), "-- Chọn nhân viên --");

        tfDate = createTextField();
        tfDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));

        cbTrangThai = createComboBox(new String[] { "Chờ Nhận", "Đã nhận", "Đã hủy" }, "-- Chọn trạng thái --");
        if (cbTrangThai.getItemCount() > 1) {
            cbTrangThai.setSelectedIndex(1); // Mặc định là "Chờ Nhận"
        }

        tfTienDaThanhToan = createTextField();
        tfTienDaThanhToan.setText("0");
        tfTienDaThanhToan.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePaymentStats();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePaymentStats();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePaymentStats();
            }
        });

        tfNgayNhan = createTextField();
        tfNgayNhan.putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");

        // Add fields to grid in the requested order:
        // Row 1
        grid.add(fieldBlock("Số HĐ / ID *", tfId));
        grid.add(fieldBlock("Nhà cung cấp *", nccPanel));
        grid.add(fieldBlock("SĐT nhà cung cấp", tfSdtNCC));

        // Row 2
        grid.add(fieldBlock("Thời gian", tfDate));
        grid.add(fieldBlock("Nhân viên", cbNV));
        grid.add(fieldBlock("Trạng thái *", cbTrangThai));

        // Row 3
        grid.add(fieldBlock("Ngày nhận hàng", tfNgayNhan));
        grid.add(fieldBlock("Tiền đã thanh toán (₫)", tfTienDaThanhToan));
        grid.add(new JPanel() {{ setOpaque(false); }});

        // Tạo ghi chú
        tfGhiChu = new JTextArea(3, 20);
        tfGhiChu.setLineWrap(true);
        tfGhiChu.setWrapStyleWord(true);
        tfGhiChu.setFont(AppTheme.FONT_BODY_MD);
        tfGhiChu.setBackground(AppTheme.SURFACE_MED);
        tfGhiChu.setForeground(AppTheme.ON_SURFACE);
        tfGhiChu.setCaretColor(AppTheme.ON_SURFACE);
        tfGhiChu.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scrollGhiChu = new JScrollPane(tfGhiChu);
        scrollGhiChu.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        scrollGhiChu.setPreferredSize(new Dimension(0, 80));

        if (isReadOnly) {
            cbNCC.setEnabled(false);
            searchNCCBtn.setEnabled(false);
            cbNV.setEnabled(false);
            tfSdtNCC.setEditable(false);
            tfSdtNCC.setEnabled(false);
            tfSdtNCC.setBackground(AppTheme.SURFACE_LOW);
            tfDate.setEditable(false);
            tfDate.setBackground(AppTheme.SURFACE_LOW);
            tfNgayNhan.setEditable(false);
            tfNgayNhan.setBackground(AppTheme.SURFACE_LOW);
            cbTrangThai.setEnabled(false);
            tfTienDaThanhToan.setEditable(false);
            tfTienDaThanhToan.setBackground(AppTheme.SURFACE_LOW);
            tfId.setEditable(false);
            tfId.setEnabled(false);
            tfGhiChu.setEditable(false);
            tfGhiChu.setBackground(AppTheme.SURFACE_LOW);
            scrollGhiChu.getViewport().setBackground(AppTheme.SURFACE_LOW);
        }

        // Tạo container bọc ngoài: grid ở trên, ghi chú ở dưới làm dòng riêng biệt
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.add(grid);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
        container.add(fieldBlock("Ghi chú", scrollGhiChu));

        return container;
    }

    /** Bảng sản phẩm + nút "Thêm sản phẩm" */
    private JPanel buildProductSection() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(AppTheme.SURFACE_MED);
        wrapper.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));

        // ── Table model ──
        String[] cols = { "Sản phẩm", "Đơn vị", "SL", "Đơn giá (₫)", "Thành tiền (₫)", "" };
        productTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                if (isReadOnly) return false;
                // Columns: 0 = Product Combo, 1 = Unit (read-only), 2 = Quantity, 3 = Unit Price, 5 = Delete button
                return c == 0 || c == 2 || c == 3 || c == 5;
            }
        };

        productTable = new JTable(productTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? AppTheme.SURFACE_MED : AppTheme.SURFACE_HIGH);
                c.setForeground(AppTheme.ON_SURFACE);
                if (c instanceof JComponent)
                    ((JComponent) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        styleTable(productTable);

        // ── Column widths ──
        productTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Sản phẩm
        productTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Đơn vị
        productTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // SL
        productTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Đơn giá
        productTable.getColumnModel().getColumn(4).setPreferredWidth(130); // Thành tiền
        productTable.getColumnModel().getColumn(5).setPreferredWidth(44);  // Xóa

        // ── Column 0: ComboBox chọn sản phẩm ──
        String[] spNames = spMap.keySet().toArray(new String[0]);
        SearchableComboBox spCombo = new SearchableComboBox(spNames);
        spCombo.setBackground(AppTheme.SURFACE_MED);
        spCombo.setForeground(AppTheme.ON_SURFACE);
        spCombo.setFont(AppTheme.FONT_BODY_MD);
        productTable.getColumnModel().getColumn(0).setCellEditor(new SearchableCellEditor(spCombo) {
            @Override
            public boolean stopCellEditing() {
                String sel = (String) spCombo.getSelectedItem();
                int row = productTable.getEditingRow();
                boolean ok = super.stopCellEditing();
                // Tự điền đơn giá nhập & đơn vị từ DB/Map
                if (ok && sel != null) {
                    Integer spId = spMap.get(sel);
                    if (spId != null) {
                        if (row >= 0) {
                            long gia = fetchGiaNhap(spId);
                            // Ghi giá nhập vào cột 3, đơn vị vào cột 1
                            productTableModel.setValueAt(spUnitMap.get(sel), row, 1);
                            productTableModel.setValueAt(vnd.format(gia), row, 3);
                            updateThanhTien(row);
                        }
                    }
                }
                return ok;
            }
        });

        // ── Column 5: Nút xóa dòng ──
        productTable.getColumnModel().getColumn(5).setCellRenderer(new DeleteBtnRenderer());
        productTable.getColumnModel().getColumn(5).setCellEditor(new DeleteBtnEditor(productTable));

        // ── Listen changes ──
        productTableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int col = e.getColumn();
                int row = e.getFirstRow();
                if (row >= 0 && (col == 2 || col == 3)) { // col 2 = SL, col 3 = Đơn giá
                    updateThanhTien(row);
                }
            }
            updatePaymentStats();
        });

        // ── Header style ──
        JTableHeader header = productTable.getTableHeader();
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setForeground(AppTheme.ON_SURFACE_VAR);
        header.setFont(AppTheme.FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT));

        JScrollPane tableScroll = new JScrollPane(productTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(AppTheme.SURFACE_MED);
        tableScroll.setPreferredSize(new Dimension(0, 320));

        // ── Nút Thêm sản phẩm ──
        JButton addRowBtn = new JButton("<html><font face='Segoe UI Emoji'>➕</font>  Thêm sản phẩm</html>");
        addRowBtn.setFont(AppTheme.FONT_LABEL);
        addRowBtn.setForeground(AppTheme.PRIMARY);
        addRowBtn.setBackground(AppTheme.SURFACE_MED);
        addRowBtn.setBorderPainted(false);
        addRowBtn.setFocusPainted(false);
        addRowBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addRowBtn.setPreferredSize(new Dimension(0, 40));
        addRowBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(0, 16, 0, 16)));
        addRowBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addRowBtn.setBackground(AppTheme.withAlpha(AppTheme.PRIMARY, 20));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                addRowBtn.setBackground(AppTheme.SURFACE_MED);
            }
        });
        addRowBtn.addActionListener(e -> addProductRow(null, null, null, null, null));

        wrapper.add(tableScroll, BorderLayout.CENTER);
        if (!isReadOnly) {
            wrapper.add(addRowBtn, BorderLayout.SOUTH);
        } else {
            productTable.removeColumn(productTable.getColumnModel().getColumn(5));
        }
        return wrapper;
    }

    // ═════════════════════════ Footer ════════════════════════════

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.SURFACE_HIGH);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 20, 14, 20)));

        // ── Thống kê tiền thanh toán (trái) ──
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsPanel.setOpaque(false);

        // Tổng cộng
        JPanel pTotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pTotal.setOpaque(false);
        JLabel lblTotalLabel = new JLabel("Tổng cộng:");
        lblTotalLabel.setFont(AppTheme.FONT_BODY_MD);
        lblTotalLabel.setForeground(AppTheme.ON_SURFACE_VAR);
        lblTotal = new JLabel("0 ₫");
        lblTotal.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 15f));
        lblTotal.setForeground(AppTheme.ON_SURFACE);
        pTotal.add(lblTotalLabel);
        pTotal.add(lblTotal);

        // Đã thanh toán
        JPanel pPaid = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pPaid.setOpaque(false);
        JLabel lblPaidLabel = new JLabel("Đã trả:");
        lblPaidLabel.setFont(AppTheme.FONT_BODY_MD);
        lblPaidLabel.setForeground(AppTheme.ON_SURFACE_VAR);
        lblPaid = new JLabel("0 ₫");
        lblPaid.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 15f));
        lblPaid.setForeground(AppTheme.TERTIARY);
        pPaid.add(lblPaidLabel);
        pPaid.add(lblPaid);

        // Còn nợ
        JPanel pDebt = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pDebt.setOpaque(false);
        JLabel lblDebtLabel = new JLabel("Còn nợ:");
        lblDebtLabel.setFont(AppTheme.FONT_BODY_MD);
        lblDebtLabel.setForeground(AppTheme.ON_SURFACE_VAR);
        lblDebt = new JLabel("0 ₫");
        lblDebt.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 16f));
        lblDebt.setForeground(AppTheme.PRIMARY);
        pDebt.add(lblDebtLabel);
        pDebt.add(lblDebt);

        statsPanel.add(pTotal);
        statsPanel.add(pPaid);
        statsPanel.add(pDebt);

        // ── Nút hành động (phải) ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = new JButton(isReadOnly ? "Đóng" : "Hủy");
        cancelBtn.setFont(AppTheme.FONT_LABEL);
        cancelBtn.setForeground(AppTheme.ON_SURFACE_VAR);
        cancelBtn.setBackground(AppTheme.SURFACE_HIGH);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(7, 20, 7, 20)));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(isEditMode
                ? "<html><font face='Segoe UI Emoji'>✔️</font>  Cập nhật đơn</html>"
                : "<html><font face='Segoe UI Emoji'>✔️</font>  Lưu đơn nhập</html>");
        saveBtn.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        saveBtn.setForeground(AppTheme.ON_PRIMARY);
        saveBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        saveBtn.setBorder(new EmptyBorder(8, 22, 8, 22));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> onSave());

        btnPanel.add(cancelBtn);
        if (!isReadOnly) {
            btnPanel.add(saveBtn);
        }

        footer.add(statsPanel, BorderLayout.WEST);
        footer.add(btnPanel, BorderLayout.EAST);
        return footer;
    }

    // ═════════════════════════ Data loading ══════════════════════

    private void loadLookups() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // Đối tác (nhà cung cấp)
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(
                        "SELECT id, ten, sdt FROM doi_tac ORDER BY ten")) {
                while (rs.next()) {
                    String ten = rs.getString("ten");
                    nccMap.put(ten, rs.getInt("id"));
                    nccSdtMap.put(ten, rs.getString("sdt") != null ? rs.getString("sdt") : "");
                }
            }
            // Nhân viên
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id, ten_nhan_vien FROM nhan_vien ORDER BY ten_nhan_vien")) {
                while (rs.next())
                    nvMap.put(rs.getString("ten_nhan_vien"), rs.getInt("id"));
            }
            // Sản phẩm
            String spSql = "SELECT sp.id, sp.ten_san_pham, dv.ten_don_vi FROM san_pham sp "
                         + "LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id "
                         + "ORDER BY sp.ten_san_pham";
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(spSql)) {
                while (rs.next()) {
                    String tenSp = rs.getString("ten_san_pham");
                    spMap.put(tenSp, rs.getInt("id"));
                    spUnitMap.put(tenSp, rs.getString("ten_don_vi") != null ? rs.getString("ten_don_vi") : "Cái");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadOrderData() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            // Header
            String sql = "SELECT nh.*, ncc.ten AS ten_ncc, nv.ten_nhan_vien FROM nhap_hang nh"
                    + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                    + " LEFT JOIN nhan_vien nv ON nh.id_nhan_vien = nv.id"
                    + " WHERE nh.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, editId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    // NCC
                    String tenNCC = rs.getString("ten_ncc");
                    selectComboItem(cbNCC, tenNCC);
                    if (tenNCC != null && nccSdtMap.containsKey(tenNCC)) {
                        tfSdtNCC.setText(nccSdtMap.get(tenNCC));
                        lastSelectedNCC = tenNCC;
                    }
                    // NV
                    String tenNV = rs.getString("ten_nhan_vien");
                    selectComboItem(cbNV, tenNV);
                    // Thời gian
                    Timestamp ts = rs.getTimestamp("thoi_gian");
                    if (ts != null)
                        tfDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts));
                    // Ngày nhận
                    java.sql.Date ngayNhan = rs.getDate("ngay_nhan");
                    if (ngayNhan != null)
                        tfNgayNhan.setText(new SimpleDateFormat("dd/MM/yyyy").format(ngayNhan));
                    // Trạng thái
                    String trangThai = rs.getString("trang_thai");
                    selectComboItem(cbTrangThai, trangThai);
                    // Tiền đã thanh toán
                    tfTienDaThanhToan.setText(String.valueOf(rs.getLong("da_thanh_toan")));
                    // Ghi chú
                    String ghiChu = rs.getString("ghi_chu");
                    tfGhiChu.setText(ghiChu != null ? ghiChu : "");
                }
            }

            // Chi tiết sản phẩm
            String detailSql = "SELECT ct.*, sp.ten_san_pham, dv.ten_don_vi AS sp_don_vi FROM chi_tiet_nhap_hang ct"
                    + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                    + " LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id"
                    + " WHERE ct.id_nhap_hang = ?";
            try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
                ps.setInt(1, editId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String donVi = rs.getString("don_vi");
                    if (donVi == null) {
                        donVi = rs.getString("sp_don_vi");
                    }
                    if (donVi == null) {
                        donVi = "Cái";
                    }
                    addProductRow(
                            rs.getString("ten_san_pham"),
                            donVi,
                            rs.getInt("so_luong"),
                            rs.getLong("gia_nhap"),
                            rs.getLong("thanh_tien"));
                }
            }
            updatePaymentStats();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu đơn nhập: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════ Save logic ════════════════════════

    private void onSave() {
        // Dừng editing đang chạy
        if (productTable.isEditing())
            productTable.getCellEditor().stopCellEditing();

        // ─ Validate ─
        String rawId = tfId.getText().trim();
        if (rawId.isEmpty()) {
            warn("Vui lòng nhập Số HĐ / ID.");
            return;
        }
        int customId;
        try {
            customId = Integer.parseInt(rawId);
        } catch (NumberFormatException ex) {
            warn("Số HĐ / ID phải là số nguyên dương.");
            return;
        }
        if (customId <= 0) {
            warn("Số HĐ / ID phải là số nguyên dương.");
            return;
        }

        String tenNCC = (String) cbNCC.getSelectedItem();
        if (tenNCC == null || tenNCC.startsWith("--")) {
            warn("Vui lòng chọn nhà cung cấp.");
            return;
        }

        String trangThai = (String) cbTrangThai.getSelectedItem();
        if (trangThai == null || trangThai.startsWith("--")) {
            warn("Vui lòng chọn trạng thái đơn nhập.");
            return;
        }

        if (productTableModel.getRowCount() == 0) {
            warn("Vui lòng thêm ít nhất một sản phẩm vào đơn nhập.");
            return;
        }

        // Kiểm tra từng dòng
        for (int r = 0; r < productTableModel.getRowCount(); r++) {
            Object tenSP = productTableModel.getValueAt(r, 0);
            if (tenSP == null || tenSP.toString().trim().isEmpty()) {
                warn("Dòng " + (r + 1) + ": Vui lòng chọn sản phẩm.");
                return;
            }
            int qty = parseIntSafe(productTableModel.getValueAt(r, 2));
            if (qty <= 0) {
                warn("Dòng " + (r + 1) + ": Số lượng phải lớn hơn 0.");
                return;
            }
            long gia = parseLongVnd(productTableModel.getValueAt(r, 3));
            if (gia < 0) {
                warn("Dòng " + (r + 1) + ": Đơn giá nhập không được âm.");
                return;
            }
        }

        // ── Kiểm tra định dạng thời gian ──
        String thoiGianText = tfDate.getText().trim();
        if (!thoiGianText.isEmpty()) {
            try {
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").parse(thoiGianText);
            } catch (Exception ex) {
                warn("Thời gian không đúng định dạng dd/MM/yyyy HH:mm.\nVí dụ: 25/05/2026 14:30");
                tfDate.requestFocus();
                return;
            }
        }

        // ── Kiểm tra định dạng ngày nhận hàng ──
        String ngayNhanText = tfNgayNhan.getText().trim();
        if (!ngayNhanText.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                sdf.setLenient(false);
                sdf.parse(ngayNhanText);
            } catch (Exception ex) {
                warn("Ngày nhận hàng không đúng định dạng dd/MM/yyyy.\nVí dụ: 25/05/2026");
                tfNgayNhan.requestFocus();
                return;
            }
        }

        // ── Kiểm tra tiền đã thanh toán ──
        long tongTienPreview = calcTotal();
        long daThanhToanPreview;
        try {
            String rawPaid = tfTienDaThanhToan.getText().replaceAll("[^0-9\\-]", "");
            daThanhToanPreview = rawPaid.isEmpty() ? 0 : Long.parseLong(rawPaid);
        } catch (NumberFormatException ex) {
            warn("Tiền đã thanh toán không hợp lệ. Vui lòng nhập số.");
            tfTienDaThanhToan.requestFocus();
            return;
        }
        if (daThanhToanPreview < 0) {
            warn("Tiền đã thanh toán không được âm.");
            tfTienDaThanhToan.requestFocus();
            return;
        }
        if (daThanhToanPreview > tongTienPreview) {
            warn("Tiền đã thanh toán (" + vnd.format(daThanhToanPreview) + " ₫) không được lớn hơn\n"
                    + "tổng tiền đơn nhập (" + vnd.format(tongTienPreview) + " ₫).");
            tfTienDaThanhToan.requestFocus();
            return;
        }

        // ── Kiểm tra sản phẩm trùng lặp ──
        Map<String, Integer> spDemSP = new LinkedHashMap<>();
        for (int r = 0; r < productTableModel.getRowCount(); r++) {
            Object tenSpObj = productTableModel.getValueAt(r, 0);
            if (tenSpObj == null) continue;
            String tenSP = tenSpObj.toString().trim();
            if (tenSP.isEmpty()) continue;
            spDemSP.merge(tenSP, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : spDemSP.entrySet()) {
            if (entry.getValue() > 1) {
                warn("Sản phẩm \"" + entry.getKey() + "\" bị trùng lặp " + entry.getValue()
                        + " lần trong danh sách nhập.\nVui lòng gộp thành một dòng.");
                return;
            }
        }

        // ── Cảnh báo xác nhận nếu trạng thái "Đã nhận" mà chưa thanh toán đủ ──
        if ("Đã nhận".equals(trangThai) && daThanhToanPreview < tongTienPreview) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "<html>Đơn nhập ở trạng thái <b>\"Đã nhận\"</b> nhưng chưa thanh toán đủ.<br>"
                    + "  • Tổng tiền: <b>" + vnd.format(tongTienPreview) + " ₫</b><br>"
                    + "  • Đã thanh toán: <b>" + vnd.format(daThanhToanPreview) + " ₫</b><br>"
                    + "  • Còn nợ: <b>" + vnd.format(tongTienPreview - daThanhToanPreview) + " ₫</b><br><br>"
                    + "Bạn có muốn tiếp tục lưu không?</html>",
                    "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (!isEditMode) {
                if (checkIdExists(conn, customId)) {
                    warn("Số HĐ / ID này đã tồn tại trong hệ thống. Vui lòng chọn số khác.");
                    return;
                }
            }
            conn.setAutoCommit(false);
            try {
                // ─ Ghi chú ─
                String ghiChu = tfGhiChu.getText().trim();

                // ─ Tổng tiền & Thanh toán ─
                long tongTien = calcTotal();
                long daThanhToan = parseLongVnd(tfTienDaThanhToan.getText());
                long conNo = tongTien - daThanhToan;
                if (conNo < 0)
                    conNo = 0;

                // ─ NCC / NV ids ─
                Integer idNCC = nccMap.get(tenNCC);
                String tenNV = (String) cbNV.getSelectedItem();
                Integer idNV = (tenNV != null && !tenNV.startsWith("--")) ? nvMap.get(tenNV) : null;

                // ─ Parse ngày nhận ─
                java.sql.Date ngayNhan = null;
                if (!ngayNhanText.isEmpty()) {
                    try {
                        java.util.Date parsed = new SimpleDateFormat("dd/MM/yyyy").parse(ngayNhanText);
                        ngayNhan = new java.sql.Date(parsed.getTime());
                    } catch (Exception ex) {
                        // bỏ qua nếu sai định dạng
                    }
                }

                // ─ Thời gian ─
                Timestamp ts;
                try {
                    ts = new Timestamp(
                            new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(tfDate.getText().trim()).getTime());
                } catch (Exception ex) {
                    ts = new Timestamp(System.currentTimeMillis());
                }

                int orderId;
                if (isEditMode) {
                    // 1. Phục hồi tồn kho cũ trước khi cập nhật (trừ bớt số lượng đã nhập trước đó)
                    String oldDetailsSql = "SELECT id_san_pham, so_luong FROM chi_tiet_nhap_hang WHERE id_nhap_hang = ?";
                    try (PreparedStatement psGetOld = conn.prepareStatement(oldDetailsSql)) {
                        psGetOld.setInt(1, editId);
                        try (ResultSet rsOld = psGetOld.executeQuery()) {
                            while (rsOld.next()) {
                                int oldSpId = rsOld.getInt("id_san_pham");
                                int oldQty = rsOld.getInt("so_luong");
                                try (PreparedStatement psRestore = conn.prepareStatement(
                                        "UPDATE san_pham SET so_luong_ton = so_luong_ton - ? WHERE id = ?")) {
                                    psRestore.setInt(1, oldQty);
                                    psRestore.setInt(2, oldSpId);
                                    psRestore.executeUpdate();
                                }
                            }
                        }
                    }

                    // 2. UPDATE nhap_hang
                    String upd = "UPDATE nhap_hang SET thoi_gian=?, tong_tien=?, da_thanh_toan=?, tien_no=?, trang_thai=?, ngay_nhan=?, id_doi_tac=?, id_nhan_vien=?, ghi_chu=? WHERE id=?";
                    try (PreparedStatement ps = conn.prepareStatement(upd)) {
                        ps.setTimestamp(1, ts);
                        ps.setLong(2, tongTien);
                        ps.setLong(3, daThanhToan);
                        ps.setLong(4, conNo);
                        ps.setString(5, trangThai);
                        if (ngayNhan != null)
                            ps.setDate(6, ngayNhan);
                        else
                            ps.setNull(6, Types.DATE);
                        if (idNCC != null)
                            ps.setInt(7, idNCC);
                        else
                            ps.setNull(7, Types.INTEGER);
                        if (idNV != null)
                            ps.setInt(8, idNV);
                        else
                            ps.setNull(8, Types.INTEGER);
                        ps.setString(9, ghiChu.isEmpty() ? null : ghiChu);
                        ps.setInt(10, editId);
                        ps.executeUpdate();
                    }
                    // Xóa chi tiết cũ
                    try (PreparedStatement ps = conn
                            .prepareStatement("DELETE FROM chi_tiet_nhap_hang WHERE id_nhap_hang=?")) {
                        ps.setInt(1, editId);
                        ps.executeUpdate();
                    }
                    orderId = editId;
                } else {
                    // INSERT nhap_hang
                    String ins = "INSERT INTO nhap_hang (id, thoi_gian, tong_tien, da_thanh_toan, tien_no, trang_thai, ngay_nhan, id_doi_tac, id_nhan_vien, ghi_chu) VALUES (?,?,?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ps = conn.prepareStatement(ins)) {
                        ps.setInt(1, customId);
                        ps.setTimestamp(2, ts);
                        ps.setLong(3, tongTien);
                        ps.setLong(4, daThanhToan);
                        ps.setLong(5, conNo);
                        ps.setString(6, trangThai);
                        if (ngayNhan != null)
                            ps.setDate(7, ngayNhan);
                        else
                            ps.setNull(7, Types.DATE);
                        if (idNCC != null)
                            ps.setInt(8, idNCC);
                        else
                            ps.setNull(8, Types.INTEGER);
                        if (idNV != null)
                            ps.setInt(9, idNV);
                        else
                            ps.setNull(9, Types.INTEGER);
                        ps.setString(10, ghiChu.isEmpty() ? null : ghiChu);
                        ps.executeUpdate();
                        orderId = customId;
                    }
                }

                // ─ INSERT chi tiết & Cộng tồn kho ─
                String insDet = "INSERT INTO chi_tiet_nhap_hang (id_nhap_hang, id_san_pham, so_luong, don_vi, gia_nhap, thanh_tien) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(insDet)) {
                    for (int r = 0; r < productTableModel.getRowCount(); r++) {
                        String tenSP = productTableModel.getValueAt(r, 0).toString();
                        Integer spId = spMap.get(tenSP);
                        if (spId == null)
                            continue;
                        String donVi = productTableModel.getValueAt(r, 1) != null ? productTableModel.getValueAt(r, 1).toString() : "Cái";
                        int qty = parseIntSafe(productTableModel.getValueAt(r, 2));
                        long gia = parseLongVnd(productTableModel.getValueAt(r, 3));
                        long thanh = qty * gia;

                        ps.setInt(1, orderId);
                        ps.setInt(2, spId);
                        ps.setInt(3, qty);
                        ps.setString(4, donVi);
                        ps.setLong(5, gia);
                        ps.setLong(6, thanh);
                        ps.addBatch();

                        // Cập nhật tồn kho
                        try (PreparedStatement upd = conn.prepareStatement(
                                "UPDATE san_pham SET so_luong_ton = so_luong_ton + ? WHERE id = ?")) {
                            upd.setInt(1, qty);
                            upd.setInt(2, spId);
                            upd.executeUpdate();
                        }
                    }
                    ps.executeBatch();
                }

                // Auto-manage thu_chi (Cash flow)
                // If daThanhToan > 0, we create/update the thu_chi record (id_loai = 1: Nhập hàng).
                // If daThanhToan == 0, we delete any existing thu_chi record for this order.
                if (daThanhToan > 0) {
                    boolean txExists = false;
                    String oldMoTa = null;
                    long oldTienChi = 0;
                    if (isEditMode) {
                        String checkTxSql = "SELECT id, mo_ta, tien_chi FROM thu_chi WHERE id_nhap_hang = ?";
                        try (PreparedStatement psCheck = conn.prepareStatement(checkTxSql)) {
                            psCheck.setInt(1, orderId);
                            try (ResultSet rsCheck = psCheck.executeQuery()) {
                                if (rsCheck.next()) {
                                    txExists = true;
                                    oldMoTa = rsCheck.getString("mo_ta");
                                    oldTienChi = rsCheck.getLong("tien_chi");
                                }
                            }
                        }
                    }
                    
                    if (txExists) {
                        // Update existing
                        String newMoTa = oldMoTa != null ? oldMoTa : "";
                        if (oldTienChi != daThanhToan) {
                            int startIdx = newMoTa.indexOf("(Thanh toán: ");
                            if (startIdx != -1) {
                                int endIdx = newMoTa.indexOf(")", startIdx);
                                if (endIdx != -1) {
                                    String prefix = newMoTa.substring(0, startIdx);
                                    String history = newMoTa.substring(startIdx + "(Thanh toán: ".length(), endIdx);
                                    String newHistory = history.trim();
                                    if (!newHistory.endsWith(vnd.format(daThanhToan) + " ₫")) {
                                        newHistory += " -> " + vnd.format(daThanhToan) + " ₫";
                                    }
                                    newMoTa = prefix + "(Thanh toán: " + newHistory + ")" + (ghiChu.isEmpty() ? "" : " | Ghi chú: " + ghiChu);
                                }
                            } else {
                                newMoTa = "Chi tiền nhập hàng cho đơn nhập NH-" + orderId + " (Thanh toán: " + vnd.format(oldTienChi) + " ₫ -> " + vnd.format(daThanhToan) + " ₫)" + (ghiChu.isEmpty() ? "" : " | Ghi chú: " + ghiChu);
                            }
                        } else {
                            int startIdx = newMoTa.indexOf("(Thanh toán: ");
                            if (startIdx != -1) {
                                int endIdx = newMoTa.indexOf(")", startIdx);
                                if (endIdx != -1) {
                                    String mainPart = newMoTa.substring(0, endIdx + 1);
                                    newMoTa = mainPart + (ghiChu.isEmpty() ? "" : " | Ghi chú: " + ghiChu);
                                }
                            }
                        }

                        String updTxSql = "UPDATE thu_chi SET thoi_gian = ?, tien_chi = ?, id_nhan_vien = ?, mo_ta = ? WHERE id_nhap_hang = ?";
                        try (PreparedStatement psUpdTx = conn.prepareStatement(updTxSql)) {
                            psUpdTx.setTimestamp(1, ts);
                            psUpdTx.setLong(2, daThanhToan);
                            if (idNV != null) {
                                psUpdTx.setInt(3, idNV);
                            } else {
                                psUpdTx.setNull(3, Types.INTEGER);
                            }
                            psUpdTx.setString(4, newMoTa);
                            psUpdTx.setInt(5, orderId);
                            psUpdTx.executeUpdate();
                        }
                    } else {
                        // Insert new
                        String newMoTa = "Chi tiền nhập hàng cho đơn nhập NH-" + orderId + " (Thanh toán: " + vnd.format(daThanhToan) + " ₫)" + (ghiChu.isEmpty() ? "" : " | Ghi chú: " + ghiChu);
                        String insTxSql = "INSERT INTO thu_chi (thoi_gian, id_loai, tien_thu, tien_chi, mo_ta, id_nhan_vien, id_nhap_hang) VALUES (?, 1, NULL, ?, ?, ?, ?)";
                        try (PreparedStatement psInsTx = conn.prepareStatement(insTxSql)) {
                            psInsTx.setTimestamp(1, ts);
                            psInsTx.setLong(2, daThanhToan);
                            psInsTx.setString(3, newMoTa);
                            if (idNV != null) {
                                psInsTx.setInt(4, idNV);
                            } else {
                                psInsTx.setNull(4, Types.INTEGER);
                            }
                            psInsTx.setInt(5, orderId);
                            psInsTx.executeUpdate();
                        }
                    }
                } else {
                    // daThanhToan == 0, delete any existing transaction for this order
                    String delTxSql = "DELETE FROM thu_chi WHERE id_nhap_hang = ?";
                    try (PreparedStatement psDelTx = conn.prepareStatement(delTxSql)) {
                        psDelTx.setInt(1, orderId);
                        psDelTx.executeUpdate();
                    }
                }

                conn.commit();
                saved = true;

                // Ghi nhật ký
                String thaoTac = isEditMode ? ActivityLogger.ACTION_SUA : ActivityLogger.ACTION_THEM;
                String moTaLog = (isEditMode ? "Cập nhật" : "Tạo mới") + " đơn nhập NH-" + orderId
                        + " | NCC: " + tenNCC
                        + " | Tổng: " + tongTien + " đ"
                        + " | TT: " + trangThai;
                ActivityLogger.log(thaoTac, ActivityLogger.TAB_NHAP_HANG, "NH-" + orderId, moTaLog);

                JOptionPane.showMessageDialog(this,
                        isEditMode ? "Cập nhật đơn nhập thành công!" : "Tạo đơn nhập thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                if (onSaveCallback != null)
                    onSaveCallback.run();
                dispose();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════ Product row helpers ════════════════

    /**
     * Thêm dòng sản phẩm vào bảng.
     * Nếu tham số null → thêm dòng trống.
     */
    private void addProductRow(String tenSP, String donVi, Integer soLuong, Long giaNhap, Long thanhTien) {
        String spLabel = tenSP != null ? tenSP : (spMap.isEmpty() ? "" : spMap.keySet().iterator().next());
        String dvLabel = donVi;
        if (dvLabel == null) {
            dvLabel = spUnitMap.get(spLabel);
        }
        if (dvLabel == null) {
            dvLabel = "Cái";
        }
        int qty = soLuong != null ? soLuong : 1;
        long gia = giaNhap != null ? giaNhap : 0;
        long thanh = thanhTien != null ? thanhTien : qty * gia;

        productTableModel.addRow(new Object[] {
                spLabel,
                dvLabel,
                qty,
                vnd.format(gia),
                vnd.format(thanh),
                "🗑"
        });
    }

    private void updateThanhTien(int row) {
        if (row < 0 || row >= productTableModel.getRowCount())
            return;
        int qty = parseIntSafe(productTableModel.getValueAt(row, 2));
        long gia = parseLongVnd(productTableModel.getValueAt(row, 3));
        long thanh = qty * gia;
        productTableModel.setValueAt(vnd.format(thanh), row, 4);
    }

    private void updatePaymentStats() {
        long total = calcTotal();
        long paid = parseLongVnd(tfTienDaThanhToan != null ? tfTienDaThanhToan.getText() : "0");
        long debt = total - paid;
        if (debt < 0)
            debt = 0;

        if (lblTotal != null)
            lblTotal.setText(vnd.format(total) + " ₫");
        if (lblPaid != null)
            lblPaid.setText(vnd.format(paid) + " ₫");
        if (lblDebt != null)
            lblDebt.setText(vnd.format(debt) + " ₫");
    }

    private long calcTotal() {
        long total = 0;
        for (int r = 0; r < productTableModel.getRowCount(); r++) {
            total += parseLongVnd(productTableModel.getValueAt(r, 4));
        }
        return total;
    }

    private long fetchGiaNhap(int spId) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT gia_nhap_hien_tai FROM san_pham WHERE id=?")) {
                ps.setInt(1, spId);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    return rs.getLong(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private boolean checkIdExists(Connection conn, int id) throws SQLException {
        String sql = "SELECT 1 FROM nhap_hang WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ═════════════════════════ UI Helpers ════════════════════════

    private JComboBox<String> createComboBox(String[] items, String placeholder) {
        String[] all = new String[items.length + 1];
        all[0] = placeholder;
        System.arraycopy(items, 0, all, 1, items.length);
        JComboBox<String> cb = new JComboBox<>(all);
        cb.setFont(AppTheme.FONT_BODY_MD);
        cb.setBackground(AppTheme.SURFACE_MED);
        cb.setForeground(AppTheme.ON_SURFACE);
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        return cb;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 36));
        // Focus highlight
        Border normal = tf.getBorder();
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.PRIMARY_CONTAINER, 1),
                new EmptyBorder(6, 10, 6, 10));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(focused);
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(normal);
            }
        });
        return tf;
    }

    private JPanel fieldBlock(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        String text = labelText;
        if (text != null && !text.startsWith("<html>")) {
            text = "<html><nobr>" + text + "</nobr></html>";
        }
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private JLabel sectionLabel(String text) {
        if (text != null && text.length() > 2 && text.charAt(0) > 127) {
            String emoji = text.substring(0, 2);
            String rest = text.substring(2);
            text = "<html><nobr><font face='Segoe UI Emoji'>" + emoji + "</font>" + rest + "</nobr></html>";
        } else if (text != null && !text.startsWith("<html>")) {
            text = "<html><nobr>" + text + "</nobr></html>";
        }
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 11f));
        lbl.setForeground(AppTheme.PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(6, 0, 6, 0));
        return lbl;
    }

    private Component fullWidth(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        int h = c.getPreferredSize().height;
        if (h <= 0)
            h = 80;
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return c;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.OUTLINE_VARIANT);
        sep.setBackground(AppTheme.OUTLINE_VARIANT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JButton iconButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(AppTheme.ON_SURFACE_VAR);
        btn.setBackground(AppTheme.SURFACE_HIGH);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(AppTheme.FONT_BODY_MD);
        t.setForeground(AppTheme.ON_SURFACE);
        t.setBackground(AppTheme.SURFACE_MED);
        t.setGridColor(AppTheme.OUTLINE_VARIANT);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(AppTheme.withAlpha(AppTheme.PRIMARY, 40));
        t.setSelectionForeground(AppTheme.ON_SURFACE);
        t.setFocusable(false);
    }

    private void selectComboItem(JComboBox<String> cb, String value) {
        if (value == null)
            return;
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (value.equals(cb.getItemAt(i))) {
                cb.setSelectedIndex(i);
                return;
            }
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
    }

    private int parseIntSafe(Object val) {
        if (val == null)
            return 0;
        try {
            return Integer.parseInt(val.toString().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLongVnd(Object val) {
        if (val == null)
            return 0;
        try {
            return Long.parseLong(val.toString().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═════════════════════════ Delete Button Renderer/Editor ═════

    /** Renderer cho cột nút Xóa */
    private class DeleteBtnRenderer implements TableCellRenderer {
        private final JButton btn = new JButton("🗑");

        DeleteBtnRenderer() {
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setForeground(AppTheme.ON_SURFACE_VAR);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            btn.setBackground(r % 2 == 0 ? AppTheme.SURFACE_MED : AppTheme.SURFACE_HIGH);
            return btn;
        }
    }

    /** Editor cho cột nút Xóa – thực sự xóa dòng khi click */
    private class DeleteBtnEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn = new JButton("🗑");
        private final JTable table;

        DeleteBtnEditor(JTable table) {
            this.table = table;
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setForeground(AppTheme.ERROR);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                int row = table.getEditingRow();
                fireEditingCanceled();
                if (row >= 0 && row < productTableModel.getRowCount()) {
                    productTableModel.removeRow(row);
                    updatePaymentStats();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            btn.setBackground(r % 2 == 0 ? AppTheme.SURFACE_MED : AppTheme.SURFACE_HIGH);
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "🗑";
        }
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(LayoutManager layout) {
            super(layout);
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

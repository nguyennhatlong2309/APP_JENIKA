package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Dialog Thêm / Sửa Hàng Hóa
 * Thiết kế theo Obsidian Brew dark theme, phù hợp với Stitch mockup.
 */
public class ProductDialog extends JDialog {

    // ===================== Fields =====================
    private JTextField tfTenHH;
    private JComboBox<String> cbDanhMuc, cbDonVi;
    private JFormattedTextField tfGiaNhap, tfGiaBan;
    private JFormattedTextField tfTonKhoThucTe;
    private JFormattedTextField tfCanhBaoTon; // ngưỡng cảnh báo tồn kho
    private JTextArea taMoTa;

    private final boolean isEditMode;
    private final String maHH; // null nếu là thêm mới
    private boolean saved = false;

    // Callback sau khi lưu thành công
    private Runnable onSaveCallback;

    // ===================== Constructor =====================

    /** Khởi tạo dialog Thêm mới */
    public ProductDialog(Frame owner) {
        super(owner, "Thêm Hàng Hóa  ", true);
        this.isEditMode = false;
        this.maHH = null;
        initUI();
    }

    /** Khởi tạo dialog Sửa – nạp dữ liệu theo mã hàng hóa */
    public ProductDialog(Frame owner, String maHH) {
        super(owner, "Sửa Hàng Hóa  ", true);
        this.isEditMode = true;
        this.maHH = maHH;
        initUI();
        loadProductData();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public boolean isSaved() {
        return saved;
    }

    // ===================== Build UI =====================
    private void initUI() {
        setSize(620, 720);
        setMinimumSize(new Dimension(580, 640));
        setLocationRelativeTo(getOwner());
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE_HIGH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildFormScrollPane(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ---- Header ----
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(16, 20, 14, 16)));

        JLabel title = new JLabel(isEditMode
                ? "<html><nobr><font face='Segoe UI Emoji'>✏</font>  Sửa Hàng Hóa  </nobr></html>"
                : "<html><nobr><font face='Segoe UI Emoji'>➕</font>  Thêm Hàng Hóa  </nobr></html>");
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

        header.add(title, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        return header;
    }

    // ---- Form Scroll ----
    private JScrollPane buildFormScrollPane() {
        JPanel form = buildForm();
        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppTheme.SURFACE_HIGH);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ---- Form Content ----
    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE_HIGH);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // ---- Thông tin cơ bản ----
        form.add(sectionLabel("📋  THÔNG TIN SẢN PHẨM  "));
        form.add(vgap(8));

        // Tên HH
        tfTenHH = createTextField();
        form.add(fullWidth(fieldBlock("Tên hàng hóa *", tfTenHH)));
        form.add(vgap(10));

        // Danh mục & Đơn vị
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        cbDanhMuc = createComboBox(loadCategories());
        cbDonVi = createComboBox(loadUnits());
        row2.add(fieldBlock("Danh mục *", cbDanhMuc));
        row2.add(fieldBlock("Đơn vị tính *", cbDonVi));
        form.add(fullWidth(row2));
        form.add(vgap(10));



        // ---- Divider ----
        form.add(divider());
        form.add(vgap(12));

        // ---- Giá cả ----
        form.add(sectionLabel("💰  GIÁ CẢ (VNĐ)  "));
        form.add(vgap(8));

        NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        nf.setGroupingUsed(true);

        tfGiaNhap = createMoneyField(nf);
        tfGiaBan = createMoneyField(nf);

        JPanel priceRow = new JPanel(new GridLayout(1, 2, 12, 0));
        priceRow.setOpaque(false);
        priceRow.add(fieldBlock("Giá nhập *", wrapWithSuffix(tfGiaNhap, "₫")));
        priceRow.add(fieldBlock("Giá bán", wrapWithSuffix(tfGiaBan, "₫")));
        form.add(fullWidth(priceRow));
        form.add(vgap(16));

        // ---- Divider ----
        form.add(divider());
        form.add(vgap(12));

        // ---- Tồn kho ----
        form.add(sectionLabel("📦  TỒN KHO  "));
        form.add(vgap(8));

        NumberFormat nfStock = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tfTonKhoThucTe = createMoneyField(nfStock);

        form.add(fullWidth(fieldBlock("Số lượng tồn kho", tfTonKhoThucTe)));
        form.add(vgap(10));

        // Cảnh báo tồn kho
        NumberFormat nfWarn = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tfCanhBaoTon = createMoneyField(nfWarn);
        ((JFormattedTextField) tfCanhBaoTon).setValue(5); // mặc định 5
        form.add(fullWidth(fieldBlock(
                "Cảnh báo khi tồn ≤ (ngưỡng)",
                wrapWithSuffix(tfCanhBaoTon, "cái"))));
        form.add(vgap(16));

        // ---- Divider ----
        form.add(divider());
        form.add(vgap(12));

        // ---- Mô tả ----
        form.add(sectionLabel("📝  MÔ TẢ  "));
        form.add(vgap(8));

        taMoTa = new JTextArea(4, 20);
        taMoTa.setFont(AppTheme.FONT_BODY_MD);
        taMoTa.setBackground(AppTheme.SURFACE_MED);
        taMoTa.setForeground(AppTheme.ON_SURFACE);
        taMoTa.setCaretColor(AppTheme.ON_SURFACE);
        taMoTa.setLineWrap(true);
        taMoTa.setWrapStyleWord(true);
        taMoTa.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane taScroll = new JScrollPane(taMoTa);
        taScroll.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        taScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        taScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        taScroll.setPreferredSize(new Dimension(0, 90));
        form.add(taScroll);
        form.add(vgap(8));

        return form;
    }

    // ---- Footer ----
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(AppTheme.SURFACE_HIGH);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT));

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(AppTheme.FONT_LABEL);
        cancelBtn.setForeground(AppTheme.ON_SURFACE_VAR);
        cancelBtn.setBackground(AppTheme.SURFACE_HIGH);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 18, 6, 18)));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(isEditMode
                ? "<html><font face='Segoe UI Emoji'>✔️</font>  Cập nhật</html>"
                : "<html><font face='Segoe UI Emoji'>✔️</font>  Lưu sản phẩm</html>");
        saveBtn.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
        saveBtn.setForeground(AppTheme.ON_PRIMARY);
        saveBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
        saveBtn.setBorder(new EmptyBorder(7, 20, 7, 20));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> onSave());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        return footer;
    }

    // ===================== Data =====================

    /** Tải danh sách danh mục từ DB */
    private String[] loadCategories() {
        java.util.List<String> cats = new ArrayList<>();
        cats.add("-- Chọn danh mục --");
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // cfe_di_rom: danh_muc.ten_danh_muc (không có cột thu_tu)
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT ten_danh_muc FROM danh_muc ORDER BY id, ten_danh_muc")) {
                while (rs.next())
                    cats.add(rs.getString("ten_danh_muc"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cats.toArray(new String[0]);
    }

    /** Tải danh sách đơn vị từ DB */
    private String[] loadUnits() {
        java.util.List<String> units = new ArrayList<>();
        units.add("-- Chọn đơn vị --");
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // cfe_di_rom: don_vi_tinh.ten_don_vi
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT ten_don_vi FROM don_vi_tinh ORDER BY ten_don_vi")) {
                while (rs.next())
                    units.add(rs.getString("ten_don_vi"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return units.toArray(new String[0]);
    }

    /** Nạp dữ liệu sản phẩm vào form (chế độ sửa) */
    private void loadProductData() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            // cfe_di_rom: san_pham JOIN danh_muc (ten_danh_muc) JOIN don_vi_tinh
            // (ten_don_vi)
            // maHH trong context này là id (int) của san_pham
            String sql = "SELECT sp.*, d.ten_danh_muc, dv.ten_don_vi FROM san_pham sp " +
                    "LEFT JOIN danh_muc d ON sp.id_danh_muc = d.id " +
                    "LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id " +
                    "WHERE sp.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(maHH));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfTenHH.setText(rs.getString("ten_san_pham"));
                    tfGiaNhap.setValue(rs.getDouble("gia_nhap_hien_tai"));
                    tfGiaBan.setValue(rs.getDouble("gia_ban_hien_tai"));
                    tfTonKhoThucTe.setValue(rs.getDouble("so_luong_ton"));
                    // Nạp ngưỡng cảnh báo
                    tfCanhBaoTon.setValue(rs.getDouble("canh_bao_ton_kho"));

                    // Chọn danh mục
                    String tenDm = rs.getString("ten_danh_muc");
                    if (tenDm != null) {
                        for (int i = 0; i < cbDanhMuc.getItemCount(); i++) {
                            if (tenDm.equals(cbDanhMuc.getItemAt(i))) {
                                cbDanhMuc.setSelectedIndex(i);
                                break;
                            }
                        }
                    }

                    // Chọn đơn vị
                    String tenDv = rs.getString("ten_don_vi");
                    if (tenDv != null) {
                        for (int i = 0; i < cbDonVi.getItemCount(); i++) {
                            if (tenDv.equals(cbDonVi.getItemAt(i))) {
                                cbDonVi.setSelectedIndex(i);
                                break;
                            }
                        }
                    }


                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Validate và lưu vào DB */
    private void onSave() {
        // --- Validate ---
        String ten = tfTenHH.getText().trim();

        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ các trường bắt buộc (*): Tên HH.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedCat = (String) cbDanhMuc.getSelectedItem();
        if (selectedCat == null || selectedCat.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục hàng hóa.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedUnit = (String) cbDonVi.getSelectedItem();
        if (selectedUnit == null || selectedUnit.startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn vị tính.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double giaNhap = parseDouble(tfGiaNhap);
        double giaBan = parseDouble(tfGiaBan);
        double tonTT = parseDouble(tfTonKhoThucTe);

        String moTa = taMoTa.getText().trim();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            // Lấy id danh mục từ bảng danh_muc (cột ten_danh_muc)
            int idDm = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM danh_muc WHERE ten_danh_muc = ?")) {
                ps.setString(1, selectedCat);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    idDm = rs.getInt(1);
            }
            // Lấy id đơn vị từ bảng don_vi_tinh (cột ten_don_vi)
            int idDv = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM don_vi_tinh WHERE ten_don_vi = ?")) {
                ps.setString(1, selectedUnit);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    idDv = rs.getInt(1);
            }

            if (idDm == -1 || idDv == -1) {
                JOptionPane.showMessageDialog(this, "Danh mục hoặc đơn vị không hợp lệ.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isEditMode) {
                // UPDATE san_pham – trang_thai sẽ do trigger tự động cập nhật
                String sql = "UPDATE san_pham SET ten_san_pham=?, id_danh_muc=?, id_don_vi=?,"
                        + " gia_nhap_hien_tai=?, gia_ban_hien_tai=?, so_luong_ton=?, canh_bao_ton_kho=?"
                        + " WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, ten);
                    ps.setInt(2, idDm);
                    ps.setInt(3, idDv);
                    ps.setDouble(4, giaNhap);
                    ps.setDouble(5, giaBan);
                    ps.setDouble(6, tonTT);
                    ps.setInt(7, (int) parseDouble(tfCanhBaoTon));
                    ps.setInt(8, Integer.parseInt(this.maHH));
                    ps.executeUpdate();
                }
            } else {
                // INSERT san_pham (id tự động AUTO_INCREMENT, không truyền ma_hh string)
                // NOTE: trang_thai sẽ do trigger tự động cập nhật (không cần truyền)
                String sql = "INSERT INTO san_pham (ten_san_pham, id_danh_muc, id_don_vi,"
                        + " gia_nhap_hien_tai, gia_ban_hien_tai, so_luong_ton, canh_bao_ton_kho)"
                        + " VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, ten);
                    ps.setInt(2, idDm);
                    ps.setInt(3, idDv);
                    ps.setDouble(4, giaNhap);
                    ps.setDouble(5, giaBan);
                    ps.setDouble(6, tonTT);
                    ps.setInt(7, (int) parseDouble(tfCanhBaoTon));
                    ps.executeUpdate();
                }
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật hàng hóa thành công!" : "Thêm hàng hóa thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            if (onSaveCallback != null)
                onSaveCallback.run();
            dispose();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Dữ liệu đã tồn tại hoặc vi phạm ràng buộc.",
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== UI Helpers =====================

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 34));
        addFocusBorder(tf);
        return tf;
    }

    private JFormattedTextField createMoneyField(NumberFormat fmt) {
        JFormattedTextField tf = new JFormattedTextField(fmt);
        tf.setValue(0);
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 34));
        tf.setHorizontalAlignment(JTextField.RIGHT);
        addFocusBorder(tf);
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppTheme.FONT_BODY_MD);
        cb.setBackground(AppTheme.SURFACE_MED);
        cb.setForeground(AppTheme.ON_SURFACE);
        cb.setPreferredSize(new Dimension(0, 34));
        return cb;
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

    /** Block gồm label + component bên dưới */
    private JPanel fieldBlock(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
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

    /** Bọc JFormattedTextField với suffix label (₫) */
    private JPanel wrapWithSuffix(JFormattedTextField tf, String suffix) {
        JPanel wrap = new JPanel(new BorderLayout(4, 0));
        wrap.setOpaque(false);
        JLabel suf = new JLabel(suffix);
        suf.setFont(AppTheme.FONT_BODY_MD);
        suf.setForeground(AppTheme.ON_SURFACE_VAR);
        suf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(0, 6, 0, 8)));
        wrap.add(tf, BorderLayout.CENTER);
        wrap.add(suf, BorderLayout.EAST);
        return wrap;
    }

    /** Full-width wrapper */
    private Component fullWidth(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height == 0 ? 60 : 60));
        return c;
    }

    /** Divider line */
    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.OUTLINE_VARIANT);
        sep.setBackground(AppTheme.OUTLINE_VARIANT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    /** Vertical gap */
    private Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    /** Focus border (highlight khi focus) */
    private void addFocusBorder(JTextField tf) {
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
    }

    /** Parse double từ JFormattedTextField (an toàn) */
    private double parseDouble(JFormattedTextField tf) {
        try {
            String raw = tf.getText().replaceAll("[^\\d.,]", "").replace(",", ".");
            if (raw.isEmpty())
                return 0;
            return Double.parseDouble(raw.replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.ActivityLogger;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dialog Tạo / Sửa Phiếu Thu Chi
 *
 * Mỗi phiếu có thể có cả khoản Thu và/hoặc khoản Chi trong cùng 1 dòng.
 * - Chọn Danh mục (từ bảng loai_thu_chi — không phân loại Thu/Chi nữa)
 * - Nhập Số tiền Thu (để trống nếu không có)
 * - Nhập Số tiền Chi (để trống nếu không có)
 * - Nhập Mô tả, Nhân viên, Ngày giờ
 * - Lưu vào bảng thu_chi (tien_thu, tien_chi)
 */
public class TransactionDialog extends JDialog {

    // ─────────────────────────── Constants ───────────────────────────
    private static final int DIALOG_W = 520;
    private static final int DIALOG_H = 620;

    // ─────────────────────────── State ───────────────────────────────
    private final boolean isEditMode;
    private final Integer editId;
    private final boolean isReadOnly;
    private boolean saved = false;
    private Runnable onSaveCallback;

    // ─────────────────────────── UI Fields ───────────────────────────
    private JComboBox<String> cbLoai;
    private JButton btnAddLoai;
    private JTextField tfTienThu, tfTienChi, tfDate;
    private JTextArea tfMoTa;
    private JComboBox<String> cbNhanVien;

    // ─────────────────────────── Lookup maps ─────────────────────────
    private final Map<String, Integer> nvMap = new LinkedHashMap<>();   // ten_nhan_vien → id
    private final Map<String, Integer> loaiMap = new LinkedHashMap<>(); // ten_loai → id

    private final NumberFormat vnd = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    // ═════════════════════════ Constructors ══════════════════════════

    /** Thêm mới phiếu */
    public TransactionDialog(Frame owner) {
        super(owner, "Lập phiếu Thu Chi  ", true);
        this.isEditMode = false;
        this.editId = null;
        this.isReadOnly = false;
        init();
    }

    /** Sửa phiếu theo id */
    public TransactionDialog(Frame owner, int id) {
        super(owner, "Sửa Phiếu #TC-" + id + "  ", true);
        this.isEditMode = true;
        this.editId = id;
        this.isReadOnly = false;
        init();
        loadData();
    }

    /** Xem phiếu theo id (Chỉ đọc) */
    public TransactionDialog(Frame owner, int id, boolean isReadOnly) {
        super(owner, isReadOnly ? "Chi Tiết Phiếu #TC-" + id + "  " : "Sửa Phiếu #TC-" + id + "  ", true);
        this.isEditMode = !isReadOnly;
        this.editId = id;
        this.isReadOnly = isReadOnly;
        init();
        loadData();
    }

    public void setOnSaveCallback(Runnable cb) {
        this.onSaveCallback = cb;
    }

    public boolean isSaved() {
        return saved;
    }

    // ═════════════════════════ Init ══════════════════════════════════

    private void init() {
        setSize(DIALOG_W, DIALOG_H);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(getOwner());
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE_HIGH);

        loadLookups();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBodyScrollPane(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JScrollPane buildBodyScrollPane() {
        JPanel body = buildBody();
        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(AppTheme.SURFACE_HIGH);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ─────────────────────────── Header ──────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(AppTheme.SURFACE_HIGH);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 20, 12, 16)));

        String titleText = isReadOnly
                ? "<html><nobr><font face='Segoe UI Emoji'>👁</font>  Chi Tiết Phiếu #TC-" + editId + "  </nobr></html>"
                : (isEditMode
                    ? "<html><nobr><font face='Segoe UI Emoji'>✏</font>  Sửa Phiếu #TC-" + editId + "  </nobr></html>"
                    : "<html><nobr><font face='Segoe UI Emoji'>📋</font>  Lập Phiếu Thu Chi  </nobr></html>");

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

    // ─────────────────────────── Body ────────────────────────────────

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.SURFACE_HIGH);
        body.setBorder(new EmptyBorder(20, 24, 12, 24));

        // ── 1. Danh mục ──
        body.add(sectionLabel("DANH MỤC"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(fullWidth(buildLoaiRow()));
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── 2. Số tiền Thu ──
        body.add(sectionLabel("SỐ TIỀN THU (₫)  —  để trống nếu không có khoản thu"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        tfTienThu = createTextField("");
        body.add(fullWidth(tfTienThu));
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── 3. Số tiền Chi ──
        body.add(sectionLabel("SỐ TIỀN CHI (₫)  —  để trống nếu không có khoản chi"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        tfTienChi = createTextField("");
        body.add(fullWidth(tfTienChi));
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── 4. Ngày giờ ──
        body.add(sectionLabel("NGÀY GIỜ"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        tfDate = createTextField(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
        body.add(fullWidth(tfDate));
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── 5. Nhân viên ──
        body.add(sectionLabel("NHÂN VIÊN"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        cbNhanVien = createComboBox(nvMap.keySet().toArray(new String[0]), "-- Chọn nhân viên --");
        body.add(fullWidth(cbNhanVien));
        body.add(Box.createRigidArea(new Dimension(0, 16)));

        // ── 6. Mô tả ──
        body.add(sectionLabel("MÔ TẢ"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        
        tfMoTa = new JTextArea(3, 20);
        tfMoTa.setLineWrap(true);
        tfMoTa.setWrapStyleWord(true);
        tfMoTa.setFont(AppTheme.FONT_BODY_MD);
        tfMoTa.setBackground(AppTheme.SURFACE_MED);
        tfMoTa.setForeground(AppTheme.ON_SURFACE);
        tfMoTa.setCaretColor(AppTheme.ON_SURFACE);
        tfMoTa.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scrollMoTa = new JScrollPane(tfMoTa);
        scrollMoTa.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        scrollMoTa.setPreferredSize(new Dimension(0, 80));
        body.add(fullWidth(scrollMoTa));

        if (isReadOnly) {
            cbLoai.setEnabled(false);
            btnAddLoai.setEnabled(false);
            tfTienThu.setEditable(false);
            tfTienThu.setEnabled(false);
            tfTienThu.setBackground(AppTheme.SURFACE_LOW);
            tfTienChi.setEditable(false);
            tfTienChi.setEnabled(false);
            tfTienChi.setBackground(AppTheme.SURFACE_LOW);
            tfDate.setEditable(false);
            tfDate.setEnabled(false);
            tfDate.setBackground(AppTheme.SURFACE_LOW);
            cbNhanVien.setEnabled(false);
            tfMoTa.setEditable(false);
            tfMoTa.setEnabled(false);
            tfMoTa.setBackground(AppTheme.SURFACE_LOW);
            scrollMoTa.getViewport().setBackground(AppTheme.SURFACE_LOW);
        }

        return body;
    }

    private JPanel buildLoaiRow() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        cbLoai = createComboBox(new String[0], "-- Chọn danh mục --");
        reloadLoaiCombo(); // load lần đầu

        btnAddLoai = new JButton("<html><font face='Segoe UI Emoji'>➕</font></html>");
        btnAddLoai.setFont(AppTheme.FONT_LABEL);
        btnAddLoai.setForeground(AppTheme.PRIMARY);
        btnAddLoai.setBackground(AppTheme.SURFACE_MED);
        btnAddLoai.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        btnAddLoai.setFocusPainted(false);
        btnAddLoai.setPreferredSize(new Dimension(38, 36));
        btnAddLoai.setToolTipText("Thêm danh mục mới");
        btnAddLoai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddLoai.addActionListener(e -> openAddLoaiDialog());

        p.add(cbLoai, BorderLayout.CENTER);
        p.add(btnAddLoai, BorderLayout.EAST);
        return p;
    }

    // ─────────────────────────── Footer ──────────────────────────────

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.SURFACE_HIGH);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(12, 20, 12, 20)));

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

        btnPanel.add(cancelBtn);

        if (!isReadOnly) {
            JButton saveBtn = new JButton(isEditMode
                    ? "<html><font face='Segoe UI Emoji'>✔️</font>  Cập nhật</html>"
                    : "<html><font face='Segoe UI Emoji'>✔️</font>  Lưu phiếu</html>");
            saveBtn.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 12f));
            saveBtn.setForeground(AppTheme.ON_PRIMARY);
            saveBtn.setBackground(AppTheme.PRIMARY_CONTAINER);
            saveBtn.setBorder(new EmptyBorder(8, 22, 8, 22));
            saveBtn.setBorderPainted(false);
            saveBtn.setFocusPainted(false);
            saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            saveBtn.addActionListener(e -> onSave());
            btnPanel.add(saveBtn);
        }
        footer.add(btnPanel, BorderLayout.EAST);
        return footer;
    }

    // ═════════════════════════ Data Loading ══════════════════════════

    private void loadLookups() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id, ten_nhan_vien FROM nhan_vien ORDER BY ten_nhan_vien")) {
                while (rs.next())
                    nvMap.put(rs.getString("ten_nhan_vien"), rs.getInt("id"));
            }
            loaiMap.clear();
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id, ten FROM loai_thu_chi ORDER BY ten")) {
                while (rs.next()) {
                    loaiMap.put(rs.getString("ten"), rs.getInt("id"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Load lại combo danh mục */
    private void reloadLoaiCombo() {
        Object prev = cbLoai != null ? cbLoai.getSelectedItem() : null;

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("-- Chọn danh mục --");
        for (String key : loaiMap.keySet()) {
            model.addElement(key);
        }
        if (cbLoai != null) {
            cbLoai.setModel(model);
            if (prev != null)
                cbLoai.setSelectedItem(prev);
        }
    }

    private void loadData() {
        if (editId == null)
            return;
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT tc.*, ltc.ten AS ten_loai, nv.ten_nhan_vien"
                    + " FROM thu_chi tc"
                    + " LEFT JOIN loai_thu_chi ltc ON tc.id_loai = ltc.id"
                    + " LEFT JOIN nhan_vien nv ON tc.id_nhan_vien = nv.id"
                    + " WHERE tc.id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, editId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String tenLoai = rs.getString("ten_loai");
                    if (tenLoai != null)
                        cbLoai.setSelectedItem(tenLoai);

                    long tienThu = rs.getLong("tien_thu");
                    if (!rs.wasNull())
                        tfTienThu.setText(String.valueOf(tienThu));

                    long tienChi = rs.getLong("tien_chi");
                    if (!rs.wasNull())
                        tfTienChi.setText(String.valueOf(tienChi));

                    Timestamp ts = rs.getTimestamp("thoi_gian");
                    if (ts != null)
                        tfDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts));

                    String tenNV = rs.getString("ten_nhan_vien");
                    if (tenNV != null)
                        cbNhanVien.setSelectedItem(tenNV);

                    String moTa = rs.getString("mo_ta");
                    if (moTa != null)
                        tfMoTa.setText(moTa);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ═════════════════════════ Save ══════════════════════════════════

    private void onSave() {
        // Validate danh mục
        String tenLoai = (String) cbLoai.getSelectedItem();
        if (tenLoai == null || tenLoai.startsWith("--")) {
            warn("Vui lòng chọn danh mục.");
            return;
        }

        // Parse tien_thu (nullable)
        Long tienThu = null;
        String rawThu = tfTienThu.getText().replaceAll("[^0-9]", "");
        if (!rawThu.isEmpty()) {
            try {
                long val = Long.parseLong(rawThu);
                if (val > 0) tienThu = val;
            } catch (NumberFormatException ex) {
                warn("Số tiền Thu không hợp lệ.");
                return;
            }
        }

        // Parse tien_chi (nullable)
        Long tienChi = null;
        String rawChi = tfTienChi.getText().replaceAll("[^0-9]", "");
        if (!rawChi.isEmpty()) {
            try {
                long val = Long.parseLong(rawChi);
                if (val > 0) tienChi = val;
            } catch (NumberFormatException ex) {
                warn("Số tiền Chi không hợp lệ.");
                return;
            }
        }

        if (tienThu == null && tienChi == null) {
            warn("Vui lòng nhập ít nhất một khoản Thu hoặc Chi (lớn hơn 0).");
            return;
        }

        // Lookup id_loai
        Integer idLoai = loaiMap.get(tenLoai);

        // Thời gian
        Timestamp ts;
        try {
            ts = new Timestamp(new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(tfDate.getText().trim()).getTime());
        } catch (Exception ex) {
            ts = new Timestamp(System.currentTimeMillis());
        }

        // Nhân viên
        String tenNV = (String) cbNhanVien.getSelectedItem();
        Integer idNV = (tenNV != null && !tenNV.startsWith("--")) ? nvMap.get(tenNV) : null;

        String moTa = tfMoTa.getText().trim();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (isEditMode) {
                String sql = "UPDATE thu_chi SET thoi_gian=?, id_loai=?, tien_thu=?, tien_chi=?, mo_ta=?, id_nhan_vien=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, ts);
                    if (idLoai != null) ps.setInt(2, idLoai); else ps.setNull(2, Types.INTEGER);
                    if (tienThu != null) ps.setLong(3, tienThu); else ps.setNull(3, Types.DECIMAL);
                    if (tienChi != null) ps.setLong(4, tienChi); else ps.setNull(4, Types.DECIMAL);
                    ps.setString(5, moTa.isEmpty() ? null : moTa);
                    if (idNV != null) ps.setInt(6, idNV); else ps.setNull(6, Types.INTEGER);
                    ps.setInt(7, editId);
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO thu_chi (thoi_gian, id_loai, tien_thu, tien_chi, mo_ta, id_nhan_vien) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(1, ts);
                    if (idLoai != null) ps.setInt(2, idLoai); else ps.setNull(2, Types.INTEGER);
                    if (tienThu != null) ps.setLong(3, tienThu); else ps.setNull(3, Types.DECIMAL);
                    if (tienChi != null) ps.setLong(4, tienChi); else ps.setNull(4, Types.DECIMAL);
                    ps.setString(5, moTa.isEmpty() ? null : moTa);
                    if (idNV != null) ps.setInt(6, idNV); else ps.setNull(6, Types.INTEGER);
                    ps.executeUpdate();
                }
            }
            saved = true;

            // Ghi nhật ký
            String thaoTac = isEditMode ? ActivityLogger.ACTION_SUA : ActivityLogger.ACTION_THEM;
            String maBanGhi = isEditMode ? "TC-" + editId : null;
            StringBuilder moTaLog = new StringBuilder(isEditMode ? "Cập nhật" : "Tạo mới");
            moTaLog.append(" phiếu thu chi | DM: ").append(tenLoai);
            if (tienThu != null) moTaLog.append(" | Thu: ").append(tienThu).append(" đ");
            if (tienChi != null) moTaLog.append(" | Chi: ").append(tienChi).append(" đ");
            if (!moTa.isEmpty()) moTaLog.append(" | Mô tả: ").append(moTa);
            ActivityLogger.log(thaoTac, ActivityLogger.TAB_THU_CHI, maBanGhi, moTaLog.toString());

            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật phiếu thành công!" : "Lập phiếu Thu Chi thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (onSaveCallback != null)
                onSaveCallback.run();
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ═════════════════════════ Add Loai Dialog ════════════════════════

    /** Mini popup để thêm danh mục mới */
    private void openAddLoaiDialog() {
        JDialog mini = new JDialog(this, "Thêm danh mục mới  ", true);
        mini.setSize(360, 180);
        mini.setLocationRelativeTo(this);
        mini.getContentPane().setBackground(AppTheme.SURFACE_HIGH);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(AppTheme.SURFACE_HIGH);
        p.setBorder(new EmptyBorder(18, 20, 14, 20));

        JLabel lbl = new JLabel("Tên danh mục mới:");
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);

        JTextField tfTen = new JTextField();
        tfTen.setFont(AppTheme.FONT_BODY_MD);
        tfTen.setBackground(AppTheme.SURFACE_MED);
        tfTen.setForeground(AppTheme.ON_SURFACE);
        tfTen.setCaretColor(AppTheme.ON_SURFACE);
        tfTen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10)));
        tfTen.setPreferredSize(new Dimension(0, 36));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton ok = new JButton("Thêm");
        ok.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD));
        ok.setForeground(AppTheme.ON_PRIMARY);
        ok.setBackground(AppTheme.PRIMARY_CONTAINER);
        ok.setBorderPainted(false);
        ok.setFocusPainted(false);

        JButton cancel = new JButton("Hủy");
        cancel.setFont(AppTheme.FONT_LABEL);
        cancel.setForeground(AppTheme.ON_SURFACE_VAR);
        cancel.setBackground(AppTheme.SURFACE_HIGH);
        cancel.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> mini.dispose());

        ok.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(mini, "Vui lòng nhập tên danh mục.", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Connection conn = DatabaseManager.getInstance().getConnection();
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO loai_thu_chi (ten) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, ten);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    keys.next();
                    newId = keys.getInt(1);
                }
                loaiMap.put(ten, newId);
                reloadLoaiCombo();
                cbLoai.setSelectedItem(ten);
                mini.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(mini, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancel);
        btnRow.add(ok);

        p.add(lbl, BorderLayout.NORTH);
        p.add(tfTen, BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);

        mini.setContentPane(p);
        mini.setVisible(true);
    }

    // ═════════════════════════ UI Helpers ════════════════════════════

    private JComboBox<String> createComboBox(String[] items, String placeholder) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement(placeholder);
        for (String item : items)
            model.addElement(item);
        JComboBox<String> cb = new JComboBox<>(model);
        cb.setFont(AppTheme.FONT_BODY_MD);
        cb.setBackground(AppTheme.SURFACE_MED);
        cb.setForeground(AppTheme.ON_SURFACE);
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        return cb;
    }

    private JTextField createTextField(String defaultVal) {
        JTextField tf = new JTextField(defaultVal);
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10));
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.PRIMARY_CONTAINER, 1),
                new EmptyBorder(6, 10, 6, 10));
        tf.setBorder(normal);
        tf.setPreferredSize(new Dimension(0, 36));
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

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 10f));
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(6, 0, 6, 0));
        return lbl;
    }

    private Component fullWidth(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(c.getPreferredSize().height, 36)));
        return c;
    }

    private JButton iconButton(String text) {
        JButton b = new JButton(text);
        b.setFont(AppTheme.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        b.setForeground(AppTheme.ON_SURFACE_VAR);
        b.setBackground(AppTheme.SURFACE_HIGH);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }
}

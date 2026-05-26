package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Dialog Thêm / Sửa Đối tác (dùng bảng doi_tac)
 * Fields: Tên *, Số điện thoại (optional), Địa chỉ (optional)
 */
public class PartnerDialog extends JDialog {

    private JTextField tfTen, tfSdt, tfDiaChi;

    private final boolean isEditMode;
    private final Integer partnerId;
    private boolean saved = false;
    private Integer generatedId = null;
    private Runnable onSaveCallback;

    /** Thêm mới */
    public PartnerDialog(Frame owner) {
        super(owner, "Thêm Đối Tác  ", true);
        this.isEditMode = false;
        this.partnerId  = null;
        initUI();
    }

    /**
     * Thêm mới (backward-compat: type param không còn dùng đến).
     */
    public PartnerDialog(Frame owner, String ignoredType) {
        super(owner, "Thêm Đối Tác  ", true);
        this.isEditMode = false;
        this.partnerId  = null;
        initUI();
    }

    /** Sửa */
    public PartnerDialog(Frame owner, int partnerId, String ignoredType) {
        super(owner, "Sửa Đối Tác  ", true);
        this.isEditMode = true;
        this.partnerId  = partnerId;
        initUI();
        loadData();
    }

    public void setOnSaveCallback(Runnable cb) { this.onSaveCallback = cb; }
    public boolean isSaved() { return saved; }
    public Integer getGeneratedId() { return generatedId; }

    private void initUI() {
        setSize(460, 440);
        setMinimumSize(new Dimension(460, 400));
        setLocationRelativeTo(getOwner());
        setResizable(true);
        getContentPane().setBackground(AppTheme.SURFACE_HIGH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE_HIGH);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.OUTLINE_VARIANT),
                new EmptyBorder(14, 20, 12, 14)));

        JLabel title = new JLabel(isEditMode
                ? "<html><nobr><font face='Segoe UI Emoji'>✏</font>  Sửa Đối Tác  </nobr></html>"
                : "<html><nobr><font face='Segoe UI Emoji'>➕</font>  Thêm Đối Tác  </nobr></html>");
        title.setFont(AppTheme.FONT_TITLE_SM.deriveFont(Font.BOLD, 16f));
        title.setForeground(AppTheme.ON_SURFACE);
        title.setBorder(new EmptyBorder(6, 0, 6, 0));

        header.add(title, BorderLayout.WEST);
        header.add(makeCloseButton(), BorderLayout.EAST);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel();
        form.setBackground(AppTheme.SURFACE_HIGH);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        tfTen = createTextField("Nhập tên đối tác...");
        form.add(fieldBlock("Tên đối tác *", tfTen));
        form.add(vgap(12));

        tfSdt = createTextField("Số điện thoại (tùy chọn)");
        form.add(fieldBlock("Số điện thoại", tfSdt));
        form.add(vgap(12));

        tfDiaChi = createTextField("Địa chỉ (tùy chọn)");
        form.add(fieldBlock("Địa chỉ", tfDiaChi));

        return form;
    }

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
                : "<html><font face='Segoe UI Emoji'>✔️</font>  Lưu</html>");
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

    private void loadData() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ten, sdt, dia_chi FROM doi_tac WHERE id = ?")) {
                ps.setInt(1, partnerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfTen.setText(rs.getString("ten"));
                    String sdt = rs.getString("sdt");
                    if (sdt != null) tfSdt.setText(sdt);
                    String diaChi = rs.getString("dia_chi");
                    if (diaChi != null) tfDiaChi.setText(diaChi);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSave() {
        String ten = tfTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đối tác.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            tfTen.requestFocus();
            return;
        }

        String sdt    = tfSdt.getText().trim();
        String diaChi = tfDiaChi.getText().trim();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (isEditMode) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE doi_tac SET ten=?, sdt=?, dia_chi=? WHERE id=?")) {
                    ps.setString(1, ten);
                    ps.setString(2, sdt.isEmpty()    ? null : sdt);
                    ps.setString(3, diaChi.isEmpty() ? null : diaChi);
                    ps.setInt(4, partnerId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO doi_tac (ten, sdt, dia_chi) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, ten);
                    ps.setString(2, sdt.isEmpty()    ? null : sdt);
                    ps.setString(3, diaChi.isEmpty() ? null : diaChi);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) generatedId = rs.getInt(1);
                    }
                }
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật đối tác thành công!" : "Thêm đối tác thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- UI Helpers ----

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.FONT_BODY_MD);
        tf.setBackground(AppTheme.SURFACE_MED);
        tf.setForeground(AppTheme.ON_SURFACE);
        tf.setCaretColor(AppTheme.ON_SURFACE);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(6, 10, 6, 10));
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.PRIMARY_CONTAINER, 1),
                new EmptyBorder(6, 10, 6, 10));
        tf.setBorder(normal);
        tf.setPreferredSize(new Dimension(0, 34));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.setBorder(focused); }
            @Override public void focusLost(FocusEvent e)   { tf.setBorder(normal); }
        });
        return tf;
    }

    private JPanel fieldBlock(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
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

    private Component vgap(int h) { return Box.createRigidArea(new Dimension(0, h)); }

    private JButton makeCloseButton() {
        JButton btn = new JButton("X");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(AppTheme.ON_SURFACE_VAR);
        btn.setBackground(AppTheme.SURFACE_HIGH);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dispose());
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(AppTheme.ERROR); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(AppTheme.ON_SURFACE_VAR); }
        });
        return btn;
    }
}

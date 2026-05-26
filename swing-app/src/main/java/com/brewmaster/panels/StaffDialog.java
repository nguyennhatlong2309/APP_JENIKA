package com.brewmaster.panels;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Dialog Thêm / Sửa Nhân viên
 * Fields: Tên *, Vai trò *, Số điện thoại (optional), Email (optional)
 */
public class StaffDialog extends JDialog {

    private JTextField tfTen, tfSdt, tfEmail;
    private JComboBox<String> cbVaiTro;

    private final boolean isEditMode;
    private final Integer staffId;
    private boolean saved = false;
    private Runnable onSaveCallback;

    /** Thêm mới */
    public StaffDialog(Frame owner) {
        super(owner, "Thêm Nhân Viên  ", true);
        this.isEditMode = false;
        this.staffId = null;
        initUI();
    }

    /** Sửa */
    public StaffDialog(Frame owner, int staffId) {
        super(owner, "Sửa Nhân Viên  ", true);
        this.isEditMode = true;
        this.staffId = staffId;
        initUI();
        loadData();
    }

    public void setOnSaveCallback(Runnable cb) { this.onSaveCallback = cb; }
    public boolean isSaved() { return saved; }

    private void initUI() {
        setSize(460, 480);
        setMinimumSize(new Dimension(460, 480));
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
                ? "<html><nobr><font face='Segoe UI Emoji'>✏</font>  Sửa Nhân Viên  </nobr></html>"
                : "<html><nobr><font face='Segoe UI Emoji'>➕</font>  Thêm Nhân Viên  </nobr></html>");
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

        tfTen = createTextField("Nhập tên nhân viên...");
        form.add(fieldBlock("Tên nhân viên *", tfTen));
        form.add(vgap(12));

        cbVaiTro = createComboBox(new String[]{
            "Barista", "Thu ngân", "Phục vụ", "Quản lý", "Bảo vệ", "Kế toán", "Khác"
        });
        form.add(fieldBlock("Vai trò *", cbVaiTro));
        form.add(vgap(12));

        tfSdt = createTextField("Số điện thoại (tùy chọn)");
        form.add(fieldBlock("Số điện thoại", tfSdt));
        form.add(vgap(12));

        tfEmail = createTextField("Email (tùy chọn)");
        form.add(fieldBlock("Email", tfEmail));

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
                    "SELECT ten_nhan_vien, vai_tro, sdt FROM nhan_vien WHERE id = ?")) {
                ps.setInt(1, staffId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfTen.setText(rs.getString("ten_nhan_vien"));
                    String sdt = rs.getString("sdt");
                    if (sdt != null) tfSdt.setText(sdt);

                    String vaiTro = rs.getString("vai_tro");
                    if (vaiTro != null) {
                        boolean found = false;
                        for (int i = 0; i < cbVaiTro.getItemCount(); i++) {
                            if (vaiTro.equals(cbVaiTro.getItemAt(i))) {
                                cbVaiTro.setSelectedIndex(i);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            cbVaiTro.addItem(vaiTro);
                            cbVaiTro.setSelectedItem(vaiTro);
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

    private void onSave() {
        String ten = tfTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhân viên.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            tfTen.requestFocus();
            return;
        }

        String vaiTro = (String) cbVaiTro.getSelectedItem();
        String sdt = tfSdt.getText().trim();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            if (isEditMode) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE nhan_vien SET ten_nhan_vien=?, vai_tro=?, sdt=? WHERE id=?")) {
                    ps.setString(1, ten);
                    ps.setString(2, vaiTro);
                    ps.setString(3, sdt.isEmpty() ? null : sdt);
                    ps.setInt(4, staffId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO nhan_vien (ten_nhan_vien, vai_tro, sdt) VALUES (?, ?, ?)")) {
                    ps.setString(1, ten);
                    ps.setString(2, vaiTro);
                    ps.setString(3, sdt.isEmpty() ? null : sdt);
                    ps.executeUpdate();
                }
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật nhân viên thành công!" : "Thêm nhân viên thành công!",
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

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppTheme.FONT_BODY_MD);
        cb.setBackground(AppTheme.SURFACE_MED);
        cb.setForeground(AppTheme.ON_SURFACE);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cb.setPreferredSize(new Dimension(0, 34));
        return cb;
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

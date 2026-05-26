package com.brewmaster;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;

/**
 * Dialog cấu hình kết nối Database và màn hình khởi động
 */
public class ConnectionDialog extends JDialog {

    private JTextField txtHost, txtPort, txtDatabase, txtUser;
    private JPasswordField txtPassword;
    private JLabel lblStatus;
    private boolean connected = false;

    public ConnectionDialog(Frame parent) {
        super(parent, "Kết nối Cơ sở dữ liệu  ", true);
        initUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(AppTheme.SURFACE_LOW);

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.SURFACE_HIGH);
        header.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel logoLabel = new JLabel("☕ BrewMaster Pro");
        logoLabel.setFont(AppTheme.FONT_TITLE_LG);
        logoLabel.setForeground(AppTheme.PRIMARY);

        JLabel subLabel = new JLabel("Quản lý Quán Cà Phê - Kết nối Database");
        subLabel.setFont(AppTheme.FONT_BODY_SM);
        subLabel.setForeground(AppTheme.ON_SURFACE_VAR);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(logoLabel);
        headerText.add(subLabel);
        header.add(headerText, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);

        // === FORM ===
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AppTheme.SURFACE_LOW);
        form.setBorder(new EmptyBorder(24, 32, 8, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 8);

        txtHost     = createField("localhost");
        txtPort     = createField("3306");
        txtDatabase = createField("brewmaster");
        txtUser     = createField("root");
        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);

        int row = 0;
        addFormRow(form, gbc, row++, "Host:", txtHost);
        addFormRow(form, gbc, row++, "Port:", txtPort);
        addFormRow(form, gbc, row++, "Database:", txtDatabase);
        addFormRow(form, gbc, row++, "Tài khoản:", txtUser);
        addFormRow(form, gbc, row++, "Mật khẩu:", txtPassword);

        mainPanel.add(form, BorderLayout.CENTER);

        // === FOOTER ===
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(AppTheme.SURFACE_LOW);
        footer.setBorder(new EmptyBorder(8, 32, 24, 32));

        lblStatus = new JLabel("Nhập thông tin kết nối và nhấn Kết nối");
        lblStatus.setFont(AppTheme.FONT_BODY_SM);
        lblStatus.setForeground(AppTheme.ON_SURFACE_VAR);

        JButton btnTest = createBtn("Kiểm tra", false);
        JButton btnConnect = createBtn("Kết nối →", true);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnTest);
        btnPanel.add(btnConnect);

        footer.add(lblStatus, BorderLayout.CENTER);
        footer.add(btnPanel, BorderLayout.EAST);

        mainPanel.add(footer, BorderLayout.SOUTH);

        // === ACTIONS ===
        btnTest.addActionListener(e -> testConnection());
        btnConnect.addActionListener(e -> connect());

        // Enter = kết nối
        getRootPane().setDefaultButton(btnConnect);

        add(mainPanel);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE_VAR);
        form.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(field, gbc);
    }

    private JTextField createField(String def) {
        JTextField f = new JTextField(def);
        f.setFont(AppTheme.FONT_BODY_MD);
        f.setPreferredSize(new Dimension(200, 36));
        return f;
    }

    private void stylePasswordField(JPasswordField f) {
        f.setFont(AppTheme.FONT_BODY_MD);
        f.setPreferredSize(new Dimension(200, 36));
    }

    private JButton createBtn(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_LABEL);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 36));
        if (primary) {
            btn.setBackground(AppTheme.PRIMARY_CONTAINER);
            btn.setForeground(AppTheme.ON_PRIMARY);
        }
        return btn;
    }

    private void testConnection() {
        applyConfig();
        lblStatus.setForeground(AppTheme.ON_SURFACE_VAR);
        lblStatus.setText("Đang kiểm tra...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Connection c = DatabaseManager.getInstance().getConnection();
                    return c != null && !c.isClosed();
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        lblStatus.setForeground(AppTheme.TERTIARY);
                        lblStatus.setText("✓ Kết nối thành công!");
                    } else {
                        lblStatus.setForeground(AppTheme.ERROR);
                        lblStatus.setText("✗ Không thể kết nối!");
                    }
                } catch (Exception e) {
                    lblStatus.setForeground(AppTheme.ERROR);
                    lblStatus.setText("✗ Lỗi: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void connect() {
        applyConfig();
        lblStatus.setForeground(AppTheme.ON_SURFACE_VAR);
        lblStatus.setText("Đang kết nối...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    Connection c = DatabaseManager.getInstance().getConnection();
                    return c != null && !c.isClosed();
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        connected = true;
                        dispose();
                    } else {
                        lblStatus.setForeground(AppTheme.ERROR);
                        lblStatus.setText("✗ Kết nối thất bại! Kiểm tra lại thông tin.");
                    }
                } catch (Exception e) {
                    lblStatus.setForeground(AppTheme.ERROR);
                    lblStatus.setText("✗ Lỗi: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void applyConfig() {
        DatabaseManager.configure(
            txtHost.getText().trim(),
            Integer.parseInt(txtPort.getText().trim()),
            txtDatabase.getText().trim(),
            txtUser.getText().trim(),
            new String(txtPassword.getPassword())
        );
    }

    public boolean isConnected() {
        return connected;
    }
}

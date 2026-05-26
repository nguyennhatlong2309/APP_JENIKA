package com.brewmaster.panels;

import com.brewmaster.theme.AppTheme;
import com.brewmaster.util.StoreConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Màn hình cấu hình thông tin cửa hàng phục vụ xuất hóa đơn/phiếu nhập.
 */
public class SettingsPanel extends JPanel {

    private JTextField txtShopName;
    private JTextField txtShopNamePnh;
    private JTextField txtShopAddr;
    private JTextField txtShopTel;
    private JTextField txtShopBank;
    private JTextArea txtShopNotes;
    private JTextArea txtShopPolicy;
    private JTextArea txtShopWarranty;
    private JTextField txtShopWarrantyLimit;

    private JLabel lblStatus;

    public SettingsPanel() {
        setBackground(AppTheme.BACKGROUND);
        setLayout(new BorderLayout());
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("Cấu hình Cửa hàng");
        title.setFont(AppTheme.FONT_TITLE_LG);
        title.setForeground(AppTheme.ON_SURFACE);
        JLabel sub = new JLabel("Quản lý thông tin quán hiển thị mặc định trên hóa đơn và phiếu nhập");
        sub.setFont(AppTheme.FONT_BODY_SM);
        sub.setForeground(AppTheme.ON_SURFACE_VAR);
        titles.add(title);
        titles.add(sub);
        header.add(titles, BorderLayout.WEST);

        // ---- Body: Form Card ----
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBackground(AppTheme.BACKGROUND);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.SURFACE_LOW);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                new EmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Form fields
        int row = 0;

        // Shop Name (Invoice)
        addFormLabel(card, "Tên cửa hàng (Hóa đơn - In hoa):", row, gbc);
        txtShopName = createTextField();
        addFormField(card, txtShopName, row++, gbc);

        // Shop Name (Purchase Order)
        addFormLabel(card, "Tên cửa hàng (Phiếu nhập):", row, gbc);
        txtShopNamePnh = createTextField();
        addFormField(card, txtShopNamePnh, row++, gbc);

        // Shop Address
        addFormLabel(card, "Địa chỉ cửa hàng:", row, gbc);
        txtShopAddr = createTextField();
        addFormField(card, txtShopAddr, row++, gbc);

        // Shop Telephone
        addFormLabel(card, "Điện thoại liên hệ:", row, gbc);
        txtShopTel = createTextField();
        addFormField(card, txtShopTel, row++, gbc);

        // Shop Bank
        addFormLabel(card, "Tài khoản ngân hàng / Thanh toán:", row, gbc);
        txtShopBank = createTextField();
        addFormField(card, txtShopBank, row++, gbc);

        // Shop Notes
        addFormLabel(card, "Lưu ý khách hàng:", row, gbc);
        txtShopNotes = new JTextArea();
        addFormField(card, wrapTextArea(txtShopNotes, 4), row++, gbc);

        // Shop Policy
        addFormLabel(card, "Quy định đổi và hoàn trả hàng:", row, gbc);
        txtShopPolicy = new JTextArea();
        addFormField(card, wrapTextArea(txtShopPolicy, 3), row++, gbc);

        // Shop Warranty
        addFormLabel(card, "Thời gian bảo hành theo từng SP:", row, gbc);
        txtShopWarranty = new JTextArea();
        addFormField(card, wrapTextArea(txtShopWarranty, 4), row++, gbc);

        // Shop Warranty Limit
        addFormLabel(card, "Dòng thời gian bảo hành:", row, gbc);
        txtShopWarrantyLimit = createTextField();
        addFormField(card, txtShopWarrantyLimit, row++, gbc);

        // Divider
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(AppTheme.OUTLINE_VARIANT);
        card.add(sep, gbc);

        // Status Label for Notifications
        gbc.gridy = row++;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(AppTheme.FONT_BODY_MD);
        lblStatus.setForeground(AppTheme.STATUS_PAID_FG);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblStatus, gbc);

        // Button row
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnRow.setOpaque(false);

        JButton btnReset = new JButton("<html>Đặt lại mặc định</html>");
        btnReset.setFont(AppTheme.FONT_LABEL);
        btnReset.setForeground(AppTheme.ON_SURFACE);
        btnReset.setBackground(AppTheme.SURFACE_VARIANT);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(140, 36));
        btnReset.addActionListener(e -> resetToDefault());

        JButton btnSave = new JButton("<html><font face='Segoe UI Emoji'>💾</font>  Lưu cấu hình</html>");
        btnSave.setFont(AppTheme.FONT_LABEL);
        btnSave.setForeground(AppTheme.ON_PRIMARY);
        btnSave.setBackground(AppTheme.PRIMARY_CONTAINER);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(150, 36));
        btnSave.addActionListener(e -> saveConfig());

        btnRow.add(btnReset);
        btnRow.add(btnSave);
        card.add(btnRow, gbc);

        // Align card to top-left of body
        GridBagConstraints bodyGbc = new GridBagConstraints();
        bodyGbc.gridx = 0;
        bodyGbc.gridy = 0;
        bodyGbc.weightx = 1.0;
        bodyGbc.weighty = 1.0;
        bodyGbc.fill = GridBagConstraints.HORIZONTAL;
        bodyGbc.anchor = GridBagConstraints.NORTH;
        bodyPanel.add(card, bodyGbc);

        content.add(header, BorderLayout.NORTH);
        content.add(bodyPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addFormLabel(JPanel p, String text, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.ON_SURFACE);
        lbl.setPreferredSize(new Dimension(240, 32));
        p.add(lbl, gbc);
    }

    private void addFormField(JPanel p, JComponent c, int row, GridBagConstraints gbc) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        p.add(c, gbc);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(400, 36));
        tf.setFont(AppTheme.FONT_BODY_MD);
        return tf;
    }

    private JScrollPane wrapTextArea(JTextArea ta, int rows) {
        ta.setFont(AppTheme.FONT_BODY_MD);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(AppTheme.SURFACE_HIGH);
        ta.setForeground(AppTheme.ON_SURFACE);
        ta.setCaretColor(AppTheme.PRIMARY);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(400, rows * 22));
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1));
        return sp;
    }

    /**
     * Tải dữ liệu từ database lên giao diện
     */
    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                StoreConfig.loadFromDatabase();
                return null;
            }

            @Override
            protected void done() {
                txtShopName.setText(StoreConfig.shopName);
                txtShopNamePnh.setText(StoreConfig.shopNamePnh);
                txtShopAddr.setText(StoreConfig.shopAddr);
                txtShopTel.setText(StoreConfig.shopTel);
                txtShopBank.setText(StoreConfig.shopBank);
                txtShopNotes.setText(StoreConfig.shopNotes);
                txtShopPolicy.setText(StoreConfig.shopPolicy);
                txtShopWarranty.setText(StoreConfig.shopWarranty);
                txtShopWarrantyLimit.setText(StoreConfig.shopWarrantyLimit);
                lblStatus.setText(" ");
            }
        };
        worker.execute();
    }

    /**
     * Lưu cấu hình xuống database
     */
    private void saveConfig() {
        String name = txtShopName.getText().trim();
        String namePnh = txtShopNamePnh.getText().trim();
        String addr = txtShopAddr.getText().trim();
        String tel = txtShopTel.getText().trim();
        String bank = txtShopBank.getText().trim();
        String notes = txtShopNotes.getText().trim();
        String policy = txtShopPolicy.getText().trim();
        String warranty = txtShopWarranty.getText().trim();
        String warrantyLimit = txtShopWarrantyLimit.getText().trim();

        if (name.isEmpty() || namePnh.isEmpty() || addr.isEmpty() || tel.isEmpty() || bank.isEmpty()
                || notes.isEmpty() || policy.isEmpty() || warranty.isEmpty() || warrantyLimit.isEmpty()) {
            lblStatus.setForeground(AppTheme.STATUS_CANC_FG);
            lblStatus.setText("⚠️ Vui lòng không để trống bất kỳ trường thông tin nào!");
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return StoreConfig.saveToDatabase(name, namePnh, addr, tel, bank, notes, policy, warranty, warrantyLimit);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        lblStatus.setForeground(AppTheme.STATUS_PAID_FG);
                        lblStatus.setText("✅ Đã lưu cấu hình cửa hàng mới thành công!");
                        Timer timer = new Timer(3000, e -> lblStatus.setText(" "));
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        lblStatus.setForeground(AppTheme.STATUS_CANC_FG);
                        lblStatus.setText("⚠️ Lỗi kết nối CSDL, không thể lưu cấu hình!");
                    }
                } catch (Exception e) {
                    lblStatus.setForeground(AppTheme.STATUS_CANC_FG);
                    lblStatus.setText("⚠️ Đã xảy ra lỗi: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Reset về mặc định ban đầu
     */
    private void resetToDefault() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn khôi phục thông tin mặc định của cửa hàng?",
                "Khôi phục mặc định",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            txtShopName.setText("JENKA COFFEE SHOP");
            txtShopNamePnh.setText("Jenka Coffee Shop");
            txtShopAddr.setText("Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM");
            txtShopTel.setText("Điện thoại: 0817909090 - 0827909090");
            txtShopBank.setText("Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công");
            txtShopNotes.setText("   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\n" +
                    "   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\n" +
                    "   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.");
            txtShopPolicy.setText("Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\n" +
                    " - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\n" +
                    " - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.");
            txtShopWarranty.setText("- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\n" +
                    "- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...");
            txtShopWarrantyLimit.setText("- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.");
            saveConfig();
        }
    }
}

package com.brewmaster;

import com.brewmaster.db.DatabaseManager;
import com.brewmaster.theme.AppTheme;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry Point của ứng dụng BrewMaster Pro
 * Kết nối thẳng vào Docker MySQL (localhost:3306) rồi mở AppFrame.
 */
public class Main {

    public static void main(String[] args) {
        // Đặt stdout/stderr sang UTF-8 để hiển thị tiếng Việt đúng trên Windows
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        // Phải chạy trên Event Dispatch Thread
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        // === 1. Cài đặt FlatLaf Dark Theme ===
        try {
            FlatDarkLaf.setup();
            applyCustomColors();
        } catch (Exception e) {
            System.err.println("Không thể khởi tạo FlatLaf: " + e.getMessage());
        }

        // Apply font settings globally
        UIManager.put("defaultFont", AppTheme.FONT_BODY_MD);
        UIManager.put("Button.font", AppTheme.FONT_LABEL);
        UIManager.put("Label.font", AppTheme.FONT_BODY_MD);
        UIManager.put("TextField.font", AppTheme.FONT_BODY_MD);
        UIManager.put("ComboBox.font", AppTheme.FONT_BODY_MD);

        // === 2. Kết nối DB tự động (Docker MySQL) ===
        // Cấu hình khớp với docker-compose.yml
        DatabaseManager.configure("localhost", 3306, "cfe_di_rom", "root", "root");

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            System.out.println("✅ Kết nối database thành công!");

            // Tự động kiểm tra và thêm cột ghi_chu cho nhap_hang nếu chưa có
            try (Statement stmt = conn.createStatement()) {
                boolean hasGhiChu = false;
                try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `nhap_hang` LIKE 'ghi_chu'")) {
                    if (rs.next()) {
                        hasGhiChu = true;
                    }
                }
                if (!hasGhiChu) {
                    stmt.executeUpdate("ALTER TABLE `nhap_hang` ADD COLUMN `ghi_chu` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Ghi chú thêm cho đơn nhập'");
                    System.out.println("✅ Đã tự động thêm cột ghi_chu vào bảng nhap_hang.");
                }

                // Tự động kiểm tra và tạo bảng store_config
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `store_config` ("
                        + " `id` INT PRIMARY KEY,"
                        + " `shop_name` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                        + " `shop_name_pnh` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                        + " `shop_addr` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                        + " `shop_tel` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                        + " `shop_bank` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL"
                        + " ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

                // Thêm các cột mới nếu chưa có
                String[] cols = {
                    "shop_notes TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL",
                    "shop_policy TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL",
                    "shop_warranty TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL",
                    "shop_warranty_limit VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL"
                };
                String[] colNames = {"shop_notes", "shop_policy", "shop_warranty", "shop_warranty_limit"};
                for (int i = 0; i < colNames.length; i++) {
                    boolean hasCol = false;
                    try (ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `store_config` LIKE '" + colNames[i] + "'")) {
                        if (rs.next()) {
                            hasCol = true;
                        }
                    }
                    if (!hasCol) {
                        stmt.executeUpdate("ALTER TABLE `store_config` ADD COLUMN " + cols[i]);
                        System.out.println("✅ Đã tự động thêm cột " + colNames[i] + " vào bảng store_config.");
                    }
                }

                // Thêm bản ghi mặc định nếu chưa có
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `store_config` WHERE `id` = 1")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        stmt.executeUpdate("INSERT INTO `store_config` (`id`, `shop_name`, `shop_name_pnh`, `shop_addr`, `shop_tel`, `shop_bank`, `shop_notes`, `shop_policy`, `shop_warranty`, `shop_warranty_limit`)"
                                + " VALUES (1, 'JENKA COFFEE SHOP', 'Jenka Coffee Shop',"
                                + " 'Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM',"
                                + " 'Điện thoại: 0817909090 - 0827909090',"
                                + " 'Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công',"
                                + " '   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\\n   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\\n   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.',"
                                + " 'Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\\n - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\\n - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.',"
                                + " '- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\\n- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...',"
                                + " '- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.')");
                        System.out.println("✅ Đã khởi tạo cấu hình cửa hàng mặc định.");
                    }
                }

                // Cập nhật giá trị mặc định cho các cột mới nếu đang NULL
                stmt.executeUpdate("UPDATE `store_config` SET `shop_notes` = '   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\\n   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\\n   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.' WHERE `id` = 1 AND `shop_notes` IS NULL");
                stmt.executeUpdate("UPDATE `store_config` SET `shop_policy` = 'Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\\n - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\\n - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.' WHERE `id` = 1 AND `shop_policy` IS NULL");
                stmt.executeUpdate("UPDATE `store_config` SET `shop_warranty` = '- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\\n- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...' WHERE `id` = 1 AND `shop_warranty` IS NULL");
                stmt.executeUpdate("UPDATE `store_config` SET `shop_warranty_limit` = '- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.' WHERE `id` = 1 AND `shop_warranty_limit` IS NULL");
            } catch (SQLException ex) {
                System.err.println("⚠️ Lỗi kiểm tra/thêm bảng store_config: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể kết nối database: " + e.getMessage());
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "<html><b>Không thể kết nối database.</b><br>" +
                            "Hãy đảm bảo Docker container đang chạy:<br>" +
                            "<code>docker-compose up -d</code><br><br>" +
                            "Bạn có muốn chạy app ở chế độ demo không?</html>",
                    "Lỗi kết nối",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[] { "Chạy demo", "Thoát" },
                    "Chạy demo");
            if (choice != 0) {
                System.exit(0);
                return;
            }
        }

        // === 3. Mở AppFrame chính ===
        AppFrame frame = new AppFrame();
        frame.setVisible(true);
    }

    private static void applyCustomColors() {
        // Override FlatLaf colors với BrewMaster theme
        Map<String, Object> uiProps = new HashMap<>();

        // Background colors
        uiProps.put("Panel.background", AppTheme.BACKGROUND);
        uiProps.put("Frame.background", AppTheme.BACKGROUND);
        uiProps.put("Component.background", AppTheme.SURFACE_LOW);

        // Text
        uiProps.put("Label.foreground", AppTheme.ON_SURFACE);
        uiProps.put("Component.foreground", AppTheme.ON_SURFACE);

        // TextField
        uiProps.put("TextField.background", AppTheme.SURFACE_HIGH);
        uiProps.put("TextField.foreground", AppTheme.ON_SURFACE);
        uiProps.put("TextField.caretForeground", AppTheme.PRIMARY);
        uiProps.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.OUTLINE_VARIANT, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // Button
        uiProps.put("Button.background", AppTheme.SURFACE_HIGH);
        uiProps.put("Button.foreground", AppTheme.ON_SURFACE);
        uiProps.put("Button.hoverBackground", AppTheme.SURFACE_HIGHEST);
        uiProps.put("Button.focusedBackground", AppTheme.SURFACE_HIGHEST);

        // ComboBox
        uiProps.put("ComboBox.background", AppTheme.SURFACE_HIGH);
        uiProps.put("ComboBox.foreground", AppTheme.ON_SURFACE);
        uiProps.put("ComboBox.selectionBackground", AppTheme.PRIMARY_CONTAINER);
        uiProps.put("ComboBox.selectionForeground", AppTheme.ON_PRIMARY);

        // Table
        uiProps.put("Table.background", AppTheme.SURFACE_LOW);
        uiProps.put("Table.foreground", AppTheme.ON_SURFACE);
        uiProps.put("Table.selectionBackground", AppTheme.withAlpha(AppTheme.PRIMARY, 40));
        uiProps.put("Table.selectionForeground", AppTheme.ON_SURFACE);
        uiProps.put("Table.gridColor", AppTheme.OUTLINE_VARIANT);
        uiProps.put("TableHeader.background", AppTheme.SURFACE_HIGH);
        uiProps.put("TableHeader.foreground", AppTheme.ON_SURFACE_VAR);

        // ScrollPane
        uiProps.put("ScrollPane.background", AppTheme.SURFACE_LOW);
        uiProps.put("ScrollBar.thumbColor", AppTheme.OUTLINE_VARIANT);
        uiProps.put("ScrollBar.hoverThumbColor", AppTheme.OUTLINE);

        // TabbedPane
        uiProps.put("TabbedPane.background", AppTheme.SURFACE_LOW);
        uiProps.put("TabbedPane.foreground", AppTheme.ON_SURFACE_VAR);
        uiProps.put("TabbedPane.selectedBackground", AppTheme.SURFACE_HIGH);
        uiProps.put("TabbedPane.selectedForeground", AppTheme.ON_SURFACE);
        uiProps.put("TabbedPane.underlineColor", AppTheme.PRIMARY);

        // ProgressBar
        uiProps.put("ProgressBar.background", AppTheme.SURFACE_HIGHEST);
        uiProps.put("ProgressBar.foreground", AppTheme.PRIMARY);

        // Dialog
        uiProps.put("Dialog.background", AppTheme.SURFACE_LOW);
        uiProps.put("OptionPane.background", AppTheme.SURFACE_LOW);
        uiProps.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Password field
        uiProps.put("PasswordField.background", AppTheme.SURFACE_HIGH);
        uiProps.put("PasswordField.foreground", AppTheme.ON_SURFACE);

        for (Map.Entry<String, Object> entry : uiProps.entrySet()) {
            UIManager.put(entry.getKey(), entry.getValue());
        }

        FlatLaf.updateUI();
    }
}

package com.brewmaster.util;

import com.brewmaster.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Tiện ích ghi nhật ký thao tác dữ liệu.
 * <p>
 * Gọi {@link #log(String, String, String, String)} mỗi khi thêm / sửa / xóa
 * bản ghi trên các tab: Bán hàng, Nhập hàng, Thu Chi.
 * <p>
 * Thao tác được ghi lên bảng {@code nhat_ky} trong cùng schema MySQL.
 * Hàm luôn bắt ngoại lệ nội bộ để không làm gián đoạn luồng chính.
 */
public class ActivityLogger {

    // Hằng định nghĩa tab
    public static final String TAB_BAN_HANG  = "Bán hàng";
    public static final String TAB_NHAP_HANG = "Nhập hàng";
    public static final String TAB_THU_CHI   = "Thu Chi";

    // Hằng định nghĩa thao tác
    public static final String ACTION_THEM = "Thêm";
    public static final String ACTION_SUA  = "Sửa";
    public static final String ACTION_XOA  = "Xóa";

    private ActivityLogger() {}

    /**
     * Ghi một bản ghi nhật ký.
     *
     * @param thaoTac   "Thêm" | "Sửa" | "Xóa"
     * @param tab       "Bán hàng" | "Nhập hàng" | "Thu Chi"
     * @param maBanGhi  Mã bản ghi liên quan, VD "BH-12", "NH-5", "TC-3"
     * @param moTa      Mô tả chi tiết bằng tiếng Việt
     */
    public static void log(String thaoTac, String tab, String maBanGhi, String moTa) {
        // Chạy trong background để không block EDT
        Thread t = new Thread(() -> {
            try {
                Connection conn = DatabaseManager.getInstance().getConnection();
                String sql = "INSERT INTO nhat_ky (thao_tac, tab, ma_ban_ghi, mo_ta) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, thaoTac);
                    ps.setString(2, tab);
                    ps.setString(3, maBanGhi);
                    ps.setString(4, moTa);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                // Ghi log lỗi ra console, không ném exception để không ảnh hưởng luồng chính
                System.err.println("[ActivityLogger] Lỗi ghi nhật ký: " + e.getMessage());
            }
        }, "ActivityLogger-Thread");
        t.setDaemon(true);
        t.start();
    }
}

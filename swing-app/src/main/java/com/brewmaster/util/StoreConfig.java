package com.brewmaster.util;

import com.brewmaster.db.DatabaseManager;
import java.sql.*;

/**
 * Quản lý cấu hình thông tin cửa hàng phục vụ xuất hóa đơn và hiển thị.
 */
public class StoreConfig {
    
    // Các giá trị mặc định fallback nếu DB gặp sự cố
    public static String shopName      = "JENKA COFFEE SHOP";
    public static String shopNamePnh   = "Jenka Coffee Shop";
    public static String shopAddr      = "Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM";
    public static String shopTel       = "Điện thoại: 0817909090 - 0827909090";
    public static String shopBank      = "Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công";
    public static String shopNotes     = "   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\n" +
                                         "   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\n" +
                                         "   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.";
    public static String shopPolicy    = "Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\n" +
                                         " - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\n" +
                                         " - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.";
    public static String shopWarranty  = "- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\n" +
                                         "- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...";
    public static String shopWarrantyLimit = "- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.";

    /**
     * Tải dữ liệu từ bảng store_config trong Database
     */
    public static synchronized void loadFromDatabase() {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT shop_name, shop_name_pnh, shop_addr, shop_tel, shop_bank, shop_notes, shop_policy, shop_warranty, shop_warranty_limit FROM store_config WHERE id = 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    shopName      = rs.getString("shop_name");
                    shopNamePnh   = rs.getString("shop_name_pnh");
                    shopAddr      = rs.getString("shop_addr");
                    shopTel       = rs.getString("shop_tel");
                    shopBank      = rs.getString("shop_bank");
                    shopNotes     = rs.getString("shop_notes");
                    shopPolicy    = rs.getString("shop_policy");
                    shopWarranty  = rs.getString("shop_warranty");
                    shopWarrantyLimit = rs.getString("shop_warranty_limit");
                    System.out.println("🔄 Đã tải cấu hình cửa hàng mới nhất từ database.");
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể tải cấu hình cửa hàng: " + e.getMessage());
        }
    }

    /**
     * Lưu dữ liệu mới xuống Database
     */
    public static synchronized boolean saveToDatabase(String name, String namePnh, String addr, String tel, String bank, String notes, String policy, String warranty, String warrantyLimit) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "UPDATE store_config SET shop_name = ?, shop_name_pnh = ?, shop_addr = ?, shop_tel = ?, shop_bank = ?, shop_notes = ?, shop_policy = ?, shop_warranty = ?, shop_warranty_limit = ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, namePnh);
                ps.setString(3, addr);
                ps.setString(4, tel);
                ps.setString(5, bank);
                ps.setString(6, notes);
                ps.setString(7, policy);
                ps.setString(8, warranty);
                ps.setString(9, warrantyLimit);
                
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    shopName      = name;
                    shopNamePnh   = namePnh;
                    shopAddr      = addr;
                    shopTel       = tel;
                    shopBank      = bank;
                    shopNotes     = notes;
                    shopPolicy    = policy;
                    shopWarranty  = warranty;
                    shopWarrantyLimit = warrantyLimit;
                    System.out.println("💾 Đã lưu cấu hình cửa hàng thành công.");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi lưu cấu hình cửa hàng: " + e.getMessage());
        }
        return false;
    }
}

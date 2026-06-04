-- Tắt kiểm tra khóa ngoại để có thể truncate các bảng có quan hệ ràng buộc
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu và reset AUTO_INCREMENT về 1 cho từng bảng được yêu cầu
TRUNCATE TABLE `chi_tiet_ban_hang`;
TRUNCATE TABLE `chi_tiet_nhap_hang`;
TRUNCATE TABLE `thu_chi`;
TRUNCATE TABLE `ban_hang`;
TRUNCATE TABLE `nhap_hang`;
TRUNCATE TABLE `san_pham`;
TRUNCATE TABLE `nhom_san_pham`;
TRUNCATE TABLE `doi_tac`;
TRUNCATE TABLE `nhan_vien`;
TRUNCATE TABLE `nhat_ky`;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

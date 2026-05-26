-- ============================================================
-- Script: Xóa dữ liệu các bảng và reset auto_increment về 1
-- File: clear_data.sql
-- ============================================================

-- Tắt kiểm tra khóa ngoại để có thể TRUNCATE các bảng có quan hệ với nhau
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Chi tiết nhập hàng & Nhập hàng
TRUNCATE TABLE `chi_tiet_nhap_hang`;
TRUNCATE TABLE `nhap_hang`;

-- 2. Chi tiết bán hàng & Bán hàng
TRUNCATE TABLE `chi_tiet_ban_hang`;
TRUNCATE TABLE `ban_hang`;

-- 3. Sản phẩm
TRUNCATE TABLE `san_pham`;

-- 4. Đối tác & Nhân viên
TRUNCATE TABLE `doi_tac`;
TRUNCATE TABLE `nhan_vien`;

-- 5. Nhật ký & Thu chi
TRUNCATE TABLE `nhat_ky`;
TRUNCATE TABLE `thu_chi`;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

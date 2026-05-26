-- ============================================================
-- Khởi tạo database cfe_di_rom cho Docker container
-- Tự động chạy khi container MySQL khởi động lần đầu
-- ============================================================

-- Bắt buộc MySQL đọc file này bằng UTF-8 (fix lỗi tiếng Việt bị vỡ)
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS `cfe_di_rom`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `cfe_di_rom`;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+07:00";

-- --------------------------------------------------------
-- Bảng: danh_muc
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `danh_muc` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ten_danh_muc` varchar(255) NOT NULL,
  `mo_ta` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: don_vi_tinh
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `don_vi_tinh` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ten_don_vi` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: doi_tac (gộp khach_hang + nha_cung_cap)
-- Một người có thể vừa là Khách hàng, vừa là Nhà cung cấp
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `doi_tac` (
  `id`      int(11)      NOT NULL AUTO_INCREMENT,
  `ten`     varchar(255) NOT NULL COMMENT 'Tên đối tác',
  `sdt`     varchar(20)  DEFAULT NULL,
  `dia_chi` text         DEFAULT NULL,
  `loai`    set('Khách hàng','Nhà cung cấp') NOT NULL DEFAULT 'Khách hàng'
            COMMENT 'Vai trò: Khách hàng | Nhà cung cấp | Khách hàng,Nhà cung cấp',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: nhan_vien
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `nhan_vien` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ten_nhan_vien` varchar(255) NOT NULL,
  `sdt` varchar(20) DEFAULT NULL,
  `vai_tro` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: san_pham
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `san_pham` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ten_san_pham` varchar(255) NOT NULL,
  `gia_nhap_hien_tai` decimal(15,0) DEFAULT 0,
  `gia_ban_hien_tai` decimal(15,0) DEFAULT 0,
  `so_luong_ton` int(11) DEFAULT 0,
  `canh_bao_ton_kho` int(11) NOT NULL DEFAULT 5
    COMMENT 'Cảnh báo khi số lượng tồn chạm hoặc thấp hơn ngưỡng này',
  `trang_thai` varchar(20) NOT NULL DEFAULT 'Còn hàng'
    COMMENT 'Trạng thái tồn kho: Còn hàng | Cảnh báo | Hết hàng',
  `id_danh_muc` int(11) DEFAULT NULL,
  `id_don_vi` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_danh_muc` (`id_danh_muc`),
  KEY `id_don_vi` (`id_don_vi`),
  CONSTRAINT `san_pham_ibfk_1` FOREIGN KEY (`id_danh_muc`) REFERENCES `danh_muc` (`id`) ON DELETE SET NULL,
  CONSTRAINT `san_pham_ibfk_2` FOREIGN KEY (`id_don_vi`) REFERENCES `don_vi_tinh` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Triggers: Tự động cập nhật trang_thai khi INSERT / UPDATE san_pham
-- --------------------------------------------------------
DROP TRIGGER IF EXISTS `trg_san_pham_before_insert`;
DROP TRIGGER IF EXISTS `trg_san_pham_before_update`;

DELIMITER $$

CREATE TRIGGER `trg_san_pham_before_insert`
BEFORE INSERT ON `san_pham`
FOR EACH ROW
BEGIN
    IF NEW.so_luong_ton = 0 THEN
        SET NEW.trang_thai = 'Hết hàng';
    ELSEIF NEW.so_luong_ton <= NEW.canh_bao_ton_kho THEN
        SET NEW.trang_thai = 'Cảnh báo';
    ELSE
        SET NEW.trang_thai = 'Còn hàng';
    END IF;
END$$

CREATE TRIGGER `trg_san_pham_before_update`
BEFORE UPDATE ON `san_pham`
FOR EACH ROW
BEGIN
    IF NEW.so_luong_ton = 0 THEN
        SET NEW.trang_thai = 'Hết hàng';
    ELSEIF NEW.so_luong_ton <= NEW.canh_bao_ton_kho THEN
        SET NEW.trang_thai = 'Cảnh báo';
    ELSE
        SET NEW.trang_thai = 'Còn hàng';
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------
-- Bảng: ban_hang
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ban_hang` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_doi_tac` int(11) DEFAULT NULL
    COMMENT 'FK → doi_tac (khách hàng của đơn bán)',
  `id_nhan_vien` int(11) DEFAULT NULL,
  `tong_tien` decimal(15,0) DEFAULT 0,
  `tien_da_thanh_toan` decimal(15,0) DEFAULT 0,
  `tien_no` decimal(15,0) DEFAULT 0,
  `dia_chi_giao_hang` text DEFAULT NULL,
  `ngay_lap` date DEFAULT NULL
    COMMENT 'Ngày lắp đặt máy',
  `trang_thai` varchar(50) DEFAULT 'Hẹn'
    COMMENT 'Trạng thái: Hoàn thành | Hẹn | Hủy',
  `ghi_chu` text DEFAULT NULL
    COMMENT 'Ghi chú thêm cho đơn hàng',
  PRIMARY KEY (`id`),
  KEY `id_doi_tac` (`id_doi_tac`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  CONSTRAINT `ban_hang_ibfk_1` FOREIGN KEY (`id_doi_tac`) REFERENCES `doi_tac` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ban_hang_ibfk_2` FOREIGN KEY (`id_nhan_vien`) REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: chi_tiet_ban_hang
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chi_tiet_ban_hang` (
  `id`          int(11)       NOT NULL AUTO_INCREMENT,
  `id_ban_hang` int(11)       NOT NULL,
  `id_san_pham` int(11)       NOT NULL,
  `so_luong`    int(11)       NOT NULL,
  `don_vi`      varchar(50)   DEFAULT NULL,
  `gia_ban`     decimal(15,0) NOT NULL,
  `thanh_tien`  decimal(15,0) NOT NULL,
  `is_gift`     tinyint(1)    NOT NULL DEFAULT 0
                COMMENT '0 = sản phẩm mua, 1 = quà tặng',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bh_sp_gift` (`id_ban_hang`, `id_san_pham`, `is_gift`),
  KEY `id_san_pham` (`id_san_pham`),
  CONSTRAINT `chi_tiet_ban_hang_ibfk_1` FOREIGN KEY (`id_ban_hang`) REFERENCES `ban_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chi_tiet_ban_hang_ibfk_2` FOREIGN KEY (`id_san_pham`) REFERENCES `san_pham` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: nhap_hang
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `nhap_hang` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `tong_tien` decimal(15,0) DEFAULT 0,
  `da_thanh_toan` decimal(15,0) DEFAULT 0,
  `tien_no` decimal(15,0) DEFAULT 0,
  `trang_thai` varchar(50) DEFAULT 'Chờ nhận',
  `ngay_nhan` date DEFAULT NULL
    COMMENT 'Ngày nhận hàng thực tế',
  `id_doi_tac` int(11) DEFAULT NULL
    COMMENT 'FK → doi_tac (nhà cung cấp của đơn nhập)',
  `id_nhan_vien` int(11) DEFAULT NULL,
  `ghi_chu` text DEFAULT NULL
    COMMENT 'Ghi chú thêm cho đơn nhập',
  PRIMARY KEY (`id`),
  KEY `id_doi_tac` (`id_doi_tac`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  CONSTRAINT `nhap_hang_ibfk_1` FOREIGN KEY (`id_doi_tac`) REFERENCES `doi_tac` (`id`) ON DELETE SET NULL,
  CONSTRAINT `nhap_hang_ibfk_2` FOREIGN KEY (`id_nhan_vien`) REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: chi_tiet_nhap_hang
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chi_tiet_nhap_hang` (
  `id_nhap_hang` int(11) NOT NULL,
  `id_san_pham` int(11) NOT NULL,
  `so_luong` int(11) NOT NULL,
  `don_vi` varchar(50) DEFAULT NULL,
  `gia_nhap` decimal(15,0) NOT NULL,
  `thanh_tien` decimal(15,0) NOT NULL,
  PRIMARY KEY (`id_nhap_hang`, `id_san_pham`),
  KEY `id_san_pham` (`id_san_pham`),
  CONSTRAINT `chi_tiet_nhap_hang_ibfk_1` FOREIGN KEY (`id_nhap_hang`) REFERENCES `nhap_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chi_tiet_nhap_hang_ibfk_2` FOREIGN KEY (`id_san_pham`) REFERENCES `san_pham` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: nhat_ky
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `nhat_ky` (
  `id`          int(11)       NOT NULL AUTO_INCREMENT,
  `thoi_gian`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `thao_tac`    varchar(20)   NOT NULL COMMENT 'THEM | SUA | XOA',
  `tab`         varchar(50)   NOT NULL COMMENT 'ban_hang | nhap_hang | thu_chi',
  `ma_ban_ghi`  varchar(30)   DEFAULT NULL COMMENT 'VD: BH-12, NH-5, TC-3',
  `mo_ta`       text          DEFAULT NULL COMMENT 'Nội dung mô tả chi tiết',
  PRIMARY KEY (`id`),
  KEY `idx_nhat_ky_thoi_gian` (`thoi_gian` DESC),
  KEY `idx_nhat_ky_tab` (`tab`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Nhật ký thao tác dữ liệu';

-- ============================================================

-- DỮ LIỆU MẪU
-- ============================================================

INSERT IGNORE INTO `danh_muc` (`id`, `ten_danh_muc`, `mo_ta`) VALUES
(1, 'Thiết bị', 'Các loại máy móc như máy pha, máy xay cà phê, máy ép...'),
(2, 'Dụng cụ', 'Các vật dụng hỗ trợ pha chế thủ công như phin, tamper, ca đánh sữa, cân điện tử...');

INSERT IGNORE INTO `don_vi_tinh` (`id`, `ten_don_vi`) VALUES
(1, 'cái'),
(2, 'gam'),
(3, 'bộ');

-- doi_tac: gộp khách hàng và nhà cung cấp
-- id 1-3: khách hàng, id 4-5: nhà cung cấp
INSERT IGNORE INTO `doi_tac` (`id`, `ten`, `sdt`, `dia_chi`, `loai`) VALUES
(1, 'Phạm Quang Khải',                '0934777888', '789 Lê Lợi, Q.1, TP.HCM',                   'Khách hàng'),
(2, 'Quán Cafe Hương Làng',           '0977999000', '101 Cách Mạng Tháng 8, Q.10, TP.HCM',        'Khách hàng'),
(3, 'Khách lẻ không tên',             NULL,         NULL,                                          'Khách hàng'),
(4, 'Công ty TNHH Kỹ Thuật Cà Phê VN','0283123456', '123 Cộng Hòa, Q. Tân Bình, TP.HCM',          'Nhà cung cấp'),
(5, 'Đại lý Dụng cụ Pha chế Quốc Tế', '0283654321', '45 Nguyễn Đình Chiểu, Q.3, TP.HCM',          'Nhà cung cấp');

INSERT IGNORE INTO `nhan_vien` (`id`, `ten_nhan_vien`, `sdt`, `vai_tro`) VALUES
(1, 'Nguyễn Hải Đăng', '0901111222', 'Quản lý'),
(2, 'Trần Thu Hà', '0988333444', 'Sale'),
(3, 'Lê Văn Mạnh', '0912555666', 'Kỹ thuật');

-- canh_bao_ton_kho: ngưỡng cảnh báo; trang_thai sẽ được trigger tự cập nhật
INSERT IGNORE INTO `san_pham` (`id`, `ten_san_pham`, `gia_nhap_hien_tai`, `gia_ban_hien_tai`, `so_luong_ton`, `canh_bao_ton_kho`, `trang_thai`, `id_danh_muc`, `id_don_vi`) VALUES
(1, 'Máy pha cà phê Gemilai CRM 3200', 12000000, 14500000, 10, 5, 'Còn hàng', 1, 1),
(2, 'Máy xay cà phê HC600',           3500000,  4200000,  15, 5, 'Còn hàng', 1, 1),
(3, 'Tamper nén cà phê inox 58mm',    150000,   250000,   50, 10,'Còn hàng', 2, 1),
(4, 'Bộ pha cà phê thủ công V60',     300000,   480000,   20, 5, 'Còn hàng', 2, 3),
(5, 'Bột vệ sinh máy pha Cafiza',     250,   350,   3000,  5000, 'Cảnh báo', 2, 2);

-- nhap_hang dùng id_doi_tac thay id_nha_cung_cap (id 4 = Công ty TNHH Kỹ Thuật Cà Phê VN)
INSERT IGNORE INTO `nhap_hang` (`id`, `thoi_gian`, `tong_tien`, `da_thanh_toan`, `tien_no`, `trang_thai`, `ngay_nhan`, `id_doi_tac`, `id_nhan_vien`) VALUES
(1, '2026-05-24 08:30:00', 95000000, 95000000, 0, 'Đã nhận', '2026-05-24', 4, 1);

INSERT IGNORE INTO `chi_tiet_nhap_hang` (`id_nhap_hang`, `id_san_pham`, `so_luong`, `gia_nhap`, `thanh_tien`) VALUES
(1, 1, 5, 12000000, 60000000),
(1, 2, 10, 3500000, 35000000);

-- ban_hang dùng id_doi_tac thay id_khach_hang (id 2 = Quán Cafe Hương Làng)
INSERT IGNORE INTO `ban_hang` (`id`, `thoi_gian`, `id_doi_tac`, `id_nhan_vien`, `tong_tien`, `tien_da_thanh_toan`, `tien_no`, `dia_chi_giao_hang`, `ngay_lap`, `trang_thai`, `ghi_chu`) VALUES
(1, '2026-05-24 10:15:00', 2, 2, 19200000, 10000000, 9200000, '101 Cách Mạng Tháng 8, Q.10, TP.HCM', '2026-05-25', 'Hoàn thành', 'Đơn hàng đầu tiên, khách đã lắp đặt xong.');

INSERT IGNORE INTO `chi_tiet_ban_hang` (`id_ban_hang`, `id_san_pham`, `so_luong`, `gia_ban`, `thanh_tien`, `is_gift`) VALUES
(1, 1, 1, 14500000, 14500000, 0),
(1, 2, 1, 4200000, 4200000, 0),
(1, 3, 2, 250000, 500000, 0);

-- --------------------------------------------------------
-- Bảng: loai_thu_chi (danh mục thu/chi dùng chung)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `loai_thu_chi` (
  `id`   int(11)      NOT NULL AUTO_INCREMENT,
  `ten`  varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Bảng: thu_chi (phiếu thu/chi — 1 dòng có thể có cả thu lẫn chi)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `thu_chi` (
  `id`           int(11)       NOT NULL AUTO_INCREMENT,
  `thoi_gian`    datetime      DEFAULT CURRENT_TIMESTAMP,
  `id_loai`      int(11)       DEFAULT NULL,
  `tien_thu`     decimal(15,0) DEFAULT NULL COMMENT 'Số tiền thu — NULL nếu không có khoản thu',
  `tien_chi`     decimal(15,0) DEFAULT NULL COMMENT 'Số tiền chi — NULL nếu không có khoản chi',
  `mo_ta`        text          DEFAULT NULL,
  `id_nhan_vien` int(11)       DEFAULT NULL,
  `id_ban_hang`  int(11)       DEFAULT NULL COMMENT 'FK den ban_hang de auto link',
  `id_nhap_hang` int(11)       DEFAULT NULL COMMENT 'FK den nhap_hang de auto link',
  PRIMARY KEY (`id`),
  KEY `id_loai` (`id_loai`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  KEY `id_ban_hang` (`id_ban_hang`),
  KEY `id_nhap_hang` (`id_nhap_hang`),
  CONSTRAINT `thu_chi_ibfk_1` FOREIGN KEY (`id_loai`)
      REFERENCES `loai_thu_chi` (`id`) ON DELETE SET NULL,
  CONSTRAINT `thu_chi_ibfk_2` FOREIGN KEY (`id_nhan_vien`)
      REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_thu_chi_ban_hang` FOREIGN KEY (`id_ban_hang`)
      REFERENCES `ban_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_thu_chi_nhap_hang` FOREIGN KEY (`id_nhap_hang`)
      REFERENCES `nhap_hang` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- DỮ LIỆU MẪU: Danh mục thu chi mặc định (không có cột kieu)
-- ============================================================
INSERT IGNORE INTO `loai_thu_chi` (`id`, `ten`) VALUES
(1,  'Nhập hàng'),
(2,  'Tiền sửa máy'),
(3,  'Tiền lắp máy'),
(4,  'Tiền setup máy'),
(5,  'Trả lương nhân viên'),
(6,  'Tiền thuê mặt bằng'),
(7,  'Điện - Nước'),
(8,  'Chi phí Marketing'),
(9,  'Bán hàng'),
(10, 'Dịch vụ kỹ thuật'),
(11, 'Cho thuê không gian'),
(12, 'Thu khác');

-- Dữ liệu mẫu phiếu thu chi (tien_thu / tien_chi tách biệt)
INSERT IGNORE INTO `thu_chi` (`id`, `thoi_gian`, `id_loai`, `tien_thu`, `tien_chi`, `mo_ta`, `id_nhan_vien`) VALUES
(1, '2026-05-24 09:00:00', 5, NULL,    15000000, 'Tra luong thang 5 cho nhan vien ky thuat', 1),
(2, '2026-05-24 11:00:00', 10, 3000000, NULL,    'Dich vu bao duong may pha cho quan doi tac', 3),
(3, '2026-05-24 14:00:00', 2, NULL,    2500000,  'Sua may xay HC600 bi ket luoi dao', 3);

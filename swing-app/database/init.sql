-- MySQL dump 10.13  Distrib 8.0.45, for Linux (x86_64)
--
-- Host: localhost    Database: cfe_di_rom
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `cfe_di_rom`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cfe_di_rom` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `cfe_di_rom`;

--
-- Table structure for table `ban_hang`
--

DROP TABLE IF EXISTS `ban_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ban_hang` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_doi_tac` int DEFAULT NULL COMMENT 'FK → doi_tac (khách hàng của đơn bán)',
  `id_nhan_vien` int DEFAULT NULL,
  `tong_tien` decimal(15,0) DEFAULT '0',
  `tien_da_thanh_toan` decimal(15,0) DEFAULT '0',
  `tien_no` decimal(15,0) DEFAULT '0',
  `dia_chi_giao_hang` text COLLATE utf8mb4_general_ci,
  `ngay_lap` date DEFAULT NULL COMMENT 'Ngày lắp đặt máy',
  `trang_thai` varchar(50) COLLATE utf8mb4_general_ci DEFAULT 'Hẹn' COMMENT 'Trạng thái: Hoàn thành | Hẹn | Hủy',
  `ghi_chu` text COLLATE utf8mb4_general_ci COMMENT 'Ghi chú thêm cho đơn hàng',
  PRIMARY KEY (`id`),
  KEY `id_doi_tac` (`id_doi_tac`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  CONSTRAINT `ban_hang_ibfk_1` FOREIGN KEY (`id_doi_tac`) REFERENCES `doi_tac` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ban_hang_ibfk_2` FOREIGN KEY (`id_nhan_vien`) REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ban_hang`
--

LOCK TABLES `ban_hang` WRITE;
/*!40000 ALTER TABLE `ban_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `ban_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_ban_hang`
--

DROP TABLE IF EXISTS `chi_tiet_ban_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_ban_hang` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_ban_hang` int NOT NULL,
  `id_san_pham` int NOT NULL,
  `so_luong` int NOT NULL,
  `don_vi` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gia_ban` decimal(15,0) NOT NULL,
  `thanh_tien` decimal(15,0) NOT NULL,
  `is_gift` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 = sản phẩm mua, 1 = quà tặng',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bh_sp_gift` (`id_ban_hang`,`id_san_pham`,`is_gift`),
  KEY `id_san_pham` (`id_san_pham`),
  CONSTRAINT `chi_tiet_ban_hang_ibfk_1` FOREIGN KEY (`id_ban_hang`) REFERENCES `ban_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chi_tiet_ban_hang_ibfk_2` FOREIGN KEY (`id_san_pham`) REFERENCES `san_pham` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_ban_hang`
--

LOCK TABLES `chi_tiet_ban_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_ban_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_ban_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_nhap_hang`
--

DROP TABLE IF EXISTS `chi_tiet_nhap_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_nhap_hang` (
  `id_nhap_hang` int NOT NULL,
  `id_san_pham` int NOT NULL,
  `so_luong` int NOT NULL,
  `don_vi` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gia_nhap` decimal(15,0) NOT NULL,
  `thanh_tien` decimal(15,0) NOT NULL,
  PRIMARY KEY (`id_nhap_hang`,`id_san_pham`),
  KEY `id_san_pham` (`id_san_pham`),
  CONSTRAINT `chi_tiet_nhap_hang_ibfk_1` FOREIGN KEY (`id_nhap_hang`) REFERENCES `nhap_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chi_tiet_nhap_hang_ibfk_2` FOREIGN KEY (`id_san_pham`) REFERENCES `san_pham` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_nhap_hang`
--

LOCK TABLES `chi_tiet_nhap_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_nhap_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `chi_tiet_nhap_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_muc`
--

DROP TABLE IF EXISTS `danh_muc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_muc` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_danh_muc` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `mo_ta` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_muc`
--

LOCK TABLES `danh_muc` WRITE;
/*!40000 ALTER TABLE `danh_muc` DISABLE KEYS */;
INSERT INTO `danh_muc` VALUES (1,'Thiết bị','Các loại máy móc như máy pha, máy xay cà phê, máy ép...'),(2,'Dụng cụ','Các vật dụng hỗ trợ pha chế thủ công như phin, tamper, ca đánh sữa, cân điện tử...');
/*!40000 ALTER TABLE `danh_muc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `doi_tac`
--

DROP TABLE IF EXISTS `doi_tac`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `doi_tac` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Tên đối tác',
  `sdt` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `dia_chi` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doi_tac`
--

LOCK TABLES `doi_tac` WRITE;
/*!40000 ALTER TABLE `doi_tac` DISABLE KEYS */;
/*!40000 ALTER TABLE `doi_tac` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `don_vi_tinh`
--

DROP TABLE IF EXISTS `don_vi_tinh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `don_vi_tinh` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_don_vi` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `don_vi_tinh`
--

LOCK TABLES `don_vi_tinh` WRITE;
/*!40000 ALTER TABLE `don_vi_tinh` DISABLE KEYS */;
INSERT INTO `don_vi_tinh` VALUES (1,'cái'),(2,'gam'),(3,'bộ'),(4,'kg');
/*!40000 ALTER TABLE `don_vi_tinh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loai_thu_chi`
--

DROP TABLE IF EXISTS `loai_thu_chi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loai_thu_chi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loai_thu_chi`
--

LOCK TABLES `loai_thu_chi` WRITE;
/*!40000 ALTER TABLE `loai_thu_chi` DISABLE KEYS */;
INSERT INTO `loai_thu_chi` VALUES (1,'Nhập hàng'),(2,'Tiền sửa máy'),(3,'Tiền lắp máy'),(4,'Tiền setup máy'),(5,'Trả lương nhân viên'),(6,'Tiền thuê mặt bằng'),(7,'Điện - Nước'),(8,'Chi phí Marketing'),(9,'Bán hàng'),(10,'Dịch vụ kỹ thuật'),(11,'Cho thuê không gian'),(12,'Thu khác');
/*!40000 ALTER TABLE `loai_thu_chi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhan_vien`
--

DROP TABLE IF EXISTS `nhan_vien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhan_vien` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_nhan_vien` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `sdt` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `vai_tro` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhan_vien`
--

LOCK TABLES `nhan_vien` WRITE;
/*!40000 ALTER TABLE `nhan_vien` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhan_vien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhap_hang`
--

DROP TABLE IF EXISTS `nhap_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhap_hang` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `tong_tien` decimal(15,0) DEFAULT '0',
  `da_thanh_toan` decimal(15,0) DEFAULT '0',
  `tien_no` decimal(15,0) DEFAULT '0',
  `trang_thai` varchar(50) COLLATE utf8mb4_general_ci DEFAULT 'Chờ nhận',
  `ngay_nhan` date DEFAULT NULL COMMENT 'Ngày nhận hàng thực tế',
  `id_doi_tac` int DEFAULT NULL COMMENT 'FK → doi_tac (nhà cung cấp của đơn nhập)',
  `id_nhan_vien` int DEFAULT NULL,
  `ghi_chu` text COLLATE utf8mb4_general_ci COMMENT 'Ghi chú thêm cho đơn nhập',
  PRIMARY KEY (`id`),
  KEY `id_doi_tac` (`id_doi_tac`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  CONSTRAINT `nhap_hang_ibfk_1` FOREIGN KEY (`id_doi_tac`) REFERENCES `doi_tac` (`id`) ON DELETE SET NULL,
  CONSTRAINT `nhap_hang_ibfk_2` FOREIGN KEY (`id_nhan_vien`) REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhap_hang`
--

LOCK TABLES `nhap_hang` WRITE;
/*!40000 ALTER TABLE `nhap_hang` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhap_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nhat_ky`
--

DROP TABLE IF EXISTS `nhat_ky`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhat_ky` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `thao_tac` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'THEM | SUA | XOA',
  `tab` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ban_hang | nhap_hang | thu_chi',
  `ma_ban_ghi` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'VD: BH-12, NH-5, TC-3',
  `mo_ta` text COLLATE utf8mb4_unicode_ci COMMENT 'Nội dung mô tả chi tiết',
  PRIMARY KEY (`id`),
  KEY `idx_nhat_ky_thoi_gian` (`thoi_gian` DESC),
  KEY `idx_nhat_ky_tab` (`tab`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Nhật ký thao tác dữ liệu';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhat_ky`
--

LOCK TABLES `nhat_ky` WRITE;
/*!40000 ALTER TABLE `nhat_ky` DISABLE KEYS */;
/*!40000 ALTER TABLE `nhat_ky` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham`
--

DROP TABLE IF EXISTS `san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_san_pham` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `gia_nhap_hien_tai` decimal(15,0) DEFAULT '0',
  `gia_ban_hien_tai` decimal(15,0) DEFAULT '0',
  `so_luong_ton` int DEFAULT '0',
  `canh_bao_ton_kho` int NOT NULL DEFAULT '5' COMMENT 'Cảnh báo khi số lượng tồn chạm hoặc thấp hơn ngưỡng này',
  `trang_thai` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Còn hàng' COMMENT 'Trạng thái tồn kho: Còn hàng | Cảnh báo | Hết hàng',
  `id_danh_muc` int DEFAULT NULL,
  `id_don_vi` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_danh_muc` (`id_danh_muc`),
  KEY `id_don_vi` (`id_don_vi`),
  CONSTRAINT `san_pham_ibfk_1` FOREIGN KEY (`id_danh_muc`) REFERENCES `danh_muc` (`id`) ON DELETE SET NULL,
  CONSTRAINT `san_pham_ibfk_2` FOREIGN KEY (`id_don_vi`) REFERENCES `don_vi_tinh` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=261 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham`
--

LOCK TABLES `san_pham` WRITE;
/*!40000 ALTER TABLE `san_pham` DISABLE KEYS */;
INSERT INTO `san_pham` VALUES (1,'Xe Ô Tô Tải Van 945kg',233000000,233000000,1,5,'Cảnh báo',1,1),(2,'Máy đo định lượng đường UB - 18',1157000,1157000,1,5,'Cảnh báo',1,1),(3,'Máy đo định lượng đường sữa 2 ngăn UBS - 26 (Trắng)',4150000,4150000,0,5,'Hết hàng',1,1),(4,'Máy làm đá',5100000,5100000,1,5,'Cảnh báo',1,1),(5,'Máy pha cà phê Astoria Tanya 2018',30300000,30300000,0,5,'Hết hàng',1,1),(6,'Máy pha cà phê Astoria Tanya 2021',41000000,41000000,0,5,'Hết hàng',1,1),(7,'Máy pha cà phê Astoria Tanya 2022',41000000,41000000,0,5,'Hết hàng',1,1),(8,'Máy pha cà phê Astoria Tanya 2025',45106000,45106000,0,5,'Hết hàng',1,1),(9,'Máy pha cà phê Nuova Simonelli Appia Life 2022 Trắng 2g',42000000,42000000,0,5,'Hết hàng',1,1),(10,'Máy pha cà phê Nuova Simonelli Appia Life 2020 Đen 2g',38500000,38500000,1,5,'Cảnh báo',1,1),(11,'Máy pha cà phê Nuova Simonelli Appia Life 2019 Trắng 2g',38400000,38400000,1,5,'Cảnh báo',1,1),(12,'Máy pha cà phê Nuova Simonelli Appia II 2014 Trắng 2g',23000000,23000000,0,5,'Hết hàng',1,1),(13,'Máy pha cà phê Nuova Simonelli Appia II 2014 2g',22000000,22000000,1,5,'Cảnh báo',1,1),(14,'Máy pha cà phê Nuova Simonelli Appia II 2016 Trắng 2g',28330000,28330000,0,5,'Hết hàng',1,1),(15,'Máy pha cà phê Nuova Simonelli Appia II 2017 2g Đen',25350000,25350000,1,5,'Cảnh báo',1,1),(16,'Máy pha cà phê Nuova Simonelli Appia II 2020 2g Trắng',35000000,35000000,0,5,'Hết hàng',1,1),(17,'Máy pha cà phê Breville 870 Mới',12500000,12500000,1,5,'Cảnh báo',1,1),(18,'Máy pha cà phê Breville 870 Thanh lý',6893750,6893750,2,5,'Cảnh báo',1,1),(19,'Máy pha cà phê Breville 878 Mới',17200000,17200000,0,5,'Hết hàng',1,1),(20,'Máy pha cà phê Breville 878 Thanh lý',9500000,9500000,2,5,'Cảnh báo',1,1),(21,'Máy pha cà phê Brewico',17325000,17325000,2,5,'Cảnh báo',1,1),(22,'Máy pha cà phê Bezzera 2018',30000000,30000000,1,5,'Cảnh báo',1,1),(23,'Máy pha cà phê Bezzera 2019',35266500,35266500,2,5,'Cảnh báo',1,1),(24,'Máy pha cà phê Bezzera 2021',20340000,20340000,1,5,'Cảnh báo',1,1),(25,'Máy pha cà phê Bezzera 2022',33000000,33000000,1,5,'Cảnh báo',1,1),(26,'Máy pha cà phê BFC Delux 2g 2019',39500000,39500000,1,5,'Cảnh báo',1,1),(27,'Máy pha cà phê BFC Delux 2g 2018',36000000,36000000,0,5,'Hết hàng',1,1),(28,'Máy pha cà phê BFC Delux 2g 2015',36000000,36000000,0,5,'Hết hàng',1,1),(29,'Máy pha cà phê BFC Delux 2g 2012',31470000,31470000,0,5,'Hết hàng',1,1),(30,'Máy pha cà phê BFC Delux 1g 2020',32000000,32000000,0,5,'Hết hàng',1,1),(31,'Máy pha cà phê BFC Lira Đỏ',28250000,28250000,1,5,'Cảnh báo',1,1),(32,'Máy pha cà phê BFC Monzza 2g 2013',29000000,29000000,0,5,'Hết hàng',1,1),(33,'Máy pha cà phê BFC Monzza 2g 2015',35138000,35138000,0,5,'Hết hàng',1,1),(34,'Máy pha cà phê BFC Monzza 2g 2017',36000000,36000000,0,5,'Hết hàng',1,1),(35,'Máy pha cà phê BFC Nuova Monza 2g 2019',52000000,52000000,0,5,'Hết hàng',1,1),(36,'Máy pha cà phê BFC Vallelunga 2g 2018',35400000,35400000,1,5,'Cảnh báo',1,1),(37,'Máy pha cà phê Carimali 1g Đỏ 2020',20000000,20000000,1,5,'Cảnh báo',1,1),(38,'Máy pha cà phê Carimali CM400',12000000,12000000,0,5,'Hết hàng',1,1),(39,'Máy pha cà phê Casadio Dieci 1g 2014',18500000,18500000,1,5,'Cảnh báo',1,1),(40,'Máy pha cà phê Casadio Dieci 1g 2016',19700000,19700000,0,5,'Hết hàng',1,1),(41,'Máy pha cà phê Casadio 1g 2020',26000000,26000000,1,5,'Cảnh báo',1,1),(42,'Máy pha cà phê Casadio 1g 2021',28000000,28000000,0,5,'Hết hàng',1,1),(43,'Máy pha cà phê Casadio 1g 2022',29130000,29130000,0,5,'Hết hàng',1,1),(44,'Máy pha cà phê Casadio 1g 2023',32000000,32000000,2,5,'Cảnh báo',1,1),(45,'Máy pha cà phê Casadio 1g 2024',32000000,32000000,0,5,'Hết hàng',1,1),(46,'Máy pha cà phê Casadio Nettuno 1g Mới',40000000,40000000,3,5,'Cảnh báo',1,1),(47,'Máy pha cà phê Casadio Nettuno (TL)',34300000,34300000,0,5,'Hết hàng',1,1),(48,'Máy pha cà phê Caso',2060000,2060000,3,5,'Cảnh báo',1,1),(49,'Máy pha cà phê Cime Co - 03 2g 2022',39520000,39520000,0,5,'Hết hàng',1,1),(50,'Máy pha cà phê Cime Co - 03 2g 2021',38470000,38470000,0,5,'Hết hàng',1,1),(51,'Máy pha cà phê Cime Co - 03 2g 2019',31000000,31000000,2,5,'Cảnh báo',1,1),(52,'Máy pha cà phê CRM 3005E (TL)',2500000,2500000,0,5,'Hết hàng',1,1),(53,'Máy pha cà phê CRM 3005L (VAT)',4500000,4500000,6,5,'Còn hàng',1,1),(54,'Máy pha cà phê Expobar 1g 2015 (Đen)',20000000,20000000,1,5,'Cảnh báo',1,1),(55,'Máy pha cà phê Faema E61 2018 1g',50000000,50000000,0,5,'Hết hàng',1,1),(56,'Máy pha cà phê Faema E61 2014 2g',47000000,47000000,0,5,'Hết hàng',1,1),(57,'Máy pha cà phê Faema E61 2016 2g',51000000,51000000,0,5,'Hết hàng',1,1),(58,'Máy pha cà phê Faema E61 2017 2g',52500000,52500000,0,5,'Hết hàng',1,1),(59,'Máy pha cà phê Faema E98up (Mới)',61500000,61500000,0,5,'Hết hàng',1,1),(60,'Máy pha cà phê Faema E98up 2021 (Đen)',40000000,40000000,0,5,'Hết hàng',1,1),(61,'Máy pha cà phê Faema E98up 2020 (Trắng)',40500000,40500000,0,5,'Hết hàng',1,1),(62,'Máy pha cà phê Faema E98up 2019',40300000,40300000,1,5,'Cảnh báo',1,1),(63,'Máy pha cà phê Faema E98 RE 2016',20000000,20000000,0,5,'Hết hàng',1,1),(64,'Máy pha cà phê Faema E98 RE 2015 (Trắng)',22610000,22610000,1,5,'Cảnh báo',1,1),(65,'Máy pha cà phê Faema E98 RE 2017 (Trắng)',24200000,24200000,0,5,'Hết hàng',1,1),(66,'Máy pha cà phê Faema E98 RE 2017 (Đen)',24270000,24270000,0,5,'Hết hàng',1,1),(67,'Máy pha cà phê Gaggia Classic (Thanh Lý)',8025000,8025000,2,5,'Cảnh báo',1,1),(68,'Máy pha cà phê Gaggia Classic E24 Đỏ (Mới)',14850000,14850000,0,5,'Hết hàng',1,1),(69,'Máy pha cà phê Gaggia Classic E24 (Xám_Mới)',15010300,15010300,4,5,'Cảnh báo',1,1),(70,'Máy pha cà phê Gemilai 3149',13950000,13950000,0,5,'Hết hàng',1,1),(71,'Máy pha cà phê Gemilai 3149 (Đen)',13950000,13950000,8,5,'Còn hàng',1,1),(72,'Máy pha cà phê Gemilai 3149 (Trắng)',13950000,13950000,0,5,'Hết hàng',1,1),(73,'Máy pha cà phê Gemilai 3149 (TL_Trắng)',10500000,10500000,0,5,'Hết hàng',1,1),(74,'Máy pha cà phê Gemilai 3200B Pro',8600000,8600000,7,5,'Còn hàng',1,1),(75,'Máy pha cà phê Gemilai 3200B Pro (TL_Đen)',7357000,7357000,0,5,'Hết hàng',1,1),(76,'Máy pha cà phê Gemilai S-3200B (Lướt)',6000000,6000000,1,5,'Cảnh báo',1,1),(77,'Máy pha cà phê La Marzocco KB90 2024',180000000,180000000,0,5,'Hết hàng',1,1),(78,'Máy pha cà phê LaCarimali 2023 2g',30000000,30000000,1,5,'Cảnh báo',1,1),(79,'Máy pha cà phê LaCarimali Crytal 2023 2g',32000000,32000000,1,5,'Cảnh báo',1,1),(80,'Máy pha cà phê Lacimbali M23 up 2021 (Đen)',40350000,40350000,0,5,'Hết hàng',1,1),(81,'Máy pha cà phê Lacimbali M27 RE',25500000,25500000,0,5,'Hết hàng',1,1),(82,'Máy pha cà phê La Nouva Era Arpa 2019',30000000,30000000,0,5,'Hết hàng',1,1),(83,'Máy pha cà phê La Nouva Era 2013',21000000,21000000,0,5,'Hết hàng',1,1),(84,'Máy pha cà phê Lelit Anita',7600000,7600000,1,5,'Cảnh báo',1,1),(85,'Máy pha cà phê Lelit Bianca V3 2023',39500000,39500000,0,5,'Hết hàng',1,1),(86,'Máy pha cà phê Lelit Marax 2022',23000000,23000000,0,5,'Hết hàng',1,1),(87,'Máy pha cà phê Milesto M19M4 (Mới_VAT)',10200000,10200000,0,5,'Hết hàng',1,1),(88,'Máy pha cà phê Milesto EM19M2 (Mới_VAT)',9200000,9200000,0,5,'Hết hàng',1,1),(89,'Máy pha cà phê Iberital IB7 2g 2023',38320000,38320000,0,5,'Hết hàng',1,1),(90,'Máy pha cà phê Rancilio 2g 2020 (Trắng)',34000000,34000000,0,5,'Hết hàng',1,1),(91,'Máy pha cà phê Rancilio 2g 2019',30000000,30000000,0,5,'Hết hàng',1,1),(92,'Máy pha cà phê Sanremo Zoe 2014',17000000,17000000,0,5,'Hết hàng',1,1),(93,'Máy pha cà phê Wega Luna 2022 1g',28000000,28000000,1,5,'Cảnh báo',1,1),(94,'Máy pha cà phê Wega Luna 2022 2g',35400000,35400000,1,5,'Cảnh báo',1,1),(95,'Máy pha cà phê Wega Pegaso 1g 2021 (Trắng)',33000000,33000000,1,5,'Cảnh báo',1,1),(96,'Máy pha cà phê Wega Pegaso 1g 2022 (Trắng)',35000000,35000000,1,5,'Cảnh báo',1,1),(97,'Máy pha cà phê Wega Pegaso 1g 2024',36500000,36500000,0,5,'Hết hàng',1,1),(98,'Máy pha cà phê Wega Pegaso 1g 2025 (Trắng)',35500000,35500000,0,5,'Hết hàng',1,1),(99,'Máy pha cà phê Wega Pegaso 2019 2g (Trắng)',34000000,34000000,1,5,'Cảnh báo',1,1),(100,'Máy pha cà phê Wega Pegaso 2020 (Đen)',36500000,36500000,0,5,'Hết hàng',1,1),(101,'Máy pha cà phê Wega Pegaso 2021 (Trắng)',37990000,37990000,0,5,'Hết hàng',1,1),(102,'Máy pha cà phê Wega Pegaso 2022 2g (Trắng)',40740000,40740000,2,5,'Cảnh báo',1,1),(103,'Máy pha cà phê Wega Pegaso 2022 2g (Đen)',42000000,42000000,0,5,'Hết hàng',1,1),(104,'Máy pha cà phê Wega Pegaso 2023 2g (Đen)',40990000,40990000,0,5,'Hết hàng',1,1),(105,'Máy pha cà phê Wega Pegaso plus 2023 2g',45065000,45065000,1,5,'Cảnh báo',1,1),(106,'Máy pha cà phê Wega Pegaso 2024 2g (Đen)',45240000,45240000,0,5,'Hết hàng',1,1),(107,'Máy pha cà phê Wega Pegaso 2g (Mới)',61500000,61500000,0,5,'Hết hàng',1,1),(108,'Máy pha cà phê Wellhome KD330 (TL)',15170000,15170000,0,5,'Hết hàng',1,1),(109,'Máy pha cà phê Wellhome KD210 mới',11700000,11700000,1,5,'Cảnh báo',1,1),(110,'Máy pha cà phê Wellhome KD210 (TL)',5930000,5930000,0,5,'Hết hàng',1,1),(111,'Máy pha cà phê Welhome KD 310 Thanh lý',11125000,11125000,1,5,'Cảnh báo',1,1),(112,'Máy Pha cà phê Wendougee Sdragon',45950000,45950000,1,5,'Cảnh báo',1,1),(113,'Máy pha trà Lacilio LT150',11000000,11000000,1,5,'Cảnh báo',1,1),(114,'Máy pha trà Gino (3g)',16155000,16155000,1,5,'Cảnh báo',1,1),(115,'Máy pha trà Gino (2g)',30000000,30000000,1,5,'Cảnh báo',1,1),(116,'Máy pha trà Gino (1g)',24000000,24000000,0,5,'Hết hàng',1,1),(117,'Máy xay cà phê Amalfi A80',6450000,6450000,3,5,'Cảnh báo',1,1),(118,'Máy xay cà phê Amalfi A80 (Trắng_TL)',5000000,5000000,0,5,'Hết hàng',1,1),(119,'Máy xay cà phê Anfim Luna',15000000,15000000,0,5,'Hết hàng',1,1),(120,'Máy xay cà phê Atom (Mới)',13430000,13430000,0,5,'Hết hàng',1,1),(121,'Máy xay cà phê Casadio',1600000,1600000,1,5,'Cảnh báo',1,1),(122,'Máy xay cà phê Casadio (Cơ)',2687750,2687750,4,5,'Cảnh báo',1,1),(123,'Máy xay cà phê Carimali (Cơ)',3000000,3000000,1,5,'Cảnh báo',1,1),(124,'Máy xay cà phê Carimali (Tự động)',3000000,3000000,1,5,'Cảnh báo',1,1),(125,'Máy xay cà phê Ceado 83',9000000,9000000,1,5,'Cảnh báo',1,1),(126,'Máy xay cà phê Ceado 75',7000000,7000000,2,5,'Cảnh báo',1,1),(127,'Máy xay cà phê Compak Cơ',2000000,2000000,2,5,'Cảnh báo',1,1),(128,'Máy xay cà phê Cunill (Cũ)',2875000,2875000,2,5,'Cảnh báo',1,1),(129,'Máy xay cà phê CRM 9015',1000000,1000000,1,5,'Cảnh báo',1,1),(130,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1),(131,'Máy xay cà phê DF64 gen 2.4 (Trắng)(Mới_VAT)',5000000,5000000,32,5,'Còn hàng',1,1),(132,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1),(133,'Máy xay cà phê DF64 gen 2.4 ( 8 Đen + 2 Trắng)',5000000,5000000,0,5,'Hết hàng',1,1),(134,'Máy xay cà phê Elektra',2060000,2060000,1,5,'Cảnh báo',1,1),(135,'Máy xay cà phê Expobar',3265000,3265000,2,5,'Cảnh báo',1,1),(136,'Máy xay cà phê Eureka firenze 75',10200000,10200000,10,5,'Còn hàng',1,1),(137,'Máy xay cà phê Eureka firenze 75 (2 Trắng + 2 Đen)',9936000,9936000,0,5,'Hết hàng',1,1),(138,'Máy xay cà phê Eureka firenze 75 (TL_Đen)',9000000,9000000,0,5,'Hết hàng',1,1),(139,'Máy xay cà phê Eureka firenze 75 (TL_Trắng)',9000000,9000000,0,5,'Hết hàng',1,1),(140,'Máy xay cà phê Eureka Firenze 65',7917000,7917000,3,5,'Cảnh báo',1,1),(141,'Máy xay cà phê Eureka Firenze 65 (2 Trắng)',7795000,7795000,0,5,'Hết hàng',1,1),(142,'Máy xay cà phê Eureka Zenith 65 cũ',4000000,4000000,1,5,'Cảnh báo',1,1),(143,'Máy xay cà phê Eureka Mignon Manuale',3700000,3700000,2,5,'Cảnh báo',1,1),(144,'Máy xay cà phê Galileo Q18 Thanh lý',6829000,6829000,6,5,'Còn hàng',1,1),(145,'Máy xay cà phê Gemilai A80 - PRO',6450000,6450000,1,5,'Cảnh báo',1,1),(146,'Máy xay cà phê Gemilai A80 - PRO (TL_Trắng)',5500000,5500000,0,5,'Hết hàng',1,1),(147,'Máy xay cà phê Gemilai A80 - PRO (TL_Đen)',5100000,5100000,0,5,'Hết hàng',1,1),(148,'Máy xay cà phê Gemilai 9012 Thanh lý',2300000,2300000,1,5,'Cảnh báo',1,1),(149,'Máy xay cà phê Gino',2167000,2167000,1,5,'Cảnh báo',1,1),(150,'Máy xay cà phê HC600 V1',2833450,2833450,4,5,'Cảnh báo',1,1),(151,'Máy xay cà phê HC600 V2 (Mới)',6500000,6500000,0,5,'Hết hàng',1,1),(152,'Máy xay cà phê HC600 V2 ̣(TL)',3500000,3500000,1,5,'Cảnh báo',1,1),(153,'Máy xay cà phê HC600 (Tự động)',2700000,2700000,1,5,'Cảnh báo',1,1),(154,'Máy xay cà phê Lingdong 020 Thanh lý',1500000,1500000,2,5,'Cảnh báo',1,1),(155,'Máy xay cà phê Lingdong 020',1993750,1993750,7,5,'Còn hàng',1,1),(156,'Máy xay cà phê Lingdong 021 Thanh lý',1545000,1545000,2,5,'Cảnh báo',1,1),(157,'Máy xay cà phê Lingdong 021',2600000,2600000,4,5,'Cảnh báo',1,1),(158,'Máy xay cà phê Lingdong 022 Thanh lý (Đen)',2000000,2000000,1,5,'Cảnh báo',1,1),(159,'Máy xay cà phê Lingdong 500N',1200000,1200000,5,5,'Cảnh báo',1,1),(160,'Máy xay cà phê Lingdong 500N (TL)',1000000,1000000,0,5,'Hết hàng',1,1),(161,'Máy xay cà phê Lingdong 900N',2122000,2122000,2,5,'Cảnh báo',1,1),(162,'Máy xay cà phê Jx600AD Mới',6750000,6750000,1,5,'Cảnh báo',1,1),(163,'Máy xay cà phê Jx600AD Mới (1 Trắng + 2 Đen)',6825000,6825000,0,5,'Hết hàng',1,1),(164,'Máy xay cà phê Jx600AD Mới (Đen)',6800000,6800000,0,5,'Hết hàng',1,1),(165,'Máy xay cà phê Jx600AD Mới (Trắng)',6800000,6800000,0,5,'Hết hàng',1,1),(166,'Máy xay cà phê Jx600AD Đen TL',5500000,5500000,0,5,'Hết hàng',1,1),(167,'Máy xay cà phê Jx650AD Thanh lý',5400000,5400000,1,5,'Cảnh báo',1,1),(168,'Máy xay cà phê Jx600AD Thanh lý',4000000,4000000,3,5,'Cảnh báo',1,1),(169,'Máy xay cà phê Jx600AD Đen Thanh lý',5000000,5000000,0,5,'Hết hàng',1,1),(170,'Máy xay cà phê Jx600AD (TL _Đen)',5000000,5000000,0,5,'Hết hàng',1,1),(171,'Máy xay cà phê Jx600AD Cơ Thanh lý',4525000,4525000,1,5,'Cảnh báo',1,1),(172,'Máy xay trà Mikita Đen (JX650)',7000000,7000000,0,5,'Hết hàng',1,1),(173,'Máy xay cà phê Fiorenzato F5 (Thanh lý)',4500000,4500000,1,5,'Cảnh báo',1,1),(174,'Máy xay cà phê Fiorenzato F64E Trắng (Mới)',16750000,16750000,1,5,'Cảnh báo',1,1),(175,'Máy xay cà phê Fiorenzato F64E 2023 Đen',12500000,12500000,0,5,'Hết hàng',1,1),(176,'Máy xay cà phê Fiorenzato F64E Đen',12000000,12000000,0,5,'Hết hàng',1,1),(177,'Máy xay cà phê Fiorenzato F64E 2023 Trắng',10500000,10500000,1,5,'Cảnh báo',1,1),(178,'Máy xay cà phê Fiorenzato F64E 2022 Trắng (Nứt màn hình)',8000000,8000000,1,5,'Cảnh báo',1,1),(179,'Máy xay cà phê Fiorenzato F64E (Màn nhỏ sọc màn)',5070000,5070000,0,5,'Hết hàng',1,1),(180,'Máy xay cà phê Fiorenzato F83 (Màn Nhỏ- TL)',12500000,12500000,1,5,'Cảnh báo',1,1),(181,'Máy xay cà phê Feama (Tự động)',4030000,4030000,2,5,'Cảnh báo',1,1),(182,'Máy xay cà phê Feama (Cơ)',2300000,2300000,1,5,'Cảnh báo',1,1),(183,'Máy xay cà phê Mahlkonig X54 (TL)',8036000,8036000,0,5,'Hết hàng',1,1),(184,'Máy xay cà phê Mazzer Cơ',2800000,2800000,1,5,'Cảnh báo',1,1),(185,'Máy xay cà phê Mazzer Super Joylly Cơ (Xám)',3000000,3000000,1,5,'Cảnh báo',1,1),(186,'Máy xay cà phê Mazzer tự động mini ( thanh lý)',7500000,7500000,1,5,'Cảnh báo',1,1),(187,'Máy xay cà phê Mazzer Major (Trắng)',16600000,16600000,1,5,'Cảnh báo',1,1),(188,'Máy xay cà phê Mazzer Luigi Spa (Thanh lý)',10500000,10500000,1,5,'Cảnh báo',1,1),(189,'Máy xay cà phê Mazzer Super Joylly',9125000,9125000,2,5,'Cảnh báo',1,1),(190,'Máy xay cà phê Macap (Cơ )',3000000,3000000,0,5,'Hết hàng',1,1),(191,'Máy xay cà phê Macap (Bán tự động)',4050000,4050000,1,5,'Cảnh báo',1,1),(192,'Máy xay cà phê Macap (Tự động )',6000000,6000000,1,5,'Cảnh báo',1,1),(193,'Máy xay cà phê Melalife C3 tự động',3500000,3500000,4,5,'Cảnh báo',1,1),(194,'Máy xay cà phê Niche Zero',11500000,11500000,0,5,'Hết hàng',1,1),(195,'Máy xay cà phê Nouva Simonelli MDXS Đen',7000000,7000000,1,5,'Cảnh báo',1,1),(196,'Máy xay cà phê Nouva MDX',3500000,3500000,1,5,'Cảnh báo',1,1),(197,'Máy xay cà phê Nouva MDX (Tự động)',4000000,4000000,0,5,'Hết hàng',1,1),(198,'Máy xay cà phê Nouva simonelli MDX',5000000,5000000,1,5,'Cảnh báo',1,1),(199,'Máy xay cà phê Otto',5000000,5000000,0,5,'Hết hàng',1,1),(200,'Máy xay cà phê Promix-600AD',6300000,6300000,0,5,'Hết hàng',1,1),(201,'Máy xay cà phê Simonelli',5355000,5355000,1,5,'Cảnh báo',1,1),(202,'Máy xay cà phê Rancilio',4000000,4000000,1,5,'Cảnh báo',1,1),(203,'Máy xay cà phê Robust',4000000,4000000,4,5,'Cảnh báo',1,1),(204,'Máy xay cà phê pha phin Tiamo',1200000,1200000,1,5,'Cảnh báo',1,1),(205,'Máy xay trà Promix',1000000,1000000,1,5,'Cảnh báo',1,1),(206,'Máy ép cam UB 160',680000,680000,2,5,'Cảnh báo',1,1),(207,'Máy ép nhanh SS - 83',2600000,2600000,1,5,'Cảnh báo',1,1),(208,'Máy nén cà phê Eureka Disko',7400000,7400000,3,5,'Cảnh báo',1,1),(209,'Máy xay UB - 712 plus',2400000,2400000,1,5,'Cảnh báo',1,1),(210,'Máy xay UB - 712',2200000,2200000,1,5,'Cảnh báo',1,1),(211,'Máy POS tính tiền',2000000,2000000,1,5,'Cảnh báo',1,1),(212,'Tay pha Carimali',1500000,1500000,1,5,'Cảnh báo',1,1),(213,'Tay pha không đáy',167000,167000,6,5,'Còn hàng',1,1),(214,'Bàn mát',5000000,5000000,1,5,'Cảnh báo',1,3),(215,'Bộ bàn ghế',1200000,1200000,1,5,'Cảnh báo',1,3),(216,'Bàn phím Nouva life',201500,201500,29,5,'Còn hàng',2,1),(217,'Bo bàn phím Nouva life',150000,150000,2,5,'Cảnh báo',2,1),(218,'Bột vệ sinh máy pha (nhỏ)',28000,28000,74,5,'Còn hàng',2,1),(219,'Hộp đập bã chữ nhật',455000,455000,0,5,'Hết hàng',2,1),(220,'Hộp đập bã',92000,92000,0,5,'Hết hàng',2,1),(221,'Hộp đập bã 15cm',80000,80000,33,5,'Còn hàng',2,1),(222,'Đế cao su',23000,23000,24,5,'Còn hàng',2,1),(223,'Chổi vệ sinh máy pha',22000,22000,46,5,'Còn hàng',2,1),(224,'Chổi vệ sinh tay pha',13200,13200,49,5,'Còn hàng',2,1),(225,'Temper trợ lực',146000,146000,0,5,'Hết hàng',2,1),(226,'Temper',70300,70300,54,5,'Còn hàng',2,1),(227,'OCD san',150625,150625,3,5,'Cảnh báo',2,1),(228,'Phin mù cao su',12000,12000,10,5,'Còn hàng',2,1),(229,'Phin mù inox',23000,23000,4,5,'Cảnh báo',2,1),(230,'Cà phê Arabica',265000,265000,5,5,'Cảnh báo',2,4),(231,'Cà phê hạt',175000,175000,0,5,'Hết hàng',2,4),(232,'Cà phê Blend 9/1',178000,178000,53,5,'Còn hàng',2,4),(233,'Cà phê Robusta',153000,153000,36,5,'Còn hàng',2,4),(234,'Lọc nước 3 lõi',264000,264000,5,5,'Cảnh báo',2,1),(235,'Ca đánh sữa',55000,55000,0,5,'Hết hàng',2,1),(236,'Ca đánh sữa (300ml)',50000,50000,4,5,'Cảnh báo',2,1),(237,'Ca đánh sữa (500ml)',64000,64000,7,5,'Còn hàng',2,1),(238,'Ca đong inox',27000,27000,16,5,'Còn hàng',2,1),(239,'Filter Đôi',47000,47000,18,5,'Còn hàng',2,1),(240,'Filter Đơn',42000,42000,19,5,'Còn hàng',2,1),(241,'Đồng hồ 2 kim',380000,380000,0,5,'Hết hàng',2,1),(242,'Gioăng Silicon 58',51500,51500,97,5,'Còn hàng',2,1),(243,'Gioăng cao su',30000,30000,78,5,'Còn hàng',2,1),(244,'Gioăng cao su Gemilai',70000,70000,6,5,'Còn hàng',2,1),(245,'Gioăng cao su nouva',52500,52500,98,5,'Còn hàng',2,1),(246,'Gioăng Carimali',80000,80000,9,5,'Còn hàng',2,1),(247,'Giỏ lọc Carimali',169000,169000,1,5,'Cảnh báo',2,1),(248,'Lưỡi dao S42 titan đen',440000,440000,0,5,'Hết hàng',2,1),(249,'Lưỡi máy pha Breville',260000,260000,1,5,'Cảnh báo',2,1),(250,'Lưỡi họng Lamvita',100000,100000,1,5,'Cảnh báo',2,1),(251,'Lưỡi máy xay 64mm',600000,600000,1,5,'Cảnh báo',2,1),(252,'Máy đánh bọt DC - 08',71500,71500,1,5,'Cảnh báo',2,1),(253,'Máy đánh bọt DC - 201',195000,195000,1,5,'Cảnh báo',2,1),(254,'Nắp máy ép nhanh Uniblend SS-33',180000,180000,0,5,'Hết hàng',2,1),(255,'Phểu máy xay LD022',400000,400000,1,5,'Cảnh báo',2,1),(256,'Tấm Micra máy Astoria',640000,640000,1,5,'Cảnh báo',2,1),(257,'Thanh đun Casadio',530000,530000,2,5,'Cảnh báo',2,1),(258,'Thùng gỗ',350000,350000,7,5,'Còn hàng',2,1),(259,'Vòi Đơn',70000,70000,9,5,'Còn hàng',2,1),(260,'Vòng nhôm chống tràn',85000,85000,1,5,'Cảnh báo',2,1);
/*!40000 ALTER TABLE `san_pham` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_san_pham_before_insert` BEFORE INSERT ON `san_pham` FOR EACH ROW BEGIN
    IF NEW.so_luong_ton = 0 THEN
        SET NEW.trang_thai = 'Hết hàng';
    ELSEIF NEW.so_luong_ton <= NEW.canh_bao_ton_kho THEN
        SET NEW.trang_thai = 'Cảnh báo';
    ELSE
        SET NEW.trang_thai = 'Còn hàng';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_san_pham_before_update` BEFORE UPDATE ON `san_pham` FOR EACH ROW BEGIN
    IF NEW.so_luong_ton = 0 THEN
        SET NEW.trang_thai = 'Hết hàng';
    ELSEIF NEW.so_luong_ton <= NEW.canh_bao_ton_kho THEN
        SET NEW.trang_thai = 'Cảnh báo';
    ELSE
        SET NEW.trang_thai = 'Còn hàng';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `store_config`
--

DROP TABLE IF EXISTS `store_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_config` (
  `id` int NOT NULL,
  `shop_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `shop_name_pnh` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `shop_addr` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `shop_tel` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `shop_bank` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `shop_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `shop_policy` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `shop_warranty` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `shop_warranty_limit` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_config`
--

LOCK TABLES `store_config` WRITE;
/*!40000 ALTER TABLE `store_config` DISABLE KEYS */;
INSERT INTO `store_config` VALUES (1,'JENKA COFFEE SHOP','Jenka Coffee Shop','Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM','Điện thoại: 0817909090 - 0827909090','Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công','   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\n   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\n   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.','Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\n - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\n - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.','- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\n- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...','- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới.');
/*!40000 ALTER TABLE `store_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thu_chi`
--

DROP TABLE IF EXISTS `thu_chi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thu_chi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_loai` int DEFAULT NULL,
  `tien_thu` decimal(15,0) DEFAULT NULL COMMENT 'Số tiền thu — NULL nếu không có khoản thu',
  `tien_chi` decimal(15,0) DEFAULT NULL COMMENT 'Số tiền chi — NULL nếu không có khoản chi',
  `mo_ta` text COLLATE utf8mb4_general_ci,
  `id_nhan_vien` int DEFAULT NULL,
  `id_ban_hang` int DEFAULT NULL COMMENT 'FK den ban_hang de auto link',
  `id_nhap_hang` int DEFAULT NULL COMMENT 'FK den nhap_hang de auto link',
  PRIMARY KEY (`id`),
  KEY `id_loai` (`id_loai`),
  KEY `id_nhan_vien` (`id_nhan_vien`),
  KEY `id_ban_hang` (`id_ban_hang`),
  KEY `id_nhap_hang` (`id_nhap_hang`),
  CONSTRAINT `fk_thu_chi_ban_hang` FOREIGN KEY (`id_ban_hang`) REFERENCES `ban_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_thu_chi_nhap_hang` FOREIGN KEY (`id_nhap_hang`) REFERENCES `nhap_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `thu_chi_ibfk_1` FOREIGN KEY (`id_loai`) REFERENCES `loai_thu_chi` (`id`) ON DELETE SET NULL,
  CONSTRAINT `thu_chi_ibfk_2` FOREIGN KEY (`id_nhan_vien`) REFERENCES `nhan_vien` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thu_chi`
--

LOCK TABLES `thu_chi` WRITE;
/*!40000 ALTER TABLE `thu_chi` DISABLE KEYS */;
/*!40000 ALTER TABLE `thu_chi` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-26 14:58:54

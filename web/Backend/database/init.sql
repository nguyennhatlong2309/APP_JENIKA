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
  `trang_thai` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Hẹn' COMMENT 'Trạng thái: Hoàn thành | Hẹn | Hủy',
  `ghi_chu` text COLLATE utf8mb4_general_ci,
  `anh_hoa_don_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `don_vi` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `don_vi` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `ten_danh_muc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `ten` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Tên đối tác',
  `sdt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `dia_chi` text COLLATE utf8mb4_general_ci,
  `email` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `ten_don_vi` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `don_vi_tinh`
--

LOCK TABLES `don_vi_tinh` WRITE;
/*!40000 ALTER TABLE `don_vi_tinh` DISABLE KEYS */;
INSERT INTO `don_vi_tinh` VALUES (1,'cái'),(2,'kg'),(3,'bộ'),(4,'kg');
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
  `ten` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
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
  `ten_nhan_vien` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `sdt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `vai_tro` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhan_vien`
--

LOCK TABLES `nhan_vien` WRITE;
/*!40000 ALTER TABLE `nhan_vien` DISABLE KEYS */;
INSERT INTO `nhan_vien` VALUES (1,'Hiện',NULL,NULL,NULL),(2,'Băng',NULL,NULL,NULL),(3,'Vân',NULL,NULL,NULL),(4,'Huy',NULL,NULL,NULL),(5,'Triệu',NULL,NULL,NULL);
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
  `trang_thai` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'Chờ nhận',
  `ngay_nhan` date DEFAULT NULL COMMENT 'Ngày nhận hàng thực tế',
  `id_doi_tac` int DEFAULT NULL COMMENT 'FK → doi_tac (nhà cung cấp của đơn nhập)',
  `id_nhan_vien` int DEFAULT NULL,
  `ghi_chu` text COLLATE utf8mb4_general_ci,
  `anh_hoa_don_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
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
  `thao_tac` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'THEM | SUA | XOA',
  `tab` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ban_hang | nhap_hang | thu_chi',
  `ma_ban_ghi` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'VD: BH-12, NH-5, TC-3',
  `mo_ta` text COLLATE utf8mb4_unicode_ci,
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
-- Table structure for table `nhom_san_pham`
--

DROP TABLE IF EXISTS `nhom_san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nhom_san_pham` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_nhom` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nhom_san_pham`
--

LOCK TABLES `nhom_san_pham` WRITE;
/*!40000 ALTER TABLE `nhom_san_pham` DISABLE KEYS */;
INSERT INTO `nhom_san_pham` VALUES (1,'Xe Ô Tô Tải'),(2,'Máy đo định lượng đường UB-18'),(3,'Máy đo định lượng đường sữa UBS-26'),(4,'Máy làm đá'),(5,'Máy pha cà phê Astoria Tanya'),(6,'Máy pha cà phê Nuova Simonelli Appia'),(7,'Máy pha cà phê Breville'),(8,'Máy pha cà phê Brewico'),(9,'Máy pha cà phê Bezzera'),(10,'Máy pha cà phê BFC'),(11,'Máy pha cà phê Carimali'),(12,'Máy pha cà phê Casadio'),(13,'Máy pha cà phê Caso'),(14,'Máy pha cà phê Cime Co-03'),(15,'Máy pha cà phê CRM 3005'),(16,'Máy pha cà phê Expobar'),(17,'Máy pha cà phê Faema'),(18,'Máy pha cà phê Gaggia Classic'),(19,'Máy pha cà phê Gemilai'),(20,'Máy pha cà phê La Marzocco KB90'),(21,'Máy pha cà phê LaCarimali'),(22,'Máy pha cà phê Lacimbali'),(23,'Máy pha cà phê La Nouva Era'),(24,'Máy pha cà phê Lelit'),(25,'Máy pha cà phê Milesto'),(26,'Máy pha cà phê Iberital IB7'),(27,'Máy pha cà phê Rancilio'),(28,'Máy pha cà phê Sanremo Zoe'),(29,'Máy pha cà phê Wega'),(30,'Máy pha cà phê Wellhome'),(31,'Máy Pha cà phê Wendougee Sdragon'),(32,'Máy pha trà Gino'),(33,'Máy pha trà Lacilio LT150'),(34,'Máy xay cà phê Amalfi A80'),(35,'Máy xay cà phê Anfim Luna'),(36,'Máy xay cà phê Atom'),(37,'Máy xay cà phê Casadio'),(38,'Máy xay cà phê Carimali'),(39,'Máy xay cà phê Ceado'),(40,'Máy xay cà phê Compak'),(41,'Máy xay cà phê Cunill'),(42,'Máy xay cà phê CRM 9015'),(43,'Máy xay cà phê DF64 gen 2.4'),(44,'Máy xay cà phê Elektra'),(45,'Máy xay cà phê Expobar'),(46,'Máy xay cà phê Eureka Firenze'),(47,'Máy xay cà phê Eureka'),(48,'Máy xay cà phê Galileo'),(49,'Máy xay cà phê Gemilai'),(50,'Máy xay cà phê Gino'),(51,'Máy xay cà phê HC600'),(52,'Máy xay cà phê Lingdong'),(53,'Máy xay cà phê Jx600AD'),(54,'Máy xay cà phê Jx650AD'),(55,'Máy xay trà Mikita'),(56,'Máy xay cà phê Fiorenzato'),(57,'Máy xay cà phê Feama'),(58,'Máy xay cà phê Mahlkonig X54'),(59,'Máy xay cà phê Mazzer'),(60,'Máy xay cà phê Macap'),(61,'Máy xay cà phê Melalife C3'),(62,'Máy xay cà phê Niche Zero'),(63,'Máy xay cà phê Nouva Simonelli MDX'),(64,'Máy xay cà phê Otto'),(65,'Máy xay cà phê Promix-600AD'),(66,'Máy xay cà phê Simonelli'),(67,'Máy xay cà phê Rancilio'),(68,'Máy xay cà phê Robust'),(69,'Máy xay cà phê pha phin Tiamo'),(70,'Máy xay trà Promix'),(71,'Máy ép cam UB 160'),(72,'Máy ép nhanh SS-83'),(73,'Máy nén cà phê Eureka Disko'),(74,'Máy xay UB-712 plus'),(75,'Máy xay UB-712'),(76,'Máy POS tính tiền'),(77,'Tay pha Carimali'),(78,'Tay pha không đáy'),(79,'Bàn mát'),(80,'Bộ bàn ghế'),(81,'Bàn phím Nouva life'),(82,'Bo bàn phím Nouva life'),(83,'Bột vệ sinh máy pha (nhỏ)'),(84,'Hộp đập bã'),(85,'Hộp đập bã 15cm'),(86,'Đế cao su'),(87,'Chổi vệ sinh máy pha'),(88,'Chổi vệ sinh tay pha'),(89,'Temper trợ lực'),(90,'Temper'),(91,'OCD san'),(92,'Phin mù cao su'),(93,'Phin mù inox'),(94,'Cà phê Arabica'),(95,'Cà phê hạt'),(96,'Cà phê Blend 9/1'),(97,'Cà phê Robusta'),(98,'Lọc nước 3 lõi'),(99,'Ca đánh sữa'),(100,'Ca đánh sữa 300ml'),(101,'Ca đánh sữa 500ml'),(102,'Ca đong inox'),(103,'Filter Đôi'),(104,'Filter Đơn'),(105,'Đế cối inox UB-712'),(106,'Đồng hồ 2 kim'),(107,'Gioăng Silicon 58'),(108,'Gioăng cao su'),(109,'Gioăng cao su Gemilai'),(110,'Gioăng cao su Nouva'),(111,'Gioăng Carimali'),(112,'Giỏ lọc Carimali'),(113,'Lưỡi dao S42 titan đen'),(114,'Lưỡi máy pha Breville'),(115,'Lưỡi họng Lamvita'),(116,'Lưỡi máy xay 64mm'),(117,'Máy đánh bọt DC-08'),(118,'Máy đánh bọt DC-201'),(119,'Nắp máy ép nhanh Uniblend SS-33'),(120,'Núm truyền động socket UB-712'),(121,'Phểu máy xay LD022'),(122,'Tấm Micra máy Astoria'),(123,'Thanh đun Casadio'),(124,'Thùng gỗ'),(125,'Vòi Đơn'),(126,'Vòng nhôm chống tràn');
/*!40000 ALTER TABLE `nhom_san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham`
--

DROP TABLE IF EXISTS `san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_san_pham` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `gia_nhap_hien_tai` decimal(15,0) DEFAULT '0',
  `gia_ban_hien_tai` decimal(15,0) DEFAULT '0',
  `so_luong_ton` int DEFAULT '0',
  `canh_bao_ton_kho` int NOT NULL DEFAULT '5' COMMENT 'Cảnh báo khi số lượng tồn chạm hoặc thấp hơn ngưỡng này',
  `trang_thai` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Còn hàng' COMMENT 'Trạng thái tồn kho: Còn hàng | Cảnh báo | Hết hàng',
  `id_danh_muc` int DEFAULT NULL,
  `id_don_vi` int DEFAULT NULL,
  `id_nhom` int DEFAULT NULL,
  `bi_xoa` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 = hiển thị, 1 = ẩn/xóa',
  `ghi_chu` text COLLATE utf8mb4_general_ci,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ten_gia` (`ten_san_pham`,`gia_nhap_hien_tai`),
  KEY `id_danh_muc` (`id_danh_muc`),
  KEY `id_don_vi` (`id_don_vi`),
  KEY `id_nhom` (`id_nhom`),
  CONSTRAINT `san_pham_ibfk_1` FOREIGN KEY (`id_danh_muc`) REFERENCES `danh_muc` (`id`) ON DELETE SET NULL,
  CONSTRAINT `san_pham_ibfk_2` FOREIGN KEY (`id_don_vi`) REFERENCES `don_vi_tinh` (`id`) ON DELETE SET NULL,
  CONSTRAINT `san_pham_ibfk_3` FOREIGN KEY (`id_nhom`) REFERENCES `nhom_san_pham` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=589 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham`
--

LOCK TABLES `san_pham` WRITE;
/*!40000 ALTER TABLE `san_pham` DISABLE KEYS */;
INSERT IGNORE INTO `san_pham` VALUES (1,'Xe Ô Tô Tải Van 945kg',233000000,233000000,1,5,'Cảnh báo',1,1,1,0,NULL),(2,'Máy đo định lượng đường UB - 18',1157000,1157000,1,5,'Cảnh báo',1,1,2,0,NULL),(3,'Máy đo định lượng đường sữa 2 ngăn UBS - 26 (Trắng)',4150000,4150000,0,5,'Hết hàng',1,1,3,0,NULL),(4,'Máy làm đá',5100000,5100000,1,5,'Cảnh báo',1,1,4,0,NULL),(5,'Máy pha cà phê Astoria Tanya 2018',29150000,29150000,0,5,'Hết hàng',1,1,5,0,NULL),(6,'Máy pha cà phê Astoria Tanya 2018',30300000,30300000,0,5,'Hết hàng',1,1,5,0,NULL),(7,'Máy pha cà phê Astoria Tanya 2021',40500000,40500000,0,5,'Hết hàng',1,1,5,0,NULL),(8,'Máy pha cà phê Astoria Tanya 2021',38000000,38000000,0,5,'Hết hàng',1,1,5,0,NULL),(9,'Máy pha cà phê Astoria Tanya 2021',41000000,41000000,0,5,'Hết hàng',1,1,5,0,NULL),(10,'Máy pha cà phê Astoria Tanya 2022',40630000,40630000,0,5,'Hết hàng',1,1,5,0,NULL),(11,'Máy pha cà phê Astoria Tanya 2022',41000000,41000000,0,5,'Hết hàng',1,1,5,0,NULL),(12,'Máy pha cà phê Astoria Tanya 2022',41000000,41000000,0,5,'Hết hàng',1,1,5,0,NULL),(13,'Máy pha cà phê Astoria Tanya 2022',41000000,41000000,0,5,'Hết hàng',1,1,5,0,NULL),(14,'Máy pha cà phê Astoria Tanya 2025',45106000,45106000,0,5,'Hết hàng',1,1,5,0,NULL),(15,'Máy pha cà phê Nuova Simonelli Appia Life 2022 Trắng 2g',42000000,42000000,0,5,'Hết hàng',1,1,6,0,NULL),(16,'Máy pha cà phê Nuova Simonelli Appia Life 2020 Đen 2g',38500000,38500000,1,5,'Cảnh báo',1,1,6,0,NULL),(17,'Máy pha cà phê Nuova Simonelli Appia Life 2019 Trắng 2g',38400000,38400000,1,5,'Cảnh báo',1,1,6,0,NULL),(18,'Máy pha cà phê Nuova Simonelli Appia II 2014 Trắng 2g',23000000,23000000,0,5,'Hết hàng',1,1,6,0,NULL),(19,'Máy pha cà phê Nuova Simonelli Appia II 2014 2g',22000000,22000000,1,5,'Cảnh báo',1,1,6,0,NULL),(20,'Máy pha cà phê Nuova Simonelli Appia II 2016 Trắng 2g',28330000,28330000,0,5,'Hết hàng',1,1,6,0,NULL),(21,'Máy pha cà phê Nuova Simonelli Appia II 2017 2g Đen',25350000,25350000,1,5,'Cảnh báo',1,1,6,0,NULL),(22,'Máy pha cà phê Nuova Simonelli Appia II 2020 2g Trắng',35000000,35000000,0,5,'Hết hàng',1,1,6,0,NULL),(23,'Máy pha cà phê Breville 870 Mới',12500000,12500000,1,5,'Cảnh báo',1,1,7,0,NULL),(24,'Máy pha cà phê Breville 870 Thanh lý',6893750,6893750,2,5,'Cảnh báo',1,1,7,0,NULL),(25,'Máy pha cà phê Breville 878 Mới',17200000,17200000,0,5,'Hết hàng',1,1,7,0,NULL),(26,'Máy pha cà phê Breville 878 Thanh lý',8500000,8500000,1,5,'Cảnh báo',1,1,7,0,NULL),(27,'Máy pha cà phê Breville 878 Thanh lý',9500000,9500000,1,5,'Cảnh báo',1,1,7,0,NULL),(28,'Máy pha cà phê Breville 878 Thanh lý',9500000,9500000,0,5,'Hết hàng',1,1,7,0,NULL),(29,'Máy pha cà phê Brewico',17325000,17325000,2,5,'Cảnh báo',1,1,8,0,NULL),(30,'Máy pha cà phê Bezzera 2018',30000000,30000000,1,5,'Cảnh báo',1,1,9,0,NULL),(31,'Máy pha cà phê Bezzera 2019',35160500,35160500,1,5,'Cảnh báo',1,1,9,0,NULL),(32,'Máy pha cà phê Bezzera 2019',35266500,35266500,1,5,'Cảnh báo',1,1,9,0,NULL),(33,'Máy pha cà phê Bezzera 2021',36500000,36500000,1,5,'Cảnh báo',1,1,9,0,NULL),(34,'Máy pha cà phê Bezzera 2021',20340000,20340000,0,5,'Hết hàng',1,1,9,0,NULL),(35,'Máy pha cà phê Bezzera 2022',33000000,33000000,1,5,'Cảnh báo',1,1,9,0,NULL),(36,'Máy pha cà phê BFC Delux 2g 2019',39500000,39500000,1,5,'Cảnh báo',1,1,10,0,NULL),(37,'Máy pha cà phê BFC Delux 2g 2018',35420000,35420000,0,5,'Hết hàng',1,1,10,0,NULL),(38,'Máy pha cà phê BFC Delux 2g 2018',36000000,36000000,0,5,'Hết hàng',1,1,10,0,NULL),(39,'Máy pha cà phê BFC Delux 2g 2015',36000000,36000000,0,5,'Hết hàng',1,1,10,0,NULL),(40,'Máy pha cà phê BFC Delux 2g 2012',31470000,31470000,0,5,'Hết hàng',1,1,10,0,NULL),(41,'Máy pha cà phê BFC Delux 1g 2020',32000000,32000000,0,5,'Hết hàng',1,1,10,0,NULL),(42,'Máy pha cà phê BFC Lira Đỏ',28250000,28250000,1,5,'Cảnh báo',1,1,10,0,NULL),(43,'Máy pha cà phê BFC Monzza 2g 2013',29000000,29000000,0,5,'Hết hàng',1,1,10,0,NULL),(44,'Máy pha cà phê BFC Monzza 2g 2015',35138000,35138000,0,5,'Hết hàng',1,1,10,0,NULL),(45,'Máy pha cà phê BFC Monzza 2g 2017',36000000,36000000,0,5,'Hết hàng',1,1,10,0,NULL),(46,'Máy pha cà phê BFC Nuova Monza 2g 2019',52000000,52000000,0,5,'Hết hàng',1,1,10,0,NULL),(47,'Máy pha cà phê BFC Vallelunga 2g 2018',35400000,35400000,1,5,'Cảnh báo',1,1,10,0,NULL),(48,'Máy pha cà phê Carimali 1g Đỏ 2020',20000000,20000000,1,5,'Cảnh báo',1,1,11,0,NULL),(49,'Máy pha cà phê Carimali CM400',12000000,12000000,0,5,'Hết hàng',1,1,11,0,NULL),(50,'Máy pha cà phê Carimali CM400',12000000,12000000,0,5,'Hết hàng',1,1,11,0,NULL),(51,'Máy pha cà phê Casadio Dieci 1g 2014',18500000,18500000,1,5,'Cảnh báo',1,1,12,0,NULL),(52,'Máy pha cà phê Casadio Dieci 1g 2016',19700000,19700000,0,5,'Hết hàng',1,1,12,0,NULL),(53,'Máy pha cà phê Casadio 1g 2020',28000000,28000000,1,5,'Cảnh báo',1,1,12,0,NULL),(54,'Máy pha cà phê Casadio 1g 2020',27000000,27000000,0,5,'Hết hàng',1,1,12,0,NULL),(55,'Máy pha cà phê Casadio 1g 2020',26000000,26000000,0,5,'Hết hàng',1,1,12,0,NULL),(56,'Máy pha cà phê Casadio 1g 2021',28050000,28050000,0,5,'Hết hàng',1,1,12,0,NULL),(57,'Máy pha cà phê Casadio 1g 2021',28000000,28000000,0,5,'Hết hàng',1,1,12,0,NULL),(58,'Máy pha cà phê Casadio 1g 2021',30000000,30000000,0,5,'Hết hàng',1,1,12,0,NULL),(59,'Máy pha cà phê Casadio 1g 2022',29130000,29130000,0,5,'Hết hàng',1,1,12,0,NULL),(60,'Máy pha cà phê Casadio 1g 2023',32150000,32150000,1,5,'Cảnh báo',1,1,12,0,NULL),(61,'Máy pha cà phê Casadio 1g 2023',30500000,30500000,1,5,'Cảnh báo',1,1,12,0,NULL),(62,'Máy pha cà phê Casadio 1g 2023',32000000,32000000,0,5,'Hết hàng',1,1,12,0,NULL),(63,'Máy pha cà phê Casadio 1g 2024',30000000,30000000,0,5,'Hết hàng',1,1,12,0,NULL),(64,'Máy pha cà phê Casadio 1g 2024',30000000,30000000,0,5,'Hết hàng',1,1,12,0,NULL),(65,'Máy pha cà phê Casadio 1g 2024',32153000,32153000,0,5,'Hết hàng',1,1,12,0,NULL),(66,'Máy pha cà phê Casadio 1g 2024',32000000,32000000,0,5,'Hết hàng',1,1,12,0,NULL),(67,'Máy pha cà phê Casadio Nettuno 1g Mới',40000000,40000000,3,5,'Cảnh báo',1,1,12,0,NULL),(68,'Máy pha cà phê Casadio Nettuno 1g Mới',40000000,40000000,0,5,'Hết hàng',1,1,12,0,NULL),(69,'Máy pha cà phê Casadio Nettuno (TL)',35000000,35000000,0,5,'Hết hàng',1,1,12,0,NULL),(70,'Máy pha cà phê Casadio Nettuno (TL)',34300000,34300000,0,5,'Hết hàng',1,1,12,0,NULL),(71,'Máy pha cà phê Caso',2060000,2060000,3,5,'Cảnh báo',1,1,13,0,NULL),(72,'Máy pha cà phê Cime Co - 03 2g 2022',39500000,39500000,0,5,'Hết hàng',1,1,14,0,NULL),(73,'Máy pha cà phê Cime Co - 03 2g 2022',39520000,39520000,0,5,'Hết hàng',1,1,14,0,NULL),(74,'Máy pha cà phê Cime Co - 03 2g 2021',38000000,38000000,0,5,'Hết hàng',1,1,14,0,NULL),(75,'Máy pha cà phê Cime Co - 03 2g 2021',38000000,38000000,0,5,'Hết hàng',1,1,14,0,NULL),(76,'Máy pha cà phê Cime Co - 03 2g 2021',38470000,38470000,0,5,'Hết hàng',1,1,14,0,NULL),(77,'Máy pha cà phê Cime Co - 03 2g 2019',31000000,31000000,2,5,'Cảnh báo',1,1,14,0,NULL),(78,'Máy pha cà phê CRM 3005E (TL)',2500000,2500000,0,5,'Hết hàng',1,1,15,0,NULL),(79,'Máy pha cà phê CRM 3005L (VAT)',4018500,4018500,6,5,'Còn hàng',1,1,15,0,NULL),(80,'Máy pha cà phê CRM 3005L (VAT)',4018500,4018500,0,5,'Hết hàng',1,1,15,0,NULL),(81,'Máy pha cà phê CRM 3005L (VAT)',4500000,4500000,0,5,'Hết hàng',1,1,15,0,NULL),(82,'Máy pha cà phê Expobar 1g 2015 (Đen)',20000000,20000000,1,5,'Cảnh báo',1,1,16,0,NULL),(83,'Máy pha cà phê Faema E61 2018 1g',50000000,50000000,0,5,'Hết hàng',1,1,17,0,NULL),(84,'Máy pha cà phê Faema E61 2014 2g',47000000,47000000,0,5,'Hết hàng',1,1,17,0,NULL),(85,'Máy pha cà phê Faema E61 2016 2g',51000000,51000000,0,5,'Hết hàng',1,1,17,0,NULL),(86,'Máy pha cà phê Faema E61 2017 2g',52500000,52500000,0,5,'Hết hàng',1,1,17,0,NULL),(87,'Máy pha cà phê Faema E61 2017 2g',52500000,52500000,0,5,'Hết hàng',1,1,17,0,NULL),(88,'Máy pha cà phê Faema E98up (Mới)',61500000,61500000,0,5,'Hết hàng',1,1,17,0,NULL),(89,'Máy pha cà phê Faema E98up 2021 (Đen)',40000000,40000000,0,5,'Hết hàng',1,1,17,0,NULL),(90,'Máy pha cà phê Faema E98up 2020 (Trắng)',40500000,40500000,0,5,'Hết hàng',1,1,17,0,NULL),(91,'Máy pha cà phê Faema E98up 2019',40300000,40300000,1,5,'Cảnh báo',1,1,17,0,NULL),(92,'Máy pha cà phê Faema E98 RE 2016',23508000,23508000,0,5,'Hết hàng',1,1,17,0,NULL),(93,'Máy pha cà phê Faema E98 RE 2016',20000000,20000000,0,5,'Hết hàng',1,1,17,0,NULL),(94,'Máy pha cà phê Faema E98 RE 2015 (Trắng)',22610000,22610000,1,5,'Cảnh báo',1,1,17,0,NULL),(95,'Máy pha cà phê Faema E98 RE 2017 (Trắng)',24200000,24200000,0,5,'Hết hàng',1,1,17,0,NULL),(96,'Máy pha cà phê Faema E98 RE 2017 (Đen)',24270000,24270000,0,5,'Hết hàng',1,1,17,0,NULL),(97,'Máy pha cà phê Gaggia Classic (Thanh Lý)',8025000,8025000,2,5,'Cảnh báo',1,1,18,0,NULL),(98,'Máy pha cà phê Gaggia Classic E24 Đỏ (Mới)',14850000,14850000,0,5,'Hết hàng',1,1,18,0,NULL),(99,'Máy pha cà phê Gaggia Classic E24 (Xám_Mới)',15010300,15010300,4,5,'Cảnh báo',1,1,18,0,NULL),(100,'Máy pha cà phê Gemilai 3149',14005000,14005000,0,5,'Hết hàng',1,1,19,0,NULL),(101,'Máy pha cà phê Gemilai 3149',13900000,13900000,0,5,'Hết hàng',1,1,19,0,NULL),(102,'Máy pha cà phê Gemilai 3149 (Đen)',13950000,13950000,8,5,'Còn hàng',1,1,19,0,NULL),(103,'Máy pha cà phê Gemilai 3149 (Đen)',13950000,13950000,0,5,'Hết hàng',1,1,19,0,NULL),(104,'Máy pha cà phê Gemilai 3149 (Trắng)',13950000,13950000,0,5,'Hết hàng',1,1,19,0,NULL),(105,'Máy pha cà phê Gemilai 3149',13950000,13950000,0,5,'Hết hàng',1,1,19,0,NULL),(106,'Máy pha cà phê Gemilai 3149 (Đen)',13950000,13950000,0,5,'Hết hàng',1,1,19,0,NULL),(107,'Máy pha cà phê Gemilai 3149 (Trắng)',13950000,13950000,0,5,'Hết hàng',1,1,19,0,NULL),(108,'Máy pha cà phê Gemilai 3149 (TL_Trắng)',10500000,10500000,0,5,'Hết hàng',1,1,19,0,NULL),(109,'Máy pha cà phê Gemilai 3200B Pro',8600000,8600000,7,5,'Còn hàng',1,1,19,0,NULL),(110,'Máy pha cà phê Gemilai 3200B Pro',8600000,8600000,0,5,'Hết hàng',1,1,19,0,NULL),(111,'Máy pha cà phê Gemilai 3200B Pro',8600000,8600000,0,5,'Hết hàng',1,1,19,0,NULL),(112,'Máy pha cà phê Gemilai 3200B Pro (TL_Đen)',7357000,7357000,0,5,'Hết hàng',1,1,19,0,NULL),(113,'Máy pha cà phê Gemilai 3200B Pro (TL_Đen)',7357000,7357000,0,5,'Hết hàng',1,1,19,0,NULL),(114,'Máy pha cà phê Gemilai 3200B Pro (TL_Đen)',7357000,7357000,0,5,'Hết hàng',1,1,19,0,NULL),(115,'Máy pha cà phê Gemilai S-3200B (Lướt)',6000000,6000000,1,5,'Cảnh báo',1,1,19,0,NULL),(116,'Máy pha cà phê La Marzocco KB90 2024',180000000,180000000,0,5,'Hết hàng',1,1,20,0,NULL),(117,'Máy pha cà phê LaCarimali 2023 2g',30000000,30000000,1,5,'Cảnh báo',1,1,21,0,NULL),(118,'Máy pha cà phê LaCarimali Crytal 2023 2g',32000000,32000000,1,5,'Cảnh báo',1,1,21,0,NULL),(119,'Máy pha cà phê Lacimbali M23 up 2021 (Đen)',40350000,40350000,0,5,'Hết hàng',1,1,22,0,NULL),(120,'Máy pha cà phê Lacimbali M27 RE',25500000,25500000,0,5,'Hết hàng',1,1,22,0,NULL),(121,'Máy pha cà phê La Nouva Era Arpa 2019',30000000,30000000,0,5,'Hết hàng',1,1,23,0,NULL),(122,'Máy pha cà phê La Nouva Era 2013',21380000,21380000,0,5,'Hết hàng',1,1,23,0,NULL),(123,'Máy pha cà phê Lelit Anita',7600000,7600000,1,5,'Cảnh báo',1,1,24,0,NULL),(124,'Máy pha cà phê Lelit Bianca V3 2023',35660000,35660000,0,5,'Hết hàng',1,1,24,0,NULL),(125,'Máy pha cà phê Lelit Bianca V3 2023',37000000,37000000,0,5,'Hết hàng',1,1,24,0,NULL),(126,'Máy pha cà phê Lelit Bianca V3 2023',37200000,37200000,0,5,'Hết hàng',1,1,24,0,NULL),(127,'Máy pha cà phê Lelit Bianca V3 2023',39500000,39500000,0,5,'Hết hàng',1,1,24,0,NULL),(128,'Máy pha cà phê Lelit Marax 2022',23000000,23000000,0,5,'Hết hàng',1,1,24,0,NULL),(129,'Máy pha cà phê Milesto M19M4 (Mới_VAT)',10200000,10200000,0,5,'Hết hàng',1,1,25,0,NULL),(130,'Máy pha cà phê Milesto EM19M2 (Mới_VAT)',9200000,9200000,0,5,'Hết hàng',1,1,25,0,NULL),(131,'Máy pha cà phê Iberital IB7 2g 2023',38320000,38320000,0,5,'Hết hàng',1,1,26,0,NULL),(132,'Máy pha cà phê Rancilio 2g 2020 (Trắng)',34000000,34000000,0,5,'Hết hàng',1,1,27,0,NULL),(133,'Máy pha cà phê Rancilio 2g 2019',33138000,33138000,0,5,'Hết hàng',1,1,27,0,NULL),(134,'Máy pha cà phê Rancilio 2g 2019',30000000,30000000,0,5,'Hết hàng',1,1,27,0,NULL),(135,'Máy pha cà phê Sanremo Zoe 2014',17000000,17000000,0,5,'Hết hàng',1,1,28,0,NULL),(136,'Máy pha cà phê Wega Luna 2022 1g',28000000,28000000,1,5,'Cảnh báo',1,1,29,0,NULL),(137,'Máy pha cà phê Wega Luna 2022 1g',28000000,28000000,0,5,'Hết hàng',1,1,29,0,NULL),(138,'Máy pha cà phê Wega Luna 2022 2g',35400000,35400000,1,5,'Cảnh báo',1,1,29,0,NULL),(139,'Máy pha cà phê Wega Pegaso 1g 2021 (Trắng)',33000000,33000000,1,5,'Cảnh báo',1,1,29,0,NULL),(140,'Máy pha cà phê Wega Pegaso 1g 2022 (Trắng)',35000000,35000000,1,5,'Cảnh báo',1,1,29,0,NULL),(141,'Máy pha cà phê Wega Pegaso 1g 2024',36500000,36500000,0,5,'Hết hàng',1,1,29,0,NULL),(142,'Máy pha cà phê Wega Pegaso 1g 2025 (Trắng)',35500000,35500000,0,5,'Hết hàng',1,1,29,0,NULL),(143,'Máy pha cà phê Wega Pegaso 2019 2g (Trắng)',34000000,34000000,1,5,'Cảnh báo',1,1,29,0,NULL),(144,'Máy pha cà phê Wega Pegaso 2020 (Đen)',36500000,36500000,0,5,'Hết hàng',1,1,29,0,NULL),(145,'Máy pha cà phê Wega Pegaso 2021 (Trắng)',37990000,37990000,0,5,'Hết hàng',1,1,29,0,NULL),(146,'Máy pha cà phê Wega Pegaso 2022 2g (Trắng)',38279000,38279000,1,5,'Cảnh báo',1,1,29,0,NULL),(147,'Máy pha cà phê Wega Pegaso 2022 2g (Trắng)',39785000,39785000,1,5,'Cảnh báo',1,1,29,0,NULL),(148,'Máy pha cà phê Wega Pegaso 2022 2g (Trắng)',40740000,40740000,0,5,'Hết hàng',1,1,29,0,NULL),(149,'Máy pha cà phê Wega Pegaso 2022 2g (Đen)',42000000,42000000,0,5,'Hết hàng',1,1,29,0,NULL),(150,'Máy pha cà phê Wega Pegaso 2023 2g (Đen)',40990000,40990000,0,5,'Hết hàng',1,1,29,0,NULL),(151,'Máy pha cà phê Wega Pegaso plus 2023 2g',45065000,45065000,1,5,'Cảnh báo',1,1,29,0,NULL),(152,'Máy pha cà phê Wega Pegaso 2024 2g (Đen)',45240000,45240000,0,5,'Hết hàng',1,1,29,0,NULL),(153,'Máy pha cà phê Wega Pegaso 2g (Mới)',61500000,61500000,0,5,'Hết hàng',1,1,29,0,NULL),(154,'Máy pha cà phê Wellhome KD330 (TL)',15170000,15170000,0,5,'Hết hàng',1,1,30,0,NULL),(155,'Máy pha cà phê Wellhome KD210 mới',11700000,11700000,1,5,'Cảnh báo',1,1,30,0,NULL),(156,'Máy pha cà phê Wellhome KD210 (TL)',5930000,5930000,0,5,'Hết hàng',1,1,30,0,NULL),(157,'Máy pha cà phê Welhome KD 310 Thanh lý',11125000,11125000,1,5,'Cảnh báo',1,1,30,0,NULL),(158,'Máy Pha cà phê Wendougee Sdragon',45950000,45950000,1,5,'Cảnh báo',1,1,31,0,NULL),(159,'Máy pha trà Lacilio LT150',11000000,11000000,1,5,'Cảnh báo',1,1,33,0,NULL),(160,'Máy pha trà Gino (3g)',38000000,38000000,0,5,'Hết hàng',1,1,32,0,NULL),(161,'Máy pha trà Gino (3g)',16155000,16155000,1,5,'Cảnh báo',1,1,32,0,NULL),(162,'Máy pha trà Gino (2g)',30000000,30000000,1,5,'Cảnh báo',1,1,32,0,NULL),(163,'Máy pha trà Gino (1g)',24000000,24000000,0,5,'Hết hàng',1,1,32,0,NULL),(164,'Máy xay cà phê Amalfi A80',6450000,6450000,3,5,'Cảnh báo',1,1,34,0,NULL),(165,'Máy xay cà phê Amalfi A80',6450000,6450000,0,5,'Hết hàng',1,1,34,0,NULL),(166,'Máy xay cà phê Amalfi A80 (Trắng_TL)',5000000,5000000,0,5,'Hết hàng',1,1,34,0,NULL),(167,'Máy xay cà phê Anfim Luna',15000000,15000000,0,5,'Hết hàng',1,1,35,0,NULL),(168,'Máy xay cà phê Atom (Mới)',13430000,13430000,0,5,'Hết hàng',1,1,36,0,NULL),(169,'Máy xay cà phê Atom (Mới)',13430000,13430000,0,5,'Hết hàng',1,1,36,0,NULL),(170,'Máy xay cà phê Casadio',1600000,1600000,1,5,'Cảnh báo',1,1,37,0,NULL),(171,'Máy xay cà phê Casadio (Cơ)',3050000,3050000,0,5,'Hết hàng',1,1,37,0,NULL),(172,'Máy xay cà phê Casadio (Cơ)',2687750,2687750,4,5,'Cảnh báo',1,1,37,0,NULL),(173,'Máy xay cà phê Carimali (Cơ)',3000000,3000000,1,5,'Cảnh báo',1,1,38,0,NULL),(174,'Máy xay cà phê Carimali (Tự động)',3000000,3000000,1,5,'Cảnh báo',1,1,38,0,NULL),(175,'Máy xay cà phê Ceado 83',9000000,9000000,1,5,'Cảnh báo',1,1,39,0,NULL),(176,'Máy xay cà phê Ceado 75',7000000,7000000,2,5,'Cảnh báo',1,1,39,0,NULL),(177,'Máy xay cà phê Compak Cơ',2000000,2000000,2,5,'Cảnh báo',1,1,40,0,NULL),(178,'Máy xay cà phê Cunill (Cũ)',2875000,2875000,2,5,'Cảnh báo',1,1,41,0,NULL),(179,'Máy xay cà phê CRM 9015',1000000,1000000,1,5,'Cảnh báo',1,1,42,0,NULL),(180,'Máy xay cà phê DF64 gen 2.4 (Trắng)',4700000,4700000,0,5,'Hết hàng',1,1,43,0,NULL),(181,'Máy xay cà phê DF64 gen 2.4 (Trắng)(Mới_VAT)',5000000,5000000,32,5,'Còn hàng',1,1,43,0,NULL),(182,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(183,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(184,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(185,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(186,'Máy xay cà phê DF64 gen 2.4 ( 8 Đen + 2 Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(187,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(188,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(189,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(190,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(191,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(192,'Máy xay cà phê DF64 gen 2.4 (Đen)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(193,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(194,'Máy xay cà phê DF64 gen 2.4 (Trắng)',5000000,5000000,0,5,'Hết hàng',1,1,43,0,NULL),(195,'Máy xay cà phê Elektra',2060000,2060000,1,5,'Cảnh báo',1,1,44,0,NULL),(196,'Máy xay cà phê Expobar',3265000,3265000,2,5,'Cảnh báo',1,1,45,0,NULL),(197,'Máy xay cà phê Eureka firenze 75',9936000,9936000,0,5,'Hết hàng',1,1,46,0,NULL),(198,'Máy xay cà phê Eureka firenze 75 (2 Trắng + 2 Đen)',9936000,9936000,0,5,'Hết hàng',1,1,46,0,NULL),(199,'Máy xay cà phê Eureka firenze 75',9936000,9936000,0,5,'Hết hàng',1,1,46,0,NULL),(200,'Máy xay cà phê Eureka firenze 75',9936000,9936000,0,5,'Hết hàng',1,1,46,0,NULL),(201,'Máy xay cà phê Eureka firenze 75',10000000,10000000,0,5,'Hết hàng',1,1,46,0,NULL),(202,'Máy xay cà phê Eureka firenze 75',10200000,10200000,10,5,'Còn hàng',1,1,46,0,NULL),(203,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(204,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(205,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(206,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(207,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(208,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(209,'Máy xay cà phê Eureka firenze 75',10200000,10200000,0,5,'Hết hàng',1,1,46,0,NULL),(210,'Máy xay cà phê Eureka firenze 75 (TL_Đen)',9000000,9000000,0,5,'Hết hàng',1,1,46,0,NULL),(211,'Máy xay cà phê Eureka firenze 75 (TL_Trắng)',9000000,9000000,0,5,'Hết hàng',1,1,46,0,NULL),(212,'Máy xay cà phê Eureka Firenze 65',7917000,7917000,3,5,'Cảnh báo',1,1,46,0,NULL),(213,'Máy xay cà phê Eureka Firenze 65',7917000,7917000,0,5,'Hết hàng',1,1,46,0,NULL),(214,'Máy xay cà phê Eureka Firenze 65',7917000,7917000,0,5,'Hết hàng',1,1,46,0,NULL),(215,'Máy xay cà phê Eureka Firenze 65 (2 Trắng)',7795000,7795000,0,5,'Hết hàng',1,1,46,0,NULL),(216,'Máy xay cà phê Eureka Zenith 65 cũ',4000000,4000000,1,5,'Cảnh báo',1,1,47,0,NULL),(217,'Máy xay cà phê Eureka Mignon Manuale',3700000,3700000,2,5,'Cảnh báo',1,1,47,0,NULL),(218,'Máy xay cà phê Galileo Q18 Thanh lý',6829000,6829000,6,5,'Còn hàng',1,1,48,0,NULL),(219,'Máy xay cà phê Gemilai A80 - PRO',6406000,6406000,1,5,'Cảnh báo',1,1,49,0,NULL),(220,'Máy xay cà phê Gemilai A80 - PRO',6450000,6450000,0,5,'Hết hàng',1,1,49,0,NULL),(221,'Máy xay cà phê Gemilai A80 - PRO (TL_Trắng)',5500000,5500000,0,5,'Hết hàng',1,1,49,0,NULL),(222,'Máy xay cà phê Gemilai A80 - PRO (TL_Trắng)',5500000,5500000,0,5,'Hết hàng',1,1,49,0,NULL),(223,'Máy xay cà phê Gemilai A80 - PRO (TL_Đen)',5100000,5100000,0,5,'Hết hàng',1,1,49,0,NULL),(224,'Máy xay cà phê Gemilai 9012 Thanh lý',2300000,2300000,1,5,'Cảnh báo',1,1,49,0,NULL),(225,'Máy xay cà phê Gino',2167000,2167000,1,5,'Cảnh báo',1,1,50,0,NULL),(226,'Máy xay cà phê HC600 V1',2500000,2500000,0,5,'Hết hàng',1,1,51,0,NULL),(227,'Máy xay cà phê HC600 V1',2833450,2833450,4,5,'Cảnh báo',1,1,51,0,NULL),(228,'Máy xay cà phê HC600 V2 (Mới)',6500000,6500000,0,5,'Hết hàng',1,1,51,0,NULL),(229,'Máy xay cà phê HC600 V2 ̣(TL)',3000000,3000000,1,5,'Cảnh báo',1,1,51,0,NULL),(230,'Máy xay cà phê HC600 V2 ̣(TL)',3000000,3000000,0,5,'Hết hàng',1,1,51,0,NULL),(231,'Máy xay cà phê HC600 V2 ̣(TL)',3050000,3050000,0,5,'Hết hàng',1,1,51,0,NULL),(232,'Máy xay cà phê HC600 V2 ̣(TL)',3500000,3500000,0,5,'Hết hàng',1,1,51,0,NULL),(233,'Máy xay cà phê HC600 (Tự động)',2700000,2700000,1,5,'Cảnh báo',1,1,51,0,NULL),(234,'Máy xay cà phê Lingdong 020 Thanh lý',1250000,1250000,1,5,'Cảnh báo',1,1,52,0,NULL),(235,'Máy xay cà phê Lingdong 020 Thanh lý',1000000,1000000,1,5,'Cảnh báo',1,1,52,0,NULL),(236,'Máy xay cà phê Lingdong 020 Thanh lý',1500000,1500000,0,5,'Hết hàng',1,1,52,0,NULL),(237,'Máy xay cà phê Lingdong 020',2050000,2050000,0,5,'Hết hàng',1,1,52,0,NULL),(238,'Máy xay cà phê Lingdong 020',1993750,1993750,7,5,'Còn hàng',1,1,52,0,NULL),(239,'Máy xay cà phê Lingdong 020',1993750,1993750,0,5,'Hết hàng',1,1,52,0,NULL),(240,'Máy xay cà phê Lingdong 020',1993750,1993750,0,5,'Hết hàng',1,1,52,0,NULL),(241,'Máy xay cà phê Lingdong 020',1993750,1993750,0,5,'Hết hàng',1,1,52,0,NULL),(242,'Máy xay cà phê Lingdong 020',1993750,1993750,0,5,'Hết hàng',1,1,52,0,NULL),(243,'Máy xay cà phê Lingdong 021 Thanh lý',2000000,2000000,1,5,'Cảnh báo',1,1,52,0,NULL),(244,'Máy xay cà phê Lingdong 021 Thanh lý',1545000,1545000,1,5,'Cảnh báo',1,1,52,0,NULL),(245,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(246,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(247,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(248,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(249,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(250,'Máy xay cà phê Lingdong 021',2650000,2650000,0,5,'Hết hàng',1,1,52,0,NULL),(251,'Máy xay cà phê Lingdong 021',2600000,2600000,4,5,'Cảnh báo',1,1,52,0,NULL),(252,'Máy xay cà phê Lingdong 021',2600000,2600000,0,5,'Hết hàng',1,1,52,0,NULL),(253,'Máy xay cà phê Lingdong 022 Thanh lý (Đen)',2000000,2000000,1,5,'Cảnh báo',1,1,52,0,NULL),(254,'Máy xay cà phê Lingdong 500N',1200000,1200000,5,5,'Cảnh báo',1,1,52,0,NULL),(255,'Máy xay cà phê Lingdong 500N (TL)',1000000,1000000,0,5,'Hết hàng',1,1,52,0,NULL),(256,'Máy xay cà phê Lingdong 900N',2060000,2060000,1,5,'Cảnh báo',1,1,52,0,NULL),(257,'Máy xay cà phê Lingdong 900N',2122000,2122000,1,5,'Cảnh báo',1,1,52,0,NULL),(258,'Máy xay cà phê Jx600AD Mới',6834500,6834500,1,5,'Cảnh báo',1,1,53,0,NULL),(259,'Máy xay cà phê Jx600AD Mới (1 Trắng + 2 Đen)',6825000,6825000,0,5,'Hết hàng',1,1,53,0,NULL),(260,'Máy xay cà phê Jx600AD Mới',6825000,6825000,0,5,'Hết hàng',1,1,53,0,NULL),(261,'Máy xay cà phê Jx600AD Mới',6750000,6750000,0,5,'Hết hàng',1,1,53,0,NULL),(262,'Máy xay cà phê Jx600AD Mới (Đen)',6800000,6800000,0,5,'Hết hàng',1,1,53,0,NULL),(263,'Máy xay cà phê Jx600AD Mới (Trắng)',6800000,6800000,0,5,'Hết hàng',1,1,53,0,NULL),(264,'Máy xay cà phê Jx600AD Đen TL',5500000,5500000,0,5,'Hết hàng',1,1,53,0,NULL),(265,'Máy xay cà phê Jx650AD Thanh lý',5500000,5500000,0,5,'Hết hàng',1,1,54,0,NULL),(266,'Máy xay cà phê Jx650AD Thanh lý',5400000,5400000,1,5,'Cảnh báo',1,1,54,0,NULL),(267,'Máy xay cà phê Jx600AD Thanh lý',5100000,5100000,2,5,'Cảnh báo',1,1,53,0,NULL),(268,'Máy xay cà phê Jx600AD Đen Thanh lý',5000000,5000000,0,5,'Hết hàng',1,1,53,0,NULL),(269,'Máy xay cà phê Jx600AD (TL _Đen)',5000000,5000000,0,5,'Hết hàng',1,1,53,0,NULL),(270,'Máy xay cà phê Jx600AD (TL _Đen)',5000000,5000000,0,5,'Hết hàng',1,1,53,0,NULL),(271,'Máy xay cà phê Jx600AD (TL _Đen)',5000000,5000000,0,5,'Hết hàng',1,1,53,0,NULL),(272,'Máy xay cà phê Jx600AD (TL _Đen)',5000000,5000000,0,5,'Hết hàng',1,1,53,0,NULL),(273,'Máy xay cà phê Jx600AD Thanh lý',4800000,4800000,1,5,'Cảnh báo',1,1,53,0,NULL),(274,'Máy xay cà phê Jx600AD Thanh lý',4700000,4700000,0,5,'Hết hàng',1,1,53,0,NULL),(275,'Máy xay cà phê Jx600AD Cơ Thanh lý',4525000,4525000,1,5,'Cảnh báo',1,1,53,0,NULL),(276,'Máy xay cà phê Jx600AD Thanh lý',4000000,4000000,0,5,'Hết hàng',1,1,53,0,NULL),(277,'Máy xay trà Mikita Đen (JX650)',7000000,7000000,0,5,'Hết hàng',1,1,55,0,NULL),(278,'Máy xay cà phê Fiorenzato F5 (Thanh lý)',4500000,4500000,1,5,'Cảnh báo',1,1,56,0,NULL),(279,'Máy xay cà phê Fiorenzato F64E Trắng (Mới)',16750000,16750000,1,5,'Cảnh báo',1,1,56,0,NULL),(280,'Máy xay cà phê Fiorenzato F64E 2023 Đen',12500000,12500000,0,5,'Hết hàng',1,1,56,0,NULL),(281,'Máy xay cà phê Fiorenzato F64E Đen',12000000,12000000,0,5,'Hết hàng',1,1,56,0,NULL),(282,'Máy xay cà phê Fiorenzato F64E 2023 Trắng',10500000,10500000,1,5,'Cảnh báo',1,1,56,0,NULL),(283,'Máy xay cà phê Fiorenzato F64E 2022 Trắng (Nứt màn hình)',8000000,8000000,1,5,'Cảnh báo',1,1,56,0,NULL),(284,'Máy xay cà phê Fiorenzato F64E (Màn nhỏ sọc màn)',5070000,5070000,0,5,'Hết hàng',1,1,56,0,NULL),(285,'Máy xay cà phê Fiorenzato F83 (Màn Nhỏ- TL)',12500000,12500000,1,5,'Cảnh báo',1,1,56,0,NULL),(286,'Máy xay cà phê Feama (Tự động)',4030000,4030000,2,5,'Cảnh báo',1,1,57,0,NULL),(287,'Máy xay cà phê Feama (Cơ)',2300000,2300000,1,5,'Cảnh báo',1,1,57,0,NULL),(288,'Máy xay cà phê Mahlkonig X54 (TL)',8036000,8036000,0,5,'Hết hàng',1,1,58,0,NULL),(289,'Máy xay cà phê Mazzer Cơ',2500000,2500000,0,5,'Hết hàng',1,1,59,0,NULL),(290,'Máy xay cà phê Mazzer Cơ',2800000,2800000,1,5,'Cảnh báo',1,1,59,0,NULL),(291,'Máy xay cà phê Mazzer Super Joylly Cơ (Xám)',3000000,3000000,1,5,'Cảnh báo',1,1,59,0,NULL),(292,'Máy xay cà phê Mazzer tự động mini ( thanh lý)',7500000,7500000,1,5,'Cảnh báo',1,1,59,0,NULL),(293,'Máy xay cà phê Mazzer Major (Trắng)',16600000,16600000,1,5,'Cảnh báo',1,1,59,0,NULL),(294,'Máy xay cà phê Mazzer Luigi Spa (Thanh lý)',10500000,10500000,1,5,'Cảnh báo',1,1,59,0,NULL),(295,'Máy xay cà phê Mazzer Super Joylly',9125000,9125000,2,5,'Cảnh báo',1,1,59,0,NULL),(296,'Máy xay cà phê Macap (Cơ )',3000000,3000000,0,5,'Hết hàng',1,1,60,0,NULL),(297,'Máy xay cà phê Macap (Bán tự động)',4050000,4050000,1,5,'Cảnh báo',1,1,60,0,NULL),(298,'Máy xay cà phê Macap (Tự động )',6000000,6000000,1,5,'Cảnh báo',1,1,60,0,NULL),(299,'Máy xay cà phê Melalife C3 tự động',3500000,3500000,4,5,'Cảnh báo',1,1,61,0,NULL),(300,'Máy xay cà phê Niche Zero',11500000,11500000,0,5,'Hết hàng',1,1,62,0,NULL),(301,'Máy xay cà phê Nouva Simonelli MDXS Đen',7000000,7000000,1,5,'Cảnh báo',1,1,63,0,NULL),(302,'Máy xay cà phê Nouva MDX',3500000,3500000,1,5,'Cảnh báo',1,1,63,0,NULL),(303,'Máy xay cà phê Nouva MDX (Tự động)',4000000,4000000,0,5,'Hết hàng',1,1,63,0,NULL),(304,'Máy xay cà phê Nouva simonelli MDX',5000000,5000000,1,5,'Cảnh báo',1,1,63,0,NULL),(305,'Máy xay cà phê Otto',5000000,5000000,0,5,'Hết hàng',1,1,64,0,NULL),(306,'Máy xay cà phê Promix-600AD',6300000,6300000,0,5,'Hết hàng',1,1,65,0,NULL),(307,'Máy xay cà phê Simonelli',5355000,5355000,1,5,'Cảnh báo',1,1,66,0,NULL),(308,'Máy xay cà phê Rancilio',4000000,4000000,1,5,'Cảnh báo',1,1,67,0,NULL),(309,'Máy xay cà phê Robust',2742500,2742500,2,5,'Cảnh báo',1,1,68,0,NULL),(310,'Máy xay cà phê Robust',2742500,2742500,1,5,'Cảnh báo',1,1,68,0,NULL),(311,'Máy xay cà phê Robust',2742500,2742500,0,5,'Hết hàng',1,1,68,0,NULL),(312,'Máy xay cà phê Robust',4000000,4000000,1,5,'Cảnh báo',1,1,68,0,NULL),(313,'Máy xay cà phê pha phin Tiamo',1200000,1200000,1,5,'Cảnh báo',1,1,69,0,NULL),(314,'Máy xay trà Promix',1000000,1000000,1,5,'Cảnh báo',1,1,70,0,NULL),(315,'Máy ép cam UB 160',680000,680000,2,5,'Cảnh báo',1,1,71,0,NULL),(316,'Máy ép nhanh SS - 83',2600000,2600000,1,5,'Cảnh báo',1,1,72,0,NULL),(317,'Máy nén cà phê Eureka Disko',7400000,7400000,3,5,'Cảnh báo',1,1,73,0,NULL),(318,'Máy nén cà phê Eureka Disko',7400000,7400000,0,5,'Hết hàng',1,1,73,0,NULL),(319,'Máy nén cà phê Eureka Disko',7400000,7400000,0,5,'Hết hàng',1,1,73,0,NULL),(320,'Máy xay UB - 712 plus',2400000,2400000,1,5,'Cảnh báo',1,1,74,0,NULL),(321,'Máy xay UB - 712',2200000,2200000,1,5,'Cảnh báo',1,1,75,0,NULL),(322,'Máy POS tính tiền',2000000,2000000,1,5,'Cảnh báo',1,1,76,0,NULL),(323,'Tay pha Carimali',1500000,1500000,1,5,'Cảnh báo',1,1,77,0,NULL),(324,'Tay pha không đáy',167000,167000,6,5,'Còn hàng',1,1,78,0,NULL),(325,'Bàn mát',5000000,5000000,1,5,'Cảnh báo',1,3,79,0,NULL),(326,'Bộ bàn ghế',1200000,1200000,1,5,'Cảnh báo',1,3,80,0,NULL),(327,'Bàn phím Nouva life',201500,201500,29,5,'Còn hàng',2,1,81,0,NULL),(328,'Bo bàn phím Nouva life',150000,150000,2,5,'Cảnh báo',2,1,82,0,NULL),(329,'Bột vệ sinh máy pha (nhỏ)',28000,28000,74,5,'Còn hàng',2,1,83,0,NULL),(330,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(331,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(332,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(333,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(334,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(335,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(336,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(337,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(338,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(339,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(340,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(341,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(342,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(343,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(344,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(345,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(346,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(347,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(348,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(349,'Bột vệ sinh máy pha (nhỏ)',28000,28000,0,5,'Hết hàng',2,1,83,0,NULL),(350,'Hộp đập bã chữ nhật',455000,455000,0,5,'Hết hàng',2,1,84,0,NULL),(351,'Hộp đập bã',100000,100000,0,5,'Hết hàng',2,1,84,0,NULL),(352,'Hộp đập bã',94000,94000,0,5,'Hết hàng',2,1,84,0,NULL),(353,'Hộp đập bã',92000,92000,0,5,'Hết hàng',2,1,84,0,NULL),(354,'Hộp đập bã 15cm',80000,80000,33,5,'Còn hàng',2,1,85,0,NULL),(355,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(356,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(357,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(358,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(359,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(360,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(361,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(362,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(363,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(364,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(365,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(366,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(367,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(368,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(369,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(370,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(371,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(372,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(373,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(374,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(375,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(376,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(377,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(378,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(379,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(380,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(381,'Hộp đập bã 15cm',80000,80000,0,5,'Hết hàng',2,1,85,0,NULL),(382,'Đế cao su',23000,23000,24,5,'Còn hàng',2,1,86,0,NULL),(383,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(384,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(385,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(386,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(387,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(388,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(389,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(390,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(391,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(392,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(393,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(394,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(395,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(396,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(397,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(398,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(399,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(400,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(401,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(402,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(403,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(404,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(405,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(406,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(407,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(408,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(409,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(410,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(411,'Đế cao su',23000,23000,0,5,'Hết hàng',2,1,86,0,NULL),(412,'Chổi vệ sinh máy pha',20000,20000,0,5,'Hết hàng',2,1,87,0,NULL),(413,'Chổi vệ sinh máy pha',23000,23000,0,5,'Hết hàng',2,1,87,0,NULL),(414,'Chổi vệ sinh máy pha',22000,22000,46,5,'Còn hàng',2,1,87,0,NULL),(415,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(416,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(417,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(418,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(419,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(420,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(421,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(422,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(423,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(424,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(425,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(426,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(427,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(428,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(429,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(430,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(431,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(432,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(433,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(434,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(435,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(436,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(437,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(438,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(439,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(440,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(441,'Chổi vệ sinh máy pha',22000,22000,0,5,'Hết hàng',2,1,87,0,NULL),(442,'Chổi vệ sinh tay pha',13000,13000,0,5,'Hết hàng',2,1,88,0,NULL),(443,'Chổi vệ sinh tay pha',13200,13200,49,5,'Còn hàng',2,1,88,0,NULL),(444,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(445,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(446,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(447,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(448,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(449,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(450,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(451,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(452,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(453,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(454,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(455,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(456,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(457,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(458,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(459,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(460,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(461,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(462,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(463,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(464,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(465,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(466,'Chổi vệ sinh tay pha',13200,13200,0,5,'Hết hàng',2,1,88,0,NULL),(467,'Temper trợ lực',140000,140000,0,5,'Hết hàng',2,1,89,0,NULL),(468,'Temper trợ lực',147000,147000,0,5,'Hết hàng',2,1,89,0,NULL),(469,'Temper trợ lực',147000,147000,0,5,'Hết hàng',2,1,89,0,NULL),(470,'Temper trợ lực',147000,147000,0,5,'Hết hàng',2,1,89,0,NULL),(471,'Temper trợ lực',146000,146000,0,5,'Hết hàng',2,1,89,0,NULL),(472,'Temper',70000,70000,0,5,'Hết hàng',2,1,90,0,NULL),(473,'Temper',70300,70300,54,5,'Còn hàng',2,1,90,0,NULL),(474,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(475,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(476,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(477,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(478,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(479,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(480,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(481,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(482,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(483,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(484,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(485,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(486,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(487,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(488,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(489,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(490,'Temper',70300,70300,0,5,'Hết hàng',2,1,90,0,NULL),(491,'OCD san',130000,130000,3,5,'Cảnh báo',2,1,91,0,NULL),(492,'OCD san',130000,130000,0,5,'Hết hàng',2,1,91,0,NULL),(493,'OCD san',130000,130000,0,5,'Hết hàng',2,1,91,0,NULL),(494,'OCD san',150625,150625,0,5,'Hết hàng',2,1,91,0,NULL),(495,'OCD san',150625,150625,0,5,'Hết hàng',2,1,91,0,NULL),(496,'Phin mù cao su',12000,12000,10,5,'Còn hàng',2,1,92,0,NULL),(497,'Phin mù cao su',12000,12000,0,5,'Hết hàng',2,1,92,0,NULL),(498,'Phin mù cao su',12000,12000,0,5,'Hết hàng',2,1,92,0,NULL),(499,'Phin mù cao su',12000,12000,0,5,'Hết hàng',2,1,92,0,NULL),(500,'Phin mù inox',23000,23000,4,5,'Cảnh báo',2,1,93,0,NULL),(501,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(502,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(503,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(504,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(505,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(506,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(507,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(508,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(509,'Phin mù inox',23000,23000,0,5,'Hết hàng',2,1,93,0,NULL),(510,'Cà phê Arabica',290,290,0,5,'Hết hàng',2,2,94,0,NULL),(511,'Cà phê Arabica',265,265,5000,5,'Còn hàng',2,2,94,0,NULL),(512,'Cà phê hạt',175,175,0,5,'Hết hàng',2,2,95,0,NULL),(513,'Cà phê Blend 9/1',178,178,53000,5,'Còn hàng',2,2,96,0,NULL),(514,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(515,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(516,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(517,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(518,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(519,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(520,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(521,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(522,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(523,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(524,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(525,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(526,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(527,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(528,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(529,'Cà phê Blend 9/1',178,178,0,5,'Hết hàng',2,2,96,0,NULL),(530,'Cà phê Robusta',138,138,0,5,'Hết hàng',2,2,97,0,NULL),(531,'Cà phê Robusta',138,138,0,5,'Hết hàng',2,2,97,0,NULL),(532,'Cà phê Robusta',138,138,0,5,'Hết hàng',2,2,97,0,NULL),(533,'Cà phê Robusta',153,153,36000,5,'Còn hàng',2,2,97,0,NULL),(534,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(535,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(536,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(537,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(538,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(539,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(540,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(541,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(542,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(543,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(544,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(545,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(546,'Cà phê Robusta',153,153,0,5,'Hết hàng',2,2,97,0,NULL),(547,'Lọc nước 3 lõi',220000,220000,5,5,'Cảnh báo',2,1,98,0,NULL),(548,'Lọc nước 3 lõi',220000,220000,0,5,'Hết hàng',2,1,98,0,NULL),(549,'Lọc nước 3 lõi',220000,220000,0,5,'Hết hàng',2,1,98,0,NULL),(550,'Lọc nước 3 lõi',220000,220000,0,5,'Hết hàng',2,1,98,0,NULL),(551,'Lọc nước 3 lõi',220000,220000,0,5,'Hết hàng',2,1,98,0,NULL),(552,'Lọc nước 3 lõi',264000,264000,0,5,'Hết hàng',2,1,98,0,NULL),(553,'Ca đánh sữa',55000,55000,0,5,'Hết hàng',2,1,99,0,NULL),(554,'Ca đánh sữa (300ml)',50000,50000,4,5,'Cảnh báo',2,1,100,0,NULL),(555,'Ca đánh sữa (500ml)',64000,64000,7,5,'Còn hàng',2,1,101,0,NULL),(556,'Ca đong inox',27000,27000,16,5,'Còn hàng',2,1,102,0,NULL),(557,'Ca đong inox',27000,27000,0,5,'Hết hàng',2,1,102,0,NULL),(558,'Ca đong inox',27000,27000,0,5,'Hết hàng',2,1,102,0,NULL),(559,'Ca đong inox',27000,27000,0,5,'Hết hàng',2,1,102,0,NULL),(560,'Filter Đôi',47000,47000,18,5,'Còn hàng',2,1,103,0,NULL),(561,'Filter Đơn',42000,42000,19,5,'Còn hàng',2,1,104,0,NULL),(562,'Đế cối inox UB-712',140000,140000,0,5,'Hết hàng',2,1,105,0,NULL),(563,'Đồng hồ 2 kim',380000,380000,0,5,'Hết hàng',2,1,106,0,NULL),(564,'Gioăng Silicon 58',51500,51500,97,5,'Còn hàng',2,1,107,0,NULL),(565,'Gioăng cao su',30000,30000,78,5,'Còn hàng',2,1,108,0,NULL),(566,'Gioăng cao su Gemilai',70000,70000,6,5,'Còn hàng',2,1,109,0,NULL),(567,'Gioăng cao su nouva',52500,52500,98,5,'Còn hàng',2,1,110,0,NULL),(568,'Gioăng Carimali',80000,80000,9,5,'Còn hàng',2,1,111,0,NULL),(569,'Giỏ lọc Carimali',169000,169000,1,5,'Cảnh báo',2,1,112,0,NULL),(570,'Lưỡi dao S42 titan đen',440000,440000,0,5,'Hết hàng',2,1,113,0,NULL),(571,'Lưỡi máy pha Breville',260000,260000,1,5,'Cảnh báo',2,1,114,0,NULL),(572,'Lưỡi họng Lamvita',100000,100000,1,5,'Cảnh báo',2,1,115,0,NULL),(573,'Lưỡi máy xay 64mm',600000,600000,1,5,'Cảnh báo',2,1,116,0,NULL),(574,'Máy đánh bọt DC - 08',71500,71500,1,5,'Cảnh báo',2,1,117,0,NULL),(575,'Máy đánh bọt DC - 201',195000,195000,1,5,'Cảnh báo',2,1,117,0,NULL),(576,'Nắp máy ép nhanh Uniblend SS-33',180000,180000,0,5,'Hết hàng',2,1,119,0,NULL),(577,'Núm truyền động socket UB-712',90000,90000,0,5,'Hết hàng',2,1,120,0,NULL),(578,'Phểu máy xay LD022',400000,400000,1,5,'Cảnh báo',2,1,121,0,NULL),(579,'Tấm Micra máy Astoria',640000,640000,1,5,'Cảnh báo',2,1,122,0,NULL),(580,'Thanh đun Casadio',530000,530000,2,5,'Cảnh báo',2,1,123,0,NULL),(581,'Thùng gỗ',350000,350000,7,5,'Còn hàng',2,1,124,0,NULL),(582,'Thùng gỗ',350000,350000,0,5,'Hết hàng',2,1,124,0,NULL),(583,'Thùng gỗ',350000,350000,0,5,'Hết hàng',2,1,124,0,NULL),(584,'Thùng gỗ',350000,350000,0,5,'Hết hàng',2,1,124,0,NULL),(585,'Thùng gỗ',350000,350000,0,5,'Hết hàng',2,1,124,0,NULL),(586,'Thùng gỗ',350000,350000,0,5,'Hết hàng',2,1,124,0,NULL),(587,'Vòi Đơn',70000,70000,9,5,'Còn hàng',2,1,125,0,NULL),(588,'Vòng nhôm chống tràn',85000,85000,1,5,'Cảnh báo',2,1,126,0,NULL);
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
  `mo_ta` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
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

-- Dump completed on 2026-05-28 18:52:21

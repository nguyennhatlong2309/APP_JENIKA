# ☕ BrewMaster Pro & WEB_JENIKA (WEB_CAFE) - Hệ Thống Quản Lý Toàn Diện

Chào mừng bạn đến với **BrewMaster Pro & WEB_CAFE (WEB_JENIKA)** - một nền tảng quản lý quán cà phê, kho hàng, và doanh thu đa nền tảng (Desktop & Web) toàn diện, chuyên nghiệp. Dự án được thiết kế với kiến trúc hiện đại, khả năng mở rộng mạnh mẽ và trải nghiệm người dùng tối ưu.

Hệ thống bao gồm hai giải pháp chính dùng chung một cơ sở dữ liệu MySQL:
1. **BrewMaster Pro (Desktop App):** Ứng dụng Desktop viết bằng **Java Swing** với giao diện **FlatLaf** hiện đại.
2. **WEB_JENIKA (Web App):** Ứng dụng Web cao cấp với giao diện **Next.js (React 19)** phong cách **Glassmorphism** siêu đẹp và **Spring Boot Backend** mạnh mẽ.

---

## 🚀 Các Tính Năng Chính Của Hệ Thống

*   **📊 Trang Tổng Quan (Dashboard):** Biểu đồ trực quan hóa doanh thu, chi phí, lợi nhuận theo ngày/tháng/năm và các chỉ số kinh doanh cốt lõi.
*   **🛒 Quản Lý Bán Hàng (Sales Orders):** Lập hóa đơn bán lẻ nhanh chóng, tích hợp tìm kiếm sản phẩm và liên kết thông tin khách hàng thành viên.
*   **📦 Quản Lý Nhập Hàng (Purchase Orders):** Quản lý đơn nhập nguyên vật liệu, thiết bị, công nợ và lịch sử giao dịch với nhà cung cấp.
*   **🔍 Quản Lý Kho Hàng (Inventory):** Theo dõi số lượng tồn kho thực tế, cảnh báo thông minh khi hàng hóa dưới mức tối thiểu và lập yêu cầu tái đặt hàng nhanh.
*   **💸 Quản Lý Thu Chi (Expenses):** Ghi nhận chi phí vận hành quán (tiền điện, nước, mặt bằng, lương nhân viên, chi phí phát sinh khác).
*   **👥 Quản Lý Đối Tác (Partners):** Phân loại và lưu trữ hồ sơ khách hàng thân thiết cùng các đối tác/nhà cung cấp.
*   **👤 Quản Lý Nhân Viên (Staff):** Quản lý hồ sơ nhân sự, phân quyền truy cập hệ thống chi tiết.
*   **📝 Nhật Ký Hoạt Động (Activity Log):** Ghi nhận toàn bộ thao tác thêm, sửa, xóa trên hệ thống nhằm phục vụ mục đích đối soát và kiểm toán dữ liệu.

---

## 🛠️ Công Nghệ Sử Dụng

### 🖥️ 1. Desktop App (BrewMaster Pro)
*   **Ngôn ngữ:** Java (JDK 17/11)
*   **Giao diện:** Java Swing kết hợp thư viện giao diện hiện đại **FlatLaf** (hỗ trợ chế độ Sáng/Tối).
*   **Build & Quản lý:** Apache Maven
*   **Công cụ xuất báo cáo:** Apache POI (hỗ trợ xuất dữ liệu ra file Excel `.xlsx`).
*   **Log:** Log4j2

### 🌐 2. Web App Platform (WEB_JENIKA)
*   **Frontend:** Next.js 16 (App Router), React 19, TypeScript, Vanilla CSS + TailwindCSS với custom variables siêu mượt.
*   **Backend:** Spring Boot 3.3, Hibernate/JPA kết hợp Maven.
*   **Design System:** Thiết kế cao cấp phong cách Glassmorphism (hiệu ứng kính mờ), phối màu HSL thời thượng, responsive hoàn toàn và tích hợp các micro-animations sinh động.

### 🗄️ 3. Cơ Sở Dữ Liệu Chung (Shared Database)
*   **Cơ sở dữ liệu:** MySQL 8.0
*   **Container hóa:** Docker & Docker Compose (cho phép dựng nhanh DB chỉ với 1 click).

---

## 📋 Yêu Cầu Hệ Thống

Để khởi chạy toàn bộ hệ thống, máy tính của bạn cần có:
1. **Java Development Kit (JDK) 17 hoặc 11**.
2. **Node.js** (để chạy ứng dụng Web Frontend).
3. **Apache Maven** (để quản lý thư viện và chạy Java Swing / Spring Boot).
4. **Docker Desktop** (để khởi chạy MySQL Server nhanh qua container) hoặc một dịch vụ **MySQL Server** độc lập.

---

## ⚙️ Hướng Dẫn Cài Đặt & Khởi Chạy

### 🐳 Bước 1: Khởi Chạy Database MySQL (Docker)

Tại thư mục gốc của dự án (nơi có file `docker-compose.yml`), chạy lệnh sau:
```bash
docker-compose up -d
```
*Lưu ý: Docker sẽ tự động tải MySQL 8.0, cấu hình database `cfe_di_rom` và thực thi script khởi tạo cơ sở dữ liệu ban đầu từ file `swing-app/database/init.sql`.*

---

### 🖥️ Bước 2: Chạy Ứng Dụng Desktop (Java Swing)

1. Di chuyển vào thư mục `swing-app`:
   ```bash
   cd swing-app
   ```
2. Build dự án bằng Maven:
   ```bash
   mvn clean package
   ```
3. Khởi chạy ứng dụng:
   ```bash
   mvn exec:java -Dexec.mainClass="com.brewmaster.Main"
   # Hoặc chạy file JAR đã build trong thư mục target:
   java -jar target/brewmaster-pro-1.0-SNAPSHOT.jar
   ```

---

### 🌐 Bước 3: Chạy Ứng Dụng Web (Next.js & Spring Boot)

#### 3.1 Khởi chạy Web Frontend (Next.js)
1. Di chuyển vào thư mục Frontend:
   ```bash
   cd web/Frontend
   ```
2. Cài đặt các thư viện (nếu cần):
   ```bash
   npm install
   ```
3. Khởi chạy máy chủ phát triển (Dev server):
   ```bash
   npm run dev
   ```
4. Truy cập giao diện Web tuyệt đẹp tại: **[http://localhost:3000](http://localhost:3000)**.

#### 3.2 Khởi chạy Web Backend (Spring Boot API)
1. Di chuyển vào thư mục Backend:
   ```bash
   cd web/Backend
   ```
2. Khởi chạy dự án Spring Boot bằng Maven:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Máy chủ API sẽ hoạt động trên cổng `8080`.*

---

## 🔒 Thông Tin Đăng Nhập & Kết Nối Mặc Định

*   **Tài khoản ứng dụng:**
    *   Tên đăng nhập: `admin`
    *   Mật khẩu: `admin123`
*   **Kết nối Database:**
    *   Host: `localhost`
    *   Port: `3306`
    *   Database Name: `cfe_di_rom`
    *   Username: `cafe_user` (hoặc `root`)
    *   Password: `cafe_pass` (hoặc `root`)

---

## 📂 Cấu Trúc Thư Mục Dự Án

```text
CAFE_DI_ROM/
├── docker-compose.yml          # Cấu hình container MySQL 8.0 tự động
├── README.md                   # Tài liệu hướng dẫn chính (file này)
├── .gitignore                  # Cấu hình Git bỏ qua thông minh (recursive)
│
├── swing-app/                  # 🖥️ Ứng dụng Desktop Java Swing (BrewMaster Pro)
│   ├── database/               # Chứa script init.sql của cơ sở dữ liệu
│   ├── pom.xml                 # Cấu hình dependencies của Swing app
│   └── src/main/java/          # Mã nguồn Java Swing
│
└── web/                        # 🌐 Nền tảng Web hiện đại (WEB_JENIKA)
    ├── Frontend/               # Next.js App với UI Glassmorphism sang trọng
    │   ├── src/                # Các route trang (inventory, purchases, sales, settings)
    │   └── package.json        # Dependencies của Frontend
    │
    └── Backend/                # Spring Boot REST API
        ├── pom.xml             # Cấu hình Spring Boot dependencies
        └── src/main/           # Mã nguồn và config của Spring Boot
```

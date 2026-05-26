# ☕ BrewMaster Pro - Hệ Thống Quản Lý Quán Cà Phê

**BrewMaster Pro** là một ứng dụng desktop hiện đại được phát triển bằng ngôn ngữ **Java (Swing)** nhằm cung cấp giải pháp quản lý toàn diện và chuyên nghiệp cho các quán cà phê, trà sữa hoặc cửa hàng dịch vụ ăn uống (F&B) vừa và nhỏ.

Với giao diện hiện đại sử dụng thư viện **FlatLaf**, ứng dụng mang lại trải nghiệm mượt mà, trực quan cùng khả năng tương thích cao trên nhiều hệ điều hành.

---

## 🚀 Các Tính Năng Chính

*   **📊 Trang Tổng Quan (Dashboard):** Hiển thị doanh thu, chi phí, lợi nhuận theo ngày/tháng/năm và các chỉ số kinh doanh cốt lõi bằng biểu đồ và số liệu trực quan.
*   **🛒 Quản Lý Bán Hàng (Sales Orders):** Lập hóa đơn bán lẻ nhanh chóng, tích hợp công cụ tìm kiếm sản phẩm và liên kết thông tin khách hàng thành viên.
*   **📦 Quản Lý Nhập Hàng (Purchase Orders):** Lập đơn nhập nguyên vật liệu, quản lý công nợ và lịch sử giao dịch với nhà cung cấp.
*   **🔍 Quản Lý Kho Hàng (Inventory):** Theo dõi số lượng tồn kho thực tế, cảnh báo khi hàng hóa dưới mức tối thiểu và cập nhật thông tin sản phẩm.
*   **💸 Quản Lý Thu Chi (Expenses):** Ghi nhận chi phí vận hành quán (tiền điện, nước, mặt bằng, lương nhân viên, chi phí phát sinh khác).
*   **👥 Quản Lý Đối Tác (Partners):** Phân loại và lưu trữ hồ sơ của khách hàng thân thiết và các nhà cung cấp.
*   **👤 Quản Lý Nhân Viên (Staff):** Quản lý hồ sơ nhân sự, phân quyền truy cập hệ thống.
*   **📝 Nhật Ký Hoạt Động (Activity Log):** Ghi nhận toàn bộ thao tác thêm, sửa, xóa trên hệ thống phục vụ mục đích giám sát và đối soát dữ liệu.

---

## 🛠️ Công Nghệ Sử Dụng

*   **Ngôn ngữ lập trình:** Java (JDK 11)
*   **Giao diện người dùng:** Java Swing kết hợp thư viện giao diện hiện đại **FlatLaf** (hỗ trợ Light/Dark mode).
*   **Quản lý dự án & Build:** Apache Maven
*   **Cơ sở dữ liệu:** MySQL 8.0
*   **Container hóa:** Docker & Docker Compose (dùng để khởi tạo nhanh DB).
*   **Công cụ xuất báo cáo:** Apache POI (hỗ trợ xuất dữ liệu ra file Excel `.xlsx`).
*   **Log:** Log4j2

---

## 📋 Yêu Cầu Hệ Thống

Để chạy ứng dụng, máy tính của bạn cần được cài đặt các công cụ sau:
1.  **Java Development Kit (JDK) 11** hoặc cao hơn.
2.  **Apache Maven** (để quản lý thư viện và build).
3.  **Docker Desktop** (để chạy cơ sở dữ liệu nhanh chóng qua container) hoặc cài đặt **MySQL Server** độc lập.

---

## ⚙️ Hướng Dẫn Cài Đặt & Khởi Chạy

### 1. Khởi chạy Cơ sở dữ liệu MySQL bằng Docker

Tại thư mục gốc của dự án (nơi có file `docker-compose.yml`), mở Terminal (CMD/PowerShell) và chạy lệnh sau để tạo và khởi động container MySQL:

```bash
docker-compose up -d
```

*Lưu ý: Docker sẽ tự động tải MySQL 8.0, cấu hình database `cfe_di_rom` và thực thi script khởi tạo cơ sở dữ liệu ban đầu từ file `swing-app/database/init.sql`.*

### 2. Build dự án bằng Maven

Di chuyển vào thư mục `swing-app` nơi chứa mã nguồn Java và file `pom.xml`:

```bash
cd swing-app
mvn clean package
```

Sau khi build thành công, file thực thi JAR đóng gói đầy đủ thư viện (Fat JAR) sẽ được tạo ra tại `swing-app/target/brewmaster-pro-1.0-SNAPSHOT.jar`.

### 3. Khởi chạy Ứng dụng

Bạn có thể chạy ứng dụng trực tiếp bằng Maven:

```bash
mvn exec:java -Dexec.mainClass="com.brewmaster.Main"
```

Hoặc chạy file JAR đã được build ở bước trên:

```bash
java -jar target/brewmaster-pro-1.0-SNAPSHOT.jar
```

---

## 🔒 Thông Tin Tài Khoản Mặc Định

*   **Tài khoản đăng nhập ứng dụng:**
    *   Tên đăng nhập: `admin`
    *   Mật khẩu: `admin123`
*   **Kết nối Database (trong trường hợp cấu hình thủ công):**
    *   Host: `localhost`
    *   Port: `3306`
    *   Database Name: `cfe_di_rom`
    *   Username: `cafe_user`
    *   Password: `cafe_pass`
    *   *(Hoặc có thể kết nối bằng tài khoản Root: `root` / `root`)*

---

## 📂 Cấu Trúc Dự Án chính

```text
├── docker-compose.yml          # File Docker Compose khởi tạo MySQL
├── README.md                   # Hướng dẫn dự án
├── .gitignore                  # File cấu hình bỏ qua Git
└── swing-app                   # Thư mục mã nguồn ứng dụng Java Swing
    ├── database                # Chứa file init.sql thiết lập bảng
    ├── pom.xml                 # Cấu hình Maven dependencies
    └── src/main/java           # Mã nguồn Java
        └── com/brewmaster      # Package chính của ứng dụng
```

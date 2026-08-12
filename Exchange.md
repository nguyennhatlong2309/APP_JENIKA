# Exchange - Lịch sử thay đổi hiện tại

## Cập nhật lúc 17:15 - 05/08/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/components/features/Sidebar.tsx`:
    1. Cập nhật `src` của avatar nhân viên từ link Google public sang hình ảnh cục bộ `/user.png` được sao chép từ `resources/images/user.png`.
  - `web/Frontend/src/app/login/page.tsx`:
    1. Đảm bảo hình nền trang đăng nhập tham chiếu chính xác đến ảnh `/jenka.jpg` trong thư mục static public (đã được đồng bộ từ `resources/images/jenka.jpg`).
- **Kết quả xác thực**:
  - Đã sao chép các tệp tin hình ảnh `user.png` và `jenka.jpg` từ thư mục `resources/images/` vào thư mục `web/Frontend/public/` để Next.js phục vụ trực tiếp dưới dạng static assets.
  - Chạy kiểm tra tĩnh TypeScript `npx tsc --noEmit` và chạy kiểm tra cú pháp ESLint trên các tệp tin thay đổi đều hoàn tất thành công và không phát sinh lỗi.

## Cập nhật lúc 14:15 - 05/08/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/api/ocr/route.ts`:
    1. Cập nhật `customerNameRegex` để nhận diện cả lỗi chính tả `Khách bàng` do bộ OCR nhận diện nhầm ký tự.
    2. Nâng cấp logic lọc/kiểm tra sau khi trích xuất: Chuẩn hóa bỏ dấu tiếng Việt (normalize) trước khi kiểm tra các từ khóa loại trừ (`thanh toan`, `tra`, `chuyen khoan`, `ck`), ngăn chặn việc nhận diện sai thông tin thanh toán (`thanh toán: 1,000,000`) thành tên khách hàng do không khớp từ khóa tiếng Việt có dấu.
  - `web/Frontend/src/components/features/OcrSalesScanner.tsx`:
    1. Đồng bộ hóa thay đổi Regex và logic chuẩn hóa bỏ dấu khi trích xuất thông tin khách hàng ở phía client-side đối với OCR Bán hàng.
  - `web/Frontend/src/components/features/OcrPurchasesScanner.tsx`:
    1. Đồng bộ hóa thay đổi Regex và logic chuẩn hóa bỏ dấu khi trích xuất thông tin đối tác/nhà cung cấp ở phía client-side đối với OCR Nhập hàng.
- **Kết quả xác thực**:
  - Viết script kiểm tra chạy thử nghiệm (`scratch/test_ocr_regex.js`) đối khớp thành công hoàn toàn giữa logic cũ (trích xuất sai thành `"thanh toán: 1,000,000"`) và logic mới (trích xuất đúng `"Anh Hoàng Tuân"`).

## Cập nhật lúc 11:27 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/page.tsx`:
    1. Thiết lập tính toán doanh thu bán hàng theo tuần trong tháng hiện tại từ danh sách hóa đơn lấy từ database, thay thế hoàn toàn dữ liệu biểu đồ tĩnh (mocked).
    2. Tính toán và áp dụng tọa độ SVG Y-coordinates và các data points động dựa trên giá trị doanh thu thực tế của từng tuần.
    3. Bổ sung thẻ `<title>` làm tooltip hiển thị số tiền chính xác khi di chuột vào từng node biểu đồ (data points).
    4. Cập nhật tiêu đề báo cáo ở Home hiển thị động tháng hiện tại: `Tổng quan Báo cáo (Tháng X/YYYY)`.
    5. Cập nhật biểu đồ "Phân tích Bán hàng" biểu diễn theo tuần trong tháng hiện tại (`TUẦN 1` đến `TUẦN 5`) thay vì các ngày trong tuần.
  - `web/Frontend/src/app/globals.css`:
    1. Bổ sung các cấu hình ghi đè CSS cho hover và container trong chế độ giao diện sáng (Light Mode) bao gồm: `hover:bg-white/5`, `hover:bg-white/10`, `hover:bg-white/20`, `hover:bg-white/[0.01]`, `hover:bg-white/[0.02]`, `hover:bg-white/[0.03]`, `bg-white/2` để các phần tử, dòng bảng và dropdown item hiển thị nổi bật và tương phản rõ ràng.
- **Kết quả xác thực**:
  - Biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.
  - Chạy `npm run build` thành công hoàn tất.

## Cập nhật lúc 10:59 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/globals.css`:
    1. Bổ sung lớp ghi đè `html.light .bg-[#0A0E17]/70` để tự động đổi màu nền của Modal xem chi tiết hóa đơn từ màu tối sang màu sáng trong suốt (`rgba(255, 255, 255, 0.7)`) khi hệ thống ở chế độ giao diện sáng (Light Mode), sửa lỗi modal bị tối màu trong giao diện sáng.
- **Kết quả xác thực**:
  - Biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 10:57 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Tăng chiều cao danh sách sản phẩm trong Modal xem chi tiết hóa đơn (tăng từ `max-h-48` lên `max-h-96`) giúp xem đầy đủ các sản phẩm cùng lúc mà không phải scroll quá sớm.
    2. Giảm độ mờ đục của background từ `bg-[#0A0E17]/95` xuống `bg-[#0A0E17]/70` và backdrop phủ từ `bg-background/80` xuống `bg-black/40` giúp background của modal trong suốt hơn và hiển thị hiệu ứng glassmorphism đẹp mắt hơn.
- **Kết quả xác thực**:
  - Biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 10:53 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Tăng kích thước Modal xem chi tiết hóa đơn (`VIEW ORDER DETAILS MODAL OVERLAY`) từ `max-w-lg` lên `max-w-2xl` giúp hiển thị rộng rãi, thoáng hơn.
    2. Đổi nhãn hiển thị thông tin người phụ trách trong chi tiết hóa đơn từ "Thu ngân" thành "Nhân viên".
    3. Thêm hiển thị thông tin dòng **Tặng kèm** (tổng giá trị quà tặng của hóa đơn) vào phần tổng hợp tiền ở góc dưới bên phải modal.
- **Kết quả xác thực**:
  - Biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 10:48 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/model/BanHang.java` & `BanHangDTO.java`:
    1. Bổ sung thêm trường `tienQuaTang` (lưu tổng giá vốn của các sản phẩm tặng kèm trong đơn hàng).
  - `web/Backend/src/main/java/com/cafe/jenika/repository/BanHangRepository.java`:
    1. Cập nhật phương thức native query `insertWithCustomId` để lưu trữ thêm trường `tien_qua_tang`.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BanHangService.java`:
    1. Tích hợp tính toán tự động `calculatedGiftCost` (cộng dồn giá vốn của các dòng chi tiết đơn hàng có `isGift == true`) và lưu vào trường `tienQuaTang` khi tạo hoặc sửa đơn hàng.
  - `web/Frontend/src/types/index.ts`:
    1. Thêm `tienQuaTang?: number;` vào TypeScript interface `SaleOrder`.
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Sắp xếp lại danh sách các cột hiển thị theo đúng thứ tự yêu cầu: Mã HĐ, Ngày tạo, Khách hàng, Nhân viên (đổi tên từ Thu ngân), Tổng tiền, Khách nợ (đổi tên từ Còn nợ), Tặng kèm (thay thế cho Đặt cọc, hiển thị `tienQuaTang`), Lợi nhuận, Trạng thái, và Thao tác.
    2. Cập nhật định dạng file CSV xuất ra theo đúng thứ tự các cột hiển thị mới.
  - `migration_loinhuan.sql`:
    1. Bổ sung lệnh UPDATE để tự động tính toán và điền cột `tien_qua_tang` cho các đơn hàng cũ dựa trên tổng giá vốn các sản phẩm tặng kèm.
- **Kết quả xác thực**:
  - Biên dịch Backend Spring Boot thành công (`mvn compile` -> `BUILD SUCCESS`).
  - Kiểm tra kiểu tĩnh Frontend Next.js thành công (`npx tsc --noEmit` -> OK).
  - Đã thực thi script di cư SQL cập nhật `tien_qua_tang` thành công trên DB.

## Cập nhật lúc 10:40 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/model/BanHang.java`:
    1. Thêm thuộc tính `tongCost` (Tổng giá vốn đơn hàng) và `loiNhuan` (Lợi nhuận đơn hàng) làm các cột cơ sở dữ liệu.
  - `web/Backend/src/main/java/com/cafe/jenika/model/ChiTietBanHang.java`:
    1. Thêm thuộc tính `giaVon` (Giá vốn lịch sử tại thời điểm bán) cho mỗi chi tiết bán hàng.
  - `web/Backend/src/main/java/com/cafe/jenika/dto/BanHangDTO.java` & `ChiTietBanHangDTO.java`:
    1. Cập nhật các trường dữ liệu và các hàm map `fromEntity` / `toEntity` để truyền nhận thông tin giá vốn, tổng cost, và lợi nhuận.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/BanHangRepository.java`:
    1. Cập nhật câu lệnh native SQL `insertWithCustomId` để hỗ trợ lưu trữ thêm `tong_cost` và `loi_nhuan` khi import hoặc lập hóa đơn bằng ID thủ công.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BanHangService.java`:
    1. Tích hợp tính toán tự động: Khi tạo (`createSalesOrder`) hoặc sửa (`updateSalesOrder`), hệ thống sẽ lưu `sp.giaNhapHienTai` làm `giaVon` trong `ChiTietBanHang` (kể cả quà tặng), tính tổng `tongCost` và tự động tính `loiNhuan = tongTien - tongCost`.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BaoCaoService.java`:
    1. Điều chỉnh báo cáo sản phẩm bán `getProductSales` và `getProductSalesStats` để ưu tiên tính lợi nhuận dựa trên trường `giaVon` lưu trong chi tiết đơn hàng thay vì lấy giá nhập hiện thời từ bảng sản phẩm, đảm bảo số liệu báo cáo không bị thay đổi khi biến động giá nhập.
  - `web/Frontend/src/types/index.ts`:
    1. Định nghĩa thêm các trường `giaVon` trong `SalesOrderDetail` và `tongCost`, `loiNhuan` trong `SaleOrder` TypeScript interface.
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Thêm cột hiển thị **Lợi nhuận** vào bảng danh sách hóa đơn (tô màu xanh/đỏ sinh động).
    2. Cập nhật modal xem chi tiết hóa đơn hiển thị thêm thông tin **Lợi nhuận**.
    3. Thêm cột Lợi nhuận vào dữ liệu xuất file báo cáo CSV.
  - `migration_loinhuan.sql` [NEW]:
    1. Tạo file SQL migration để điền dữ liệu giá vốn lịch sử, tổng cost, lợi nhuận cho toàn bộ các hóa đơn đã tồn tại trong DB trước đó.
- **Kết quả xác thực**:
  - Biên dịch Backend Spring Boot thành công (`mvn compile` -> `BUILD SUCCESS`).
  - Đã thực thi script migration SQL thành công trên MySQL để di cư dữ liệu cũ.

## Cập nhật lúc 10:05 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Cấu hình gán `isDepositManual` thành `true` khi áp dụng dữ liệu từ kết quả OCR của hóa đơn bán hàng (`onApply`), ngăn không cho hook `useEffect` tự động ghi đè số tiền khách thanh toán thành tổng tiền hóa đơn khi cập nhật danh sách sản phẩm.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Đồng bộ cấu hình gán `isPaidManual` thành `true` khi áp dụng dữ liệu OCR (`onApply`) cho đơn nhập hàng.
- **Kết quả xác thực**:
  - Chạy biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 09:47 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Bổ sung state `isPaidManual` quản lý việc tự động đồng bộ số tiền đã thanh toán với tổng tiền tính toán của đơn nhập.
    2. Cài đặt hook `useEffect` theo dõi tổng tiền đơn nhập để tự động cập nhật vào ô "Tiền đã trả" nếu người dùng chưa sửa đổi thủ công.
    3. Hỗ trợ tự động vô hiệu hóa chế độ đồng bộ khi người dùng gõ số tiền thanh toán khác, hoặc khi tải dữ liệu đơn nhập cũ từ cơ sở dữ liệu lên để chỉnh sửa.
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Bổ sung cải tiến UX tương tự cho trang Bán hàng (`isDepositManual` và `useEffect` cập nhật `formDeposit` theo tổng tiền hóa đơn).
- **Kết quả xác thực**:
  - Chạy biên dịch kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 09:15 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/inventory/page.tsx`:
    1. Thay thế hộp thoại xác nhận native `window.confirm` và thông báo native `alert` khi xóa mềm/ẩn sản phẩm bằng một custom Delete Confirmation Modal được thiết kế theo giao diện glassmorphism mượt mà.
    2. Bổ sung các state `productToDelete` (kiểu `ProductItem`), `isDeleting` (quản lý trạng thái loading) và `deleteError` (hiển thị thông báo lỗi trực tiếp trên modal nếu API thất bại).
    3. Thêm hiệu ứng loading spinner và disable các nút điều khiển trong quá trình xóa sản phẩm để tránh các thao tác trùng lặp.
    4. Điều chỉnh độ mờ đục (opacity) của background phủ overlay ở các modal (Thêm/Sửa sản phẩm, Tạo nhóm mới, Xác nhận xóa) từ `bg-background/80` và `bg-background/90` sang `bg-black/30` và `backdrop-blur-sm` giúp hiển thị trong suốt hơn và đồng nhất với `ConfirmModal` toàn hệ thống.
    5. Loại bỏ lớp màu nền đặc `bg-[#131929]` ở các hộp thoại modal con, để chúng hiển thị hiệu ứng kính mờ trong suốt (translucent glassmorphic) tự nhiên và cao cấp của lớp `.glass-card`.
- **Kết quả xác thực**:
  - Chạy kiểm tra kiểu tĩnh TypeScript (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 10:50 - 23/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/config/SecurityConfig.java`:
    1. Cho phép đường dẫn `/error` không yêu cầu xác thực (`permitAll()`). Điều này ngăn việc Spring Security chặn các lỗi xử lý nội bộ của máy chủ (ví dụ: lỗi cơ sở dữ liệu, lỗi logic nghiệp vụ ngoài ý muốn) thành mã `403 Forbidden`, giúp Frontend nhận được đúng mã lỗi (500 hoặc 400) để hiển thị thông báo thay vì bị ép đăng xuất về trang login.
  - `web/Frontend/src/services/purchaseService.ts`:
    1. Khai báo thêm hàm `getPurchaseOrderById(id: number)` để lấy chi tiết đơn nhập hàng thông qua API client được bảo mật (gắn Bearer Token).
    2. Cấu hình tự động lấy token từ `localStorage` và đính kèm vào tiêu đề `Authorization` cho hàm `exportPurchaseOrderExcel` để tránh lỗi 403 khi tải file Excel.
  - `web/Frontend/src/services/saleService.ts`:
    1. Cấu hình tương tự cho hàm `exportSaleOrderExcel` để đính kèm token khi xuất Excel hóa đơn.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Sửa lỗi `formOrderId` được khởi tạo bằng `nextId` (dựa trên danh sách `orders` đã phân trang ở client-side) khi tạo đơn hàng mới, dẫn đến xung đột ID đã tồn tại trong cơ sở dữ liệu. Thiết lập giá trị mặc định là chuỗi rỗng `''` để cho phép cơ sở dữ liệu tự động tăng (auto-increment) ID mới.
    2. Thay thế hai nút "Xem chi tiết" và "Chỉnh sửa đơn" gọi `fetch` native sang sử dụng hàm `purchaseService.getPurchaseOrderById` để gửi kèm thông tin đăng nhập (JWT token), tránh lỗi 403 Forbidden khi tải dữ liệu đơn nhập.
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Đồng bộ sửa lỗi khởi tạo ID đơn hàng bán mới `formOrderId` thành chuỗi rỗng `''` thay vì sử dụng client-side `nextId` để tránh xung đột mã hóa đơn.
- **Kết quả xác thực**:
  - Biên dịch Backend Spring Boot thành công (`mvn compile` -> `BUILD SUCCESS`).
  - Chạy kiểm tra kiểu tĩnh TypeScript (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

## Cập nhật lúc 10:10 - 23/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/accounts/page.tsx`:
    1. Bỏ các thẻ thống kê stats (Tổng số tài khoản, Số vai trò hiện có, Quyền cơ sở) ở đầu trang quản lý tài khoản theo yêu cầu.
- **Kết quả xác thực**:
  - Biên dịch Frontend Next.js và kiểm tra kiểu dữ liệu tĩnh thành công.

## Cập nhật lúc 02:05 - 22/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/repository/ChiTietNhapHangRepository.java`:
    1. Bổ sung phương thức `findFiltered` kèm truy vấn JPQL dùng `JOIN FETCH` để lấy dữ liệu sản phẩm chi tiết theo thời gian lập đơn nhập.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BaoCaoService.java`:
    1. Thêm phương thức `getProductImports` lấy chi tiết danh sách sản phẩm nhập theo bộ lọc thời gian và tìm kiếm.
    2. Thêm phương thức `getProductImportsStats` thống kê tổng chi phí nhập, công nợ, số đơn hàng và lượng sản phẩm nhập.
  - `web/Backend/src/main/java/com/cafe/jenika/controller/BaoCaoController.java`:
    1. Expose 2 endpoints mới: `GET /api/v1/bao-cao/san-pham-nhap` và `GET /api/v1/bao-cao/san-pham-nhap/stats`.
  - `web/Frontend/src/services/reportService.ts`:
    1. Thêm các hàm `getProductImports` và `getProductImportsStats` gọi API về Backend.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Refactor giao diện thành 2 Tab (Đơn nhập và Sản phẩm) tương tự trang Bán hàng.
    2. Tách biệt các bộ lọc tìm kiếm, bộ lọc ngày tháng và thống kê động theo Tab.
    3. Hỗ trợ xuất CSV cho cả 2 Tab: Xuất danh sách đơn nhập hàng và Chi tiết sản phẩm đã nhập.
- **Kết quả xác thực**: Chạy `mvn compile` và `npx tsc --noEmit` thành công hoàn toàn.

## Cập nhật lúc 02:00 - 22/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/repository/BanHangRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<BanHang>` để hỗ trợ Specifications cho Bán hàng.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhapHangRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<NhapHang>` để hỗ trợ Specifications cho Nhập hàng.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/ThuChiRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<ThuChi>` để hỗ trợ Specifications cho Thu Chi.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/DoiTacRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<DoiTac>` để hỗ trợ Specifications cho Đối Tác.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhanVienRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<NhanVien>` để hỗ trợ Specifications cho Nhân Viên.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhatKyRepository.java`:
    1. Kế thừa `JpaSpecificationExecutor<NhatKy>` để hỗ trợ Specifications cho Nhật Ký Hoạt Động.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/BanHangSpecification.java`:
    1. Viết Specification lọc dynamic cho đơn hàng bán theo từ khóa, trạng thái, và khoảng thời gian.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhapHangSpecification.java`:
    1. Viết Specification lọc dynamic cho đơn nhập hàng.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/ThuChiSpecification.java`:
    1. Viết Specification lọc dynamic cho thu chi theo từ khóa, phân loại và khoảng thời gian.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/DoiTacSpecification.java`:
    1. Viết Specification lọc dynamic cho đối tác theo từ khóa.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhanVienSpecification.java`:
    1. Viết Specification lọc dynamic cho nhân viên.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhatKySpecification.java`:
    1. Viết Specification lọc dynamic cho nhật ký hoạt động.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BanHangService.java`, `NhapHangService.java`, `ThuChiService.java`, `DoiTacService.java`, `NhanVienService.java`, `NhatKyService.java`:
    1. Bổ sung các phương thức lấy dữ liệu phân trang Spring Pageable sử dụng Specification tương ứng.
    2. Viết thêm các phương thức tính toán thống kê (stats) hoàn toàn trên server-side cho Thu Chi và Nhập hàng.
  - `web/Backend/src/main/java/com/cafe/jenika/controller/BanHangController.java`, `NhapHangController.java`, `ThuChiController.java`, `MetadataController.java`:
    1. Thêm mapping `/page` và `/stats` phục vụ các API phân trang và thống kê trên máy chủ.
  - `web/Frontend/src/services/saleService.ts`, `purchaseService.ts`, `partnerService.ts`, `expenseService.ts`, `activityService.ts`:
    1. Bổ sung các phương thức gọi API phân trang (`*Page`) và API thống kê (`*Stats`).
  - `web/Frontend/src/app/sales/page.tsx`, `purchases/page.tsx`, `partners/page.tsx`, `expenses/page.tsx`, `activity/page.tsx`:
    1. Thay thế phân trang client-side bằng phân trang server-side hoàn toàn.
    2. Sử dụng hook debounce 400ms để tối ưu hóa tần suất gọi API khi tìm kiếm.
    3. Thêm thuộc tính `data-lenis-prevent` tĩnh vào các container overflow-auto để giải quyết cảnh báo Hydration Mismatch.
- **Kết quả xác thực**: Chạy `mvn compile` thành công (BUILD SUCCESS) và `npx tsc --noEmit` thành công (không có lỗi TypeScript).

## Cập nhật lúc 01:45 - 18/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/globals.css`:
    1. Bổ sung lớp ghi đè `html.light .bg-\[\#0A0E17\]\/95` để chuyển đổi màu nền các popup overlay từ màu tối sang màu sáng (`rgba(255, 255, 255, 0.95)`) khi hệ thống ở chế độ giao diện sáng.
    2. Bổ sung lớp ghi đè `html.light .border-white\/\[0\.02\]` để chuyển đổi màu đường viền siêu mờ sang màu tối dịu (`rgba(15, 23, 42, 0.02)`) trong giao diện sáng.
    3. Cấu hình ghi đè chế độ hiển thị native date/time pickers (`input[type="date"]`, `input[type="datetime-local"]`, `input[type="time"]`) bằng cách gán `color-scheme: light` và tắt hiệu ứng `filter: invert(1)` trong giao diện sáng, đảm bảo biểu tượng lịch và đồng hồ hiển thị rõ ràng trên nền sáng.
- **Kết quả xác thực**: Các thay đổi đã được áp dụng và tự động biên dịch lại thành công.

## Cập nhật lúc 05:00 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/security/TokenBucket.java`:
    1. Tạo mới lớp hỗ trợ giới hạn tốc độ luồng an toàn (thread-safe Token Bucket).
    2. Cấu hình dung lượng 60 token và nạp lại 1 token/giây (tương ứng 60/phút).
    3. Hỗ trợ phương thức `isExpired()` để kiểm tra trạng thái nhàn rỗi quá 5 phút của IP phục vụ thu hồi bộ nhớ.
  - `web/Backend/src/main/java/com/cafe/jenika/security/RateLimitingFilter.java`:
    1. Tạo mới bộ lọc servlet toàn cục (`Filter`) để chặn mọi request tới API.
    2. Phân biệt người dùng dựa trên IP (ưu tiên `X-Forwarded-For` từ proxy, CDN trước khi fallback về IP socket).
    3. Bỏ qua các yêu cầu CORS preflight `OPTIONS`.
    4. Trả về mã lỗi HTTP `429 Too Many Requests` kèm payload thông báo JSON chuẩn nếu vượt giới hạn.
    5. Cấu hình task định kỳ `@Scheduled` mỗi 5 phút dọn dẹp các bucket đã hết hạn khỏi bộ nhớ.
  - `web/Backend/src/main/java/com/cafe/jenika/WebCafeApplication.java`:
    1. Bổ sung annotation `@EnableScheduling` để kích hoạt cơ chế dọn dẹp bộ nhớ định kỳ.
  - `web/Backend/src/test/java/com/cafe/jenika/security/RateLimitingFilterTest.java`:
    1. Tạo mới bộ kiểm thử tự động với Mockito bao phủ các kịch bản: chấp nhận OPTIONS, giới hạn dưới 60, từ chối request thứ 61, kiểm tra IP từ Header, dọn dẹp bucket hết hạn.
- **Kết quả xác thực**: Chạy lệnh `mvn test` trong thư mục `web/Backend` thành công hoàn toàn, toàn bộ 5 test case đều PASS (`BUILD SUCCESS`).

## Cập nhật lúc 04:47 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/globals.css`:
    1. Bổ sung cấu hình ghi đè CSS `html.light .hover\:text-white:hover` để chuyển đổi màu chữ khi hover của class `hover:text-white` thành màu chữ tối chính (`var(--text-primary)`) trong giao diện sáng.
    2. Sửa lỗi chữ bị đổi sang màu trắng và trùng màu nền sáng (gây mất chữ/tàng hình khi di chuột qua các mục danh mục, tab điều hướng và các menu).
- **Kết quả xác thực**: Biên dịch ứng dụng (`npm run build`) thành công hoàn toàn.

## Cập nhật lúc 04:44 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/components/providers/SmoothScrollProvider.tsx`:
    1. Cải tiến component `SmoothScrollProvider` bằng cách thêm một `MutationObserver` để tự động gán thuộc tính `data-lenis-prevent` vào toàn bộ các container scroll con có class `.overflow-y-auto` hoặc `.overflow-auto` (kể cả các phần tử sinh ra động do chuyển trang hoặc mở modal).
    2. Khắc phục triệt để lỗi cuộn chuột bị khóa (do Lenis root chặn tất cả sự kiện wheel trên viewport cố định).
- **Kết quả xác thực**: Biên dịch ứng dụng (`npm run build`) thành công hoàn toàn, tính năng cuộn chuột của các bảng biểu hoạt động lại bình thường.

## Cập nhật lúc 04:42 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/package.json`:
    1. Cài đặt thư viện cuộn mượt `lenis`.
  - `web/Frontend/src/components/providers/SmoothScrollProvider.tsx`:
    1. Tạo mới component Client-side provider cấu hình và khởi chạy cuộn mượt bằng `ReactLenis` từ `lenis/react`.
  - `web/Frontend/src/app/layout.tsx`:
    1. Tích hợp `SmoothScrollProvider` bọc ngoài toàn bộ cấu trúc nội dung của trang web để áp dụng cuộn mượt.
- **Kết quả xác thực**: Biên dịch ứng dụng (`npm run build`) thành công hoàn toàn.

## Cập nhật lúc 04:38 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/globals.css`:
    1. Định nghĩa lại các token màu Tailwind v4 thông qua CSS variables, lấy giao diện sáng (Light Theme) làm mặc định cho `:root`, đồng thời bảo toàn giao diện tối (Dark Theme) khi ở trạng thái `html:not(.light)`.
    2. Viết thêm các thuộc tính override đặc biệt cho các mã màu và đường viền hex tối cứng (ví dụ: `.bg-\[\#0A0E17\]`, `.border-white\/10`) để chuyển đổi tự động và hoàn hảo sang cấu trúc giao diện sáng khi class `.light` được gắn vào thẻ `<html>`.
  - `web/Frontend/src/app/layout.tsx`:
    1. Inject inline script kiểm tra `localStorage` và gán class `.light` ngay tại `<head>` để đảm bảo không bị hiện tượng giật/nháy giao diện tối (FART) khi tải trang.
  - `web/Frontend/src/app/settings/page.tsx`:
    1. Cấu hình nút chuyển đổi giao diện trong phần cài đặt kết nối trực tiếp với sự thay đổi của class hệ thống và đồng bộ lưu trữ tại `localStorage`.
- **Kết quả xác thực**: Biên dịch kiểu dữ liệu tĩnh (`npx tsc --noEmit`) thành công hoàn toàn.

## Cập nhật lúc 04:30 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Tích hợp Component `Pagination` cho danh sách hóa đơn bán (Tab 0) và danh sách phân rã sản phẩm đã bán (Tab 1).
    2. Thêm các state `currentPage`, `itemsPerPage` và viết hook `useEffect` tự động reset trang về 1 khi chuyển đổi tab hoặc thay đổi bất kỳ bộ lọc nào.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Tích hợp Component `Pagination` cho bảng đơn nhập hàng.
    2. Tự động reset trang về 1 khi bộ lọc tìm kiếm / ngày tháng thay đổi.
  - `web/Frontend/src/app/expenses/page.tsx`:
    1. Tích hợp Component `Pagination` cho bảng nhật ký thu chi thực tế.
    2. Tự động reset trang về 1 khi chuyển đổi danh mục hoặc thay đổi bộ lọc tìm kiếm.
  - `web/Frontend/src/app/activity/page.tsx`:
    1. Tích hợp Component `Pagination` cho bảng nhật ký hoạt động hệ thống.
    2. Tự động reset trang về 1 khi đổi phân hệ lọc hoặc từ khóa tìm kiếm.
- **Kết quả xác thực**: Chạy biên dịch kiểm tra kiểu dữ liệu tĩnh (`npx tsc --noEmit`) thành công không phát sinh lỗi.

## Cập nhật lúc 04:22 - 16/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/repository/NhapHangRepository.java`:
    1. Tích hợp annotation `@Query` với JPQL `LEFT JOIN FETCH` để gom toàn bộ dữ liệu quan hệ (`doiTac`, `nhanVien`, `chiTietNhapHangs` và các nested entity như `sanPham`, `danhMuc`, `donViTinh`, `nhomSanPham`) trong 1 câu truy vấn duy nhất.
    2. Loại bỏ triệt để vấn đề truy vấn N+1 làm suy giảm hiệu năng cơ sở dữ liệu.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Thiết kế lại cơ chế tải dữ liệu: Trì hoãn việc gọi 6 API tải metadata khi tải trang (lazy load), chỉ kích hoạt fetch khi người dùng bấm "Lập đơn mới" hoặc "Chỉnh sửa đơn".
    2. Loại bỏ 1 lượt gọi API `getActiveProducts()` trùng lặp trên hook `mount`, giảm tải số lượng request từ 8 xuống còn duy nhất 1 request lúc khởi tạo trang.
    3. Bổ sung giao diện loading overlay cao cấp phủ lên form khi metadata đang tải để tránh click nhầm hoặc tương tác lỗi.
- **Kết quả xác thực**: Biên dịch Backend thành công (`BUILD SUCCESS`) và kiểm tra kiểu dữ liệu Frontend không lỗi (`npx tsc --noEmit`).

## Cập nhật lúc 05:47 - 20/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Bổ sung cột tiêu đề "Giá bán" trong phần danh sách sản phẩm mua.
    2. Bổ sung trường input `Giá bán` cho phép chỉnh sửa giá bán trực tiếp trên từng dòng sản phẩm.
    3. Đồng bộ hóa việc tính toán tổng cộng hóa đơn dựa trên giá bán thực tế chỉnh sửa thay vì dùng mặc định `giaBanHienTai` của sản phẩm.
    4. Cập nhật `chiTietBanHangs` trong payload gửi đi để lưu đúng trường `giaBan`.
    5. Cập nhật `onApply` từ OCR Scanner để tự động mapping giá bán từ hóa đơn đã quét vào trường nhập `giaBan` mới.
  - `web/Backend/src/main/java/com/cafe/jenika/service/BanHangService.java`:
    1. Cho phép nhận `giaBan` do khách hàng chỉnh sửa (nếu có và > 0) để lưu trữ trong chi tiết bán hàng.
    2. Tự động cập nhật lại `giaBanHienTai` của sản phẩm tương ứng trong database khi tạo hoặc chỉnh sửa hóa đơn bán.
- **Kết quả xác thực**: Các thay đổi đã được áp dụng, tự động biên dịch thành công.

## Cập nhật lúc 05:40 - 20/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/purchases/page.tsx` & `web/Frontend/src/app/sales/page.tsx`: Cập nhật hàm `onApply` nhận dữ liệu từ `OcrScanner` để:
    1. Tự động tách phần số đằng sau chữ "HĐ" trong mã hóa đơn (ví dụ: `HĐ59` -> `59`) và điền trực tiếp vào ô nhập ID đơn hàng (`Số HD/ID`).
    2. Điền chính xác ngày hóa đơn đã trích xuất vào trường "Ngày lắp máy" / "Ngày nhận".
  - `web/Frontend/src/components/features/OcrScanner.tsx` & `web/Frontend/src/app/api/ocr/route.ts`: Cải tiến bộ lọc ngày `dateRegex` để nhận diện thêm định dạng ngày 8 chữ số không có ký tự phân cách (ví dụ `13032026` -> ngày `2026-03-13`).
- **Kết quả xác thực**: Các thay đổi đã được áp dụng và tự động biên dịch lại thành công.

## Cập nhật lúc 05:35 - 20/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/components/features/OcrScanner.tsx`:
    1. Tích hợp thanh tìm kiếm trực tiếp vào menu sản phẩm thành một Combobox (Searchable Select) đồng nhất và hiện đại.
    2. Người dùng có thể click trực tiếp vào ô sản phẩm để nhập từ khóa lọc, thay vì hiển thị ô tìm kiếm riêng biệt.
    3. Hỗ trợ lọc nhanh danh sách sản phẩm theo từ khóa (tiếng Việt không dấu và không phân biệt hoa thường).
    4. Giữ sản phẩm hiện tại luôn hiển thị ở đầu danh sách để tránh mất trạng thái lựa chọn khi đang nhập từ khóa mới.
    5. Đồng bộ hóa thu hồi danh sách khi nhấp ra ngoài hoặc thay đổi trạng thái hóa đơn.
- **Kết quả xác thực**: Chạy Typecheck (`npx tsc --noEmit`) thành công, không phát hiện lỗi kiểu dữ liệu.

## Cập nhật lúc 05:30 - 20/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/components/features/OcrScanner.tsx`: Bổ sung tính năng tự động phóng to (upscale) ảnh có kích thước nhỏ/độ phân giải thấp lên 2 hoặc 3 lần trước khi chạy OCR.
    1. Giúp Tesseract nhận diện chính xác nét chữ siêu nhỏ.
    2. Tự động điều chỉnh tỷ lệ các ngưỡng lọc dòng kẻ bảng (gridline run length và thickness) tương ứng với tỷ lệ phóng to để giữ nguyên hiệu năng khử nhiễu lưới Excel.
- **Kết quả xác thực**: Các thay đổi đã được Next.js tự động biên dịch lại thành công.

## Cập nhật lúc 05:20 - 20/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/components/features/OcrScanner.tsx`: Cập nhật logic phân tích văn bản OCR phía Client-side:
    1. Loại bỏ các ký tự phân cách/nhiễu (như hyphens `-`, symbols) nằm giữa dòng để tránh làm xô lệch thứ tự các từ khi tách cột.
    2. Nâng cấp Regex xóa số thứ tự đầu dòng (`STT`) để nhận diện và loại bỏ thêm các ký tự nhiễu kèm theo (như `=`, `.`, `-`).
    3. Thêm đơn vị tính `ca` và `cá` (loại lỗi OCR của `cái`) vào danh sách các đơn vị tính được nhận diện phổ biến, giới hạn tìm kiếm ĐVT ở các chỉ số `idx > 0` để tránh bắt nhầm từ đầu tiên trong tên sản phẩm (như "Ca đong").
  - `web/Frontend/src/app/api/ocr/route.ts`: Đồng bộ hóa logic phân tích văn bản OCR phía Server-side giống với Client-side để đảm bảo kết quả trích xuất nhất quán.
- **Kết quả xác thực**: Các thay đổi đã được áp dụng và tự động biên dịch lại thành công.

## Cập nhật lúc 05:25 - 19/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`: Cập nhật component `SearchableProductSelect` để:
    1. Tăng chiều cao tối đa của danh sách dropdown (`max-h-80` thay vì `max-h-48`).
    2. Tự động hiển thị đầy đủ danh sách sản phẩm khi người dùng click/focus mở dropdown, thay vì chỉ lọc ra sản phẩm đang được chọn.
    3. Tự động chọn (select) toàn bộ text khi focus để người dùng có thể nhập từ khóa tìm kiếm mới ngay lập tức.
    4. Hỗ trợ tìm kiếm tiếng Việt không dấu (accent-insensitive) và không phân biệt hoa thường.
- **Kết quả xác thực**: Chạy Typecheck (`npx tsc --noEmit`) thành công, không phát hiện lỗi kiểu dữ liệu.

## Cập nhật lúc 05:20 - 19/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`: Cập nhật vị trí hiển thị của menu dropdown danh sách sản phẩm trong component `SearchableProductSelect` hiển thị hướng lên trên (`bottom-full mb-1`) thay vì hướng xuống dưới (`mt-1`), đồng thời tự động đảo hướng icon caret (`arrow_drop_up`/`arrow_drop_down`) tương ứng với trạng thái mở/đóng.
- **Kết quả xác thực**: Giao diện đã được Next.js tự động cập nhật và chạy ổn định.

## Cập nhật lúc 05:15 - 19/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`: Loại bỏ nhân viên mặc định khi mở form lập hóa đơn bán mới (nhân viên phụ trách sẽ được để trống và tìm kiếm thủ công).
  - `web/Frontend/src/app/purchases/page.tsx`: Loại bỏ nhân viên mặc định khi mở form lập đơn nhập mới để thống nhất hành vi chọn nhân viên thủ công.
- **Kết quả xác thực**: Các thay đổi đã được Next.js tự động biên dịch thành công.

## Cập nhật lúc 05:10 - 19/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`: Loại bỏ các số thứ tự (10., 11., 12.) trước các tiêu đề trường ghi chú và danh sách sản phẩm trong form lập/sửa hóa đơn xuất.
  - `web/Frontend/src/app/purchases/page.tsx`: Loại bỏ các số thứ tự (10., 11.) tương tự trước tiêu đề ghi chú và danh sách sản phẩm trong form nhập hàng để đảm bảo tính nhất quán của giao diện.
- **Kết quả xác thực**: Các thay đổi giao diện đã được Next.js tự động biên dịch lại thành công.

---
## Cập nhật lúc 02:50 - 19/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/sales/page.tsx`:
    1. Bổ sung `customerContainerRef` và `employeeContainerRef` để nhận biết sự kiện click bên ngoài khung dropdown tìm kiếm Khách hàng và Nhân viên.
    2. Viết hook `useEffect` bắt sự kiện click chuột để tự động đóng dropdown và hoàn trả lại tên Khách hàng/Nhân viên hiện đang chọn nếu người dùng hủy bỏ việc tìm kiếm/chỉnh sửa.
    3. Thêm trình xử lý `onFocus={(e) => e.target.select()}` trên cả hai ô nhập liệu để tự động bôi đen toàn bộ văn bản khi click, giúp việc xóa/nhập từ khóa tìm kiếm mới dễ dàng hơn.
    4. Cải tiến bộ lọc tìm kiếm sử dụng hàm `removeDiacritics` hỗ trợ tìm kiếm tiếng Việt không dấu và bỏ qua việc lọc khi giá trị ô nhập khớp với tên đối tượng hiện đang được chọn.
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Đồng bộ hóa toàn bộ các cải tiến về trải nghiệm tìm kiếm, bôi đen khi focus, đóng khi nhấp chuột ra ngoài và tìm kiếm không dấu cho ô nhập Nhà cung cấp (NCC) và Nhân viên.
- **Kết quả xác thực**: Chạy biên dịch kiểm tra kiểu tĩnh (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

---
## Cập nhật lúc 08:46 - 22/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/repository/SanPhamSpecification.java`:
    1. Tạo lớp dynamic-filtering `SanPhamSpecification` sử dụng JPA Specification API để sinh các câu truy vấn động.
  - `web/Backend/src/main/java/com/cafe/jenika/repository/SanPhamRepository.java`:
    1. Bổ sung kế thừa `JpaSpecificationExecutor<SanPham>` cho Repository.
    2. Thêm các câu truy vấn JPA/SQL tối ưu để thống kê nhanh tổng số lượng tồn, tổng giá trị kho, số lượng hàng cảnh báo, đang kinh doanh, và bị ẩn.
  - `web/Backend/src/main/java/com/cafe/jenika/service/SanPhamService.java`:
    1. Triển khai phương thức `getProductsPaginated` hỗ trợ truy vấn phân trang qua Specification.
    2. Triển khai phương thức `getProductStats` trả về kết quả thống kê kho.
  - `web/Backend/src/main/java/com/cafe/jenika/controller/SanPhamController.java`:
    1. Tạo hai endpoints mới `GET /api/v1/san-pham/page` (cho dữ liệu trang hiện tại và tổng số trang) và `GET /api/v1/san-pham/stats` (cho dòng thông số phía trên và số đếm tab).
  - `web/Frontend/src/services/productService.ts`:
    1. Khai báo interface `PageResponse<T>` phản ánh kiểu Page từ Spring Boot.
    2. Định nghĩa các phương thức API client `getProductsPage` và `getProductStats`.
  - `web/Frontend/src/app/inventory/page.tsx`:
    1. Chuyển đổi toàn bộ logic hiển thị và tính toán phân trang từ Client-side (in-memory) sang Server-side.
    2. Bổ sung hook debounce 400ms cho việc tìm kiếm tên và mã sản phẩm để giảm số lượng API request gửi lên Server.
    3. Đồng bộ hóa việc gọi lại API tải trang mới và stats mỗi khi người dùng thay đổi bộ lọc, chuyển tab hoặc thêm/sửa/xóa/khôi phục sản phẩm.
    4. Thêm thuộc tính `data-lenis-prevent=""` trực tiếp vào ba phần tử có tính năng cuộn (overflow) để giải quyết triệt để cảnh báo Hydration Mismatch do MutationObserver thêm thuộc tính động khi React đang mount.
  - `web/Frontend/src/app/layout.tsx`:
    1. Bổ sung `suppressHydrationWarning` cho thẻ `<html>` để triệt tiêu cảnh báo Hydration Mismatch sinh ra bởi đoạn mã kiểm tra theme chạy trực tiếp tại thẻ `<head>` trước khi React kịp hydrate.
- **Kết quả xác thực**:
  - Biên dịch Backend Spring Boot thành công (`mvn compile` -> `BUILD SUCCESS`).
  - Kiểm tra kiểu dữ liệu tĩnh Frontend Next.js thành công (`npx tsc --noEmit` -> OK).

---
## Cập nhật lúc 09:30 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/service/NhapHangService.java`:
    1. Chỉnh sửa logic kiểm tra thay đổi giá nhập sản phẩm trong hai phương thức `createImportOrder` và `updateImportOrder`.
    2. Thay đổi cách gán giá bán hiện tại (`giaBanHienTai`) cho các sản phẩm mới được tự động tạo do thay đổi giá nhập: thiết lập giá bán mới bằng chính giá nhập (`detail.getGiaNhap()`) thay vì sao chép trực tiếp giá bán từ sản phẩm cũ trước đó (`sp.getGiaBanHienTai()`).
- **Kết quả xác thực**:
  - Đã chạy kiểm tra biên dịch backend thành công (`mvn compile` -> `BUILD SUCCESS`).

---
## Cập nhật lúc 09:35 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/purchases/page.tsx`:
    1. Chỉnh sửa số lượng mặc định (`soLuong`) của chi tiết mặt hàng khi lập đơn nhập hàng hoặc thêm dòng mặt hàng mới từ 10 thành 1.
- **Kết quả xác thực**:
  - Đã chạy kiểm tra kiểu tĩnh của Frontend (`npx tsc --noEmit`) thành công hoàn toàn không có lỗi.

---
## Cập nhật lúc 10:31 - 28/07/2026
- **Danh sách file thay đổi**:
  - `web/Backend/src/main/java/com/cafe/jenika/service/BaoCaoService.java`:
    1. Chỉnh sửa logic tính lợi nhuận cho sản phẩm tặng: tính lợi nhuận bằng `giaBan - giaNhap` (giá bán = 0 nên lợi nhuận sẽ là âm) thay vì đặt bằng 0 như trước đây.
    2. Cập nhật thống kê lợi nhuận sản phẩm (`prodProfit`) trong `getProductSalesStats` để khấu trừ giá nhập của sản phẩm tặng.
- **Kết quả xác thực**:
  - Biên dịch Backend Spring Boot thành công (`mvn compile` -> `BUILD SUCCESS`).

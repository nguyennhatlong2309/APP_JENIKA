---
name: ocr-invoice-order-creator
description: >-
  Gửi ảnh hóa đơn qua POST request đến local API /api/ocr để lấy text, dùng khả năng suy luận của LLM để sửa lỗi chính tả và trích xuất thông tin hóa đơn (Mã HĐ chỉ số, sản phẩm, đối tác), đối khớp Database Spring Boot và tạo đơn hàng.
---

# OCR Invoice & Order Creator Skill

## Overview
Skill này hướng dẫn Agent (như Hermes) cách xử lý một ảnh hóa đơn (nhập/bán hàng) do người dùng cung cấp bằng cách gửi tệp tin tới API OCR cục bộ của ứng dụng Next.js, sửa lỗi chính tả từ kết quả trả về, thực hiện đối khớp thông minh với cơ sở dữ liệu qua các API Spring Boot, và gửi yêu cầu tạo mới đơn hàng sau khi người dùng xác nhận.

## Dependencies
- **API OCR cục bộ (Next.js):** Đang chạy ở cổng `http://localhost:3000` (Endpoint: `POST /api/ocr`).
- **Backend Spring Boot:** Đang chạy ở cổng `http://localhost:8080` (Base URL: `http://localhost:8080/api/v1`).
- **Quyền hạn:** Agent có quyền chạy lệnh cURL/PowerShell qua công cụ `run_command` để gọi các API cục bộ.

## Quy trình Thực hiện (Workflow)

### Bước 1: Gửi ảnh tới API Server-side OCR
1. Xác định đường dẫn tệp ảnh tuyệt đối của hóa đơn.
2. Sử dụng công cụ `run_command` để gọi API OCR Next.js qua `curl.exe` (để tránh xung đột Alias trên PowerShell của Windows):
   ```bash
   curl.exe -X POST -F "file=@<đường_dẫn_tuyệt_đối_ảnh>" -F "lang=vie" http://localhost:3000/api/ocr
   ```
3. Lưu và phân tích phản hồi JSON từ API. Dữ liệu trả về sẽ có cấu trúc:
   ```json
   {
     "success": true,
     "rawText": "...",
     "invoiceId": "...",
     "date": "...",
     "totalAmount": 0,
     "items": []
   }
   ```

### Bước 2: Phân tích & Sửa lỗi dữ liệu OCR bằng LLM (Trường hợp Model không hỗ trợ Vision)
Do kết quả OCR thô từ Tesseract (`rawText`) thường bị lỗi font và nhiễu ký tự nghiêm trọng (ví dụ: quét `260.000` thành `m0`000`, quét `1` thành `@1`, quét `Anh Tân` thành `Mh '1'âIl`), Agent cần sử dụng danh sách sản phẩm/đối tác lấy được từ Database ở Bước 3 để đối chiếu ngược và sửa lỗi theo các quy tắc sau:

1. **Đối khớp tên sản phẩm (Fuzzy Match):** So sánh các từ bị lỗi chính tả với danh sách sản phẩm thực tế trong DB. 
   - Ví dụ: `1J1]ê Eend 9/1` khớp với `Cà phê Blend 9/1` (ID: 513).
   - Ví dụ: `Gemilai 3200B Pro (TL_ Đ` khớp với `Máy pha cà phê Gemilai 3200B Pro (TL_Đen)` (ID: 112).
2. **Xử lý Mã hóa đơn:** Lọc ký tự chữ, chỉ giữ lại số. Tránh đoán sai các ký tự nhiễu của Tesseract đứng trước số (ví dụ: `m3135` -> mã đúng là `135` chứ không phải `3135`, vì ký tự `m3` hoặc `HĐ` bị quét lỗi từ chữ `HĐ`).
3. **Ngày lập hóa đơn:** Chuẩn hóa về dạng `YYYY-MM-DD`. Nếu ngày bị lỗi (ví dụ: `W/04/2026`), tuyệt đối không tự ý điền ngày ngẫu nhiên (như `2026-04-04`). Hãy kiểm tra ngày hiện tại hoặc để trống để hỏi người dùng.
4. **Đối tác (Khách hàng/NCC):** Trích xuất tên (ví dụ: `Mh '1'âIl` -> `Anh Tân`).

> [!IMPORTANT]
> **Quy tắc xử lý số liệu bị lỗi font (Đơn giá, Số lượng, Tổng tiền):**
> - Tesseract thường quét sai các số tiền lớn (ví dụ: `260.000` thành `m0`000` hoặc `uo,ooo`). Agent **tuyệt đối không được đoán mò** số lượng hay đơn giá (như đoán số lượng là 9, đơn giá 10.000).
> - **Giải pháp:** Đối chiếu với giá bán/giá nhập hiện tại của sản phẩm đó trong Database để đưa ra gợi ý giá trị mặc định.
> - **Bắt buộc hỏi xác nhận:** Trong bảng tóm tắt hiển thị cho người dùng ở Bước 6, Agent phải đánh dấu `[Nghi vấn]` hoặc `[Cần xác nhận]` tại các trường số liệu bị lỗi chữ và yêu cầu người dùng điền hoặc xác nhận lại trước khi tạo payload. Ví dụ:
>   *   *Mã HĐ: `135` (Từ OCR: `m3135` - Cần xác nhận)*
>   *   *Khách hàng: `Anh Tân` (Từ OCR: `Mh '1'âIl' - Cần xác nhận)*
>   *   *Đơn giá SP 'Cà phê Blend 9/1': OCR nhận diện lỗi (`m0`000`). Hệ thống gợi ý giá hiện tại: [Giá trong DB]. Xin vui lòng nhập giá đúng.*

### Bước 3: Thu thập thông tin từ cơ sở dữ liệu hiện tại
Sử dụng công cụ `run_command` để truy vấn dữ liệu từ Backend Spring Boot cục bộ phục vụ việc đối khớp:
- **Sản phẩm:** `GET http://localhost:8080/api/v1/san-pham`
- **Đối tác:** `GET http://localhost:8080/api/v1/metadata/doi-tac`
- **Nhân viên:** `GET http://localhost:8080/api/v1/metadata/nhan-vien`

*Lưu ý: Thiết lập mã hóa đầu ra của Terminal hoặc Python (nếu chạy script phụ trợ) thành UTF-8 để hiển thị tiếng Việt có dấu chính xác.*

### Bước 4: Đối khớp dữ liệu thông minh
1. **Đối khớp đối tác (DoiTac):** So sánh tên đối tác trên hóa đơn với cơ sở dữ liệu.
   - *Trường hợp chưa tồn tại đối tác:* Thông báo cho người dùng và đề xuất tạo mới thông qua API:
     `POST http://localhost:8080/api/v1/metadata/doi-tac` với body: `{"ten": "[Tên đối tác]", "sdt": "[SĐT]", "diaChi": "[Địa chỉ]", "email": ""}`.
2. **Đối khớp sản phẩm (SanPham):** Sử dụng so khớp gần đúng (Fuzzy match) hoặc so khớp tập con (substring) để tìm `id` sản phẩm thích hợp từ danh sách sản phẩm hệ thống.
   - *Nếu khớp 1 sản phẩm:* Ghi nhận `productId`.
   - *Nếu khớp nhiều sản phẩm (ví dụ: cùng tên nhưng khác đời máy/giá cả):* Liệt kê danh sách tất cả sản phẩm khớp kèm thông tin ID, giá, tồn kho để người dùng tự chọn lựa chọn mong muốn.
   - *Nếu không tìm thấy sản phẩm nào khớp:* Đề xuất tạo sản phẩm mới thông qua API sản phẩm.
3. **Đối khớp nhân viên (NhanVien):** Gán mặc định nhân viên lập đơn đầu tiên trong danh sách (thường là ID 1) hoặc hỏi ý kiến người dùng để chọn nhân viên chính xác.

### Bước 5: Kiểm tra tính nhất quán & tồn kho
1. **Tính toán lại tổng tiền:** Nhân lại `soLuong * gia` của tất cả các dòng sản phẩm để kiểm tra chéo độ chính xác của hóa đơn.
2. **Kiểm tra tồn kho (Chỉ áp dụng với đơn bán hàng - sales):**
   - Đảm bảo tồn kho của sản phẩm trong hệ thống (`soLuongTon`) lớn hơn hoặc bằng số lượng bán trên hóa đơn.
   - *Nếu sản phẩm bị hết hàng (tồn kho = 0 hoặc không đủ):* Thông báo cho người dùng biết và đề xuất 2 giải pháp:
     - Tạo một Đơn Nhập Hàng (`nhap-hang`) trước để cộng kho lên rồi mới tạo đơn bán hàng.
     - Cho phép người dùng chuyển sang đối khớp với sản phẩm thay thế cùng loại còn hàng.

### Bước 6: Báo cáo & Yêu cầu xác nhận từ người dùng
Trước khi lưu đơn, in ra báo cáo tóm tắt chi tiết hóa đơn bằng Markdown theo cấu trúc rõ ràng như sau:

📋 **Tóm tắt Hóa đơn**
- **Loại giao dịch:** [Nhập hàng / Bán hàng]
- **Mã HĐ:** [Mã hóa đơn chỉ có số] | **Ngày lập:** [YYYY-MM-DD]
- **Đối tác:** [Tên đối tác] (ID: [partnerId]) | **Nhân viên:** [Tên nhân viên] (ID: [employeeId])

**Danh sách sản phẩm:**
| STT | Tên hàng (HĐ) | Sản phẩm Hệ thống | ID | Số lượng | Đơn giá (OCR) | Đơn giá (DB) | Thành tiền (Hệ thống) | Tồn kho | Ghi chú / Cảnh báo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | [Tên hàng trên HĐ] | [Tên sản phẩm DB] | [ID] | [Số lượng] [⚠️ nếu nghi vấn] | [Đơn giá OCR] | [Đơn giá DB] | [Thành tiền] | [Tồn kho] | [Cảnh báo chênh lệch giá, số lượng...] |

*Ghi chú về đơn giá & ghi đè:*
- Đối với đơn **Bán hàng (Sales)**: Hệ thống Spring Boot mặc định ghi đè giá bán thực tế theo `giaBanHienTai` trong cơ sở dữ liệu (xem chi tiết tại [BanHangService.java](file:///c:/Long/hk2_2025-2026/PersonalProject/CAFE_DI_ROM/web/Backend/src/main/java/com/cafe/jenika/service/BanHangService.java#L97-L98)). Nếu giá OCR khác giá DB, hãy hiển thị cảnh báo: `⚠️ Giá OCR ([giá OCR]) khác giá DB ([giá DB]). Backend sẽ ghi đè giá bán về [giá DB].`
- Đối với đơn **Nhập hàng (Purchase)**: Cho phép áp dụng giá nhập trực tiếp từ hóa đơn. Nếu có thay đổi giá nhập, hệ thống sẽ tự động tạo phiên bản sản phẩm mới hoặc cập nhật giá nhập hiện hành (xem chi tiết tại [NhapHangService.java](file:///c:/Long/hk2_2025-2026/PersonalProject/CAFE_DI_ROM/web/Backend/src/main/java/com/cafe/jenika/service/NhapHangService.java#L84-L112)).

**Thanh toán:**
- **Tổng tiền (Hệ thống):** [Tổng tiền tính theo đơn giá thực tế sẽ lưu vào DB]đ
- **Tổng tiền (OCR):** [Tổng tiền đọc từ hóa đơn gốc]đ
- **Đã thanh toán:** [daThanhToan]đ (Từ OCR hoặc người dùng cung cấp)
- **Còn nợ:** [tienNo]đ (Còn lại = Tổng tiền Hệ thống - Đã thanh toán)

> [!IMPORTANT]
> Agent bắt buộc phải dừng lại và chờ phản hồi: *"Bạn có xác nhận thông tin đơn hàng này để tạo trên hệ thống không?"*

### Bước 7: Gửi Request tạo đơn hàng mới
Khi người dùng gõ xác nhận đồng ý, gửi POST Request với payload JSON tương ứng:

#### A. Đối với đơn Nhập hàng (Purchase Order):
- **Endpoint:** `POST http://localhost:8080/api/v1/nhap-hang`
- **Payload:**
```json
{
  "doiTac": { "id": [partnerId] },
  "nhanVien": { "id": [employeeId] },
  "trangThai": "Hoàn thành",
  "daThanhToan": [daThanhToan],
  "ghiChu": "Quét OCR từ HĐ: [Mã hóa đơn]",
  "thoiGian": "[ISO_DateTime_Hiện_Tại]",
  "ngayNhan": "[ngayLap]",
  "chiTietNhapHangs": [
    {
      "sanPham": { "id": [productId] },
      "soLuong": [soLuong],
      "giaNhap": [giaNhap],
      "donVi": "[donVi]"
    }
  ]
}
```

#### B. Đối với đơn Bán hàng (Sales Order):
- **Endpoint:** `POST http://localhost:8080/api/v1/ban-hang`
- **Payload:**
```json
{
  "doiTac": { "id": [partnerId] },
  "nhanVien": { "id": [employeeId] },
  "tongTien": [tongTien],
  "tienDaThanhToan": [tienDaThanhToan],
  "tienNo": [tienNo],
  "diaChiGiaoHang": "[diaChiDoiTac]",
  "ngayLap": "[ngayLap]",
  "trangThai": "Hoàn thành",
  "ghiChu": "Quét OCR từ HĐ: [Mã hóa đơn]",
  "thoiGian": "[ISO_DateTime_Hiện_Tại]",
  "chiTietBanHangs": [
    {
      "sanPham": { "id": [productId] },
      "soLuong": [soLuong],
      "donVi": "[donVi]",
      "giaBan": [giaBan],
      "thanhTien": [thanhTien],
      "isGift": false
    }
  ]
}
```

### Bước 8: Thông báo kết quả thành công
In ra thông tin mã đơn hàng vừa được tạo (ví dụ: `Tạo thành công đơn bán hàng BH-134` hoặc `đơn nhập hàng NH-123`).

## Common Mistakes & Lỗi thường gặp
1. **Ép giá đơn hàng (Chỉ xảy ra với Đơn Bán Hàng):** Backend có thể tự động ghi đè đơn giá gửi lên bằng giá bán hiện hành trong DB của sản phẩm đó. Agent cần nhận thức điều này để giải thích cho người dùng khi giá trị tổng tiền bị thay đổi sau khi tạo đơn thành công.
2. **Trùng tên sản phẩm:** Không được tự ý liên kết bừa bãi khi có nhiều hơn 1 sản phẩm trùng tên. Phải dừng lại liệt kê các lựa chọn và yêu cầu người dùng xác nhận lựa chọn.
3. **Mã HĐ chứa ký tự chữ:** Phải dùng regex lọc bỏ chữ, chỉ lấy phần số để gán vào `id` khi gửi lên backend (Ví dụ: `HĐ134` -> `134`).

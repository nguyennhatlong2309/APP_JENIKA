# Exchange - Lịch sử thay đổi hiện tại

## Cập nhật lúc 01:15 - 12/06/2026
- **Danh sách file thay đổi**:
  - `web/Frontend/src/app/activity/page.tsx`: Khử biểu thức điều kiện lồng nhau (nested ternary) bằng cách chuyển thành hàm helper `renderTableBody()`.
  - `web/Frontend/src/services/activityService.ts`: Đưa API endpoints vào hằng số `ENDPOINTS` tránh hardcode và bọc trong khối `try-catch` để xử lý lỗi API.
- **Kết quả xác thực**: Đã chạy kiểm tra biên dịch TypeScript `npx tsc --noEmit` thành công không có lỗi.

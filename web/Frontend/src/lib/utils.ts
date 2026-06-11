/**
 * Định dạng số thành chuỗi tiền tệ VND (ví dụ: 100.000 ₫)
 * @param num Số tiền cần định dạng
 */
export const formatVND = (num: number): string => {
  if (num === undefined || num === null || isNaN(num)) {
    return '0 ₫';
  }
  return num.toLocaleString('vi-VN') + ' ₫';
};

/**
 * Định dạng ngày tháng theo chuẩn Việt Nam (DD/MM/YYYY HH:mm:ss)
 * @param dateString Chuỗi thời gian đầu vào
 */
export const formatDateTime = (dateString: string): string => {
  if (!dateString) return '---';
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return '---';
    return date.toLocaleString('vi-VN');
  } catch (err) {
    return '---';
  }
};

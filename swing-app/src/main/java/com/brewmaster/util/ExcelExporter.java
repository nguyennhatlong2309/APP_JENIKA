package com.brewmaster.util;

import com.brewmaster.db.DatabaseManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;


import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Xuất phiếu nhập hàng / hóa đơn bán hàng ra file .xlsx
 * theo đúng mẫu trong file "MẪU HĐ+PNH.xlsx".
 *
 * Tất cả font: Times New Roman.
 * Bảng sản phẩm HÓA ĐƠN: tối thiểu 9 dòng.
 * Bảng sản phẩm PHIẾU NHẬP: tối thiểu 23 dòng.
 * Sử dụng công thức Excel cho STT, Thành Tiền, Tổng cộng, Còn lại.
 */
public class ExcelExporter {

    // Các thông tin cửa hàng đã được chuyển sang nạp động từ database qua StoreConfig
    private static final String FONT_NAME = "Times New Roman";

    // ══════════════════════════════════════════════════════════════
    //  HÓA ĐƠN BÁN HÀNG
    // ══════════════════════════════════════════════════════════════

    public static void exportSalesOrder(int orderId, File targetFile) throws Exception {
        Connection conn = DatabaseManager.getInstance().getConnection();

        // ── Load header ──
        String headerSql = "SELECT bh.*, kh.ten AS ten_khach_hang, kh.dia_chi AS kh_dc, kh.sdt AS kh_sdt,"
                + " nv.ten_nhan_vien FROM ban_hang bh"
                + " LEFT JOIN doi_tac kh ON bh.id_doi_tac = kh.id"
                + " LEFT JOIN nhan_vien nv ON bh.id_nhan_vien = nv.id"
                + " WHERE bh.id = ?";

        String tenKH = "", diaChiKH = "", sdtKH = "", tenNV = "";
        long tongTien = 0, daThanhToan = 0, conNo = 0;
        String ngayLap = "";

        try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenKH       = nvl(rs.getString("ten_khach_hang"), "Khách vãng lai");
                    diaChiKH    = nvl(rs.getString("kh_dc"), "");
                    sdtKH       = nvl(rs.getString("kh_sdt"), "");
                    tenNV       = nvl(rs.getString("ten_nhan_vien"), "");
                    tongTien    = rs.getLong("tong_tien");
                    daThanhToan = rs.getLong("tien_da_thanh_toan");
                    conNo       = rs.getLong("tien_no");
                    Timestamp ts = rs.getTimestamp("thoi_gian");
                    if (ts != null)
                        ngayLap = new SimpleDateFormat("dd/MM/yyyy").format(ts);
                }
            }
        }

        // ── Load chi tiết ──
        String detailSql = "SELECT ct.so_luong, ct.gia_ban, ct.thanh_tien, sp.ten_san_pham,"
                + " IFNULL(ct.don_vi, IFNULL(dv.ten_don_vi, 'Cái')) AS ten_don_vi"
                + " FROM chi_tiet_ban_hang ct"
                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                + " LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id"
                + " WHERE ct.id_ban_hang = ? AND ct.is_gift = 0"
                + " ORDER BY ct.id";

        List<Object[]> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Object[]{
                        rs.getString("ten_san_pham"),
                        nvl(rs.getString("ten_don_vi"), "Cái"),
                        rs.getInt("so_luong"),
                        rs.getLong("gia_ban"),
                        rs.getLong("thanh_tien")
                    });
                }
            }
        }

        int N = items.size();
        int maxRows = Math.max(9, N);

        // ── Build workbook ──
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("HÓA ĐƠN");
            setSalesColumnWidths(ws);

            // Set margins: Top: 0.6, right: 0.3493, bottom: 0.1, left: 0.4, header: 0.25, footer: 0.15
            ws.setMargin(Sheet.TopMargin, 0.6);
            ws.setMargin(Sheet.RightMargin, 0.3493);
            ws.setMargin(Sheet.BottomMargin, 0.1);
            ws.setMargin(Sheet.LeftMargin, 0.4);
            ws.setMargin(Sheet.HeaderMargin, 0.25);
            ws.setMargin(Sheet.FooterMargin, 0.15);

            // Print setup: scale to 48%, paper size A5
            PrintSetup printSetup = ws.getPrintSetup();
            printSetup.setScale((short) 48);
            printSetup.setPaperSize(PrintSetup.A5_PAPERSIZE);

            DataFormat dfmt = wb.createDataFormat();

            // ── Create all styles ──
            // 14pt: shop info (address/phone/bank), notes content
            // 18pt: main title "HÓA ĐƠN BÁN HÀNG"
            // 16pt: everything else (shop name, order#, date, KH info, table, totals, signature)
            CellStyle shopNameStyle  = createFontStyle(wb, 16, true, false);
            CellStyle shopInfoStyle  = createFontStyle(wb, 14, false, false);
            CellStyle titleStyle     = createFontStyle(wb, 18, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerBold16   = createFontStyle(wb, 16, true, false);
            centerBold16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerPlain16  = createFontStyle(wb, 16, false, false);
            centerPlain16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle plainStyle     = createFontStyle(wb, 16, false, false);
            CellStyle boldStyle      = createFontStyle(wb, 16, true, false);

            CellStyle headerCell     = createSalesTableHeaderStyle(wb, 16);
            CellStyle dataCellCenter = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 16);
            CellStyle dataCellLeft   = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 16);
            CellStyle dataCellNumber = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);
            // Formula cells for Thành Tiền (RIGHT, #,##0, borders)
            CellStyle formulaNumStyle = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);

            CellStyle salesTotalLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle salesTotalValueStyle = createFontStyle(wb, 16, true, false);
            salesTotalValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            salesTotalValueStyle.setDataFormat(dfmt.getFormat("#,##0"));
            CellStyle summaryLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle summaryValueStyle = createFontStyle(wb, 16, false, false);
            summaryValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            summaryValueStyle.setDataFormat(dfmt.getFormat("#,##0"));

            // Bold + Underline for note headings (14pt)
            CellStyle noteHeadingStyle = createFontStyle(wb, 14, true, true);
            // WrapText for note content (14pt)
            CellStyle noteContentStyle = createFontStyle(wb, 14, false, false);
            noteContentStyle.setWrapText(true);
            noteContentStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // ─────────────────────────────────────
            // ROW 0 – Tên cửa hàng  (dòng 1: 21.1pt)
            // ─────────────────────────────────────
            StoreConfig.loadFromDatabase();
            Row r = ws.createRow(0);
            r.setHeightInPoints(21.1f);
            setCell(r, 0, StoreConfig.shopName, shopNameStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // ROW 1 – Địa chỉ  (dòng 2: 18pt)
            r = ws.createRow(1);
            r.setHeightInPoints(18f);
            setCell(r, 0, StoreConfig.shopAddr, shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // ROW 2 – Điện thoại  (dòng 3: 18pt)
            r = ws.createRow(2);
            r.setHeightInPoints(18f);
            setCell(r, 0, StoreConfig.shopTel, shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

            // ROW 3 – Số TK  (dòng 4: 18pt)
            r = ws.createRow(3);
            r.setHeightInPoints(18f);
            setCell(r, 0, StoreConfig.shopBank, shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));

            // ROW 4 – blank  (dòng 5: 18pt)
            r = ws.createRow(4);
            r.setHeightInPoints(18f);

            // ROW 5 – Tiêu đề "HÓA ĐƠN BÁN HÀNG"  (dòng 6: 22.8pt)
            r = ws.createRow(5);
            r.setHeightInPoints(22.8f);
            setCell(r, 0, "HÓA ĐƠN BÁN HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            // ROW 6 – Số hóa đơn (centered, not bold)  (dòng 7: 19.5pt)
            r = ws.createRow(6);
            r.setHeightInPoints(19.5f);
            setCell(r, 0, "Số hóa đơn: HĐ-" + orderId, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(6, 6, 0, 5));

            // ROW 7 – Ngày  (dòng 8: 21pt)
            r = ws.createRow(7);
            r.setHeightInPoints(21f);
            setCell(r, 1, "Ngày: " + ngayLap, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(7, 7, 1, 4));

            // ROW 8 – Khách hàng  (dòng 9: 21pt)
            r = ws.createRow(8);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Khách hàng: " + tenKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(8, 8, 0, 5));

            // ROW 9 – Địa chỉ KH  (dòng 10: 24pt)
            r = ws.createRow(9);
            r.setHeightInPoints(24f);
            setCell(r, 0, "Địa chỉ: " + diaChiKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(9, 9, 0, 5));

            // ROW 10 – SĐT KH  (dòng 11: 23.3pt)
            r = ws.createRow(10);
            r.setHeightInPoints(23.3f);
            setCell(r, 0, "SĐT: " + sdtKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(10, 10, 0, 5));

            // ─────────────────────────────────────
            // ROW 11 – Table header  (dòng 12: 32pt)
            // ─────────────────────────────────────
            r = ws.createRow(11);
            r.setHeightInPoints(32f);
            setCell(r, 0, "STT",        headerCell);
            setCell(r, 1, "Tên Hàng",   headerCell);
            setCell(r, 2, "ĐVT",        headerCell);
            setCell(r, 3, "SL",         headerCell);
            setCell(r, 4, "Đơn giá",    headerCell);
            setCell(r, 5, "Thành Tiền", headerCell);

            // ─────────────────────────────────────
            // ROWS 12.. – Product rows (min 9)
            // dòng 13..21: 23pt each
            // ─────────────────────────────────────
            // Product data starts at Excel row 13 (index 12).
            // STT formula: =ROW()-12  →  row 13 gives 1, row 14 gives 2...
            int dataStartRow = 12; // 0-indexed
            for (int i = 0; i < maxRows; i++) {
                int rowIdx = dataStartRow + i;
                int excelRow = rowIdx + 1; // 1-indexed for formulas
                r = ws.createRow(rowIdx);
                r.setHeightInPoints(23f);

                // Col A – STT (formula)
                Cell cSTT = r.createCell(0);
                cSTT.setCellFormula("ROW()-12");
                cSTT.setCellStyle(dataCellCenter);

                if (i < N) {
                    Object[] item = items.get(i);
                    // Col B – Tên Hàng
                    setCell(r, 1, (String) item[0], dataCellLeft);
                    // Col C – ĐVT
                    setCell(r, 2, (String) item[1], dataCellCenter);
                    // Col D – SL
                    setCellNum(r, 3, (int) item[2], dataCellCenter);
                    // Col E – Đơn giá
                    setCellNum(r, 4, (long) item[3], dataCellNumber);
                } else {
                    // Empty rows – create cells with style only (no value)
                    // so formulas don't get #VALUE! from text strings
                    createBlankCell(r, 1, dataCellLeft);
                    createBlankCell(r, 2, dataCellCenter);
                    createBlankCell(r, 3, dataCellCenter);
                    createBlankCell(r, 4, dataCellNumber);
                }

                // Col F – Thành Tiền (formula = D*E, with IFERROR to avoid #VALUE!)
                Cell cTT = r.createCell(5);
                cTT.setCellFormula("IFERROR(D" + excelRow + "*E" + excelRow + ",0)");
                cTT.setCellStyle(formulaNumStyle);
            }

            // ─────────────────────────────────────
            // Tổng / Thanh toán / Còn lại / Chữ ký
            // dòng 22,23,24: 21.8pt; dòng 25: 21pt; dòng 26: 29pt
            // ─────────────────────────────────────
            int endRowIdx = dataStartRow + maxRows; // 0-indexed row after last product row
            int firstDataExcelRow = dataStartRow + 1;    // = 13
            int lastDataExcelRow  = endRowIdx;           // = 12 + maxRows (1-indexed)

            // ── Tổng cộng ── (dòng 22: 21.8pt)
            r = ws.createRow(endRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Tổng cộng:", salesTotalLabelStyle);
            setCell(r, 1, "", salesTotalLabelStyle);
            ws.addMergedRegion(new CellRangeAddress(endRowIdx, endRowIdx, 0, 1));
            
            // Cột C (ĐVT) - để trống
            setCell(r, 2, "", salesTotalLabelStyle);
            
            // Cột D (SL) - Tổng cộng số lượng
            CellStyle salesTotalQtyStyle = createFontStyle(wb, 16, true, false);
            salesTotalQtyStyle.setAlignment(HorizontalAlignment.CENTER);
            salesTotalQtyStyle.setDataFormat(dfmt.getFormat("#,##0"));
            Cell cTotalQty = r.createCell(3);
            cTotalQty.setCellFormula("SUM(D" + firstDataExcelRow + ":D" + lastDataExcelRow + ")");
            cTotalQty.setCellStyle(salesTotalQtyStyle);
            
            // Cột E (Đơn giá) - để trống
            setCell(r, 4, "", salesTotalLabelStyle);
            
            // Cột F (Thành Tiền) - Tổng cộng thành tiền
            Cell cTotal = r.createCell(5);
            cTotal.setCellFormula("SUM(F" + firstDataExcelRow + ":F" + lastDataExcelRow + ")");
            cTotal.setCellStyle(salesTotalValueStyle);

            int totalExcelRow = endRowIdx + 1; // 1-indexed

            // ── Khách hàng thanh toán ── (dòng 23: 21.8pt)
            int paidRowIdx = endRowIdx + 1;
            r = ws.createRow(paidRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Khách hàng thanh toán:", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(paidRowIdx, paidRowIdx, 0, 4));
            setCellNum(r, 5, daThanhToan, summaryValueStyle);

            int paidExcelRow = paidRowIdx + 1;

            // ── Còn lại ── (dòng 24: 21.8pt)
            int debtRowIdx = endRowIdx + 2;
            r = ws.createRow(debtRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Còn lại:", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(debtRowIdx, debtRowIdx, 0, 4));
            Cell cDebt = r.createCell(5);
            cDebt.setCellFormula("F" + totalExcelRow + "-F" + paidExcelRow);
            cDebt.setCellStyle(summaryValueStyle);

            // ── Blank row (dòng 25: 21pt) → chuyển Chữ ký lên đây ──
            int signRowIdx = endRowIdx + 3;  // 0-indexed 24 → Excel dòng 25
            r = ws.createRow(signRowIdx);
            r.setHeightInPoints(21f);
            setCell(r, 4, "Người bán hàng", centerBold16);
            ws.addMergedRegion(new CellRangeAddress(signRowIdx, signRowIdx, 4, 5));

            // ── Dòng 26: Tên nhân viên bán hàng (cột E,F gộp vào, not bold) ──
            r = ws.createRow(endRowIdx + 4);
            r.setHeightInPoints(29f);
            setCell(r, 4, tenNV, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(endRowIdx + 4, endRowIdx + 4, 4, 5));

            // ─────────────────────────────────────
            // LƯU Ý & QUY ĐỊNH
            // noteStart = endRowIdx+5 → dòng 27 (0-indexed 26)
            // dòng 27: 19.1pt; dòng 28: 13.1pt; dòng 29: 108.8pt;
            // dòng 30: 46.5pt; dòng 31: 24pt; dòng 32: 78pt; dòng 33: 118.1pt
            // ─────────────────────────────────────
            int noteStart = endRowIdx + 5;  // 0-indexed = 26 → Excel dòng 27
            int noteRowIdx = noteStart;

            // Dòng 27+28 (index 26+27): Tiêu đề LƯU Ý – merge 2 rows thành 1 ô
            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(19.1f);
            setCell(r, 0, "LƯU Ý:", noteHeadingStyle);
            // Tạo dòng 28 (index 27) – chỉ set height, không set value
            Row row28 = ws.createRow(noteRowIdx + 1);
            row28.setHeightInPoints(13.1f);
            // Merge dòng 27+28, cột A:F
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx + 1, 0, 5));
            noteRowIdx += 2;  // bước qua cả 2 dòng

            // Dòng 29 (index 28): Nội dung lưu ý 1 (108.8pt)
            String notesText = StoreConfig.shopNotes != null ? StoreConfig.shopNotes : "";
            String[] notesLines = notesText.split("\n");
            StringBuilder notesPart1 = new StringBuilder();
            StringBuilder notesPart2 = new StringBuilder();
            for (String line : notesLines) {
                if (line.contains("bảo hành máy") || line.contains("bao hanh may") || line.contains("sửa chữa")) {
                    if (notesPart2.length() > 0) notesPart2.append("\n");
                    notesPart2.append(line);
                } else {
                    if (notesPart1.length() > 0) notesPart1.append("\n");
                    notesPart1.append(line);
                }
            }
            if (notesPart1.length() == 0 && notesPart2.length() > 0) {
                notesPart1.append(notesPart2);
                notesPart2.setLength(0);
            }

            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(108.8f);
            setCell(r, 0, notesPart1.toString(), noteContentStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            // Dòng 30 (index 29): Nội dung lưu ý 2 (46.5pt)
            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(46.5f);
            setCell(r, 0, notesPart2.toString(), noteContentStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            // Dòng 31 (index 30): Tiêu đề QUY ĐịNH ĐỔI VÀ HOÀN TRẢ HÀNG (24pt)
            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(24f);
            setCell(r, 0, "QUY ĐịNH ĐỔI  VÀ HOÀN TRẢ HÀNG:", noteHeadingStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            // Dòng 32 (index 31): Nội dung quy định đổi trả + Thời gian bảo hành (78 + 118.1 = 196.1pt)
            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(196.1f);

            // Tạo font cho tiêu đề: bold + underline (14pt)
            XSSFFont fontHeadingBU = (XSSFFont) wb.createFont();
            fontHeadingBU.setFontName(FONT_NAME);
            fontHeadingBU.setFontHeightInPoints((short) 14);
            fontHeadingBU.setBold(true);
            fontHeadingBU.setUnderline(Font.U_SINGLE);

            // Tạo font bình thường (14pt)
            XSSFFont fontNormal14 = (XSSFFont) wb.createFont();
            fontNormal14.setFontName(FONT_NAME);
            fontNormal14.setFontHeightInPoints((short) 14);

            // Tạo font bold (14pt)
            XSSFFont fontBold14 = (XSSFFont) wb.createFont();
            fontBold14.setFontName(FONT_NAME);
            fontBold14.setFontHeightInPoints((short) 14);
            fontBold14.setBold(true);

            String pDoiTra = (StoreConfig.shopPolicy != null ? StoreConfig.shopPolicy.trim() : "") + "\n\n";
            String part1 = "THỜI GIAN BẢO HÀNH THEO TỪNG SẢN PHẨM:\n";
            String part2 = (StoreConfig.shopWarranty != null ? StoreConfig.shopWarranty.trim() : "") + "\n";
            String part3 = StoreConfig.shopWarrantyLimit != null ? StoreConfig.shopWarrantyLimit.trim() : "";

            XSSFRichTextString richText = new XSSFRichTextString();
            richText.append(pDoiTra, fontNormal14);
            richText.append(part1, fontHeadingBU);  // Tô đen + gạch chân
            richText.append(part2, fontNormal14);   // Bình thường
            richText.append(part3, fontBold14);     // Tô đen

            r.createCell(0).setCellValue(richText);
            r.getCell(0).setCellStyle(noteContentStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            // ── Print area A1:F[noteRowIdx] ──
            wb.setPrintArea(wb.getSheetIndex(ws), 0, 5, 0, noteRowIdx - 1);

            // ── Save ──
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                wb.write(fos);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PHIẾU NHẬP HÀNG
    // ══════════════════════════════════════════════════════════════

    public static void exportPurchaseOrder(int orderId, File targetFile) throws Exception {
        Connection conn = DatabaseManager.getInstance().getConnection();

        // ── Load header ──
        String headerSql = "SELECT nh.*, ncc.ten AS ten_ncc, ncc.dia_chi AS ncc_dc, ncc.sdt AS ncc_sdt,"
                + " nv.ten_nhan_vien FROM nhap_hang nh"
                + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                + " LEFT JOIN nhan_vien nv ON nh.id_nhan_vien = nv.id"
                + " WHERE nh.id = ?";

        String tenNCC = "", diaChiNCC = "", sdtNCC = "";
        long tongTien = 0, daThanhToan = 0, conNo = 0;
        String ngayLap = "";

        try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenNCC      = nvl(rs.getString("ten_ncc"), "Tư nhân");
                    diaChiNCC   = nvl(rs.getString("ncc_dc"), "");
                    sdtNCC      = nvl(rs.getString("ncc_sdt"), "");
                    tongTien    = rs.getLong("tong_tien");
                    daThanhToan = rs.getLong("da_thanh_toan");
                    conNo       = rs.getLong("tien_no");
                    Timestamp ts = rs.getTimestamp("thoi_gian");
                    if (ts != null)
                        ngayLap = new SimpleDateFormat("dd/MM/yyyy").format(ts);
                }
            }
        }

        // ── Load chi tiết ──
        String detailSql = "SELECT ct.so_luong, ct.gia_nhap, ct.thanh_tien, sp.ten_san_pham,"
                + " IFNULL(ct.don_vi, IFNULL(dv.ten_don_vi, 'Cái')) AS ten_don_vi"
                + " FROM chi_tiet_nhap_hang ct"
                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                + " LEFT JOIN don_vi_tinh dv ON sp.id_don_vi = dv.id"
                + " WHERE ct.id_nhap_hang = ?"
                + " ORDER BY ct.id_san_pham";

        List<Object[]> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(detailSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Object[]{
                        rs.getString("ten_san_pham"),
                        nvl(rs.getString("ten_don_vi"), "Cái"),
                        rs.getInt("so_luong"),
                        rs.getLong("gia_nhap"),
                        rs.getLong("thanh_tien")
                    });
                }
            }
        }

        int N = items.size();
        int maxRows = Math.max(9, N);

        // ── Build workbook ──
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("PHIEU NHAP HANG");
            setColumnWidths(ws);

            // Margins: top:0.65; header:0.3; right:0.25; footer:0.2; bottom:0.2; left:0.55
            ws.setMargin(Sheet.TopMargin, 0.65);
            ws.setMargin(Sheet.HeaderMargin, 0.3);
            ws.setMargin(Sheet.RightMargin, 0.25);
            ws.setMargin(Sheet.FooterMargin, 0.2);
            ws.setMargin(Sheet.BottomMargin, 0.2);
            ws.setMargin(Sheet.LeftMargin, 0.55);

            // Print setup: orientation landscape, scale 69%, paper size A5
            PrintSetup printSetup = ws.getPrintSetup();
            printSetup.setLandscape(true);
            printSetup.setScale((short) 69);
            printSetup.setPaperSize(PrintSetup.A5_PAPERSIZE);

            DataFormat dfmt = wb.createDataFormat();

            // ── Create all styles ──
            // 14pt: shop info (address/phone)
            // 18pt: main title "PHIẾU NHẬP HÀNG"
            // 16pt: everything else
            CellStyle shopNameStyle  = createFontStyle(wb, 16, true, false);
            CellStyle shopInfoStyle  = createFontStyle(wb, 14, false, false);
            CellStyle titleStyle     = createFontStyle(wb, 18, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerBold16   = createFontStyle(wb, 16, true, false);
            centerBold16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerPlain16  = createFontStyle(wb, 16, false, false);
            centerPlain16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle plainStyle     = createFontStyle(wb, 16, false, false);
            CellStyle boldStyle      = createFontStyle(wb, 16, true, false);

            CellStyle headerCell     = createSalesTableHeaderStyle(wb, 16);
            CellStyle dataCellCenter = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 16);
            CellStyle dataCellLeft   = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 16);
            CellStyle dataCellNumber = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);
            CellStyle formulaNumStyle = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);

            CellStyle totalLabelStyle = createTotalRowStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 16);
            CellStyle totalValueStyle = createTotalRowStyle(wb, HorizontalAlignment.RIGHT, "#,##0", dfmt, 16);
            CellStyle summaryLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle summaryValueStyle = createFontStyle(wb, 16, false, false);
            summaryValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            summaryValueStyle.setDataFormat(dfmt.getFormat("#,##0"));

            // ─────────────────────────────────────
            // ROW 0 – Tên cửa hàng
            // ─────────────────────────────────────
            StoreConfig.loadFromDatabase();
            Row r = ws.createRow(0);
            r.setHeightInPoints(21f);
            setCell(r, 0, StoreConfig.shopNamePnh, shopNameStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // ROW 1 – Địa chỉ
            r = ws.createRow(1);
            r.setHeightInPoints(21f);
            setCell(r, 0, StoreConfig.shopAddr, shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // ROW 2 – Điện thoại
            r = ws.createRow(2);
            r.setHeightInPoints(21f);
            setCell(r, 0, StoreConfig.shopTel, shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

            // ROW 3 – blank
            r = ws.createRow(3);
            r.setHeightInPoints(12f);

            // ROW 4 – Tiêu đề "PHIẾU NHẬP HÀNG"
            r = ws.createRow(4);
            r.setHeightInPoints(20f);
            setCell(r, 0, "PHIẾU NHẬP HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));

            // ROW 5 – Số phiếu nhập (centered, not bold)
            r = ws.createRow(5);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Số phiếu nhập: PN-" + orderId, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            // ROW 6 – Ngày (B7:E7 merged, centered)
            r = ws.createRow(6);
            r.setHeightInPoints(20f);
            setCell(r, 1, "Ngày: " + ngayLap, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(6, 6, 1, 4));

            // ROW 7 – Nhà cung cấp
            r = ws.createRow(7);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Nhà cung cấp: " + tenNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(7, 7, 0, 5));

            // ROW 8 – Địa chỉ NCC
            r = ws.createRow(8);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Địa chỉ: " + diaChiNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(8, 8, 0, 5));

            // ROW 9 – SĐT NCC
            r = ws.createRow(9);
            r.setHeightInPoints(21f);
            setCell(r, 0, "SĐT: " + sdtNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(9, 9, 0, 5));

            // ─────────────────────────────────────
            // ROW 10 – Table header
            // ─────────────────────────────────────
            r = ws.createRow(10);
            r.setHeightInPoints(27f);
            setCell(r, 0, "STT",        headerCell);
            setCell(r, 1, "Tên Hàng",   headerCell);
            setCell(r, 2, "ĐVT",        headerCell);
            setCell(r, 3, "SL",         headerCell);
            setCell(r, 4, "Đơn giá",    headerCell);
            setCell(r, 5, "Thành Tiền", headerCell);

            // ─────────────────────────────────────
            // ROWS 11.. – Product rows (min 23)
            // ─────────────────────────────────────
            // Product data starts at Excel row 12 (index 11).
            // STT formula: =ROW()-11  →  row 12 gives 1, row 13 gives 2...
            int dataStartRow = 11; // 0-indexed
            for (int i = 0; i < maxRows; i++) {
                int rowIdx = dataStartRow + i;
                int excelRow = rowIdx + 1; // 1-indexed for formulas
                r = ws.createRow(rowIdx);
                if (i < 9) {
                    r.setHeightInPoints(20f);
                } else {
                    r.setHeightInPoints(21f);
                }

                // Col A – STT (formula)
                Cell cSTT = r.createCell(0);
                cSTT.setCellFormula("ROW()-11");
                cSTT.setCellStyle(dataCellCenter);

                if (i < N) {
                    Object[] item = items.get(i);
                    // Col B – Tên Hàng
                    setCell(r, 1, (String) item[0], dataCellLeft);
                    // Col C – ĐVT
                    setCell(r, 2, (String) item[1], dataCellCenter);
                    // Col D – SL
                    setCellNum(r, 3, (int) item[2], dataCellCenter);
                    // Col E – Đơn giá
                    setCellNum(r, 4, (long) item[3], dataCellNumber);
                } else {
                    // Empty rows – create cells with style only (no value)
                    // so formulas don't get #VALUE! from text strings
                    createBlankCell(r, 1, dataCellLeft);
                    createBlankCell(r, 2, dataCellCenter);
                    createBlankCell(r, 3, dataCellCenter);
                    createBlankCell(r, 4, dataCellNumber);
                }

                // Col F – Thành Tiền (formula = D*E, with IFERROR to avoid #VALUE!)
                Cell cTT = r.createCell(5);
                cTT.setCellFormula("IFERROR(D" + excelRow + "*E" + excelRow + ",0)");
                cTT.setCellStyle(formulaNumStyle);
            }

            // ─────────────────────────────────────
            // Tổng / Đã TT / Còn lại / Chữ ký
            // ─────────────────────────────────────
            int endRowIdx = dataStartRow + maxRows; // 0-indexed row after last product row
            int firstDataExcelRow = dataStartRow + 1;    // = 12
            int lastDataExcelRow  = endRowIdx;           // = 11 + maxRows (1-indexed)

            // ── Tổng Cộng ──
            r = ws.createRow(endRowIdx);
            r.setHeightInPoints(21.8f);
            
            CellStyle totalQtyStyleNoBorder = createFontStyle(wb, 16, true, false);
            totalQtyStyleNoBorder.setAlignment(HorizontalAlignment.CENTER);
            totalQtyStyleNoBorder.setDataFormat(dfmt.getFormat("#,##0"));

            CellStyle totalValueStyleNoBorder = createFontStyle(wb, 16, true, false);
            totalValueStyleNoBorder.setAlignment(HorizontalAlignment.RIGHT);
            totalValueStyleNoBorder.setDataFormat(dfmt.getFormat("#,##0"));
            
            setCell(r, 0, "Tổng Cộng:", summaryLabelStyle);
            setCell(r, 1, "", summaryLabelStyle);
            ws.addMergedRegion(new CellRangeAddress(endRowIdx, endRowIdx, 0, 1));
            
            setCell(r, 2, "", summaryLabelStyle);
            
            Cell cTotalQty = r.createCell(3);
            cTotalQty.setCellFormula("SUM(D" + firstDataExcelRow + ":D" + lastDataExcelRow + ")");
            cTotalQty.setCellStyle(totalQtyStyleNoBorder);
            
            setCell(r, 4, "", summaryLabelStyle);
            
            Cell cTotal = r.createCell(5);
            cTotal.setCellFormula("SUM(F" + firstDataExcelRow + ":F" + lastDataExcelRow + ")");
            cTotal.setCellStyle(totalValueStyleNoBorder);

            int totalExcelRow = endRowIdx + 1; // 1-indexed

            // ── Đã TT ──
            int paidRowIdx = endRowIdx + 1;
            r = ws.createRow(paidRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Đã TT", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(paidRowIdx, paidRowIdx, 0, 4));
            setCellNum(r, 5, daThanhToan, summaryValueStyle);

            int paidExcelRow = paidRowIdx + 1;

            // ── Còn lại ──
            int debtRowIdx = endRowIdx + 2;
            r = ws.createRow(debtRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Còn lại:", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(debtRowIdx, debtRowIdx, 0, 4));
            Cell cDebt = r.createCell(5);
            cDebt.setCellFormula("F" + totalExcelRow + "-F" + paidExcelRow);
            cDebt.setCellStyle(summaryValueStyle);

            // ── Save ──
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                wb.write(fos);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  XUẤT DANH SÁCH ĐƠN NHẬP HÀNG (.xlsx)
    // ══════════════════════════════════════════════════════════════

    public static void exportPurchaseOrderList(String search, List<String> selectedStatuses, String fromDate, String toDate, File targetFile) throws Exception {
        java.sql.Connection conn = DatabaseManager.getInstance().getConnection();

        StringBuilder cond = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank())
            cond.append(" AND (ncc.ten LIKE '%").append(search)
                    .append("%' OR CAST(nh.id AS CHAR) LIKE '%").append(search).append("%')");
        if (selectedStatuses != null && !selectedStatuses.isEmpty()) {
            cond.append(" AND nh.trang_thai IN (");
            for (int i = 0; i < selectedStatuses.size(); i++) {
                if (i > 0) cond.append(",");
                cond.append("'").append(selectedStatuses.get(i).replace("'", "''")).append("'");
            }
            cond.append(")");
        }
        if (fromDate != null && !fromDate.isBlank())
            cond.append(" AND nh.thoi_gian >= '").append(fromDate).append("'");
        if (toDate != null && !toDate.isBlank())
            cond.append(" AND nh.thoi_gian <= '").append(toDate).append("'");

        String sql = "SELECT nh.id, nh.thoi_gian, nh.ngay_nhan,"
                + " IFNULL(ncc.ten, '---') AS ten_ncc,"
                + " IFNULL(nv.ten_nhan_vien, '---') AS ten_nv,"
                + " nh.tong_tien, nh.da_thanh_toan, nh.tien_no, nh.trang_thai"
                + " FROM nhap_hang nh"
                + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                + " LEFT JOIN nhan_vien nv ON nh.id_nhan_vien = nv.id"
                + cond
                + " ORDER BY nh.thoi_gian DESC";

        List<Object[]> rows = new ArrayList<>();
        try (java.sql.Statement s = conn.createStatement();
             java.sql.ResultSet rs = s.executeQuery(sql)) {
            java.text.SimpleDateFormat dtFmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            java.text.SimpleDateFormat dFmt  = new java.text.SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                String ngayLap = "";
                java.sql.Timestamp ts = rs.getTimestamp("thoi_gian");
                if (ts != null) ngayLap = dtFmt.format(ts);

                String ngayNhan = "--";
                java.sql.Date dateNhan = rs.getDate("ngay_nhan");
                if (dateNhan != null) ngayNhan = dFmt.format(dateNhan);

                rows.add(new Object[]{
                    "NH-" + rs.getInt("id"),
                    ngayLap,
                    ngayNhan,
                    rs.getString("ten_ncc"),
                    rs.getString("ten_nv"),
                    rs.getLong("tong_tien"),
                    rs.getLong("da_thanh_toan"),
                    rs.getLong("tien_no"),
                    rs.getString("trang_thai")
                });
            }
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("Danh Sach Don Nhap");

            // Column widths
            ws.setColumnWidth(0, 10 * 256); // Mã đơn
            ws.setColumnWidth(1, 18 * 256); // Ngày lập
            ws.setColumnWidth(2, 14 * 256); // Ngày nhận
            ws.setColumnWidth(3, 22 * 256); // Nhà cung cấp
            ws.setColumnWidth(4, 18 * 256); // Người nhập
            ws.setColumnWidth(5, 16 * 256); // Tổng tiền
            ws.setColumnWidth(6, 16 * 256); // Đã thanh toán
            ws.setColumnWidth(7, 14 * 256); // Còn nợ
            ws.setColumnWidth(8, 12 * 256); // Trạng thái

            DataFormat dfmt = wb.createDataFormat();

            // Title row
            CellStyle titleStyle = createFontStyle(wb, 16, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Row rTitle = ws.createRow(0);
            rTitle.setHeightInPoints(22);
            setCell(rTitle, 0, "DANH SÁCH ĐƠN NHẬP HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Blank row
            ws.createRow(1);

            // Header row
            CellStyle hdrStyle = createTableHeaderStyle(wb, 13);
            String[] headers = {"Mã đơn", "Ngày lập", "Ngày nhận", "Nhà cung cấp", "Người nhập",
                                 "Tổng tiền", "Đã thanh toán", "Còn nợ", "Trạng thái"};
            Row rHdr = ws.createRow(2);
            rHdr.setHeightInPoints(20);
            for (int c = 0; c < headers.length; c++) {
                setCell(rHdr, c, headers[c], hdrStyle);
            }

            // Data cells styles
            CellStyle ctrStyle = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 13);
            CellStyle leftStyle = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 13);
            CellStyle numStyle  = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0", dfmt, 13);

            // Data rows
            for (int i = 0; i < rows.size(); i++) {
                Object[] item = rows.get(i);
                Row r = ws.createRow(3 + i);
                r.setHeightInPoints(18);

                setCell(r, 0, item[0].toString(), ctrStyle);           // Mã đơn
                setCell(r, 1, item[1].toString(), ctrStyle);           // Ngày lập
                setCell(r, 2, item[2].toString(), ctrStyle);           // Ngày nhận
                setCell(r, 3, item[3] != null ? item[3].toString() : "", leftStyle); // NCC
                setCell(r, 4, item[4] != null ? item[4].toString() : "", leftStyle); // Người nhập
                setCellNum(r, 5, (long) item[5], numStyle);            // Tổng tiền
                setCellNum(r, 6, (long) item[6], numStyle);            // Đã TT
                setCellNum(r, 7, (long) item[7], numStyle);            // Còn nợ
                setCell(r, 8, item[8] != null ? item[8].toString() : "", ctrStyle); // Trạng thái
            }

            // Summary row
            if (!rows.isEmpty()) {
                int lastDataRow = 3 + rows.size(); // 1-indexed
                CellStyle sumStyle = createTotalRowStyle(wb, HorizontalAlignment.RIGHT, "#,##0", dfmt, 13);
                CellStyle sumLbl   = createTotalRowStyle(wb, HorizontalAlignment.LEFT,  null, dfmt, 13);
                Row rSum = ws.createRow(3 + rows.size());
                rSum.setHeightInPoints(20);
                setCell(rSum, 0, "Tổng cộng:", sumLbl);
                for (int c = 1; c <= 4; c++) setCell(rSum, c, "", sumLbl);
                ws.addMergedRegion(new CellRangeAddress(3 + rows.size(), 3 + rows.size(), 0, 4));
                Cell cSum5 = rSum.createCell(5);
                cSum5.setCellFormula("SUM(F4:F" + lastDataRow + ")");
                cSum5.setCellStyle(sumStyle);
                Cell cSum6 = rSum.createCell(6);
                cSum6.setCellFormula("SUM(G4:G" + lastDataRow + ")");
                cSum6.setCellStyle(sumStyle);
                Cell cSum7 = rSum.createCell(7);
                cSum7.setCellFormula("SUM(H4:H" + lastDataRow + ")");
                cSum7.setCellStyle(sumStyle);
                setCell(rSum, 8, "", sumLbl);
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                wb.write(fos);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  XUẤT DANH SÁCH CHI TIẾT SẢN PHẨM NHẬP HÀNG (.xlsx)
    // ══════════════════════════════════════════════════════════════

    public static void exportPurchaseOrderProductList(String search, String fromDate, String toDate, File targetFile) throws Exception {
        java.sql.Connection conn = DatabaseManager.getInstance().getConnection();

        StringBuilder cond = new StringBuilder(" WHERE 1=1");
        if (search != null && !search.isBlank()) {
            cond.append(" AND (sp.ten_san_pham LIKE '%").append(search.replace("'", "''"))
                .append("%' OR ncc.ten LIKE '%").append(search.replace("'", "''"))
                .append("%' OR CAST(nh.id AS CHAR) LIKE '%").append(search.replace("'", "''")).append("%')");
        }
        if (fromDate != null && !fromDate.isBlank()) {
            cond.append(" AND nh.thoi_gian >= '").append(fromDate).append("'");
        }
        if (toDate != null && !toDate.isBlank()) {
            cond.append(" AND nh.thoi_gian <= '").append(toDate).append("'");
        }

        String sql = "SELECT ct.id_nhap_hang, ct.so_luong, ct.gia_nhap, ct.thanh_tien, "
                + " sp.ten_san_pham, "
                + " IFNULL(ncc.ten, '---') AS ten_ncc, nh.thoi_gian"
                + " FROM chi_tiet_nhap_hang ct"
                + " JOIN nhap_hang nh ON ct.id_nhap_hang = nh.id"
                + " JOIN san_pham sp ON ct.id_san_pham = sp.id"
                + " LEFT JOIN doi_tac ncc ON nh.id_doi_tac = ncc.id"
                + cond
                + " ORDER BY nh.thoi_gian DESC";

        List<Object[]> rows = new ArrayList<>();
        try (java.sql.Statement s = conn.createStatement();
             java.sql.ResultSet rs = s.executeQuery(sql)) {
            java.text.SimpleDateFormat dtFmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                String ngayNhap = "";
                java.sql.Timestamp ts = rs.getTimestamp("thoi_gian");
                if (ts != null) ngayNhap = dtFmt.format(ts);

                rows.add(new Object[]{
                    rs.getString("ten_san_pham"),
                    "NH-" + rs.getInt("id_nhap_hang"),
                    ngayNhap,
                    rs.getString("ten_ncc"),
                    rs.getLong("gia_nhap"),
                    rs.getInt("so_luong"),
                    rs.getLong("thanh_tien")
                });
            }
        }

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("Danh Sach SP Nhap");

            // Column widths
            ws.setColumnWidth(0, 24 * 256); // Sản phẩm
            ws.setColumnWidth(1, 12 * 256); // Mã đơn
            ws.setColumnWidth(2, 18 * 256); // Ngày nhập
            ws.setColumnWidth(3, 22 * 256); // Nhà cung cấp
            ws.setColumnWidth(4, 16 * 256); // Giá nhập
            ws.setColumnWidth(5, 12 * 256); // Số lượng
            ws.setColumnWidth(6, 16 * 256); // Thành tiền

            DataFormat dfmt = wb.createDataFormat();

            // Title row
            CellStyle titleStyle = createFontStyle(wb, 16, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Row rTitle = ws.createRow(0);
            rTitle.setHeightInPoints(22);
            setCell(rTitle, 0, "DANH SÁCH CHI TIẾT SẢN PHẨM NHẬP HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Blank row
            ws.createRow(1);

            // Header row
            CellStyle hdrStyle = createTableHeaderStyle(wb, 13);
            String[] headers = {"Sản phẩm", "Mã đơn", "Ngày nhập", "Nhà cung cấp", "Giá nhập", "Số lượng", "Thành tiền"};
            Row rHdr = ws.createRow(2);
            rHdr.setHeightInPoints(20);
            for (int c = 0; c < headers.length; c++) {
                setCell(rHdr, c, headers[c], hdrStyle);
            }

            // Data cells styles
            CellStyle ctrStyle = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 13);
            CellStyle leftStyle = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 13);
            CellStyle numStyle  = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0", dfmt, 13);

            // Data rows
            for (int i = 0; i < rows.size(); i++) {
                Object[] item = rows.get(i);
                Row r = ws.createRow(3 + i);
                r.setHeightInPoints(18);

                setCell(r, 0, item[0].toString(), leftStyle);            // Sản phẩm
                setCell(r, 1, item[1].toString(), ctrStyle);             // Mã đơn
                setCell(r, 2, item[2].toString(), ctrStyle);             // Ngày nhập
                setCell(r, 3, item[3] != null ? item[3].toString() : "", leftStyle); // NCC
                setCellNum(r, 4, (long) item[4], numStyle);            // Giá nhập
                setCellNum(r, 5, (int) item[5], ctrStyle);             // Số lượng
                setCellNum(r, 6, (long) item[6], numStyle);            // Thành tiền
            }

            // Summary row
            if (!rows.isEmpty()) {
                int lastDataRow = 3 + rows.size(); // 1-indexed
                CellStyle sumStyle = createTotalRowStyle(wb, HorizontalAlignment.RIGHT, "#,##0", dfmt, 13);
                CellStyle sumLbl   = createTotalRowStyle(wb, HorizontalAlignment.LEFT,  null, dfmt, 13);
                Row rSum = ws.createRow(3 + rows.size());
                rSum.setHeightInPoints(20);
                setCell(rSum, 0, "Tổng cộng:", sumLbl);
                for (int c = 1; c <= 4; c++) setCell(rSum, c, "", sumLbl);
                ws.addMergedRegion(new CellRangeAddress(3 + rows.size(), 3 + rows.size(), 0, 4));
                
                // Số lượng tổng cộng
                Cell cSum5 = rSum.createCell(5);
                cSum5.setCellFormula("SUM(F4:F" + lastDataRow + ")");
                cSum5.setCellStyle(sumStyle);
                
                // Thành tiền tổng cộng
                Cell cSum6 = rSum.createCell(6);
                cSum6.setCellFormula("SUM(G4:G" + lastDataRow + ")");
                cSum6.setCellStyle(sumStyle);
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                wb.write(fos);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════

    /** Set standard column widths for PHIẾU NHẬP HÀNG. */
    private static void setColumnWidths(XSSFSheet ws) {
        ws.setColumnWidth(0, (int)(7    * 256));  // A – STT
        ws.setColumnWidth(1, (int)(61.1 * 256));  // B – Tên Hàng
        ws.setColumnWidth(2, (int)(9.2  * 256));  // C – ĐVT
        ws.setColumnWidth(3, (int)(9.2  * 256));  // D – SL
        ws.setColumnWidth(4, (int)(17.4 * 256));  // E – Đơn giá
        ws.setColumnWidth(5, (int)(17.4 * 256));  // F – Thành Tiền
    }

    /**
     * Set column widths for HÓA ĐƠN BÁN HÀNG theo kích thước yêu cầu.
     * Đơn vị: Excel character units (1/256 của 1 ký tự).
     * A=10.6, B=51.5, C=9.8, D=9.8, E=17.9, F=17.9
     */
    private static void setSalesColumnWidths(XSSFSheet ws) {
        ws.setColumnWidth(0, (int)(10.6 * 256));  // A – STT
        ws.setColumnWidth(1, (int)(51.5 * 256));  // B – Tên Hàng
        ws.setColumnWidth(2, (int)(9.8  * 256));  // C – ĐVT
        ws.setColumnWidth(3, (int)(9.8  * 256));  // D – SL
        ws.setColumnWidth(4, (int)(17.9 * 256));  // E – Đơn giá
        ws.setColumnWidth(5, (int)(17.9 * 256));  // F – Thành Tiền
    }

    // ──────────────────────────────────────────────────────────────
    //  Font & Style factories — all use Times New Roman
    // ──────────────────────────────────────────────────────────────

    /** Create a basic CellStyle with Times New Roman font. */
    private static CellStyle createFontStyle(XSSFWorkbook wb, int fontSize,
                                              boolean bold, boolean underline) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName(FONT_NAME);
        f.setFontHeightInPoints((short) fontSize);
        f.setBold(bold);
        if (underline) f.setUnderline(Font.U_SINGLE);
        s.setFont(f);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    /** Table header style: bold, centered, thin borders, light blue background. */
    private static CellStyle createTableHeaderStyle(XSSFWorkbook wb, int fontSize) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName(FONT_NAME);
        f.setBold(true);
        f.setFontHeightInPoints((short) fontSize);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorders(s);
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    /** Table header style for Sales Order: bold, centered, thin borders, no background color. */
    private static CellStyle createSalesTableHeaderStyle(XSSFWorkbook wb, int fontSize) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName(FONT_NAME);
        f.setBold(true);
        f.setFontHeightInPoints((short) fontSize);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        applyThinBorders(s);
        return s;
    }

    /** Data cell style with thin borders, given alignment & optional number format. */
    private static CellStyle createDataCellStyle(XSSFWorkbook wb,
                                                  HorizontalAlignment align,
                                                  String numFormat,
                                                  DataFormat dfmt,
                                                  int fontSize) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName(FONT_NAME);
        f.setFontHeightInPoints((short) fontSize);
        s.setFont(f);
        applyThinBorders(s);
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (numFormat != null) s.setDataFormat(dfmt.getFormat(numFormat));
        return s;
    }

    /** Total row style: bold, thin top + double bottom borders. */
    private static CellStyle createTotalRowStyle(XSSFWorkbook wb,
                                                  HorizontalAlignment align,
                                                  String numFormat,
                                                  DataFormat dfmt,
                                                  int fontSize) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName(FONT_NAME);
        f.setBold(true);
        f.setFontHeightInPoints((short) fontSize);
        s.setFont(f);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.DOUBLE);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (numFormat != null) s.setDataFormat(dfmt.getFormat(numFormat));
        return s;
    }

    /** Apply THIN borders on all four sides. */
    private static void applyThinBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    // ──────────────────────────────────────────────────────────────
    //  Cell creation helpers
    // ──────────────────────────────────────────────────────────────

    private static void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        if (style != null) c.setCellStyle(style);
    }

    private static void setCellNum(Row row, int col, long value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue((double) value);
        if (style != null) c.setCellStyle(style);
    }

    private static void setCellNum(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue((double) value);
        if (style != null) c.setCellStyle(style);
    }

    /** Create a blank cell with only a style (no value) — prevents #VALUE! in formulas. */
    private static void createBlankCell(Row row, int col, CellStyle style) {
        Cell c = row.createCell(col, CellType.BLANK);
        if (style != null) c.setCellStyle(style);
    }

    private static String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}

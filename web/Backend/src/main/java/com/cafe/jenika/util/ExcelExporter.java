package com.cafe.jenika.util;

import com.cafe.jenika.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Xuất phiếu nhập hàng / hóa đơn bán hàng ra file Excel (.xlsx) dưới dạng byte array
 * theo đúng định dạng mẫu của hệ thống.
 */
public class ExcelExporter {

    private static final String FONT_NAME = "Times New Roman";

    // ══════════════════════════════════════════════════════════════
    //  HÓA ĐƠN BÁN HÀNG
    // ══════════════════════════════════════════════════════════════

    public static byte[] exportSalesOrder(BanHang order, StoreConfig config) throws Exception {
        String tenKH = "Khách vãng lai";
        String diaChiKH = "";
        String sdtKH = "";
        String tenNV = "";
        
        if (order.getDoiTac() != null) {
            tenKH = nvl(order.getDoiTac().getTen(), "Khách vãng lai");
            diaChiKH = nvl(order.getDoiTac().getDiaChi(), "");
            sdtKH = nvl(order.getDoiTac().getSdt(), "");
        }
        
        if (order.getNhanVien() != null) {
            tenNV = nvl(order.getNhanVien().getTenNhanVien(), "");
        }

        String ngayLap = "";
        if (order.getThoiGian() != null) {
            ngayLap = order.getThoiGian().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Lọc lấy danh sách sản phẩm bán (không tính quà tặng)
        List<ChiTietBanHang> items = new ArrayList<>();
        if (order.getChiTietBanHangs() != null) {
            items = order.getChiTietBanHangs().stream()
                    .filter(ct -> ct.getIsGift() == null || !ct.getIsGift())
                    .collect(Collectors.toList());
        }

        int N = items.size();
        int maxRows = Math.max(9, N);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("HÓA ĐƠN");
            setSalesColumnWidths(ws);

            // Căn lề trang in
            ws.setMargin(Sheet.TopMargin, 0.6);
            ws.setMargin(Sheet.RightMargin, 0.3493);
            ws.setMargin(Sheet.BottomMargin, 0.1);
            ws.setMargin(Sheet.LeftMargin, 0.4);
            ws.setMargin(Sheet.HeaderMargin, 0.25);
            ws.setMargin(Sheet.FooterMargin, 0.15);

            // Cấu hình in
            PrintSetup printSetup = ws.getPrintSetup();
            printSetup.setScale((short) 48);
            printSetup.setPaperSize(PrintSetup.A5_PAPERSIZE);

            DataFormat dfmt = wb.createDataFormat();

            // Khởi tạo các style chữ & bảng
            CellStyle shopNameStyle  = createFontStyle(wb, 16, true, false);
            CellStyle shopInfoStyle  = createFontStyle(wb, 14, false, false);
            CellStyle titleStyle     = createFontStyle(wb, 18, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerBold16   = createFontStyle(wb, 16, true, false);
            centerBold16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerPlain16  = createFontStyle(wb, 16, false, false);
            centerPlain16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle plainStyle     = createFontStyle(wb, 16, false, false);

            CellStyle headerCell     = createSalesTableHeaderStyle(wb, 16);
            CellStyle dataCellCenter = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 16);
            CellStyle dataCellLeft   = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 16);
            CellStyle dataCellNumber = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);
            CellStyle formulaNumStyle = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);

            CellStyle salesTotalLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle salesTotalValueStyle = createFontStyle(wb, 16, true, false);
            salesTotalValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            salesTotalValueStyle.setDataFormat(dfmt.getFormat("#,##0"));
            CellStyle summaryLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle summaryValueStyle = createFontStyle(wb, 16, false, false);
            summaryValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            summaryValueStyle.setDataFormat(dfmt.getFormat("#,##0"));

            CellStyle noteHeadingStyle = createFontStyle(wb, 14, true, true);
            CellStyle noteContentStyle = createFontStyle(wb, 14, false, false);
            noteContentStyle.setWrapText(true);
            noteContentStyle.setVerticalAlignment(VerticalAlignment.TOP);

            // Tên cửa hàng
            Row r = ws.createRow(0);
            r.setHeightInPoints(21.1f);
            setCell(r, 0, config.getShopName() != null ? config.getShopName() : "JENKA COFFEE SHOP", shopNameStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Địa chỉ
            r = ws.createRow(1);
            r.setHeightInPoints(18f);
            setCell(r, 0, config.getShopAddr(), shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Điện thoại
            r = ws.createRow(2);
            r.setHeightInPoints(18f);
            setCell(r, 0, config.getShopTel(), shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

            // Số TK
            r = ws.createRow(3);
            r.setHeightInPoints(18f);
            setCell(r, 0, config.getShopBank(), shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(3, 3, 0, 5));

            // Blank row
            r = ws.createRow(4);
            r.setHeightInPoints(18f);

            // Tiêu đề
            r = ws.createRow(5);
            r.setHeightInPoints(22.8f);
            setCell(r, 0, "HÓA ĐƠN BÁN HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            // Số hóa đơn
            r = ws.createRow(6);
            r.setHeightInPoints(19.5f);
            setCell(r, 0, "Số hóa đơn: HĐ-" + order.getId(), centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(6, 6, 0, 5));

            // Ngày
            r = ws.createRow(7);
            r.setHeightInPoints(21f);
            setCell(r, 1, "Ngày: " + ngayLap, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(7, 7, 1, 4));

            // Khách hàng
            r = ws.createRow(8);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Khách hàng: " + tenKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(8, 8, 0, 5));

            // Địa chỉ KH
            r = ws.createRow(9);
            r.setHeightInPoints(24f);
            setCell(r, 0, "Địa chỉ: " + diaChiKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(9, 9, 0, 5));

            // SĐT KH
            r = ws.createRow(10);
            r.setHeightInPoints(23.3f);
            setCell(r, 0, "SĐT: " + sdtKH, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(10, 10, 0, 5));

            // Table Header
            r = ws.createRow(11);
            r.setHeightInPoints(32f);
            setCell(r, 0, "STT",        headerCell);
            setCell(r, 1, "Tên Hàng",   headerCell);
            setCell(r, 2, "ĐVT",        headerCell);
            setCell(r, 3, "SL",         headerCell);
            setCell(r, 4, "Đơn giá",    headerCell);
            setCell(r, 5, "Thành Tiền", headerCell);

            // Table Data Rows
            int dataStartRow = 12;
            for (int i = 0; i < maxRows; i++) {
                int rowIdx = dataStartRow + i;
                int excelRow = rowIdx + 1;
                r = ws.createRow(rowIdx);
                r.setHeightInPoints(23f);

                Cell cSTT = r.createCell(0);
                cSTT.setCellFormula("ROW()-12");
                cSTT.setCellStyle(dataCellCenter);

                if (i < N) {
                    ChiTietBanHang item = items.get(i);
                    String tenSp = item.getSanPham() != null ? item.getSanPham().getTenSanPham() : "";
                    String dvt = item.getDonVi() != null ? item.getDonVi() : 
                                 (item.getSanPham() != null && item.getSanPham().getDonViTinh() != null ? 
                                  item.getSanPham().getDonViTinh().getTenDonVi() : "Cái");
                    
                    setCell(r, 1, tenSp, dataCellLeft);
                    setCell(r, 2, dvt, dataCellCenter);
                    setCellNum(r, 3, item.getSoLuong(), dataCellCenter);
                    setCellNum(r, 4, item.getGiaBan() != null ? item.getGiaBan().doubleValue() : 0, dataCellNumber);
                } else {
                    createBlankCell(r, 1, dataCellLeft);
                    createBlankCell(r, 2, dataCellCenter);
                    createBlankCell(r, 3, dataCellCenter);
                    createBlankCell(r, 4, dataCellNumber);
                }

                Cell cTT = r.createCell(5);
                cTT.setCellFormula("IFERROR(D" + excelRow + "*E" + excelRow + ",0)");
                cTT.setCellStyle(formulaNumStyle);
            }

            int endRowIdx = dataStartRow + maxRows;
            int firstDataExcelRow = dataStartRow + 1;
            int lastDataExcelRow  = endRowIdx;

            // Tổng cộng
            r = ws.createRow(endRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Tổng cộng:", salesTotalLabelStyle);
            setCell(r, 1, "", salesTotalLabelStyle);
            ws.addMergedRegion(new CellRangeAddress(endRowIdx, endRowIdx, 0, 1));
            setCell(r, 2, "", salesTotalLabelStyle);

            CellStyle salesTotalQtyStyle = createFontStyle(wb, 16, true, false);
            salesTotalQtyStyle.setAlignment(HorizontalAlignment.CENTER);
            salesTotalQtyStyle.setDataFormat(dfmt.getFormat("#,##0"));
            Cell cTotalQty = r.createCell(3);
            cTotalQty.setCellFormula("SUM(D" + firstDataExcelRow + ":D" + lastDataExcelRow + ")");
            cTotalQty.setCellStyle(salesTotalQtyStyle);
            setCell(r, 4, "", salesTotalLabelStyle);

            Cell cTotal = r.createCell(5);
            cTotal.setCellFormula("SUM(F" + firstDataExcelRow + ":F" + lastDataExcelRow + ")");
            cTotal.setCellStyle(salesTotalValueStyle);

            int totalExcelRow = endRowIdx + 1;

            // Khách hàng thanh toán
            int paidRowIdx = endRowIdx + 1;
            r = ws.createRow(paidRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Khách hàng thanh toán:", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(paidRowIdx, paidRowIdx, 0, 4));
            setCellNum(r, 5, order.getTienDaThanhToan() != null ? order.getTienDaThanhToan().doubleValue() : 0, summaryValueStyle);

            int paidExcelRow = paidRowIdx + 1;

            // Còn lại
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

            // Chữ ký
            int signRowIdx = endRowIdx + 3;
            r = ws.createRow(signRowIdx);
            r.setHeightInPoints(21f);
            setCell(r, 4, "Người bán hàng", centerBold16);
            ws.addMergedRegion(new CellRangeAddress(signRowIdx, signRowIdx, 4, 5));

            r = ws.createRow(endRowIdx + 4);
            r.setHeightInPoints(29f);
            setCell(r, 4, tenNV, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(endRowIdx + 4, endRowIdx + 4, 4, 5));

            // Lưu ý & Quy định
            int noteStart = endRowIdx + 5;
            int noteRowIdx = noteStart;

            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(19.1f);
            setCell(r, 0, "LƯU Ý:", noteHeadingStyle);
            Row row28 = ws.createRow(noteRowIdx + 1);
            row28.setHeightInPoints(13.1f);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx + 1, 0, 5));
            noteRowIdx += 2;

            String notesText = config.getShopNotes() != null ? config.getShopNotes() : "";
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

            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(46.5f);
            setCell(r, 0, notesPart2.toString(), noteContentStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(24f);
            setCell(r, 0, "QUY ĐỊNH ĐỔI  VÀ HOÀN TRẢ HÀNG:", noteHeadingStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            r = ws.createRow(noteRowIdx);
            r.setHeightInPoints(196.1f);

            XSSFFont fontHeadingBU = (XSSFFont) wb.createFont();
            fontHeadingBU.setFontName(FONT_NAME);
            fontHeadingBU.setFontHeightInPoints((short) 14);
            fontHeadingBU.setBold(true);
            fontHeadingBU.setUnderline(Font.U_SINGLE);

            XSSFFont fontNormal14 = (XSSFFont) wb.createFont();
            fontNormal14.setFontName(FONT_NAME);
            fontNormal14.setFontHeightInPoints((short) 14);

            XSSFFont fontBold14 = (XSSFFont) wb.createFont();
            fontBold14.setFontName(FONT_NAME);
            fontBold14.setFontHeightInPoints((short) 14);
            fontBold14.setBold(true);

            String pDoiTra = (config.getShopPolicy() != null ? config.getShopPolicy().trim() : "") + "\n\n";
            String part1 = "THỜI GIAN BẢO HÀNH THEO TỪNG SẢN PHẨM:\n";
            String part2 = (config.getShopWarranty() != null ? config.getShopWarranty().trim() : "") + "\n";
            String part3 = config.getShopWarrantyLimit() != null ? config.getShopWarrantyLimit().trim() : "";

            XSSFRichTextString richText = new XSSFRichTextString();
            richText.append(pDoiTra, fontNormal14);
            richText.append(part1, fontHeadingBU);
            richText.append(part2, fontNormal14);
            richText.append(part3, fontBold14);

            r.createCell(0).setCellValue(richText);
            r.getCell(0).setCellStyle(noteContentStyle);
            ws.addMergedRegion(new CellRangeAddress(noteRowIdx, noteRowIdx, 0, 5));
            noteRowIdx++;

            wb.setPrintArea(wb.getSheetIndex(ws), 0, 5, 0, noteRowIdx - 1);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PHIẾU NHẬP HÀNG
    // ══════════════════════════════════════════════════════════════

    public static byte[] exportPurchaseOrder(NhapHang order, StoreConfig config) throws Exception {
        String tenNCC = "Tư nhân";
        String diaChiNCC = "";
        String sdtNCC = "";
        
        if (order.getDoiTac() != null) {
            tenNCC = nvl(order.getDoiTac().getTen(), "Tư nhân");
            diaChiNCC = nvl(order.getDoiTac().getDiaChi(), "");
            sdtNCC = nvl(order.getDoiTac().getSdt(), "");
        }

        String ngayLap = "";
        if (order.getThoiGian() != null) {
            ngayLap = order.getThoiGian().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        List<ChiTietNhapHang> items = new ArrayList<>();
        if (order.getChiTietNhapHangs() != null) {
            items = order.getChiTietNhapHangs();
        }

        int N = items.size();
        int maxRows = Math.max(9, N);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet ws = wb.createSheet("PHIEU NHAP HANG");
            setColumnWidths(ws);

            // Căn lề trang in
            ws.setMargin(Sheet.TopMargin, 0.65);
            ws.setMargin(Sheet.HeaderMargin, 0.3);
            ws.setMargin(Sheet.RightMargin, 0.25);
            ws.setMargin(Sheet.FooterMargin, 0.2);
            ws.setMargin(Sheet.BottomMargin, 0.2);
            ws.setMargin(Sheet.LeftMargin, 0.55);

            // Cấu hình in
            PrintSetup printSetup = ws.getPrintSetup();
            printSetup.setLandscape(true);
            printSetup.setScale((short) 69);
            printSetup.setPaperSize(PrintSetup.A5_PAPERSIZE);

            DataFormat dfmt = wb.createDataFormat();

            CellStyle shopNameStyle  = createFontStyle(wb, 16, true, false);
            CellStyle shopInfoStyle  = createFontStyle(wb, 14, false, false);
            CellStyle titleStyle     = createFontStyle(wb, 18, true, false);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle centerPlain16  = createFontStyle(wb, 16, false, false);
            centerPlain16.setAlignment(HorizontalAlignment.CENTER);
            CellStyle plainStyle     = createFontStyle(wb, 16, false, false);

            CellStyle headerCell     = createSalesTableHeaderStyle(wb, 16);
            CellStyle dataCellCenter = createDataCellStyle(wb, HorizontalAlignment.CENTER, null, dfmt, 16);
            CellStyle dataCellLeft   = createDataCellStyle(wb, HorizontalAlignment.LEFT, null, dfmt, 16);
            CellStyle dataCellNumber = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);
            CellStyle formulaNumStyle = createDataCellStyle(wb, HorizontalAlignment.RIGHT, "#,##0;-#,##0;\" - \"", dfmt, 16);

            CellStyle summaryLabelStyle = createFontStyle(wb, 16, true, false);
            CellStyle summaryValueStyle = createFontStyle(wb, 16, false, false);
            summaryValueStyle.setAlignment(HorizontalAlignment.RIGHT);
            summaryValueStyle.setDataFormat(dfmt.getFormat("#,##0"));

            // Tên cửa hàng
            Row r = ws.createRow(0);
            r.setHeightInPoints(21f);
            setCell(r, 0, config.getShopNamePnh() != null ? config.getShopNamePnh() : "Jenka Coffee Shop", shopNameStyle);
            ws.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Địa chỉ
            r = ws.createRow(1);
            r.setHeightInPoints(21f);
            setCell(r, 0, config.getShopAddr(), shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Điện thoại
            r = ws.createRow(2);
            r.setHeightInPoints(21f);
            setCell(r, 0, config.getShopTel(), shopInfoStyle);
            ws.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));

            // Blank row
            r = ws.createRow(3);
            r.setHeightInPoints(12f);

            // Tiêu đề
            r = ws.createRow(4);
            r.setHeightInPoints(20f);
            setCell(r, 0, "PHIẾU NHẬP HÀNG", titleStyle);
            ws.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));

            // Số phiếu nhập
            r = ws.createRow(5);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Số phiếu nhập: PN-" + order.getId(), centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            // Ngày
            r = ws.createRow(6);
            r.setHeightInPoints(20f);
            setCell(r, 1, "Ngày: " + ngayLap, centerPlain16);
            ws.addMergedRegion(new CellRangeAddress(6, 6, 1, 4));

            // Nhà cung cấp
            r = ws.createRow(7);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Nhà cung cấp: " + tenNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(7, 7, 0, 5));

            // Địa chỉ NCC
            r = ws.createRow(8);
            r.setHeightInPoints(21f);
            setCell(r, 0, "Địa chỉ: " + diaChiNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(8, 8, 0, 5));

            // SĐT NCC
            r = ws.createRow(9);
            r.setHeightInPoints(21f);
            setCell(r, 0, "SĐT: " + sdtNCC, plainStyle);
            ws.addMergedRegion(new CellRangeAddress(9, 9, 0, 5));

            // Table Header
            r = ws.createRow(10);
            r.setHeightInPoints(27f);
            setCell(r, 0, "STT",        headerCell);
            setCell(r, 1, "Tên Hàng",   headerCell);
            setCell(r, 2, "ĐVT",        headerCell);
            setCell(r, 3, "SL",         headerCell);
            setCell(r, 4, "Đơn giá",    headerCell);
            setCell(r, 5, "Thành Tiền", headerCell);

            // Table Data Rows
            int dataStartRow = 11;
            for (int i = 0; i < maxRows; i++) {
                int rowIdx = dataStartRow + i;
                int excelRow = rowIdx + 1;
                r = ws.createRow(rowIdx);
                if (i < 9) {
                    r.setHeightInPoints(20f);
                } else {
                    r.setHeightInPoints(21f);
                }

                Cell cSTT = r.createCell(0);
                cSTT.setCellFormula("ROW()-11");
                cSTT.setCellStyle(dataCellCenter);

                if (i < N) {
                    ChiTietNhapHang item = items.get(i);
                    String tenSp = item.getSanPham() != null ? item.getSanPham().getTenSanPham() : "";
                    String dvt = item.getDonVi() != null ? item.getDonVi() :
                                 (item.getSanPham() != null && item.getSanPham().getDonViTinh() != null ?
                                  item.getSanPham().getDonViTinh().getTenDonVi() : "Cái");
                    
                    setCell(r, 1, tenSp, dataCellLeft);
                    setCell(r, 2, dvt, dataCellCenter);
                    setCellNum(r, 3, item.getSoLuong(), dataCellCenter);
                    setCellNum(r, 4, item.getGiaNhap() != null ? item.getGiaNhap().doubleValue() : 0, dataCellNumber);
                } else {
                    createBlankCell(r, 1, dataCellLeft);
                    createBlankCell(r, 2, dataCellCenter);
                    createBlankCell(r, 3, dataCellCenter);
                    createBlankCell(r, 4, dataCellNumber);
                }

                Cell cTT = r.createCell(5);
                cTT.setCellFormula("IFERROR(D" + excelRow + "*E" + excelRow + ",0)");
                cTT.setCellStyle(formulaNumStyle);
            }

            int endRowIdx = dataStartRow + maxRows;
            int firstDataExcelRow = dataStartRow + 1;
            int lastDataExcelRow  = endRowIdx;

            // Tổng cộng
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

            int totalExcelRow = endRowIdx + 1;

            // Đã TT
            int paidRowIdx = endRowIdx + 1;
            r = ws.createRow(paidRowIdx);
            r.setHeightInPoints(21.8f);
            setCell(r, 0, "Đã TT", summaryLabelStyle);
            for (int c = 1; c <= 4; c++) {
                r.createCell(c);
            }
            ws.addMergedRegion(new CellRangeAddress(paidRowIdx, paidRowIdx, 0, 4));
            setCellNum(r, 5, order.getDaThanhToan() != null ? order.getDaThanhToan().doubleValue() : 0, summaryValueStyle);

            int paidExcelRow = paidRowIdx + 1;

            // Còn lại
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

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ══════════════════════════════════════════════════════════════

    private static void setColumnWidths(XSSFSheet ws) {
        ws.setColumnWidth(0, (int)(7    * 256));  // A – STT
        ws.setColumnWidth(1, (int)(61.1 * 256));  // B – Tên Hàng
        ws.setColumnWidth(2, (int)(9.2  * 256));  // C – ĐVT
        ws.setColumnWidth(3, (int)(9.2  * 256));  // D – SL
        ws.setColumnWidth(4, (int)(17.4 * 256));  // E – Đơn giá
        ws.setColumnWidth(5, (int)(17.4 * 256));  // F – Thành Tiền
    }

    private static void setSalesColumnWidths(XSSFSheet ws) {
        ws.setColumnWidth(0, (int)(10.6 * 256));  // A – STT
        ws.setColumnWidth(1, (int)(51.5 * 256));  // B – Tên Hàng
        ws.setColumnWidth(2, (int)(9.8  * 256));  // C – ĐVT
        ws.setColumnWidth(3, (int)(9.8  * 256));  // D – SL
        ws.setColumnWidth(4, (int)(17.9 * 256));  // E – Đơn giá
        ws.setColumnWidth(5, (int)(17.9 * 256));  // F – Thành Tiền
    }

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

    private static void applyThinBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private static void setCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        if (style != null) c.setCellStyle(style);
    }

    private static void setCellNum(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        if (style != null) c.setCellStyle(style);
    }

    private static void createBlankCell(Row row, int col, CellStyle style) {
        Cell c = row.createCell(col, CellType.BLANK);
        if (style != null) c.setCellStyle(style);
    }

    private static String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }
}

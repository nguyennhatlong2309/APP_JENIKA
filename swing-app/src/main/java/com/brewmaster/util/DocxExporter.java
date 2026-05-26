package com.brewmaster.util;

import com.brewmaster.db.DatabaseManager;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Xuất phiếu nhập hàng / hóa đơn bán hàng ra file .docx
 * theo đúng mẫu đẹp mắt như Excel và mau hoa don.jpg.
 *
 * Tất cả font: Times New Roman.
 * Bảng sản phẩm HÓA ĐƠN: tối thiểu 9 dòng.
 * Bảng sản phẩm PHIẾU NHẬP: tối thiểu 23 dòng.
 * Nội dung thường: 14pt. Tiêu đề và nội dung bảng: 16pt.
 */
public class DocxExporter {

    // Các thông tin cửa hàng đã được chuyển sang nạp động từ database qua StoreConfig
    private static final String FONT_NAME = "Times New Roman";

    // Format number to #,##0
    private static String formatNumber(long amount) {
        return new DecimalFormat("#,##0").format(amount);
    }

    private static String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    // Helper to add a stylized paragraph with specific properties
    private static XWPFParagraph addParagraph(XWPFDocument doc, String text, int fontSize, boolean bold, boolean italic, boolean underline, ParagraphAlignment alignment, int spaceBefore, int spaceAfter) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(alignment);
        p.setSpacingBefore(spaceBefore);
        p.setSpacingAfter(spaceAfter);
        
        if (text != null && !text.isEmpty()) {
            XWPFRun r = p.createRun();
            r.setFontFamily(FONT_NAME);
            r.setFontSize(fontSize);
            r.setBold(bold);
            r.setItalic(italic);
            if (underline) {
                r.setUnderline(UnderlinePatterns.SINGLE);
            }
            r.setText(text);
        }
        return p;
    }

    // Helper to format a cell paragraph
    private static void formatCellParagraph(XWPFTableCell cell, String text, int fontSize, boolean bold, ParagraphAlignment alignment) {
        // Clear default paragraphs if any
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(alignment);
        p.setSpacingBefore(80); // inner spacing in dxa (20 dxa = 1 pt)
        p.setSpacingAfter(80);
        
        XWPFRun r = p.createRun();
        r.setFontFamily(FONT_NAME);
        r.setFontSize(fontSize);
        r.setBold(bold);
        r.setText(text != null ? text : "");
    }

    // Set background color of a cell using standard setColor
    private static void setCellBackground(XWPFTableCell cell, String hexColor) {
        cell.setColor(hexColor);
    }

    // Apply borders to table
    private static void applyTableBorders(XWPFTable table) {
        table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
        table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
        table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
        table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
        table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
        table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "A0A0A0");
    }

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

        String tenKH = "", diaChiKH = "", sdtKH = "";
        long tongTien = 0, daThanhToan = 0, conNo = 0;
        String ngayLap = "";

        try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenKH       = nvl(rs.getString("ten_khach_hang"), "Khách vãng lai");
                    diaChiKH    = nvl(rs.getString("kh_dc"), "");
                    sdtKH       = nvl(rs.getString("kh_sdt"), "");
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

        // ── Build Word Document ──
        try (XWPFDocument doc = new XWPFDocument()) {
            
            // ── STORE INFO (Times New Roman, ShopName: 16pt bold, ShopInfo: 14pt plain) ──
            // Nạp động từ cấu hình cửa hàng
            StoreConfig.loadFromDatabase();
            addParagraph(doc, StoreConfig.shopName, 16, true, false, false, ParagraphAlignment.LEFT, 0, 20);
            addParagraph(doc, StoreConfig.shopAddr, 14, false, false, false, ParagraphAlignment.LEFT, 0, 20);
            addParagraph(doc, StoreConfig.shopTel, 14, false, false, false, ParagraphAlignment.LEFT, 0, 20);
            addParagraph(doc, StoreConfig.shopBank, 14, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 60);

            // ── TITLE (Times New Roman, 16pt, bold) ──
            addParagraph(doc, "HÓA ĐƠN BÁN HÀNG", 16, true, false, false, ParagraphAlignment.CENTER, 0, 20);
            addParagraph(doc, "Số hóa đơn: HĐ-" + orderId, 16, true, false, false, ParagraphAlignment.CENTER, 0, 20);
            addParagraph(doc, "Ngày: " + ngayLap, 16, false, false, false, ParagraphAlignment.CENTER, 0, 200);

            // ── CUSTOMER INFO (Times New Roman, 14pt plain) ──
            addParagraph(doc, "Khách hàng: " + tenKH, 14, false, false, false, ParagraphAlignment.LEFT, 0, 40);
            addParagraph(doc, "Địa chỉ: " + diaChiKH, 14, false, false, false, ParagraphAlignment.LEFT, 0, 40);
            addParagraph(doc, "SĐT: " + sdtKH, 14, false, false, false, ParagraphAlignment.LEFT, 0, 200);

            // ── TABLE (Times New Roman, 16pt, Header background: LIGHT CORNFLOWER BLUE #B4C6E7) ──
            // 6 columns: STT, Tên Hàng, ĐVT, SL, Đơn giá, Thành Tiền
            XWPFTable table = doc.createTable(maxRows + 1, 6);
            table.setWidth("100%");
            applyTableBorders(table);

            // Column Widths in dxa (approximate: STT: 5%, Tên Hàng: 45%, ĐVT: 10%, SL: 8%, Đơn giá: 16%, Thành tiền: 16%)
            int[] colWidths = {600, 4000, 1000, 800, 1600, 1600};

            // Table Header
            String[] headers = {"STT", "Tên Hàng", "ĐVT", "SL", "Đơn giá", "Thành Tiền"};
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.setHeight(400); // height in dxa
            for (int col = 0; col < 6; col++) {
                XWPFTableCell cell = headerRow.getCell(col);
                cell.setWidth(String.valueOf(colWidths[col]));
                setCellBackground(cell, "B4C6E7"); // Light blue backgound
                formatCellParagraph(cell, headers[col], 16, true, ParagraphAlignment.CENTER);
            }

            // Table Rows
            for (int i = 0; i < maxRows; i++) {
                XWPFTableRow row = table.getRow(i + 1);
                row.setHeight(360);
                
                String sttVal = String.valueOf(i + 1);
                String nameVal = "";
                String dvtVal = "Cái";
                String slVal = "";
                String priceVal = "0";
                String totalVal = "0";

                if (i < N) {
                    Object[] item = items.get(i);
                    nameVal = (String) item[0];
                    dvtVal = (String) item[1];
                    slVal = String.valueOf(item[2]);
                    priceVal = formatNumber((long) item[3]);
                    totalVal = formatNumber((long) item[4]);
                } else {
                    priceVal = "";
                    totalVal = "";
                    slVal = "";
                }

                // Col 0: STT (center)
                formatCellParagraph(row.getCell(0), sttVal, 16, false, ParagraphAlignment.CENTER);
                // Col 1: Tên Hàng (left)
                formatCellParagraph(row.getCell(1), nameVal, 16, false, ParagraphAlignment.LEFT);
                // Col 2: ĐVT (center)
                formatCellParagraph(row.getCell(2), dvtVal, 16, false, ParagraphAlignment.CENTER);
                // Col 3: SL (center)
                formatCellParagraph(row.getCell(3), slVal, 16, false, ParagraphAlignment.CENTER);
                // Col 4: Đơn giá (right)
                formatCellParagraph(row.getCell(4), priceVal, 16, false, ParagraphAlignment.RIGHT);
                // Col 5: Thành Tiền (right)
                formatCellParagraph(row.getCell(5), totalVal, 16, false, ParagraphAlignment.RIGHT);
                
                // Apply widths to all cells in the row
                for (int col = 0; col < 6; col++) {
                    row.getCell(col).setWidth(String.valueOf(colWidths[col]));
                }
            }

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // ── TOTALS (Times New Roman, 16pt, Bold labels, Double bottom border for values) ──
            // Using a borderless table to align Totals nicely on the right
            XWPFTable totalsTable = doc.createTable(3, 2);
            totalsTable.setWidth("100%");
            // Set borderless
            totalsTable.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");

            String[][] totalsData = {
                {"Tổng cộng:", formatNumber(tongTien) + " ₫"},
                {"Khách hàng thanh toán:", formatNumber(daThanhToan) + " ₫"},
                {"Còn lại:", formatNumber(conNo) + " ₫"}
            };

            for (int r = 0; r < 3; r++) {
                XWPFTableRow row = totalsTable.getRow(r);
                row.setHeight(300);
                
                // Label (Col 0) - Width 5000 dxa (align right)
                XWPFTableCell cellLbl = row.getCell(0);
                cellLbl.setWidth("6800");
                formatCellParagraph(cellLbl, totalsData[r][0], 16, true, ParagraphAlignment.RIGHT);
                
                // Value (Col 1) - Width 2000 dxa (align right)
                XWPFTableCell cellVal = row.getCell(1);
                cellVal.setWidth("2800");
                // Bold totals and debt, plain for paid
                boolean isBold = (r == 0 || r == 2);
                formatCellParagraph(cellVal, totalsData[r][1], 16, isBold, ParagraphAlignment.RIGHT);
                
                // No cell-level borders needed for totals table to ensure cross-version compatibility
                if (r == 0) {
                    // cell-level borders omitted
                } else if (r == 2) {
                    // cell-level borders omitted
                }
            }

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // ── SIGNATURES (Times New Roman, 16pt, centered on right) ──
            XWPFTable sigTable = doc.createTable(2, 2);
            sigTable.setWidth("100%");
            sigTable.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            
            XWPFTableRow sigRow0 = sigTable.getRow(0);
            sigRow0.setHeight(300);
            sigRow0.getCell(0).setWidth("6000");
            
            XWPFTableCell sigCell = sigRow0.getCell(1);
            sigCell.setWidth("3600");
            formatCellParagraph(sigCell, "Người bán hàng", 16, true, ParagraphAlignment.CENTER);
            
            // Spacer row for actual signature
            XWPFTableRow sigRow1 = sigTable.getRow(1);
            sigRow1.setHeight(1000); // Big space for signature
            sigRow1.getCell(0).setWidth("6000");
            sigRow1.getCell(1).setWidth("3600");
            formatCellParagraph(sigRow1.getCell(1), "\n\n\n", 16, false, ParagraphAlignment.CENTER);

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 150);

            // ── TERMS & CONDITIONS (Times New Roman, 14pt, Bold Underline Headings) ──
            addParagraph(doc, "LƯU Ý:", 14, true, false, true, ParagraphAlignment.LEFT, 0, 40);
            if (StoreConfig.shopNotes != null && !StoreConfig.shopNotes.isBlank()) {
                String[] notes = StoreConfig.shopNotes.split("\n");
                for (int i = 0; i < notes.length; i++) {
                    String noteLine = notes[i];
                    int bottomSpace = (i == notes.length - 1) ? 120 : 40;
                    addParagraph(doc, noteLine, 14, false, false, false, ParagraphAlignment.LEFT, 0, bottomSpace);
                }
            }

            addParagraph(doc, "QUY ĐỊNH ĐỔI  VÀ HOÀN TRẢ HÀNG:", 14, true, false, true, ParagraphAlignment.LEFT, 0, 40);
            if (StoreConfig.shopPolicy != null && !StoreConfig.shopPolicy.isBlank()) {
                String[] policies = StoreConfig.shopPolicy.split("\n");
                for (int i = 0; i < policies.length; i++) {
                    String policyLine = policies[i];
                    int bottomSpace = (i == policies.length - 1) ? 120 : 20;
                    addParagraph(doc, policyLine, 14, false, false, false, ParagraphAlignment.LEFT, 0, bottomSpace);
                }
            }

            addParagraph(doc, "THỜI GIAN BẢO HÀNH THEO TỪNG SẢN PHẨM:", 14, true, false, true, ParagraphAlignment.LEFT, 0, 40);
            if (StoreConfig.shopWarranty != null && !StoreConfig.shopWarranty.isBlank()) {
                String[] warranties = StoreConfig.shopWarranty.split("\n");
                for (String warLine : warranties) {
                    addParagraph(doc, warLine, 14, false, false, false, ParagraphAlignment.LEFT, 0, 20);
                }
            }
            if (StoreConfig.shopWarrantyLimit != null && !StoreConfig.shopWarrantyLimit.isBlank()) {
                addParagraph(doc, StoreConfig.shopWarrantyLimit, 14, false, false, false, ParagraphAlignment.LEFT, 0, 40);
            }

            // ── Save File ──
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                doc.write(fos);
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
        int maxRows = Math.max(23, N);

        // ── Build Word Document ──
        try (XWPFDocument doc = new XWPFDocument()) {
            
            // ── STORE INFO (Times New Roman, ShopName: 16pt bold, ShopInfo: 14pt plain) ──
            // Nạp động từ cấu hình cửa hàng
            StoreConfig.loadFromDatabase();
            addParagraph(doc, StoreConfig.shopNamePnh, 16, true, false, false, ParagraphAlignment.LEFT, 0, 20);
            addParagraph(doc, StoreConfig.shopAddr, 14, false, false, false, ParagraphAlignment.LEFT, 0, 20);
            addParagraph(doc, StoreConfig.shopTel, 14, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 60);

            // ── TITLE (Times New Roman, 16pt, bold) ──
            addParagraph(doc, "PHIẾU NHẬP HÀNG", 16, true, false, false, ParagraphAlignment.CENTER, 0, 20);
            addParagraph(doc, "Số phiếu nhập: PN-" + orderId, 16, true, false, false, ParagraphAlignment.CENTER, 0, 20);
            addParagraph(doc, "Ngày: " + ngayLap, 16, false, false, false, ParagraphAlignment.CENTER, 0, 200);

            // ── SUPPLIER INFO (Times New Roman, 14pt plain) ──
            addParagraph(doc, "Nhà cung cấp: " + tenNCC, 14, false, false, false, ParagraphAlignment.LEFT, 0, 40);
            addParagraph(doc, "Địa chỉ: " + diaChiNCC, 14, false, false, false, ParagraphAlignment.LEFT, 0, 40);
            addParagraph(doc, "SĐT: " + sdtNCC, 14, false, false, false, ParagraphAlignment.LEFT, 0, 200);

            // ── TABLE (Times New Roman, 16pt, Header background: LIGHT CORNFLOWER BLUE #B4C6E7) ──
            // 6 columns: STT, Tên Hàng, ĐVT, SL, Đơn giá, Thành Tiền
            XWPFTable table = doc.createTable(maxRows + 1, 6);
            table.setWidth("100%");
            applyTableBorders(table);

            int[] colWidths = {600, 4000, 1000, 800, 1600, 1600};

            // Table Header
            String[] headers = {"STT", "Tên Hàng", "ĐVT", "SL", "Đơn giá", "Thành Tiền"};
            XWPFTableRow headerRow = table.getRow(0);
            headerRow.setHeight(400);
            for (int col = 0; col < 6; col++) {
                XWPFTableCell cell = headerRow.getCell(col);
                cell.setWidth(String.valueOf(colWidths[col]));
                setCellBackground(cell, "B4C6E7");
                formatCellParagraph(cell, headers[col], 16, true, ParagraphAlignment.CENTER);
            }

            // Table Rows
            for (int i = 0; i < maxRows; i++) {
                XWPFTableRow row = table.getRow(i + 1);
                row.setHeight(360);
                
                String sttVal = String.valueOf(i + 1);
                String nameVal = "";
                String dvtVal = "Cái";
                String slVal = "";
                String priceVal = "0";
                String totalVal = "0";

                if (i < N) {
                    Object[] item = items.get(i);
                    nameVal = (String) item[0];
                    dvtVal = (String) item[1];
                    slVal = String.valueOf(item[2]);
                    priceVal = formatNumber((long) item[3]);
                    totalVal = formatNumber((long) item[4]);
                } else {
                    priceVal = "";
                    totalVal = "";
                    slVal = "";
                }

                // Col 0: STT
                formatCellParagraph(row.getCell(0), sttVal, 16, false, ParagraphAlignment.CENTER);
                // Col 1: Tên Hàng
                formatCellParagraph(row.getCell(1), nameVal, 16, false, ParagraphAlignment.LEFT);
                // Col 2: ĐVT
                formatCellParagraph(row.getCell(2), dvtVal, 16, false, ParagraphAlignment.CENTER);
                // Col 3: SL
                formatCellParagraph(row.getCell(3), slVal, 16, false, ParagraphAlignment.CENTER);
                // Col 4: Đơn giá
                formatCellParagraph(row.getCell(4), priceVal, 16, false, ParagraphAlignment.RIGHT);
                // Col 5: Thành Tiền
                formatCellParagraph(row.getCell(5), totalVal, 16, false, ParagraphAlignment.RIGHT);
                
                for (int col = 0; col < 6; col++) {
                    row.getCell(col).setWidth(String.valueOf(colWidths[col]));
                }
            }

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // ── TOTALS (Times New Roman, 16pt, Bold labels, double underline) ──
            XWPFTable totalsTable = doc.createTable(3, 2);
            totalsTable.setWidth("100%");
            totalsTable.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            totalsTable.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");

            String[][] totalsData = {
                {"Tổng Cộng:", formatNumber(tongTien) + " ₫"},
                {"Đã TT:", formatNumber(daThanhToan) + " ₫"},
                {"Còn lại:", formatNumber(conNo) + " ₫"}
            };

            for (int r = 0; r < 3; r++) {
                XWPFTableRow row = totalsTable.getRow(r);
                row.setHeight(300);
                
                XWPFTableCell cellLbl = row.getCell(0);
                cellLbl.setWidth("6800");
                formatCellParagraph(cellLbl, totalsData[r][0], 16, true, ParagraphAlignment.RIGHT);
                
                XWPFTableCell cellVal = row.getCell(1);
                cellVal.setWidth("2800");
                boolean isBold = (r == 0 || r == 2);
                formatCellParagraph(cellVal, totalsData[r][1], 16, isBold, ParagraphAlignment.RIGHT);
                
                if (r == 0) {
                    // cell-level borders omitted
                } else if (r == 2) {
                    // cell-level borders omitted
                }
            }

            // Spacer
            addParagraph(doc, "", 10, false, false, false, ParagraphAlignment.LEFT, 0, 100);

            // ── SIGNATURES (Times New Roman, 16pt, centered on right) ──
            XWPFTable sigTable = doc.createTable(2, 2);
            sigTable.setWidth("100%");
            sigTable.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            sigTable.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "FFFFFF");
            
            XWPFTableRow sigRow0 = sigTable.getRow(0);
            sigRow0.setHeight(300);
            sigRow0.getCell(0).setWidth("6000");
            
            XWPFTableCell sigCell = sigRow0.getCell(1);
            sigCell.setWidth("3600");
            formatCellParagraph(sigCell, "Người lập phiếu", 16, true, ParagraphAlignment.CENTER);
            
            XWPFTableRow sigRow1 = sigTable.getRow(1);
            sigRow1.setHeight(1000);
            sigRow1.getCell(0).setWidth("6000");
            sigRow1.getCell(1).setWidth("3600");
            formatCellParagraph(sigRow1.getCell(1), "\n\n\n", 16, false, ParagraphAlignment.CENTER);

            // ── Save File ──
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                doc.write(fos);
            }
        }
    }
}

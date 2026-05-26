package com.brewmaster.theme;

import java.awt.*;

/**
 * Hệ thống màu sắc và font BrewMaster Pro
 * Ánh xạ từ Tailwind CSS color tokens sang Java Color
 */
public class AppTheme {

    // === MÀU SẮC CHÍNH ===
    public static final Color BACKGROUND       = hex("#131313");
    public static final Color SURFACE          = hex("#131313");
    public static final Color SURFACE_LOW      = hex("#1C1B1B"); // sidebar
    public static final Color SURFACE_MED      = hex("#201F1F");
    public static final Color SURFACE_HIGH     = hex("#2A2A2A"); // table header, cards
    public static final Color SURFACE_HIGHEST  = hex("#353534"); // hover
    public static final Color SURFACE_BRIGHT   = hex("#393939");
    public static final Color SURFACE_VARIANT  = hex("#353534");

    // === ACCENT CHÍNH - Cam ấm ===
    public static final Color PRIMARY          = hex("#F2BE8C");
    public static final Color PRIMARY_DARK     = hex("#D4A373");
    public static final Color PRIMARY_CONTAINER= hex("#D4A373");
    public static final Color ON_PRIMARY       = hex("#482904");
    public static final Color ON_PRIMARY_CONT  = hex("#5B3912");

    // === ACCENT PHỤ - Xanh dương ===
    public static final Color SECONDARY       = hex("#9BCBF8");
    public static final Color SECONDARY_CONT  = hex("#144D73");

    // === ACCENT XANH LÁ ===
    public static final Color TERTIARY        = hex("#C3CCA6");
    public static final Color TERTIARY_CONT   = hex("#A8B18C");

    // === LỖI / CẢNH BÁO ===
    public static final Color ERROR           = hex("#FFB4AB");
    public static final Color ERROR_CONTAINER = hex("#93000A");
    public static final Color ERROR_LIGHT     = new Color(255, 180, 171, 30); // transparent

    // === TEXT ===
    public static final Color ON_SURFACE      = hex("#E5E2E1");
    public static final Color ON_SURFACE_VAR  = hex("#D4C4B7");

    // === BORDER / DIVIDER ===
    public static final Color OUTLINE         = hex("#9C8E82");
    public static final Color OUTLINE_VARIANT = hex("#50453B");

    // === TRẠNG THÁI HÓA ĐƠN ===
    public static final Color STATUS_PAID_BG  = new Color(16, 185, 129, 25);
    public static final Color STATUS_PAID_FG  = new Color(52, 211, 153);
    public static final Color STATUS_PEND_BG  = new Color(245, 158, 11, 25);
    public static final Color STATUS_PEND_FG  = new Color(251, 191, 36);
    public static final Color STATUS_CANC_BG  = new Color(239, 68, 68, 25);
    public static final Color STATUS_CANC_FG  = new Color(252, 165, 165);

    // === FONT ===
    public static final Font FONT_TITLE_LG   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE_MD   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TITLE_SM   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_MD    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_SM    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_MONO       = new Font("Consolas", Font.PLAIN, 13);

    // === KÍCH THƯỚC ===
    public static final int SIDEBAR_WIDTH    = 240;
    public static final int TOPBAR_HEIGHT    = 58;
    public static final int ROW_HEIGHT       = 46;
    public static final int CARD_ARC         = 14;
    public static final int BORDER_RADIUS    = 8;

    // === HELPER ===
    public static Color hex(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    /** Tạo màu với độ trong suốt */
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Lấy màu badge theo trạng thái hóa đơn xuất */
    public static Color[] getSalesStatusColor(String status) {
        if (status == null) return new Color[]{SURFACE_HIGH, ON_SURFACE_VAR};
        switch (status) {
            case "Đã thanh toán":
            case "Đã hoàn thành":
            case "Hoàn thành":    return new Color[]{STATUS_PAID_BG, STATUS_PAID_FG};
            case "Chờ thanh toán":
            case "Chờ xử lý":
            case "Hẹn":           return new Color[]{STATUS_PEND_BG, STATUS_PEND_FG};
            case "Đã hủy":
            case "Đã Hủy":        return new Color[]{STATUS_CANC_BG, STATUS_CANC_FG};
            default:              return new Color[]{SURFACE_HIGH, ON_SURFACE_VAR};
        }
    }

    /** Lấy màu badge theo trạng thái tồn kho */
    public static Color[] getStockStatusColor(String status) {
        if (status == null) return new Color[]{SURFACE_HIGH, ON_SURFACE_VAR};
        switch (status) {
            case "Còn hàng":  return new Color[]{STATUS_PAID_BG, STATUS_PAID_FG};
            case "Cảnh báo":
            case "Sắp hết":   return new Color[]{STATUS_PEND_BG, STATUS_PEND_FG};
            case "Hết hàng":  return new Color[]{STATUS_CANC_BG, STATUS_CANC_FG};
            default:          return new Color[]{SURFACE_HIGH, ON_SURFACE_VAR};
        }
    }

    /** Lấy màu badge thu/chi */
    public static Color[] getTransactionColor(String loai) {
        if ("Thu".equals(loai)) return new Color[]{new Color(195, 204, 166, 25), TERTIARY};
        return new Color[]{new Color(255, 180, 171, 25), ERROR};
    }
}

package com.brewmaster.theme;

import java.awt.*;

/**
 * Hệ thống màu sắc và font BrewMaster Pro
 * Ánh xạ từ Tailwind CSS color tokens sang Java Color
 */
public class AppTheme {

    // === MÀU SẮC CHÍNH ===
    public static final Color BACKGROUND       = hex("#FAF8F6"); // Soft warm cream white
    public static final Color SURFACE          = hex("#FAF8F6");
    public static final Color SURFACE_LOW      = hex("#F3ECE6"); // Slightly darker warm cream for sidebar/panels
    public static final Color SURFACE_MED      = hex("#EBE1D8"); // Table header, search fields, card background
    public static final Color SURFACE_HIGH     = hex("#E2D5C9"); // For dialog headers, secondary buttons
    public static final Color SURFACE_HIGHEST  = hex("#D9CAB9"); // For hover states, active selections
    public static final Color SURFACE_BRIGHT   = hex("#FFFFFF");
    public static final Color SURFACE_VARIANT  = hex("#EADECF");

    // === ACCENT CHÍNH - Cam/Nâu ấm (Coffee Theme) ===
    public static final Color PRIMARY          = hex("#7A5030"); // Rich coffee brown (used for primary buttons, focus highlights, badges)
    public static final Color PRIMARY_DARK     = hex("#5D3B22");
    public static final Color PRIMARY_CONTAINER= hex("#8D5F3D"); // Slightly lighter rich brown (used for sidebar active backgrounds)
    public static final Color ON_PRIMARY       = hex("#FFFFFF"); // Pure white text/icons on primary and container
    public static final Color ON_PRIMARY_CONT  = hex("#FFFFFF");

    // === ACCENT PHỤ - Xanh dương (Blue/Sky) ===
    public static final Color SECONDARY       = hex("#1D4ED8"); // Vibrant blue for secondary accent
    public static final Color SECONDARY_CONT  = hex("#DBEAFE");

    // === ACCENT XANH LÁ (Green) ===
    public static final Color TERTIARY        = hex("#059669"); // Emerald green
    public static final Color TERTIARY_CONT   = hex("#D1FAE5");

    // === LỖI / CẢNH BÁO (Red) ===
    public static final Color ERROR           = hex("#DC2626"); // Red
    public static final Color ERROR_CONTAINER = hex("#FEE2E2");
    public static final Color ERROR_LIGHT     = new Color(220, 38, 38, 30); // transparent

    // === TEXT ===
    public static final Color ON_SURFACE      = hex("#2D221A"); // Very dark coffee brown/charcoal (excellent readability, premium feel)
    public static final Color ON_SURFACE_VAR  = hex("#6E5E53"); // Muted medium coffee brown for subtitles/secondary text

    // === BORDER / DIVIDER ===
    public static final Color OUTLINE         = hex("#BCAE9F"); // Muted warm grey border
    public static final Color OUTLINE_VARIANT = hex("#DDD4CA"); // Subtle divider line

    // === TRẠNG THÁI HÓA ĐƠN ===
    public static final Color STATUS_PAID_BG  = new Color(5, 150, 105, 30); // 12% opacity emerald green
    public static final Color STATUS_PAID_FG  = new Color(4, 120, 87);      // dark green text
    public static final Color STATUS_PEND_BG  = new Color(217, 119, 6, 30);  // 12% opacity amber
    public static final Color STATUS_PEND_FG  = new Color(180, 83, 9);       // dark amber text
    public static final Color STATUS_CANC_BG  = new Color(220, 38, 38, 30);  // 12% opacity red
    public static final Color STATUS_CANC_FG  = new Color(185, 28, 28);      // dark red text

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

    /** Chuyển màu sang định dạng hex String */
    public static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
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
        if ("Thu".equals(loai)) return new Color[]{new Color(5, 150, 105, 30), TERTIARY};
        return new Color[]{new Color(220, 38, 38, 30), ERROR};
    }
}

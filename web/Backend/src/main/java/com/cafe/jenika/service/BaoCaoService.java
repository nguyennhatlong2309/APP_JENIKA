package com.cafe.jenika.service;

import com.cafe.jenika.model.ChiTietBanHang;
import com.cafe.jenika.model.ChiTietNhapHang;
import com.cafe.jenika.model.BanHang;
import com.cafe.jenika.model.NhapHang;
import com.cafe.jenika.dto.BanHangDTO;
import com.cafe.jenika.dto.NhapHangDTO;
import com.cafe.jenika.dto.SanPhamDTO;
import com.cafe.jenika.model.SanPham;
import com.cafe.jenika.repository.ChiTietBanHangRepository;
import com.cafe.jenika.repository.ChiTietNhapHangRepository;
import com.cafe.jenika.repository.NhapHangRepository;
import com.cafe.jenika.repository.NhapHangSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaoCaoService {

    @Autowired
    private BanHangService banHangService;

    @Autowired
    private NhapHangService nhapHangService;

    @Autowired
    private SanPhamService sanPhamService;

    @Autowired
    private ChiTietBanHangRepository chiTietBanHangRepository;

    @Autowired
    private ChiTietNhapHangRepository chiTietNhapHangRepository;

    @Autowired
    private NhapHangRepository nhapHangRepository;

    /**
     * Lấy dữ liệu tổng quan cho Dashboard.
     */
    public Map<String, Object> getDashboardData() {
        List<BanHangDTO> sales = banHangService.getAllSalesOrders();
        List<NhapHangDTO> imports = nhapHangService.getAllImportOrders();
        List<SanPhamDTO> products = sanPhamService.getAllActiveProducts();

        BigDecimal dailyRevenue = BigDecimal.ZERO;
        BigDecimal monthlyRevenue = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        for (BanHangDTO b : sales) {
            if ("Hủy".equalsIgnoreCase(b.getTrangThai())) {
                continue;
            }
            LocalDateTime saleTime = b.getThoiGian();

            if (saleTime.toLocalDate().isEqual(today)) {
                dailyRevenue = dailyRevenue.add(b.getTongTien());
            }

            if (saleTime.getYear() == now.getYear() && saleTime.getMonth() == now.getMonth()) {
                monthlyRevenue = monthlyRevenue.add(b.getTongTien());
            }

            if (b.getTienNo() != null) {
                totalDebt = totalDebt.add(b.getTienNo());
            }
        }

        long lowStockCount = products.stream()
                .filter(p -> p.getSoLuongTon() != null && p.getSoLuongTon() > 0
                        && p.getSoLuongTon() <= p.getCanhBaoTonKho())
                .count();

        long outOfStockCount = products.stream()
                .filter(p -> p.getSoLuongTon() == null || p.getSoLuongTon() == 0)
                .count();

        BigDecimal monthlyExpenses = BigDecimal.ZERO;
        for (NhapHangDTO n : imports) {
            if ("Hủy".equalsIgnoreCase(n.getTrangThai())) {
                continue;
            }
            LocalDateTime impTime = n.getThoiGian();
            if (impTime.getYear() == now.getYear() && impTime.getMonth() == now.getMonth()) {
                monthlyExpenses = monthlyExpenses.add(n.getTongTien());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dailyRevenue", dailyRevenue);
        data.put("monthlyRevenue", monthlyRevenue);
        data.put("totalDebt", totalDebt);
        data.put("lowStockCount", lowStockCount);
        data.put("outOfStockCount", outOfStockCount);
        data.put("monthlyExpenses", monthlyExpenses);
        data.put("totalProducts", products.size());

        return data;
    }

    /**
     * Lấy chi tiết danh sách sản phẩm bán kèm theo bộ lọc.
     */
    public List<Map<String, Object>> getProductSales(String search, Boolean isGift, String fromDate, String toDate) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }

        // Truy vấn dữ liệu đã lọc khoảng ngày và isGift trực tiếp tại Database
        List<ChiTietBanHang> details = chiTietBanHangRepository.findFiltered(from, to, isGift);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChiTietBanHang ct : details) {
            BanHang bh = ct.getBanHang();
            if (bh == null)
                continue;

            SanPham sp = ct.getSanPham();
            String prodName = sp != null ? sp.getTenSanPham() : "Sản phẩm ẩn";
            String partnerName = bh.getDoiTac() != null ? bh.getDoiTac().getTen() : "Khách vãng lai";
            String orderCode = "BH-" + bh.getId();

            // Bộ lọc tìm kiếm In-Memory trên tập dữ liệu đã thu hẹp
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                boolean matches = prodName.toLowerCase().contains(searchLower) ||
                        partnerName.toLowerCase().contains(searchLower) ||
                        orderCode.toLowerCase().contains(searchLower);
                if (!matches)
                    continue;
            }

            BigDecimal costPrice = ct.getGiaVon() != null ? ct.getGiaVon()
                    : (sp != null && sp.getGiaNhapHienTai() != null ? sp.getGiaNhapHienTai() : BigDecimal.ZERO);
            BigDecimal salePrice = ct.getGiaBan() != null ? ct.getGiaBan() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(ct.getSoLuong());

            BigDecimal actualSalePrice = ct.getIsGift() ? BigDecimal.ZERO : salePrice;
            BigDecimal theoreticalProfit = actualSalePrice.subtract(costPrice).multiply(qty);

            Map<String, Object> map = new HashMap<>();
            map.put("sanPham", prodName);
            map.put("phanLoai", ct.getIsGift() ? "Tặng" : "Bán");
            map.put("maHD", orderCode);
            map.put("ngayBan", bh.getThoiGian());
            map.put("giaNhap", costPrice);
            map.put("giaBan", actualSalePrice);
            map.put("soLuong", ct.getSoLuong());
            map.put("loiNhuan", theoreticalProfit);
            map.put("khachHang", partnerName);

            result.add(map);
        }

        // Sắp xếp giảm dần theo thời gian bán
        result.sort((a, b) -> ((LocalDateTime) b.get("ngayBan")).compareTo((LocalDateTime) a.get("ngayBan")));

        return result;
    }

    /**
     * Lấy thống kê tổng quan doanh thu/lợi nhuận bán hàng theo sản phẩm.
     */
    public Map<String, Object> getProductSalesStats(String fromDate, String toDate) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }

        // Lọc khoảng ngày từ DB
        List<ChiTietBanHang> details = chiTietBanHangRepository.findFiltered(from, to, null);

        BigDecimal prodRevenue = BigDecimal.ZERO;
        BigDecimal prodProfit = BigDecimal.ZERO;
        long qtySold = 0;
        long qtyGifted = 0;

        for (ChiTietBanHang ct : details) {
            BanHang bh = ct.getBanHang();
            if (bh == null || !"Hoàn thành".equalsIgnoreCase(bh.getTrangThai()))
                continue;

            SanPham sp = ct.getSanPham();
            BigDecimal costPrice = ct.getGiaVon() != null ? ct.getGiaVon()
                    : (sp != null && sp.getGiaNhapHienTai() != null ? sp.getGiaNhapHienTai() : BigDecimal.ZERO);
            BigDecimal salePrice = ct.getGiaBan() != null ? ct.getGiaBan() : BigDecimal.ZERO;
            int qty = ct.getSoLuong();

            BigDecimal actualSalePrice = ct.getIsGift() ? BigDecimal.ZERO : salePrice;
            BigDecimal itemProfit = actualSalePrice.subtract(costPrice).multiply(BigDecimal.valueOf(qty));
            prodProfit = prodProfit.add(itemProfit);

            if (ct.getIsGift()) {
                qtyGifted += qty;
            } else {
                qtySold += qty;
                BigDecimal itemRevenue = ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO;
                prodRevenue = prodRevenue.add(itemRevenue);
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("prodRevenue", prodRevenue);
        stats.put("prodProfit", prodProfit);
        stats.put("qtySold", qtySold);
        stats.put("qtyGifted", qtyGifted);

        return stats;
    }

    /**
     * Lấy chi tiết danh sách sản phẩm nhập kèm theo bộ lọc.
     */
    public List<Map<String, Object>> getProductImports(String search, String fromDate, String toDate) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }

        List<ChiTietNhapHang> details = chiTietNhapHangRepository.findFiltered(from, to);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChiTietNhapHang ct : details) {
            NhapHang nh = ct.getNhapHang();
            if (nh == null)
                continue;

            SanPham sp = ct.getSanPham();
            String prodName = sp != null ? sp.getTenSanPham() : "Sản phẩm ẩn";
            String partnerName = nh.getDoiTac() != null ? nh.getDoiTac().getTen() : "Không xác định";
            String orderCode = "NH-" + nh.getId();

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                boolean matches = prodName.toLowerCase().contains(searchLower) ||
                        partnerName.toLowerCase().contains(searchLower) ||
                        orderCode.toLowerCase().contains(searchLower);
                if (!matches)
                    continue;
            }

            BigDecimal costPrice = ct.getGiaNhap() != null ? ct.getGiaNhap() : BigDecimal.ZERO;

            Map<String, Object> map = new HashMap<>();
            map.put("sanPham", prodName);
            map.put("maHD", orderCode);
            map.put("ngayNhap", nh.getThoiGian());
            map.put("giaNhap", costPrice);
            map.put("soLuong", ct.getSoLuong());
            map.put("thanhTien", ct.getThanhTien());
            map.put("donVi", ct.getDonVi() != null ? ct.getDonVi() : "ly");
            map.put("nhaCungCap", partnerName);
            map.put("trangThai", nh.getTrangThai());

            result.add(map);
        }

        result.sort((a, b) -> ((LocalDateTime) b.get("ngayNhap")).compareTo((LocalDateTime) a.get("ngayNhap")));
        return result;
    }

    /**
     * Lấy thống kê tổng quan nhập hàng theo sản phẩm.
     */
    public Map<String, Object> getProductImportsStats(String fromDate, String toDate) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        try {
            if (fromDate != null && !fromDate.isEmpty()) {
                from = LocalDate.parse(fromDate).atStartOfDay();
            }
            if (toDate != null && !toDate.isEmpty()) {
                to = LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Sử dụng YYYY-MM-DD.");
        }

        List<ChiTietNhapHang> details = chiTietNhapHangRepository.findFiltered(from, to);

        BigDecimal prodRevenue = BigDecimal.ZERO;
        BigDecimal totalDebt = BigDecimal.ZERO;
        long qtySold = 0;
        java.util.Set<Integer> uniqueOrders = new java.util.HashSet<>();

        for (ChiTietNhapHang ct : details) {
            NhapHang nh = ct.getNhapHang();
            if (nh == null || !"Hoàn thành".equalsIgnoreCase(nh.getTrangThai()))
                continue;

            BigDecimal itemTotal = ct.getThanhTien() != null ? ct.getThanhTien() : BigDecimal.ZERO;
            prodRevenue = prodRevenue.add(itemTotal);
            qtySold += ct.getSoLuong();
            uniqueOrders.add(nh.getId());
        }

        Specification<NhapHang> spec = NhapHangSpecification.filterPurchases(null, null, from, to);
        List<NhapHang> ordersList = nhapHangRepository.findAll(spec);
        for (NhapHang nh : ordersList) {
            if ("Hoàn thành".equalsIgnoreCase(nh.getTrangThai()) && nh.getTienNo() != null) {
                totalDebt = totalDebt.add(nh.getTienNo());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProcurement", prodRevenue);
        stats.put("totalDebt", totalDebt);
        stats.put("qtySold", qtySold);
        stats.put("totalOrders", uniqueOrders.size());
        return stats;
    }
}

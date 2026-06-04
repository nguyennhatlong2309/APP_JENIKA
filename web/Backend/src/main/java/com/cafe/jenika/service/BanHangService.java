package com.cafe.jenika.service;

import com.cafe.jenika.dto.BanHangDTO;
import com.cafe.jenika.model.BanHang;
import com.cafe.jenika.model.ChiTietBanHang;
import com.cafe.jenika.model.SanPham;
import com.cafe.jenika.repository.BanHangRepository;
import com.cafe.jenika.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BanHangService {

    @Autowired
    private BanHangRepository banHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private NhatKyService nhatKyService;

    public List<BanHangDTO> getAllSalesOrders() {
        return banHangRepository.findAllByOrderByThoiGianDesc().stream()
                .map(BanHangDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<BanHangDTO> getSalesOrderById(Integer id) {
        return banHangRepository.findById(id).map(BanHangDTO::fromEntity);
    }

    @Transactional
    public BanHangDTO createSalesOrder(BanHangDTO orderDTO) {
        BanHang order = orderDTO.toEntity();
        if (order.getThoiGian() == null) {
            order.setThoiGian(LocalDateTime.now());
        }
        
        if (order.getTrangThai() == null) {
            order.setTrangThai("Hẹn");
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        
        if (order.getChiTietBanHangs() != null) {
            java.util.Set<String> uniqueKeys = new java.util.HashSet<>();
            for (ChiTietBanHang detail : order.getChiTietBanHangs()) {
                if (detail.getSanPham() == null || detail.getSanPham().getId() == null) {
                    throw new IllegalArgumentException("Đơn bán hàng chứa sản phẩm không hợp lệ!");
                }
                Integer spId = detail.getSanPham().getId();
                boolean isGift = detail.getIsGift() != null && detail.getIsGift();
                String key = spId + "_" + isGift;
                if (!uniqueKeys.add(key)) {
                    SanPham sp = sanPhamRepository.findById(spId).orElse(null);
                    String name = sp != null ? sp.getTenSanPham() : "ID " + spId;
                    String type = isGift ? "tặng" : "bán";
                    throw new IllegalArgumentException("Sản phẩm '" + name + "' (loại " + type + ") bị trùng lặp trong đơn bán hàng. Vui lòng gộp chúng lại.");
                }
                
                detail.setBanHang(order);
                
                SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));
                
                int requestedQty = detail.getSoLuong();
                if (sp.getSoLuongTon() < requestedQty) {
                    throw new IllegalStateException("Sản phẩm '" + sp.getTenSanPham() + "' không đủ hàng tồn kho. Còn lại: " + sp.getSoLuongTon());
                }

                sp.setSoLuongTon(sp.getSoLuongTon() - requestedQty);
                sp.updateTrangThai();
                sanPhamRepository.save(sp);

                if (detail.getDonVi() == null && sp.getDonViTinh() != null) {
                    detail.setDonVi(sp.getDonViTinh().getTenDonVi());
                }
                
                BigDecimal itemPrice = detail.getIsGift() ? BigDecimal.ZERO : sp.getGiaBanHienTai();
                detail.setGiaBan(itemPrice);
                
                BigDecimal detailTotal = itemPrice.multiply(BigDecimal.valueOf(requestedQty));
                detail.setThanhTien(detailTotal);
                
                calculatedTotal = calculatedTotal.add(detailTotal);
            }
        }

        order.setTongTien(calculatedTotal);
        
        BigDecimal paid = order.getTienDaThanhToan() != null ? order.getTienDaThanhToan() : BigDecimal.ZERO;
        order.setTienDaThanhToan(paid);
        order.setTienNo(calculatedTotal.subtract(paid));

        BanHang saved;
        if (order.getId() != null) {
            if (banHangRepository.existsById(order.getId())) {
                throw new IllegalArgumentException("Mã HĐ " + order.getId() + " đã tồn tại!");
            }
            banHangRepository.insertWithCustomId(
                order.getId(),
                order.getThoiGian(),
                order.getDoiTac() != null ? order.getDoiTac().getId() : null,
                order.getNhanVien() != null ? order.getNhanVien().getId() : null,
                order.getTongTien(),
                order.getTienDaThanhToan(),
                order.getTienNo(),
                order.getDiaChiGiaoHang(),
                order.getNgayLap(),
                order.getTrangThai(),
                order.getGhiChu()
            );
            // Fetch the parent to manage it in the Persistence Context
            saved = banHangRepository.findById(order.getId()).orElse(order);
            if (order.getChiTietBanHangs() != null) {
                if (saved.getChiTietBanHangs() == null) {
                    saved.setChiTietBanHangs(new java.util.ArrayList<>());
                } else {
                    saved.getChiTietBanHangs().clear();
                }
                for (ChiTietBanHang detail : order.getChiTietBanHangs()) {
                    detail.setBanHang(saved);
                    saved.getChiTietBanHangs().add(detail);
                }
                // Cascading save details
                saved = banHangRepository.save(saved);
            }
        } else {
            saved = banHangRepository.save(order);
        }
        
        nhatKyService.log("THEM", "ban_hang", "BH-" + saved.getId(), 
                "Tạo đơn bán hàng mới. Khách hàng: " + (saved.getDoiTac() != null ? saved.getDoiTac().getTen() : "Khách lẻ") + 
                ". Tổng tiền: " + saved.getTongTien() + "đ");
        
        return BanHangDTO.fromEntity(saved);
    }

    @Transactional
    public BanHangDTO updateOrderStatus(Integer id, String status) {
        BanHang order = banHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán hàng ID: " + id));
        
        String oldStatus = order.getTrangThai();
        order.setTrangThai(status);
        
        if ("Hủy".equalsIgnoreCase(status) && !"Hủy".equalsIgnoreCase(oldStatus)) {
            if (order.getChiTietBanHangs() != null) {
                for (ChiTietBanHang detail : order.getChiTietBanHangs()) {
                    SanPham sp = detail.getSanPham();
                    sp.setSoLuongTon(sp.getSoLuongTon() + detail.getSoLuong());
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        } 
        else if (!"Hủy".equalsIgnoreCase(status) && "Hủy".equalsIgnoreCase(oldStatus)) {
            if (order.getChiTietBanHangs() != null) {
                for (ChiTietBanHang detail : order.getChiTietBanHangs()) {
                    SanPham sp = detail.getSanPham();
                    if (sp.getSoLuongTon() < detail.getSoLuong()) {
                        throw new IllegalStateException("Sản phẩm '" + sp.getTenSanPham() + "' không đủ hàng để khôi phục đơn.");
                    }
                    sp.setSoLuongTon(sp.getSoLuongTon() - detail.getSoLuong());
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        }

        BanHang saved = banHangRepository.save(order);
        nhatKyService.log("SUA", "ban_hang", "BH-" + order.getId(), 
                "Cập nhật trạng thái đơn bán từ [" + oldStatus + "] sang [" + status + "]");
        return BanHangDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getSalesStats(java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate) {
        List<BanHang> all = banHangRepository.findAll();
        
        java.math.BigDecimal completedRevenue = java.math.BigDecimal.ZERO;
        java.math.BigDecimal scheduledDeposit = java.math.BigDecimal.ZERO;
        long scheduledCount = 0;
        long unpaidCompletedCount = 0;
        
        for (BanHang b : all) {
            java.time.LocalDateTime time = b.getThoiGian();
            if (fromDate != null && time.isBefore(fromDate)) continue;
            if (toDate != null && time.isAfter(toDate)) continue;
            
            if ("Hoàn thành".equalsIgnoreCase(b.getTrangThai())) {
                completedRevenue = completedRevenue.add(b.getTongTien() != null ? b.getTongTien() : java.math.BigDecimal.ZERO);
                if (b.getTienNo() != null && b.getTienNo().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    unpaidCompletedCount++;
                }
            } else if ("Hẹn".equalsIgnoreCase(b.getTrangThai())) {
                scheduledDeposit = scheduledDeposit.add(b.getTienDaThanhToan() != null ? b.getTienDaThanhToan() : java.math.BigDecimal.ZERO);
                scheduledCount++;
            }
        }
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("completedRevenue", completedRevenue);
        stats.put("scheduledDeposit", scheduledDeposit);
        stats.put("scheduledCount", scheduledCount);
        stats.put("unpaidCompletedCount", unpaidCompletedCount);
        return stats;
    }

    @Transactional
    public BanHangDTO updateSalesOrder(Integer id, BanHangDTO updatedOrderDTO) {
        BanHang updatedOrder = updatedOrderDTO.toEntity();
        if (updatedOrder.getChiTietBanHangs() != null) {
            java.util.Set<String> uniqueKeys = new java.util.HashSet<>();
            for (ChiTietBanHang detail : updatedOrder.getChiTietBanHangs()) {
                if (detail.getSanPham() == null || detail.getSanPham().getId() == null) {
                    throw new IllegalArgumentException("Đơn bán hàng chứa sản phẩm không hợp lệ!");
                }
                Integer spId = detail.getSanPham().getId();
                boolean isGift = detail.getIsGift() != null && detail.getIsGift();
                String key = spId + "_" + isGift;
                if (!uniqueKeys.add(key)) {
                    SanPham sp = sanPhamRepository.findById(spId).orElse(null);
                    String name = sp != null ? sp.getTenSanPham() : "ID " + spId;
                    String type = isGift ? "tặng" : "bán";
                    throw new IllegalArgumentException("Sản phẩm '" + name + "' (loại " + type + ") bị trùng lặp trong đơn bán hàng. Vui lòng gộp số lượng của chúng lại thành một dòng.");
                }
            }
        }

        BanHang existingOrder = banHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán hàng ID: " + id));

        // Revert old stock if the old status was NOT "Hủy" (which means stock was decremented)
        boolean oldWasActive = !"Hủy".equalsIgnoreCase(existingOrder.getTrangThai());
        if (oldWasActive) {
            if (existingOrder.getChiTietBanHangs() != null) {
                for (ChiTietBanHang detail : existingOrder.getChiTietBanHangs()) {
                    SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));
                    sp.setSoLuongTon(sp.getSoLuongTon() + detail.getSoLuong());
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        }

        // Clear existing details
        existingOrder.getChiTietBanHangs().clear();
        banHangRepository.saveAndFlush(existingOrder);

        // Update fields
        existingOrder.setDoiTac(updatedOrder.getDoiTac());
        existingOrder.setNhanVien(updatedOrder.getNhanVien());
        existingOrder.setTrangThai(updatedOrder.getTrangThai());
        existingOrder.setTienDaThanhToan(updatedOrder.getTienDaThanhToan());
        existingOrder.setGhiChu(updatedOrder.getGhiChu());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        boolean isNewActive = !"Hủy".equalsIgnoreCase(updatedOrder.getTrangThai());

        if (updatedOrder.getChiTietBanHangs() != null) {
            for (ChiTietBanHang detail : updatedOrder.getChiTietBanHangs()) {
                detail.setBanHang(existingOrder);

                SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));

                int requestedQty = detail.getSoLuong();
                if (isNewActive) {
                    if (sp.getSoLuongTon() < requestedQty) {
                        throw new IllegalStateException("Sản phẩm '" + sp.getTenSanPham() + "' không đủ hàng tồn kho. Còn lại: " + sp.getSoLuongTon());
                    }
                    sp.setSoLuongTon(sp.getSoLuongTon() - requestedQty);
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }

                if (detail.getDonVi() == null && sp.getDonViTinh() != null) {
                    detail.setDonVi(sp.getDonViTinh().getTenDonVi());
                }

                BigDecimal itemPrice = detail.getIsGift() ? BigDecimal.ZERO : sp.getGiaBanHienTai();
                detail.setGiaBan(itemPrice);

                BigDecimal detailTotal = itemPrice.multiply(BigDecimal.valueOf(requestedQty));
                detail.setThanhTien(detailTotal);

                calculatedTotal = calculatedTotal.add(detailTotal);
                existingOrder.getChiTietBanHangs().add(detail);
            }
        }

        existingOrder.setTongTien(calculatedTotal);
        BigDecimal paid = updatedOrder.getTienDaThanhToan() != null ? updatedOrder.getTienDaThanhToan() : BigDecimal.ZERO;
        existingOrder.setTienDaThanhToan(paid);
        existingOrder.setTienNo(calculatedTotal.subtract(paid));

        BanHang saved = banHangRepository.save(existingOrder);

        nhatKyService.log("SUA", "ban_hang", "BH-" + saved.getId(),
                "Chỉnh sửa đơn bán hàng. Khách hàng: " + (saved.getDoiTac() != null ? saved.getDoiTac().getTen() : "Khách lẻ") +
                ". Tổng tiền: " + saved.getTongTien() + "đ");

        return BanHangDTO.fromEntity(saved);
    }
}

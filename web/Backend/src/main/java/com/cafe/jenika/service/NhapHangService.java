package com.cafe.jenika.service;

import com.cafe.jenika.dto.NhapHangDTO;
import com.cafe.jenika.model.NhapHang;
import com.cafe.jenika.model.ChiTietNhapHang;
import com.cafe.jenika.model.ChiTietNhapHangId;
import com.cafe.jenika.model.SanPham;
import com.cafe.jenika.repository.NhapHangRepository;
import com.cafe.jenika.repository.SanPhamRepository;
import com.cafe.jenika.repository.ThuChiRepository;
import com.cafe.jenika.repository.LoaiThuChiRepository;
import com.cafe.jenika.model.ThuChi;
import com.cafe.jenika.model.LoaiThuChi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NhapHangService {

    @Autowired
    private NhapHangRepository nhapHangRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private ThuChiRepository thuChiRepository;

    @Autowired
    private LoaiThuChiRepository loaiThuChiRepository;

    @Autowired
    private NhatKyService nhatKyService;

    public List<NhapHangDTO> getAllImportOrders() {
        return nhapHangRepository.findAllByOrderByThoiGianDesc().stream()
                .map(NhapHangDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<NhapHangDTO> getImportOrderById(Integer id) {
        return nhapHangRepository.findById(id).map(NhapHangDTO::fromEntity);
    }

    @Transactional
    public NhapHangDTO createImportOrder(NhapHangDTO orderDTO) {
        NhapHang order = orderDTO.toEntity();
        if (order.getThoiGian() == null) {
            order.setThoiGian(LocalDateTime.now());
        }
        
        if (order.getTrangThai() == null) {
            order.setTrangThai("Chờ nhận");
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        boolean isReceived = "Hoàn thành".equalsIgnoreCase(order.getTrangThai()) || "Đã nhận".equalsIgnoreCase(order.getTrangThai());

        if (order.getChiTietNhapHangs() != null) {
            java.util.Set<Integer> productIds = new java.util.HashSet<>();
            for (ChiTietNhapHang detail : order.getChiTietNhapHangs()) {
                if (detail.getSanPham() == null || detail.getSanPham().getId() == null) {
                    throw new IllegalArgumentException("Đơn nhập hàng chứa sản phẩm không hợp lệ!");
                }
                Integer spId = detail.getSanPham().getId();
                if (!productIds.add(spId)) {
                    SanPham sp = sanPhamRepository.findById(spId).orElse(null);
                    String name = sp != null ? sp.getTenSanPham() : "ID " + spId;
                    throw new IllegalArgumentException("Sản phẩm '" + name + "' bị trùng lặp trong đơn nhập hàng. Vui lòng gộp số lượng của chúng lại thành một dòng.");
                }

                detail.setNhapHang(order);
                
                SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));
                
                // --- Price change check ---
                if (sp.getGiaNhapHienTai() != null && detail.getGiaNhap() != null 
                        && detail.getGiaNhap().compareTo(BigDecimal.ZERO) > 0
                        && detail.getGiaNhap().compareTo(sp.getGiaNhapHienTai()) != 0) {
                    
                    String baseName = getBaseProductName(sp.getTenSanPham());
                    
                    Optional<SanPham> existingSpOpt = sanPhamRepository.findFirstByTenSanPhamIgnoreCaseAndGiaNhapHienTaiAndBiXoaFalse(
                            baseName, detail.getGiaNhap());
                    
                    if (existingSpOpt.isPresent()) {
                        sp = existingSpOpt.get();
                    } else {
                        SanPham newSp = SanPham.builder()
                                .tenSanPham(baseName)
                                .giaNhapHienTai(detail.getGiaNhap())
                                .giaBanHienTai(sp.getGiaBanHienTai())
                                .soLuongTon(0)
                                .canhBaoTonKho(sp.getCanhBaoTonKho())
                                .trangThai("Hết hàng")
                                .danhMuc(sp.getDanhMuc())
                                .donViTinh(sp.getDonViTinh())
                                .nhomSanPham(sp.getNhomSanPham())
                                .biXoa(false)
                                .ghiChu(sp.getGhiChu())
                                .build();
                        sp = sanPhamRepository.save(newSp);
                    }
                    detail.setSanPham(sp);
                }

                detail.setId(ChiTietNhapHangId.builder()
                        .idSanPham(sp.getId())
                        .build());

                if (isReceived) {
                    sp.setSoLuongTon(sp.getSoLuongTon() + detail.getSoLuong());
                    if (detail.getGiaNhap() != null && detail.getGiaNhap().compareTo(BigDecimal.ZERO) > 0) {
                        sp.setGiaNhapHienTai(detail.getGiaNhap());
                    }
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }

                if (detail.getDonVi() == null && sp.getDonViTinh() != null) {
                    detail.setDonVi(sp.getDonViTinh().getTenDonVi());
                }

                BigDecimal importPrice = detail.getGiaNhap() != null ? detail.getGiaNhap() : sp.getGiaNhapHienTai();
                detail.setGiaNhap(importPrice);

                BigDecimal itemTotal = importPrice.multiply(BigDecimal.valueOf(detail.getSoLuong()));
                detail.setThanhTien(itemTotal);

                calculatedTotal = calculatedTotal.add(itemTotal);
            }
        }

        order.setTongTien(calculatedTotal);
        
        BigDecimal paid = order.getDaThanhToan() != null ? order.getDaThanhToan() : BigDecimal.ZERO;
        order.setDaThanhToan(paid);
        order.setTienNo(calculatedTotal.subtract(paid));

        NhapHang saved;
        if (order.getId() != null) {
            if (nhapHangRepository.existsById(order.getId())) {
                throw new IllegalArgumentException("Mã đơn nhập " + order.getId() + " đã tồn tại!");
            }
            nhapHangRepository.insertWithCustomId(
                order.getId(),
                order.getThoiGian(),
                order.getTongTien(),
                order.getDaThanhToan(),
                order.getTienNo(),
                order.getTrangThai(),
                order.getNgayNhan(),
                order.getDoiTac() != null ? order.getDoiTac().getId() : null,
                order.getNhanVien() != null ? order.getNhanVien().getId() : null,
                order.getGhiChu()
            );
            saved = nhapHangRepository.findById(order.getId()).orElse(order);
            if (order.getChiTietNhapHangs() != null) {
                if (saved.getChiTietNhapHangs() == null) {
                    saved.setChiTietNhapHangs(new java.util.ArrayList<>());
                } else {
                    saved.getChiTietNhapHangs().clear();
                }
                for (ChiTietNhapHang detail : order.getChiTietNhapHangs()) {
                    detail.setNhapHang(saved);
                    detail.getId().setIdNhapHang(saved.getId());
                    saved.getChiTietNhapHangs().add(detail);
                }
                saved = nhapHangRepository.save(saved);
            }
        } else {
            saved = nhapHangRepository.save(order);
            if (saved.getChiTietNhapHangs() != null) {
                for (ChiTietNhapHang detail : saved.getChiTietNhapHangs()) {
                    detail.getId().setIdNhapHang(saved.getId());
                }
                saved = nhapHangRepository.save(saved);
            }
        }

        nhatKyService.log("THEM", "nhap_hang", "NH-" + saved.getId(), 
                "Tạo đơn nhập hàng mới. Nhà cung cấp: " + (saved.getDoiTac() != null ? saved.getDoiTac().getTen() : "N/A") + 
                ". Tổng tiền: " + saved.getTongTien() + "đ");
        
        syncToThuChi(saved);

        return NhapHangDTO.fromEntity(saved);
    }

    @Transactional
    public NhapHangDTO updateImportOrderStatus(Integer id, String status) {
        NhapHang order = nhapHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập hàng ID: " + id));
        
        String oldStatus = order.getTrangThai();
        order.setTrangThai(status);
        
        boolean wasReceived = "Hoàn thành".equalsIgnoreCase(oldStatus) || "Đã nhận".equalsIgnoreCase(oldStatus);
        boolean isReceivedNow = "Hoàn thành".equalsIgnoreCase(status) || "Đã nhận".equalsIgnoreCase(status);

        if (isReceivedNow && !wasReceived) {
            if (order.getChiTietNhapHangs() != null) {
                for (ChiTietNhapHang detail : order.getChiTietNhapHangs()) {
                    SanPham sp = detail.getSanPham();
                    sp.setSoLuongTon(sp.getSoLuongTon() + detail.getSoLuong());
                    if (detail.getGiaNhap() != null && detail.getGiaNhap().compareTo(BigDecimal.ZERO) > 0) {
                        sp.setGiaNhapHienTai(detail.getGiaNhap());
                    }
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        }
        else if (!isReceivedNow && wasReceived) {
            if (order.getChiTietNhapHangs() != null) {
                for (ChiTietNhapHang detail : order.getChiTietNhapHangs()) {
                    SanPham sp = detail.getSanPham();
                    if (sp.getSoLuongTon() < detail.getSoLuong()) {
                        throw new IllegalStateException("Không thể hoàn tác đơn nhập vì sản phẩm '" + sp.getTenSanPham() + "' đã bán hoặc hao hụt.");
                    }
                    sp.setSoLuongTon(sp.getSoLuongTon() - detail.getSoLuong());
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        }

        NhapHang saved = nhapHangRepository.save(order);
        nhatKyService.log("SUA", "nhap_hang", "NH-" + order.getId(), 
                "Cập nhật trạng thái đơn nhập từ [" + oldStatus + "] sang [" + status + "]");
        
        syncToThuChi(saved);

        return NhapHangDTO.fromEntity(saved);
    }

    @Transactional
    public NhapHangDTO updateImportOrder(Integer id, NhapHangDTO updatedOrderDTO) {
        NhapHang updatedOrder = updatedOrderDTO.toEntity();
        if (updatedOrder.getChiTietNhapHangs() != null) {
            java.util.Set<Integer> productIds = new java.util.HashSet<>();
            for (ChiTietNhapHang detail : updatedOrder.getChiTietNhapHangs()) {
                if (detail.getSanPham() == null || detail.getSanPham().getId() == null) {
                    throw new IllegalArgumentException("Đơn nhập hàng chứa sản phẩm không hợp lệ!");
                }
                Integer spId = detail.getSanPham().getId();
                if (!productIds.add(spId)) {
                    SanPham sp = sanPhamRepository.findById(spId).orElse(null);
                    String name = sp != null ? sp.getTenSanPham() : "ID " + spId;
                    throw new IllegalArgumentException("Sản phẩm '" + name + "' bị trùng lặp trong danh sách chỉnh sửa. Vui lòng gộp số lượng của chúng lại thành một dòng.");
                }
            }
        }

        NhapHang existingOrder = nhapHangRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nhập hàng ID: " + id));

        // Revert old stock if the old status was "Hoàn thành" or "Đã nhận"
        boolean oldWasReceived = "Hoàn thành".equalsIgnoreCase(existingOrder.getTrangThai()) || "Đã nhận".equalsIgnoreCase(existingOrder.getTrangThai());
        if (oldWasReceived) {
            if (existingOrder.getChiTietNhapHangs() != null) {
                for (ChiTietNhapHang detail : existingOrder.getChiTietNhapHangs()) {
                    SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));
                    if (sp.getSoLuongTon() < detail.getSoLuong()) {
                        throw new IllegalStateException("Không thể chỉnh sửa đơn nhập vì sản phẩm '" + sp.getTenSanPham() + "' đã được bán hoặc sử dụng, khiến tồn kho không đủ để hoàn tác.");
                    }
                    sp.setSoLuongTon(sp.getSoLuongTon() - detail.getSoLuong());
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }
            }
        }

        // Clear existing details
        existingOrder.getChiTietNhapHangs().clear();
        nhapHangRepository.saveAndFlush(existingOrder);

        // Update fields
        existingOrder.setDoiTac(updatedOrder.getDoiTac());
        existingOrder.setNhanVien(updatedOrder.getNhanVien());
        existingOrder.setTrangThai(updatedOrder.getTrangThai());
        existingOrder.setDaThanhToan(updatedOrder.getDaThanhToan());
        existingOrder.setGhiChu(updatedOrder.getGhiChu());

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        boolean isReceivedNow = "Hoàn thành".equalsIgnoreCase(updatedOrder.getTrangThai()) || "Đã nhận".equalsIgnoreCase(updatedOrder.getTrangThai());

        if (updatedOrder.getChiTietNhapHangs() != null) {
            for (ChiTietNhapHang detail : updatedOrder.getChiTietNhapHangs()) {
                detail.setNhapHang(existingOrder);

                SanPham sp = sanPhamRepository.findById(detail.getSanPham().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + detail.getSanPham().getId()));

                // --- Price change check ---
                if (sp.getGiaNhapHienTai() != null && detail.getGiaNhap() != null 
                        && detail.getGiaNhap().compareTo(BigDecimal.ZERO) > 0
                        && detail.getGiaNhap().compareTo(sp.getGiaNhapHienTai()) != 0) {
                    
                    String baseName = getBaseProductName(sp.getTenSanPham());
                    
                    Optional<SanPham> existingSpOpt = sanPhamRepository.findFirstByTenSanPhamIgnoreCaseAndGiaNhapHienTaiAndBiXoaFalse(
                            baseName, detail.getGiaNhap());
                    
                    if (existingSpOpt.isPresent()) {
                        sp = existingSpOpt.get();
                    } else {
                        SanPham newSp = SanPham.builder()
                                .tenSanPham(baseName)
                                .giaNhapHienTai(detail.getGiaNhap())
                                .giaBanHienTai(sp.getGiaBanHienTai())
                                .soLuongTon(0)
                                .canhBaoTonKho(sp.getCanhBaoTonKho())
                                .trangThai("Hết hàng")
                                .danhMuc(sp.getDanhMuc())
                                .donViTinh(sp.getDonViTinh())
                                .nhomSanPham(sp.getNhomSanPham())
                                .biXoa(false)
                                .ghiChu(sp.getGhiChu())
                                .build();
                        sp = sanPhamRepository.save(newSp);
                    }
                    detail.setSanPham(sp);
                }

                detail.setId(ChiTietNhapHangId.builder()
                        .idNhapHang(existingOrder.getId())
                        .idSanPham(sp.getId())
                        .build());

                if (isReceivedNow) {
                    sp.setSoLuongTon(sp.getSoLuongTon() + detail.getSoLuong());
                    if (detail.getGiaNhap() != null && detail.getGiaNhap().compareTo(BigDecimal.ZERO) > 0) {
                        sp.setGiaNhapHienTai(detail.getGiaNhap());
                    }
                    sp.updateTrangThai();
                    sanPhamRepository.save(sp);
                }

                if (detail.getDonVi() == null && sp.getDonViTinh() != null) {
                    detail.setDonVi(sp.getDonViTinh().getTenDonVi());
                }

                BigDecimal importPrice = detail.getGiaNhap() != null ? detail.getGiaNhap() : sp.getGiaNhapHienTai();
                detail.setGiaNhap(importPrice);

                BigDecimal itemTotal = importPrice.multiply(BigDecimal.valueOf(detail.getSoLuong()));
                detail.setThanhTien(itemTotal);

                calculatedTotal = calculatedTotal.add(itemTotal);
                existingOrder.getChiTietNhapHangs().add(detail);
            }
        }

        existingOrder.setTongTien(calculatedTotal);
        BigDecimal paid = updatedOrder.getDaThanhToan() != null ? updatedOrder.getDaThanhToan() : BigDecimal.ZERO;
        existingOrder.setDaThanhToan(paid);
        existingOrder.setTienNo(calculatedTotal.subtract(paid));

        NhapHang saved = nhapHangRepository.save(existingOrder);

        nhatKyService.log("SUA", "nhap_hang", "NH-" + saved.getId(),
                "Chỉnh sửa đơn nhập hàng. Nhà cung cấp: " + (saved.getDoiTac() != null ? saved.getDoiTac().getTen() : "N/A") +
                ". Tổng tiền: " + saved.getTongTien() + "đ");

        syncToThuChi(saved);

        return NhapHangDTO.fromEntity(saved);
    }

    private String getBaseProductName(String name) {
        if (name == null) return "";
        int index = name.indexOf(" (Giá nhập: ");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }

    private String formatPrice(java.math.BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,d", price.longValue()).replace(',', '.');
    }

    private void syncToThuChi(NhapHang order) {
        if (order == null || order.getId() == null) return;

        Optional<ThuChi> existingOpt = thuChiRepository.findByNhapHang(order);

        if ("Hủy".equalsIgnoreCase(order.getTrangThai())) {
            existingOpt.ifPresent(thuChi -> {
                thuChiRepository.delete(thuChi);
                nhatKyService.log("XOA", "thu_chi", "TC-" + thuChi.getId(),
                        "Tự động xóa khoản chi do đơn nhập hàng NH-" + order.getId() + " bị hủy.");
            });
            return;
        }

        BigDecimal paid = order.getDaThanhToan() != null ? order.getDaThanhToan() : BigDecimal.ZERO;

        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            existingOpt.ifPresent(thuChi -> {
                thuChiRepository.delete(thuChi);
                nhatKyService.log("XOA", "thu_chi", "TC-" + thuChi.getId(),
                        "Tự động xóa khoản chi do đơn nhập hàng NH-" + order.getId() + " thay đổi số tiền đã thanh toán về 0.");
            });
            return;
        }

        ThuChi thuChi = existingOpt.orElseGet(() -> ThuChi.builder()
                .nhapHang(order)
                .build());

        thuChi.setThoiGian(order.getThoiGian() != null ? order.getThoiGian() : LocalDateTime.now());
        thuChi.setTienThu(BigDecimal.ZERO);
        thuChi.setTienChi(paid);
        thuChi.setPhuongThuc("Chuyển khoản ngân hàng");
        thuChi.setTrangThai("Đã chi");
        thuChi.setNhanVien(order.getNhanVien());
        
        LoaiThuChi loai = loaiThuChiRepository.findById(1).orElse(null);
        thuChi.setLoaiThuChi(loai);

        String partnerName = order.getDoiTac() != null ? order.getDoiTac().getTen() : "Không xác định";
        thuChi.setMoTa("Thanh toán đơn nhập hàng NH-" + order.getId() + ". Nhà cung cấp: " + partnerName);

        ThuChi saved = thuChiRepository.save(thuChi);

        String logAction = existingOpt.isPresent() ? "SUA" : "THEM";
        nhatKyService.log(logAction, "thu_chi", "TC-" + saved.getId(),
                "Tự động đồng bộ khoản chi từ đơn nhập hàng NH-" + order.getId() + ". Số tiền: " + paid + "đ");
    }
}

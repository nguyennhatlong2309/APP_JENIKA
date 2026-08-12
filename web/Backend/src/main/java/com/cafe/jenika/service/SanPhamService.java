package com.cafe.jenika.service;

import com.cafe.jenika.dto.SanPhamDTO;
import com.cafe.jenika.model.SanPham;
import com.cafe.jenika.repository.SanPhamRepository;
import com.cafe.jenika.repository.SanPhamSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private NhatKyService nhatKyService;

    public List<SanPhamDTO> getAllActiveProducts() {
        return sanPhamRepository.findByBiXoaFalse().stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SanPhamDTO> getProductsByCategory(Integer categoryId) {
        return sanPhamRepository.findByDanhMucIdAndBiXoaFalse(categoryId).stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SanPhamDTO> getProductsByGroup(Integer groupId) {
        return sanPhamRepository.findByNhomSanPhamIdAndBiXoaFalse(groupId).stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SanPhamDTO> getLowStockWarnings() {
        return sanPhamRepository.findLowStockWarnings().stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SanPhamDTO> searchProducts(String query) {
        return sanPhamRepository.findByTenSanPhamContainingIgnoreCaseAndBiXoaFalse(query).stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<SanPhamDTO> getProductById(Integer id) {
        return sanPhamRepository.findById(id)
                .filter(p -> !p.getBiXoa())
                .map(SanPhamDTO::fromEntity);
    }

    @Transactional
    public SanPhamDTO saveProduct(SanPhamDTO sanPhamDTO) {
        SanPham sanPham = sanPhamDTO.toEntity();
        sanPham.setBiXoa(false);
        
        // Nếu là thêm mới, kiểm tra xem đã có sản phẩm trùng Tên (không phân biệt hoa thường) và Giá nhập chưa
        if (sanPham.getId() == null) {
            java.util.Optional<SanPham> existingOpt = sanPhamRepository.findFirstByTenSanPhamIgnoreCaseAndGiaNhapHienTaiAndBiXoaFalse(
                    sanPham.getTenSanPham(), sanPham.getGiaNhapHienTai());
            if (existingOpt.isPresent()) {
                SanPham existing = existingOpt.get();
                // Cộng dồn vào tồn kho hiện tại
                int addedStock = sanPham.getSoLuongTon() != null ? sanPham.getSoLuongTon() : 0;
                int currentStock = existing.getSoLuongTon() != null ? existing.getSoLuongTon() : 0;
                existing.setSoLuongTon(currentStock + addedStock);
                
                // Cập nhật giá bán mới nếu có và lớn hơn 0
                if (sanPham.getGiaBanHienTai() != null && sanPham.getGiaBanHienTai().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    existing.setGiaBanHienTai(sanPham.getGiaBanHienTai());
                }
                
                // Cập nhật ghi chú nếu có ghi chú mới
                if (sanPham.getGhiChu() != null && !sanPham.getGhiChu().trim().isEmpty()) {
                    existing.setGhiChu(sanPham.getGhiChu());
                }
                
                existing.updateTrangThai();
                SanPham saved = sanPhamRepository.save(existing);
                nhatKyService.log("SUA", "san_pham", "SP-" + saved.getId(), 
                        "Cộng dồn tồn kho sản phẩm trùng tên và giá nhập: " + saved.getTenSanPham() + " (Tăng thêm " + addedStock + " tồn kho)");
                return SanPhamDTO.fromEntity(saved);
            }
        }
        
        sanPham.updateTrangThai();
        boolean isNew = sanPham.getId() == null;
        SanPham saved = sanPhamRepository.save(sanPham);
        
        String action = isNew ? "THEM" : "SUA";
        nhatKyService.log(action, "san_pham", "SP-" + saved.getId(), 
                (isNew ? "Thêm mới sản phẩm: " : "Cập nhật sản phẩm: ") + saved.getTenSanPham());
        
        return SanPhamDTO.fromEntity(saved);
    }

    @Transactional
    public void softDeleteProduct(Integer id) {
        sanPhamRepository.findById(id).ifPresent(p -> {
            p.setBiXoa(true);
            sanPhamRepository.save(p);
            nhatKyService.log("XOA", "san_pham", "SP-" + p.getId(), 
                    "Xóa mềm sản phẩm: " + p.getTenSanPham());
        });
    }

    public List<SanPhamDTO> getAllProducts() {
        return sanPhamRepository.findAll().stream()
                .map(SanPhamDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void restoreProduct(Integer id) {
        sanPhamRepository.findById(id).ifPresent(p -> {
            p.setBiXoa(false);
            p.updateTrangThai();
            sanPhamRepository.save(p);
            nhatKyService.log("RESTORE", "san_pham", "SP-" + p.getId(), 
                    "Khôi phục sản phẩm: " + p.getTenSanPham());
        });
    }

    public Page<SanPhamDTO> getProductsPaginated(
            Boolean biXoa,
            String search,
            List<Integer> categoryIds,
            List<Integer> groupIds,
            List<String> statuses,
            Pageable pageable) {
        Specification<SanPham> spec = SanPhamSpecification.filterProducts(biXoa, search, categoryIds, groupIds, statuses);
        return sanPhamRepository.findAll(spec, pageable).map(SanPhamDTO::fromEntity);
    }

    public Map<String, Object> getProductStats() {
        Long totalItems = sanPhamRepository.sumTotalItems();
        BigDecimal totalValue = sanPhamRepository.sumTotalValue();
        Long lowStockCount = sanPhamRepository.countLowStock();
        Long activeCount = sanPhamRepository.countActive();
        Long deletedCount = sanPhamRepository.countDeleted();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", totalItems != null ? totalItems : 0L);
        stats.put("totalValue", totalValue != null ? totalValue : BigDecimal.ZERO);
        stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0L);
        stats.put("activeCount", activeCount != null ? activeCount : 0L);
        stats.put("deletedCount", deletedCount != null ? deletedCount : 0L);
        return stats;
    }
}

package com.cafe.jenika.repository;

import com.cafe.jenika.model.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    
    List<SanPham> findByBiXoaFalse();
    
    List<SanPham> findByDanhMucIdAndBiXoaFalse(Integer categoryId);

    List<SanPham> findByNhomSanPhamIdAndBiXoaFalse(Integer groupId);

    @Query("SELECT s FROM SanPham s WHERE s.biXoa = false AND s.soLuongTon <= s.canhBaoTonKho")
    List<SanPham> findLowStockWarnings();

    List<SanPham> findByTenSanPhamContainingIgnoreCaseAndBiXoaFalse(String name);

    @Query("SELECT s FROM SanPham s WHERE LOWER(TRIM(s.tenSanPham)) = LOWER(TRIM(:ten)) AND s.giaNhapHienTai = :giaNhap AND s.biXoa = false")
    java.util.Optional<SanPham> findFirstByTenSanPhamIgnoreCaseAndGiaNhapHienTaiAndBiXoaFalse(
            @org.springframework.data.repository.query.Param("ten") String ten,
            @org.springframework.data.repository.query.Param("giaNhap") java.math.BigDecimal giaNhap);
}

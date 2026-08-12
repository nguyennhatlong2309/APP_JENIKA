package com.cafe.jenika.repository;

import com.cafe.jenika.model.NhapHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NhapHangRepository extends JpaRepository<NhapHang, Integer>, JpaSpecificationExecutor<NhapHang> {
    @Query("SELECT DISTINCT n FROM NhapHang n " +
           "LEFT JOIN FETCH n.doiTac " +
           "LEFT JOIN FETCH n.nhanVien " +
           "LEFT JOIN FETCH n.chiTietNhapHangs ct " +
           "LEFT JOIN FETCH ct.sanPham sp " +
           "LEFT JOIN FETCH sp.danhMuc " +
           "LEFT JOIN FETCH sp.donViTinh " +
           "LEFT JOIN FETCH sp.nhomSanPham " +
           "ORDER BY n.thoiGian DESC")
    List<NhapHang> findAllByOrderByThoiGianDesc();

    @Modifying
    @Query(value = "INSERT INTO nhap_hang (id, thoi_gian, tong_tien, da_thanh_toan, tien_no, trang_thai, ngay_nhan, id_doi_tac, id_nhan_vien, ghi_chu) " +
                   "VALUES (:id, :thoiGian, :tongTien, :daThanhToan, :tienNo, :trangThai, :ngayNhan, :idDoiTac, :idNhanVien, :ghiChu)", nativeQuery = true)
    void insertWithCustomId(
        @Param("id") Integer id,
        @Param("thoiGian") java.time.LocalDateTime thoiGian,
        @Param("tongTien") java.math.BigDecimal tongTien,
        @Param("daThanhToan") java.math.BigDecimal daThanhToan,
        @Param("tienNo") java.math.BigDecimal tienNo,
        @Param("trangThai") String trangThai,
        @Param("ngayNhan") java.time.LocalDate ngayNhan,
        @Param("idDoiTac") Integer idDoiTac,
        @Param("idNhanVien") Integer idNhanVien,
        @Param("ghiChu") String ghiChu
    );
}

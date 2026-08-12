package com.cafe.jenika.repository;

import com.cafe.jenika.model.BanHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BanHangRepository extends JpaRepository<BanHang, Integer>, JpaSpecificationExecutor<BanHang> {
    List<BanHang> findAllByOrderByThoiGianDesc();

    @Modifying
    @Query(value = "INSERT INTO ban_hang (id, thoi_gian, id_doi_tac, id_nhan_vien, tong_tien, tien_da_thanh_toan, tien_no, dia_chi_giao_hang, ngay_lap, trang_thai, ghi_chu, tong_cost, loi_nhuan, tien_qua_tang) " +
                   "VALUES (:id, :thoiGian, :idDoiTac, :idNhanVien, :tongTien, :tienDaThanhToan, :tienNo, :diaChiGiaoHang, :ngayLap, :trangThai, :ghiChu, :tongCost, :loiNhuan, :tienQuaTang)", nativeQuery = true)
    void insertWithCustomId(
        @Param("id") Integer id,
        @Param("thoiGian") java.time.LocalDateTime thoiGian,
        @Param("idDoiTac") Integer idDoiTac,
        @Param("idNhanVien") Integer idDoiTacNhanVien,
        @Param("tongTien") java.math.BigDecimal tongTien,
        @Param("tienDaThanhToan") java.math.BigDecimal tienDaThanhToan,
        @Param("tienNo") java.math.BigDecimal tienNo,
        @Param("diaChiGiaoHang") String diaChiGiaoHang,
        @Param("ngayLap") java.time.LocalDate ngayLap,
        @Param("trangThai") String trangThai,
        @Param("ghiChu") String ghiChu,
        @Param("tongCost") java.math.BigDecimal tongCost,
        @Param("loiNhuan") java.math.BigDecimal loiNhuan,
        @Param("tienQuaTang") java.math.BigDecimal tienQuaTang
    );
}

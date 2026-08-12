package com.cafe.jenika.repository;

import com.cafe.jenika.model.ChiTietNhapHang;
import com.cafe.jenika.model.ChiTietNhapHangId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

@Repository
public interface ChiTietNhapHangRepository extends JpaRepository<ChiTietNhapHang, ChiTietNhapHangId> {
    List<ChiTietNhapHang> findByIdIdNhapHang(Integer idNhapHang);

    @Query("SELECT ct FROM ChiTietNhapHang ct " +
           "JOIN FETCH ct.nhapHang nh " +
           "LEFT JOIN FETCH nh.doiTac dt " +
           "LEFT JOIN FETCH ct.sanPham sp " +
           "WHERE (:from IS NULL OR nh.thoiGian >= :from) " +
           "AND (:to IS NULL OR nh.thoiGian <= :to)")
    List<ChiTietNhapHang> findFiltered(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}

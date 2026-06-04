package com.cafe.jenika.repository;

import com.cafe.jenika.model.ChiTietBanHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChiTietBanHangRepository extends JpaRepository<ChiTietBanHang, Integer> {
    List<ChiTietBanHang> findByBanHangId(Integer banHangId);

    @Query("SELECT ct FROM ChiTietBanHang ct " +
           "JOIN FETCH ct.banHang bh " +
           "LEFT JOIN FETCH bh.doiTac dt " +
           "LEFT JOIN FETCH ct.sanPham sp " +
           "WHERE (:from IS NULL OR bh.thoiGian >= :from) " +
           "AND (:to IS NULL OR bh.thoiGian <= :to) " +
           "AND (:isGift IS NULL OR ct.isGift = :isGift)")
    List<ChiTietBanHang> findFiltered(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("isGift") Boolean isGift);
}

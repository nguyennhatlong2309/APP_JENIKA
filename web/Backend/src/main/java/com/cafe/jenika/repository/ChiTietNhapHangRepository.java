package com.cafe.jenika.repository;

import com.cafe.jenika.model.ChiTietNhapHang;
import com.cafe.jenika.model.ChiTietNhapHangId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietNhapHangRepository extends JpaRepository<ChiTietNhapHang, ChiTietNhapHangId> {
    List<ChiTietNhapHang> findByIdIdNhapHang(Integer idNhapHang);
}

package com.cafe.jenika.repository;

import com.cafe.jenika.model.BanHang;
import com.cafe.jenika.model.NhapHang;
import com.cafe.jenika.model.ThuChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThuChiRepository extends JpaRepository<ThuChi, Integer> {
    List<ThuChi> findAllByOrderByThoiGianDesc();
    Optional<ThuChi> findByBanHang(BanHang banHang);
    Optional<ThuChi> findByNhapHang(NhapHang nhapHang);
}

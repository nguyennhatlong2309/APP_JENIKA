package com.cafe.jenika.repository;

import com.cafe.jenika.model.LoaiThuChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiThuChiRepository extends JpaRepository<LoaiThuChi, Integer> {
}

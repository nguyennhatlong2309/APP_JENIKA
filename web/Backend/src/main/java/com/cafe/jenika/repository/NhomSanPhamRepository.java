package com.cafe.jenika.repository;

import com.cafe.jenika.model.NhomSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhomSanPhamRepository extends JpaRepository<NhomSanPham, Integer> {
}

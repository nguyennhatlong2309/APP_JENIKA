package com.cafe.jenika.repository;

import com.cafe.jenika.model.ThuChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThuChiRepository extends JpaRepository<ThuChi, Integer> {
    List<ThuChi> findAllByOrderByThoiGianDesc();
}

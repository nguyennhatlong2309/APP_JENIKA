package com.cafe.jenika.repository;

import com.cafe.jenika.model.NhatKy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NhatKyRepository extends JpaRepository<NhatKy, Integer>, JpaSpecificationExecutor<NhatKy> {
    List<NhatKy> findAllByOrderByThoiGianDesc();
}

package com.cafe.jenika.repository;

import com.cafe.jenika.model.DonViTinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonViTinhRepository extends JpaRepository<DonViTinh, Integer> {
}
